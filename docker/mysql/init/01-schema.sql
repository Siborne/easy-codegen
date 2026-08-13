-- easy-codegen 演示数据库初始化脚本
-- mysql 容器首次启动时自动执行（/docker-entrypoint-initdb.d）
-- 注意：官方镜像执行 init 脚本时客户端默认 latin1，必须显式切到 utf8mb4，
-- 否则脚本中的中文会被错误转码入库（mojibake）
SET NAMES utf8mb4;

USE linklite;

CREATE TABLE IF NOT EXISTS user_info (
  user_id           VARCHAR(32)  NOT NULL COMMENT '用户ID',
  email             VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  nickname          VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
  join_type         TINYINT      DEFAULT 0 COMMENT '0:直接加入 1:同意后加好友',
  sex               TINYINT      DEFAULT 0 COMMENT '性别 0:女 1:男',
  password          VARCHAR(128) DEFAULT NULL COMMENT '密码',
  personal_signature VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
  status            TINYINT      DEFAULT 1 COMMENT '状态',
  create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

INSERT INTO user_info (user_id, email, nickname, sex, status)
VALUES ('u_10001', 'demo@easy-codegen.dev', '演示用户', 1, 1);
