package com.veteroch4k.logistics.controllers;

import com.veteroch4k.logistics.services.MlPredictionService;
import com.veteroch4k.logistics.services.OsrmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripRestController {

    private final MlPredictionService mlService;
    private final OsrmService osrmService;

    @GetMapping("/predict")
    public Map<String, Object> predictRoute(
            @RequestParam double startLat,
            @RequestParam double startLon,
            @RequestParam double endLat,
            @RequestParam double endLon,
            @RequestParam double traffic,
            @RequestParam double weather,
            @RequestParam double hour,
            @RequestParam double isFtl)
    {
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> osrmData = osrmService.getRouteData(startLat, startLon, endLat, endLon);
        double distanceKm = (double) osrmData.get("distance");
        double osrmTime = (double) osrmData.get("duration");

        if (distanceKm == 0.0) {
            response.put("error", "Невозможно проложить маршрут");
            return response;
        }

        double delayMinutes = mlService.predict(distanceKm, traffic, weather, hour, isFtl);
        double totalTime = osrmTime + delayMinutes;

        response.put("distance", distanceKm);
        response.put("osrmTime", osrmTime);
        response.put("delay", delayMinutes);
        response.put("totalTime", totalTime);
        response.put("geometry", osrmData.get("geometry"));

        return response;
    }
}