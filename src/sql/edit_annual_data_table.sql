-- this SQL query retrieves a list of all available years from the annual_data
-- used to populate the year selection dropdown in the application
SELECT DISTINCT year FROM annual_data 
WHERE male_population IS NOT NULL 
ORDER BY year DESC;

-- this SQL query retrieves the annual data for all countries for a specific year
-- used for the compare feature in the application
SELECT c.country_name, a.* 
FROM annual_data a 
JOIN countries c ON a.country_id = c.country_id 
WHERE a.year = ? 
ORDER BY c.country_name ASC;

-- this SQL query retrieves the annual data for a specific country
-- used for the country analytics feature in the application
SELECT c.country_name, a.* 
FROM annual_data a 
JOIN countries c ON a.country_id = c.country_id 
WHERE c.country_name = ? 
ORDER BY a.year ASC;

-- this SQL query inserts or updates the population data for a specific country and year
INSERT INTO annual_data (country_id, year, male_population, female_population) 
SELECT country_id, ?, ?, ? 
FROM countries 
WHERE country_name = ? 
ON DUPLICATE KEY UPDATE
    male_population = VALUES(male_population), female_population = VALUES(female_population);

-- this SQL query inserts or updates the inflation data for a specific country and year
INSERT INTO annual_data (country_id, year, inflation) 
SELECT country_id, ?, ? 
FROM countries 
WHERE country_code = ? 
ON DUPLICATE KEY UPDATE
    inflation = VALUES(inflation);

-- this SQL query inserts or updates the migration data for a specific country and year
INSERT INTO annual_data (country_id, year, migration)
SELECT country_id, ?, ? 
FROM countries 
WHERE country_name = ? 
ON DUPLICATE KEY UPDATE
    migration = VALUES(migration);

