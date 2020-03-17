START TRANSACTION;

create SCHEMA weather;

CREATE TABLE weather."user"(
    id INTEGER PRIMARY KEY,
    id_Town INT REFERENCES weather.town(id)
);

CREATE TABLE weather.town(
    id SERIAL PRIMARY KEY,
    name VARCHAR(1024) UNIQUE,
    latitude NUMERIC not NULL,
    longitude NUMERIC not NULL
);

CREATE TABLE weather.forecast(
    time TIMESTAMP PRIMARY KEY,
    summary VARCHAR(4096),
    apparentTemperatureHigh NUMERIC,
    temperatureHigh NUMERIC,
    apparentTemperatureLow NUMERIC,
    temperatureLow NUMERIC,
    cloudCover NUMERIC,
    humidity NUMERIC,
    pressure NUMERIC,
    precipProbability NUMERIC,
    precipType VARCHAR(128),
    precipIntensity NUMERIC,
    windBearing INT,
    windSpeed NUMERIC,
    id_Town INT REFERENCES weather.tow(id)
);

COMMIT;

