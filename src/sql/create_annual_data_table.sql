CREATE TABLE IF NOT EXISTS annual_data(
    country_id INTEGER NOT NULL, 
    year INTEGER NOT NULL, 
    male_population INTEGER, 
    female_population INTEGER, 
    inflation FLOAT, 
    migration INTEGER, 
    FOREIGN KEY (country_id) REFERENCES countries(country_id), 
    PRIMARY KEY (country_id, year)
);

