CREATE TABLE IF NOT EXISTS countries(
    country_id INTEGER NOT NULL AUTO_INCREMENT, 
    country_name VARCHAR(25) UNIQUE NOT NULL, 
    country_code VARCHAR(2) NOT NULL, 
    PRIMARY KEY (country_id)
);

