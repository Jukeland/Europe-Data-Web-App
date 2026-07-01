CREATE TABLE IF NOT EXISTS users(
    user_id INTEGER NOT NULL AUTO_INCREMENT, 
    username VARCHAR(30) UNIQUE NOT NULL, 
    password VARCHAR(32) NOT NULL, 
    PRIMARY KEY (user_id)
);

