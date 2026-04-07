-- 增量升级脚本：为已有数据库添加处方模块相关表
USE pharmacy_db;
SET NAMES utf8mb4;

-- 12. 处方表
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

-- 13. 处方明细表
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
