CREATE DATABASE IF NOT EXISTS pharmacy_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE pharmacy_db;
SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    real_name   VARCHAR(50),
    phone       VARCHAR(20),
    email       VARCHAR(100),
    avatar      VARCHAR(255),
    role        TINYINT      NOT NULL DEFAULT 0 COMMENT '0-管理员 1-药剂师 2-仓库管理员',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 2. 药品分类表
-- ----------------------------
DROP TABLE IF EXISTS drug_category;
CREATE TABLE drug_category (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    parent_id   BIGINT       DEFAULT 0 COMMENT '父分类ID，0为顶级',
    sort        INT          DEFAULT 0,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 3. 药品表
-- ----------------------------
DROP TABLE IF EXISTS drug;
CREATE TABLE drug (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    drug_code       VARCHAR(50)   NOT NULL UNIQUE COMMENT '药品编码',
    generic_name    VARCHAR(100)  NOT NULL COMMENT '通用名',
    trade_name      VARCHAR(100)  COMMENT '商品名',
    dosage_form     VARCHAR(50)   COMMENT '剂型',
    specification   VARCHAR(100)  COMMENT '规格',
    manufacturer    VARCHAR(200)  COMMENT '生产厂家',
    approval_number VARCHAR(100)  COMMENT '批准文号',
    barcode         VARCHAR(50)   COMMENT '条形码',
    category_id     BIGINT        COMMENT '分类ID',
    unit            VARCHAR(20)   COMMENT '单位(盒/瓶/片等)',
    purchase_price  DECIMAL(10,2) COMMENT '采购价',
    retail_price    DECIMAL(10,2) COMMENT '零售价',
    description     TEXT          COMMENT '药品说明',
    image           VARCHAR(255)  COMMENT '药品图片',
    status          TINYINT       NOT NULL DEFAULT 1 COMMENT '0-下架 1-上架',
    deleted         TINYINT       NOT NULL DEFAULT 0,
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_category_id (category_id),
    INDEX idx_drug_code (drug_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 4. 供应商表
-- ----------------------------
DROP TABLE IF EXISTS supplier;
CREATE TABLE supplier (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(200) NOT NULL,
    contact_person VARCHAR(50),
    phone          VARCHAR(20),
    email          VARCHAR(100),
    address        VARCHAR(500),
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    deleted        TINYINT      NOT NULL DEFAULT 0,
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 5. 库存表
-- ----------------------------
DROP TABLE IF EXISTS inventory;
CREATE TABLE inventory (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    drug_id           BIGINT      NOT NULL,
    batch_number      VARCHAR(50) COMMENT '批次号',
    quantity          INT         NOT NULL DEFAULT 0 COMMENT '库存数量',
    warning_threshold INT         DEFAULT 10 COMMENT '预警阈值',
    production_date   DATE        COMMENT '生产日期',
    expiry_date       DATE        COMMENT '有效期至',
    location          VARCHAR(100) COMMENT '存放位置',
    create_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_drug_id (drug_id),
    INDEX idx_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 6. 出入库记录表
-- ----------------------------
DROP TABLE IF EXISTS inventory_log;
CREATE TABLE inventory_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    drug_id         BIGINT       NOT NULL,
    inventory_id    BIGINT,
    type            TINYINT      NOT NULL COMMENT '0-入库 1-出库',
    quantity         INT          NOT NULL,
    before_quantity INT          NOT NULL DEFAULT 0,
    after_quantity  INT          NOT NULL DEFAULT 0,
    reason          VARCHAR(255) COMMENT '原因说明',
    operator_id     BIGINT,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_drug_id (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 7. 采购单表
-- ----------------------------
DROP TABLE IF EXISTS purchase_order;
CREATE TABLE purchase_order (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    order_no     VARCHAR(50)   NOT NULL UNIQUE COMMENT '采购单号',
    supplier_id  BIGINT        NOT NULL,
    total_amount DECIMAL(12,2) DEFAULT 0,
    status       TINYINT       NOT NULL DEFAULT 0 COMMENT '0-待审核 1-已审核 2-已入库 3-已取消',
    remark       VARCHAR(500),
    creator_id   BIGINT,
    approver_id  BIGINT,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 8. 采购单明细表
-- ----------------------------
DROP TABLE IF EXISTS purchase_order_item;
CREATE TABLE purchase_order_item (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    purchase_order_id BIGINT        NOT NULL,
    drug_id           BIGINT        NOT NULL,
    quantity          INT           NOT NULL,
    unit_price        DECIMAL(10,2) NOT NULL,
    amount            DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_order_id (purchase_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 9. 销售记录表
-- ----------------------------
DROP TABLE IF EXISTS sale_record;
CREATE TABLE sale_record (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    order_no      VARCHAR(50)   NOT NULL UNIQUE,
    drug_id       BIGINT        NOT NULL,
    batch_number  VARCHAR(50),
    quantity      INT           NOT NULL,
    unit_price    DECIMAL(10,2) NOT NULL,
    amount        DECIMAL(12,2) NOT NULL,
    customer_name VARCHAR(100),
    operator_id   BIGINT,
    deleted       TINYINT       NOT NULL DEFAULT 0,
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_drug_id (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 10. 效期预警表
-- ----------------------------
DROP TABLE IF EXISTS expiry_alert;
CREATE TABLE expiry_alert (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    drug_id      BIGINT       NOT NULL,
    inventory_id BIGINT,
    batch_number VARCHAR(50),
    expiry_date  DATE         NOT NULL,
    alert_level  TINYINT      NOT NULL COMMENT '0-黄色(6个月内) 1-橙色(3个月内) 2-红色(1个月内)',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '0-待处理 1-已处理',
    handler_note VARCHAR(500),
    handle_time  DATETIME,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 11. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT,
    username    VARCHAR(50),
    module      VARCHAR(50)  COMMENT '操作模块',
    action      VARCHAR(50)  COMMENT '操作类型',
    description VARCHAR(500) COMMENT '操作描述',
    ip          VARCHAR(50),
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 12. 处方表
-- ----------------------------
DROP TABLE IF EXISTS prescription;
CREATE TABLE prescription (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    prescription_no VARCHAR(50)  NOT NULL UNIQUE COMMENT '处方编号 RX+yyyymmdd+序号',
    doctor_id       BIGINT       NOT NULL COMMENT '开方医师 sys_user.id',
    patient_name    VARCHAR(50)  NOT NULL,
    patient_gender  TINYINT      COMMENT '0-女 1-男',
    patient_age     INT,
    patient_phone   VARCHAR(20),
    diagnosis       VARCHAR(500) COMMENT '诊断',
    remark          VARCHAR(500),
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0-作废 1-有效',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_patient (patient_name, patient_phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 13. 处方明细表
-- ----------------------------
DROP TABLE IF EXISTS prescription_item;
CREATE TABLE prescription_item (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    prescription_id BIGINT       NOT NULL,
    drug_id         BIGINT       NOT NULL,
    quantity        INT          NOT NULL,
    dosage          VARCHAR(100) COMMENT '用法用量 如：每次1片 一日3次',
    usage_note      VARCHAR(255),
    PRIMARY KEY (id),
    INDEX idx_prescription (prescription_id),
    INDEX idx_drug (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
