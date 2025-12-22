-- 用户表
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  real_name VARCHAR(50),
  email VARCHAR(100),
  phone VARCHAR(20),
  avatar VARCHAR(255),
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 活动表
CREATE TABLE activity (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  description TEXT,
  location VARCHAR(100),
  start_time DATETIME,
  end_time DATETIME,
  publisher_id BIGINT,
  status TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (publisher_id) REFERENCES user(id)
);

-- 报名表
CREATE TABLE registration (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  activity_id BIGINT NOT NULL,
  register_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  status TINYINT DEFAULT 1,
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (activity_id) REFERENCES activity(id)
);

-- 签到表
CREATE TABLE attendance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  activity_id BIGINT NOT NULL,
  sign_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  sign_type VARCHAR(20),
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (activity_id) REFERENCES activity(id)
);

-- 消息通知表
CREATE TABLE notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  content VARCHAR(255),
  is_read TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 反馈表
CREATE TABLE feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  activity_id BIGINT NOT NULL,
  content TEXT,
  rating INT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (activity_id) REFERENCES activity(id)
);

-- 角色表
CREATE TABLE role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(100)
);

-- 用户角色关联表
CREATE TABLE user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (role_id) REFERENCES role(id)
);
