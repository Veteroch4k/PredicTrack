package com.veteroch4k.logistics.ml;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import smile.regression.MLP;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ModelTrainer {

    public static void main(String[] args) {
        System.out.println("1. Загрузка исторического архива погоды в память (Data Join)...");
        Map<String, Double> weatherCache = loadWeatherArchive("weather_archive.csv");
        System.out.println("Погода загружена. Дней в кэше: " + weatherCache.size());

        System.out.println("2. Чтение и обогащение датасета Delhivery...");
        List<double[]> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Reader in = new FileReader("delhivery_data.csv")) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withTrim()
                    .parse(in);

            int count = 0;
            int errorCount = 0;

            for (CSVRecord row : records) {
                try {
                    if (row.get("osrm_distance").isEmpty() || row.get("actual_time").isEmpty()) continue;

                    String creationTimeStr = row.get("trip_creation_time").substring(0, 19);
                    LocalDateTime creationTime = LocalDateTime.parse(creationTimeStr, formatter);


                    String dateKey = creationTime.toString().substring(0, 13);
                    double weather = weatherCache.getOrDefault(dateKey, 0.0);

                    double actualTime = Double.parseDouble(row.get("actual_time"));
                    double osrmTime = Double.parseDouble(row.get("osrm_time"));


                    double trafficIndex = actualTime / Math.max(osrmTime, 1.0);

                    double normTraffic = Math.min(trafficIndex, 3.0) / 3.0;

                    double distance = Double.parseDouble(row.get("osrm_distance"));
                    if (distance < 5.0 || distance > 2000.0) continue;
                    double normDistance = distance / 2000.0;

                    double hour = creationTime.getHour() / 24.0;

                    String routeTypeStr = row.get("route_type");
                    double isFTL = routeTypeStr.equalsIgnoreCase("FTL") ? 1.0 : 0.0;

                    xList.add(new double[]{normDistance, normTraffic, weather, hour, isFTL});

                    double delay = actualTime - osrmTime;
                    delay = Math.max(delay, 0.0);
                    yList.add(delay / 500.0);

                    count++;
                    if (count >= 30000) break;

                } catch (Exception ex) {
                    if (errorCount < 3) {
                        System.err.println("Skipped row: " + ex.getMessage());
                        errorCount++;
                    }
                }
            }

            System.out.println("Успешно подготовлено " + count + " векторов для Enterprise логистики!");

            double[][] X = xList.toArray(new double[0][0]);
            double[] y = yList.stream().mapToDouble(Double::doubleValue).toArray();

            System.out.println("3. Обучение ИИ (Архитектура MLP)...");
            Properties props = new Properties();
            props.setProperty("smile.mlp.layers", "ReLU(32)|ReLU(16)");
            props.setProperty("smile.mlp.epochs", "150");
            props.setProperty("smile.mlp.learning.rate", "0.01");

            MLP model = MLP.fit(X, y, props);

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("mlp_model.bin"))) {
                oos.writeObject(model);
            }
            System.out.println("ГОТОВО! Модель сохранена в mlp_model.bin.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Double> loadWeatherArchive(String filename) {
        Map<String, Double> cache = new HashMap<>();
        try (Reader in = new FileReader(filename)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
            int rowNum = 0;
            int timeCol = -1;
            int rainCol = -1;

            for (CSVRecord row : records) {
                rowNum++;
                if (rowNum == 4) {
                    for (int i = 0; i < row.size(); i++) {
                        if (row.get(i).contains("time")) timeCol = i;
                        if (row.get(i).contains("rain")) rainCol = i;
                    }
                    continue;
                }

                if (rowNum < 4 || timeCol == -1 || rainCol == -1 || row.size() < 2) continue;

                try {
                    String timeStr = row.get(timeCol).trim();
                    String hourKey = timeStr.substring(0, 13);

                    double rain = Double.parseDouble(row.get(rainCol));

                    cache.put(hourKey, rain > 0.5 ? 1.0 : 0.0);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке погоды: Убедитесь, что файл называется правильно!");
        }
        return cache;
    }
}