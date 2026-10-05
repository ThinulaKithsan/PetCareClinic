-- Existing installations only. Adds accounts without deleting clinic data.
USE petcare;
CREATE TABLE IF NOT EXISTS users (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 full_name VARCHAR(100) NOT NULL,
 username VARCHAR(40) NOT NULL UNIQUE,
 password_salt CHAR(32) NOT NULL,
 password_hash CHAR(64) NOT NULL,
 password_iterations INT NOT NULL,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
