CREATE TABLE IF NOT EXISTS migration(
    country_id INTEGER NOT NULL, 
    year INTEGER NOT NULL, 
    value INTEGER NOT NULL,
    FOREIGN KEY (country_id) REFERENCES countries(country_id), 
    PRIMARY KEY (country_id, year)
);

