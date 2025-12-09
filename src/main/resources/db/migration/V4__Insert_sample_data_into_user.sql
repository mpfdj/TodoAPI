INSERT INTO app_user (email, name, password_hash, created_at, updated_at)
VALUES
    ('alice@example.com', 'Alice Johnson', 'hashed_pw_1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bob@example.com', 'Bob Smith', 'hashed_pw_2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('carol@example.com', 'Carol Davis', 'hashed_pw_3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
