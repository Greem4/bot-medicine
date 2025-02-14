CREATE TABLE group_schedule (
                                group_chat_id BIGINT PRIMARY KEY,
                                schedule_url TEXT
);

CREATE TABLE authorized_group_user (
                                       group_chat_id BIGINT NOT NULL,
                                       user_id BIGINT NOT NULL,
                                       PRIMARY KEY (group_chat_id, user_id)
);
