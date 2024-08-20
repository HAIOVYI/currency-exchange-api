INSERT INTO users (id, email, first_name, last_name, password, is_blocked, is_deleted)
VALUES (1, 'default@gmail.com', 'test', 'test', '$2a$10$BNOXP0gtVrzMoWn2EkU5/u.OCDFEm0fCmelw8oAsFebW.eQwSWw3y', false, false);

INSERT INTO users_roles (user_id, role_id) VALUES (1, 2);
