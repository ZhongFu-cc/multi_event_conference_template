-- 贊助商
CREATE TABLE `sponsor` (
  `sponsor_id`   BIGINT       NOT NULL COMMENT '主鍵ID',
  `name`         VARCHAR(255) NOT NULL COMMENT '贊助商名稱',
  `logo_url`     VARCHAR(500)          DEFAULT NULL COMMENT '贊助商 Logo URL',
  `tier`         VARCHAR(20)  NOT NULL DEFAULT 'none' COMMENT '贊助等級 diamond:鑽石級, platinum:白金級, gold:金級, silver:銀級, bronze:銅級, none:無級別',
  `scale`        INT          NOT NULL DEFAULT 100 COMMENT 'Logo 縮放比例 1-100',
  `sort`         INT          NOT NULL DEFAULT 500 COMMENT '排序 (數字越小越前面)',
  `create_by`    VARCHAR(50)           DEFAULT NULL COMMENT '創建者',
  `create_date`  DATETIME              DEFAULT NULL COMMENT '創建時間',
  `update_by`    VARCHAR(50)           DEFAULT NULL COMMENT '最後修改者',
  `update_date`  DATETIME              DEFAULT NULL COMMENT '最後修改時間',
  `is_deleted`   TINYINT      NOT NULL DEFAULT 0 COMMENT '邏輯刪除,預設為0活耀,1為刪除',
  PRIMARY KEY (`sponsor_id`),
  KEY `idx_sponsor_tier` (`tier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贊助商';
