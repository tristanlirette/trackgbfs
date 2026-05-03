CREATE TABLE IF NOT EXISTS station_information (
    station_id     VARCHAR    NOT NULL,
    name           VARCHAR    NOT NULL,
    short_name     VARCHAR,
    lat            REAL    NOT NULL,
    lon            REAL    NOT NULL,
    address        VARCHAR,
    cross_street   VARCHAR,
    region_id      VARCHAR,
    post_code      VARCHAR,
    rental_methods VARCHAR,
    capacity       INTEGER,
    observed_at    INTEGER NOT NULL,
    PRIMARY KEY (station_id, observed_at)
);

CREATE TABLE IF NOT EXISTS station_status (
    station_id           VARCHAR    NOT NULL,
    num_bikes_available  INTEGER NOT NULL,
    num_bikes_disabled   INTEGER,
    num_docks_available  INTEGER NOT NULL,
    num_docks_disabled   INTEGER,
    is_installed         INTEGER NOT NULL,
    is_renting           INTEGER NOT NULL,
    is_returning         INTEGER NOT NULL,
    last_reported        INTEGER NOT NULL,
    -- future: num_ebikes_available INTEGER, vehicle_types_available TEXT
    observed_at          INTEGER NOT NULL,
    PRIMARY KEY (station_id, observed_at)
);
