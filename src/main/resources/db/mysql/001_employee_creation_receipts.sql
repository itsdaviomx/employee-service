CREATE TABLE IF NOT EXISTS employee_creation_receipts (
    request_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    payload_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    response_json JSON NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX ix_receipts_created_at (created_at)
) ENGINE=InnoDB;
