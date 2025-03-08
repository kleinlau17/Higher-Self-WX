use underwear;

-- 创建表
CREATE TABLE `information` (
   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   `openid` VARCHAR(128) NOT NULL COMMENT '微信openid',
   `birth_date` DATE NOT NULL COMMENT '出生日期',
   `birth_time` TIME COMMENT '出生时间',
   `birth_place` VARCHAR(128) NOT NULL COMMENT '出生地',
   `gender` VARCHAR(10) NOT NULL COMMENT '性别',
   `ziwei` TEXT COMMENT '紫微斗数',
   `bazi` TEXT COMMENT '生辰八字',
   `fortune_year` INT NOT NULL DEFAULT 2025 COMMENT '运势年份',
   `career_personality` TEXT COMMENT '事业性格',
   `annual_fortune` TEXT COMMENT '流年大运',
   `monthly_fortune` JSON COMMENT '十二个月各个月的事业运和对应分数，JSON 格式',
   `career_signature` TEXT COMMENT '事业灵签',
   `annual_summary` TEXT COMMENT '年度总结',
   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   PRIMARY KEY (`id`),
   UNIQUE KEY `uk_openid_year` (`openid`, `fortune_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户运势信息表';

-- 示例 JSON 数据格式（仅供参考）
-- {
--   "January": {"score": 85, "fortune": "事业运较强"},
--   "February": {"score": 90, "fortune": "事业表现突出"},
--   "March": {"score": 78, "fortune": "有进步空间"},
--   ...
--   "December": {"score": 70, "fortune": "需注意职场挑战"}
-- }