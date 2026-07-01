-- this SQL query inserts a new country into the countries table 
-- used in loop to insert all European countries into the database
INSERT INTO countries (country_name, country_code, continent) 
VALUES (?, ?, 'europe');

-- this SQL query retrieves a country from the database by providing its name
SELECT * 
FROM countries 
WHERE country_name = ?;

-- this SQL query retrieves a country from the database by providing its id
SELECT * 
FROM countries 
WHERE country_id = ?;


