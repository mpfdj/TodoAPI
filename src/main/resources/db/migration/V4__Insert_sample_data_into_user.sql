INSERT INTO app_user (email, name, password_hash, is_administrator, created_at, updated_at)
VALUES
    ('alice@example.com', 'Alice Johnson', 'hashed_pw_1', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bob@example.com', 'Bob Smith', 'hashed_pw_2', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('carol@example.com', 'Carol Davis', 'hashed_pw_3', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('mpf.dejaeger@gmail.com', 'Miel de Jaeger', 'admin', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
