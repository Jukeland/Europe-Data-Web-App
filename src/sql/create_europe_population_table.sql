CREATE TABLE IF NOT EXISTS europe_population(
    country_id INTEGER NOT NULL, 
    year INTEGER NOT NULL, 
    male_population INTEGER NOT NULL, 
    female_population INTEGER NOT NULL, 
    FOREIGN KEY (country_id) REFERENCES countries(country_id), 
    PRIMARY KEY (country_id, year)
);

