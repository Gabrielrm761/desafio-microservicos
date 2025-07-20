INSERT INTO users (username, email, password, first_name, last_name, enabled, created_at) VALUES
('admin', 'admin@microservices.com', '$2a$10$e0TbP8.GKz09nLt5v0cQ2OQFyM/EqPQdJ/KeKKvWgLt8tNdWjL5a2', 'Admin', 'User', true, CURRENT_TIMESTAMP),
('user1', 'user1@example.com', '$2a$10$e0TbP8.GKz09nLt5v0cQ2OQFyM/EqPQdJ/KeKKvWgLt8tNdWjL5a2', 'João', 'Silva', true, CURRENT_TIMESTAMP),
('user2', 'user2@example.com', '$2a$10$e0TbP8.GKz09nLt5v0cQ2OQFyM/EqPQdJ/KeKKvWgLt8tNdWjL5a2', 'Maria', 'Santos', true, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role) VALUES
(1, 'ADMIN'),
(1, 'USER'),
(2, 'USER'),
(3, 'USER');