CREATE TABLE group_schedule
(
    group_chat_id BIGINT PRIMARY KEY,
    schedule_url  TEXT
);
CREATE TABLE authorized_group_user
(
    user_name     VARCHAR(50),
    user_id       BIGINT NOT NULL,
    group_chat_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, group_chat_id)
);
