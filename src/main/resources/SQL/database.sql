START TRANSACTION;

create SCHEMA weather;

CREATE TABLE weather."user"(
    id SERIAL PRIMARY key,
    id_Town INT REFERENCES weather.town(id)
);

CREATE TABLE weather.town(
    id SERIAL PRIMARY key,
    latitude NUMERIC not NULL,
    longitude NUMERIC not NULL
);

CREATE TABLE weather.forecast(
    time DATE PRIMARY key,
    summary VARCHAR(4096),
    apparentTemperatureHigh NUMERIC,
    temperatureHign NUMERIC,
    apparentTemperatureLow NUMERIC,
    temperatureLow NUMERIC,
    cloudCover NUMERIC,
    humidity NUMERIC,
    pressure NUMERIC,
    precipProbability NUMERIC,
    precipType VARCHAR(10),
    precipIntensity NUMERIC,
    windBearing INT,
    windSpeed NUMERIC,
    id_Town INT REFERENCES weather.tow(id)
);

COMMIT;

