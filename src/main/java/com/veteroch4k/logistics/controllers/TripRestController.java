package com.veteroch4k.logistics.controllers;

import com.veteroch4k.logistics.services.MlPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripRestController {

    private final MlPredictionService mlService;



    @GetMapping("/predict")
    public String testModel(
            @RequestParam double distance, // в километрах (например, 350)
            @RequestParam double traffic,  // 1.0 (идеально по навигатору), 2.0 (ехать в 2 раза дольше)
            @RequestParam double weather,  // 0.0 (ясно), 1.0 (дождь/шторм)
            @RequestParam double hour,     // 0-23 (час выезда)
            @RequestParam double isFtl)    // 1.0 (полная фура), 0.0 (сборный груз)
    {
        double delayMinutes = mlService.predict(distance, traffic, weather, hour, isFtl);

        return String.format(
                " ЛОГИСТИКА: МАРШРУТ %.2f км\n" +
                        "Трафик-индекс: %.1f | ⛈ Погода: %.1f | Час выезда: %.0f:00 |  FTL: %.1f\n" +
                        "===========================================\n" +
                        "ПРОГНОЗ ЗАДЕРЖКИ В ПУТИ: %.2f минут (или %.2f часов)",
                distance, traffic, weather, hour, isFtl, delayMinutes, delayMinutes / 60.0
        );
    }
}