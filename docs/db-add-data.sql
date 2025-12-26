-- 用户表
INSERT INTO user (username, password, real_name, email, phone, avatar, status) VALUES
('admin', 'admin123', '管理员', 'admin@school.edu', '13800000001', NULL, 1),
('alice', 'alice123', '张三', 'alice@school.edu', '13800000002', NULL, 1),
('bob', 'bob123', '李四', 'bob@school.edu', '13800000003', NULL, 1);

-- 角色表
INSERT INTO role (name, description) VALUES
('管理员', '系统管理员'),
('学生', '普通学生用户');

-- 用户角色关联表
INSERT INTO user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2);

-- 活动表
INSERT INTO activity (title, description, location, start_time, end_time, publisher_id) VALUES
('迎新晚会', '2025年迎新晚会，欢迎新同学！', '大礼堂', '2025-09-01 19:00:00', '2025-09-01 21:00:00', 1),
('编程马拉松', '24小时编程挑战赛', '实验楼A101', '2025-10-15 08:00:00', '2025-10-16 08:00:00', 1);

-- 报名表
INSERT INTO registration (user_id, activity_id, register_time, status) VALUES
(2, 1, '2025-08-25 10:00:00', 1),
(3, 1, '2025-08-26 11:00:00', 1),
(2, 2, '2025-10-01 09:00:00', 1);

-- 签到表
INSERT INTO attendance (user_id, activity_id, sign_time, sign_type) VALUES
(2, 1, '2025-09-01 19:05:00', '二维码'),
(3, 1, '2025-09-01 19:10:00', '二维码'),
(2, 2, '2025-10-15 08:10:00', '手动');

-- 消息通知表
INSERT INTO notification (user_id, content, is_read, create_time) VALUES
(2, '您已成功报名迎新晚会', 1, '2025-08-25 10:01:00'),
(3, '您已成功报名迎新晚会', 1, '2025-08-26 11:01:00'),
(2, '编程马拉松即将开始，请准时参加', 0, '2025-10-14 20:00:00');

-- 反馈表
INSERT INTO feedback (user_id, activity_id, content, rating, create_time) VALUES
(2, 1, '活动很精彩，组织有序！', 5, '2025-09-02 10:00:00'),
(3, 1, '希望明年能有更多互动环节。', 4, '2025-09-02 11:00:00'),
(2, 2, '编程马拉松很有挑战性，收获很大。', 5, '2025-10-16 09:00:00');
