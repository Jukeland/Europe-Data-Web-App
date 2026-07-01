CREATE TABLE IF NOT EXISTS inflation(
    country_id INTEGER NOT NULL, 
    year INTEGER NOT NULL, 
    rate FLOAT NOT NULL, 
    FOREIGN KEY (country_id) REFERENCES countries(country_id), 
    PRIMARY KEY (country_id, year)
);

