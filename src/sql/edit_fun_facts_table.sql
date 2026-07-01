-- this SQL query retrieves a specific fun fact for a given country
SELECT c.country_name, f.fun_fact_no, f.fun_fact
FROM fun_facts f
JOIN countries c ON f.country_id = c.country_id
WHERE c.country_name = ? AND f.fun_fact_no = ?;

-- this SQL query inserts a new fun fact for a specific country
INSERT INTO fun_facts (country_id, fun_fact_no, fun_fact)
SELECT country_id, ?, ?
FROM countries
WHERE country_name = ?;

