CREATE TABLE IF NOT EXISTS fun_facts(
    country_id INTEGER NOT NULL, 
    fun_fact_no INTEGER NOT NULL, 
    fun_fact VARCHAR(200) NOT NULL, 
    FOREIGN KEY (country_id) REFERENCES countries(country_id),
    PRIMARY KEY (country_id, fun_fact_no)
);

