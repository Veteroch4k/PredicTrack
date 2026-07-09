package com.veteroch4k.logistics.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import smile.regression.MLP;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

@Slf4j
@Service
public class MlPredictionService {

    private MLP mlpModel;

    @PostConstruct
    public void init() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("mlp_model.bin"))) {
            mlpModel = (MLP) ois.readObject();
            log.info("ML ENGINE: Enterprise B2B модель успешно загружена!");
        } catch (Exception e) {
            log.warn("ВНИМАНИЕ! Модель не найдена.");
        }
    }

    public double predict(double distance, double trafficIndex, double weather, double hour, double isFtl) {
        if (mlpModel != null) {
            // нормализуем
            double[] features = new double[] {
                    distance / 2000.0,
                    Math.min(trafficIndex, 3.0) / 3.0,
                    weather,
                    hour / 24.0,
                    isFtl
            };

            return mlpModel.predict(features) * 500.0;
        }
        return 0.0;
    }
}