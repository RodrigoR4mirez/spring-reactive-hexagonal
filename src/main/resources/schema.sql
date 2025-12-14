CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255)
);

INSERT INTO users (first_name, last_name, email) VALUES ('John', 'Doe', 'john.doe@example.com');
INSERT INTO users (first_name, last_name, email) VALUES ('Jane', 'Smith', 'jane.smith@example.com');
