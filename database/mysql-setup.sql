CREATE DATABASE IF NOT EXISTS feature_flag_db;

CREATE USER IF NOT EXISTS 'feature_flag_user'@'localhost' IDENTIFIED BY 'feature_flag_password';

GRANT ALL PRIVILEGES ON feature_flag_db.* TO 'feature_flag_user'@'localhost';

FLUSH PRIVILEGES;
