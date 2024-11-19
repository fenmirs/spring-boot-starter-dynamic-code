-- ------------------------------------
-- dynamic_logic_c
-- 动态逻辑配置
-- ------------------------------------
DROP TABLE IF EXISTS `dynamic_logic_c`;
CREATE TABLE `dynamic_logic_c`
(
    `id`          bigint unsigned NOT NULL,
    `title`       varchar(100)    NOT NULL DEFAULT '' COMMENT '标题',
    `note`        varchar(255)    NULL COMMENT '备注',
    `grammar`     TEXT            NOT NULL COMMENT '语法：json格式',
    `create_time` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime        NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='动态逻辑配置';