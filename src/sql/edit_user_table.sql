-- this SQL query inserts a new user into the user table
INSERT INTO user (username, password) 
VALUES (?, ?);

-- this SQL query checks if a user with the given username 
-- and password exists in the user table 
SELECT EXISTS(
    SELECT * 
    FROM user 
    WHERE username = ? AND password = ?
);

