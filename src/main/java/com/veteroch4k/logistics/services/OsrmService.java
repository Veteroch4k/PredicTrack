package com.veteroch4k.logistics.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Service
public class OsrmService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getRouteData(double startLat, double startLon, double endLat, double endLon) {
        Map<String, Object> result = new HashMap<>();
        result.put("distance", 0.0);
        result.put("duration", 0.0);
        result.put("geometry", null);

        String url = String.format(
                "http://router.project-osrm.org/route/v1/driving/%s,%s;%s,%s?overview=full&geometries=geojson",
                startLon, startLat, endLon, endLat
        );

        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("routes") && !response.get("routes").isEmpty()) {
                JsonNode route = response.get("routes").get(0);

                result.put("distance", route.get("distance").asDouble() / 1000.0);
                result.put("duration", route.get("duration").asDouble() / 60.0);

                result.put("geometry", route.get("geometry"));
            }
        } catch (Exception e) {
            System.err.println("Ошибка при запросе к OSRM: " + e.getMessage());
        }
        return result;
    }
}