# PredicTrack — Predictive Logistics ETA & Delay Forecasting

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Smile ML](https://img.shields.io/badge/ML-Smile_(Java)-blue?style=flat-square)](https://haifengl.github.io/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

## 📌 О проекте
**PredicTrack** — это высоконагруженный сервис предиктивного мониторинга логистических задержек, разработанный в рамках дипломного проекта. Система прогнозирует время прибытия (ETA) грузового транспорта в реальном времени, сочетая классические алгоритмы маршрутизации на графах (OSRM) с нейросетевой моделью (MLP), написанной нативно на Java.

Проект демонстрирует полный цикл ML-инженерии: от сбора и обогащения данных до инференса модели в production-окружении без использования Python-скриптов.

## 🚀 Ключевые особенности (Features)
- **ML на Java (без Python):** Обучение и инференс многослойного персептрона (MLP) с использованием библиотеки **Smile**. Модель предсказывает задержку в минутах на основе 5 признаков (дистанция, трафик, погода, время суток, тип загрузки).
- **Алгоритмы на графах:** Интеграция с **OSRM** (Open Source Routing Machine) для построения маршрутов по реальному графу дорог (OpenStreetMap) и расчёта базового времени в пути.
- **Пайплайн данных (ETL):** Очистка, валидация и обогащение исторического датасета **Delhivery** (30 000+ записей) экзогенными факторами (погода из Open-Meteo). Кэширование погодных условий через `HashMap` для доступа за O(1).
- **REST API:** Разработан на Spring Boot для приёма параметров рейса и возврата прогноза ETA с геометрией маршрута (GeoJSON).
- **Веб-интерфейс:** Интерактивная карта на Leaflet.js с панелью управления параметрами рейса для диспетчера.

## 🛠 Технологический стек
- **Язык:** Java 21
- **Фреймворк:** Spring Boot
- **ML-библиотека:** Smile (Statistical Machine Intelligence and Learning Engine)
- **Внешние API:** OSRM (маршрутизация), Open-Meteo (историческая погода)
- **Фронтенд:** HTML5, JavaScript, Bootstrap 5, Leaflet.js
- **Сборка:** Maven
- **Контейнеризация:** Docker / Docker Compose

## 🏗 Архитектура
Проект построен по принципу **модульного монолита** с чётким разделением слоёв:

1. **Controller Layer:** REST-контроллер (`TripRestController`), валидирует входные параметры и оркестрирует вызовы сервисов.
2. **Service Layer:**
   - `OsrmService`: HTTP-клиент к OSRM для получения дистанции и базового времени.
   - `MlPredictionService`: Загружает бинарный файл `mlp_model.bin` и выполняет инференс.
3. **ML Pipeline (Offline):** `ModelTrainer` — утилита для обучения модели на исторических данных с последующей сериализацией.

## 📊 ML-модель
- **Архитектура:** MLP (входной слой — 5 нейронов, скрытые слои — 32 и 16 нейронов с активацией ReLU, выходной слой — 1 нейрон).
- **Обучение:** 150 эпох, learning rate = 0.01, функция потерь — MSE.
- **Метрика качества:** MAE (Mean Absolute Error) составила **18.4 минуты** на тестовой выборке (маршруты до 2000 км).
- **Нормализация:** Все признаки (дистанция, трафик, время) масштабируются в диапазон [0, 1] перед подачей в сеть.

## 🚦 Быстрый старт (Getting Started)

### Предварительные требования
- Java 21+
- Maven
- Docker (опционально)

### Запуск локально

**Шаг 1. Клонируйте репозиторий:**
```bash
git clone https://github.com/Veteroch4k/PredicTrack.git
cd PredicTrack
```

**Шаг 2. Соберите проект:**
```bash
mvn clean package
```

**Шаг 3. Запустите приложение:**
```bash
java -jar target/PredicTrack-0.0.1-SNAPSHOT.jar
```

**Шаг 4. Откройте в браузере:**
Перейдите по адресу: `http://localhost:8080`

### Запуск через Docker
```bash
docker-compose up --build
```

## 📂 Структура проекта
```text
PredicTrack/
├── src/main/java/com/veteroch4k/logistics/
│   ├── controllers/       # REST API (TripRestController)
│   ├── services/          # Бизнес-логика (OsrmService, MlPredictionService)
│   └── ml/                # Обучение модели (ModelTrainer)
├── src/main/resources/
│   └── static/            # Фронтенд (index.html)
├── delhivery_data.csv     # Обучающий датасет
├── weather_archive.csv    # Исторические погодные данные
├── mlp_model.bin          # Сериализованная ML-модель
├── docker-compose.yml
└── pom.xml
```

## 📡 API Endpoints
| Метод | Endpoint | Описание |
|-------|----------|-----------|
| `GET` | `/api/v1/trips/predict` | Принимает координаты, трафик, погоду, время и тип груза. Возвращает прогноз ETA, дистанцию и GeoJSON маршрута. |

**Пример запроса:**
```http
GET /api/v1/trips/predict?startLat=28.61&startLon=77.20&endLat=27.17&endLon=78.00&traffic=1.5&weather=1.0&hour=14&isFtl=1.0
```

## 🖼 Скриншот интерфейса
<img width="1916" height="926" alt="Screenshot 2026-11-02 175245" src="https://github.com/user-attachments/assets/e4ac675f-d5b7-4d29-94d3-18319f9768b3" />

