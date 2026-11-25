-- ============================================================================
-- LABOUR MANAGEMENT ERP SYSTEM - INITIAL RELATIONAL SCHEMA (FLYWAY V1)
-- Standards: ISO/IEC 9075, Strict Foreign Keys, Decimal Financial Precision,
-- Tamper-Evident Auditability & Business Data Isolation.
-- ============================================================================

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. ROLES TABLE
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(40) NOT NULL UNIQUE,
    role_name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. USER ROLES MAPPING
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. MASTER CATEGORIES (FOR DYNAMIC ADMIN MASTERS)
CREATE TABLE IF NOT EXISTS master_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL UNIQUE,
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_system BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BUSINESSES (MULTI-TENANT / MULTI-ORGANIZATION ROOT)
CREATE TABLE IF NOT EXISTS businesses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_code VARCHAR(30) NOT NULL UNIQUE,
    business_name VARCHAR(150) NOT NULL,
    business_type VARCHAR(50) NOT NULL DEFAULT 'ELECTRICAL_CONTRACTOR',
    registration_no VARCHAR(50),
    gstin VARCHAR(20),
    address_line1 VARCHAR(150),
    address_line2 VARCHAR(150),
    city VARCHAR(60),
    state VARCHAR(60),
    pincode VARCHAR(10),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BUSINESS USERS ACCESS MAPPING
CREATE TABLE IF NOT EXISTS business_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_bus_user_role UNIQUE (business_id, user_id, role_id),
    CONSTRAINT fk_bu_business FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT fk_bu_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_bu_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. MASTER VALUES (FULLY DYNAMIC SYSTEM & BUSINESS MASTERS)
CREATE TABLE IF NOT EXISTS master_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    master_category_id BIGINT NOT NULL,
    business_id BIGINT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    color_code VARCHAR(15) DEFAULT '#1B3557',
    is_payable BOOLEAN DEFAULT TRUE,
    wage_percentage DECIMAL(5, 2) DEFAULT 100.00,
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_cat_bus_code UNIQUE (master_category_id, business_id, code),
    CONSTRAINT fk_mv_cat FOREIGN KEY (master_category_id) REFERENCES master_categories (id) ON DELETE CASCADE,
    CONSTRAINT fk_mv_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. CUSTOM FIELD DEFINITIONS (METADATA-DRIVEN CUSTOM FIELDS)
CREATE TABLE IF NOT EXISTS custom_field_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    field_key VARCHAR(50) NOT NULL,
    field_label VARCHAR(100) NOT NULL,
    field_type VARCHAR(20) NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    default_value VARCHAR(255),
    placeholder VARCHAR(150),
    options_json TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_cfd_bus_ent_key UNIQUE (business_id, entity_type, field_key),
    CONSTRAINT fk_cfd_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. CUSTOM FIELD VALUES (EAV STORAGE FOR EXTENSIBLE FIELDS)
CREATE TABLE IF NOT EXISTS custom_field_values (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    definition_id BIGINT NOT NULL,
    entity_id BIGINT NOT NULL,
    field_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_cfv_def_entity UNIQUE (definition_id, entity_id),
    CONSTRAINT fk_cfv_def FOREIGN KEY (definition_id) REFERENCES custom_field_definitions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. SITES TABLE
CREATE TABLE IF NOT EXISTS sites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    site_code VARCHAR(30) NOT NULL,
    site_name VARCHAR(150) NOT NULL,
    client_name VARCHAR(150),
    site_category VARCHAR(50) DEFAULT 'ELECTRICAL_INSTALLATION',
    location_address TEXT,
    city VARCHAR(60),
    state VARCHAR(60),
    site_manager_id BIGINT NULL,
    supervisor_id BIGINT NULL,
    start_date DATE,
    expected_end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT uq_site_bus_code UNIQUE (business_id, site_code),
    CONSTRAINT fk_site_business FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT fk_site_mgr FOREIGN KEY (site_manager_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_site_sup FOREIGN KEY (supervisor_id) REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. LABOURERS TABLE
CREATE TABLE IF NOT EXISTS labourers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    labour_code VARCHAR(30) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    photo_url VARCHAR(255),
    mobile_number VARCHAR(15) NOT NULL,
    alt_mobile_number VARCHAR(15),
    aadhar_number VARCHAR(20),
    address TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(15),
    date_of_joining DATE NOT NULL,
    designation VARCHAR(80) NOT NULL,
    labour_category VARCHAR(80) NOT NULL,
    skill_level VARCHAR(40) DEFAULT 'SKILLED',
    daily_wage DECIMAL(10, 2) NOT NULL,
    overtime_hourly_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    current_site_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT uq_labour_bus_code UNIQUE (business_id, labour_code),
    CONSTRAINT fk_labour_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT fk_labour_site FOREIGN KEY (current_site_id) REFERENCES sites (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. WAGE RATE HISTORY (HISTORICAL TRACKING FOR ACCURATE PAYROLL RECONCILIATION)
CREATE TABLE IF NOT EXISTS wage_rate_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    labourer_id BIGINT NOT NULL,
    daily_wage DECIMAL(10, 2) NOT NULL,
    overtime_hourly_rate DECIMAL(10, 2) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    reason VARCHAR(255),
    changed_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wrh_labour FOREIGN KEY (labourer_id) REFERENCES labourers (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. ATTENDANCE RECORDS TABLE
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    labourer_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PRESENT',
    overtime_hours DECIMAL(4, 2) NOT NULL DEFAULT 0.00,
    wage_multiplier DECIMAL(5, 2) NOT NULL DEFAULT 1.00,
    daily_wage_applied DECIMAL(10, 2) NOT NULL,
    calculated_wage DECIMAL(10, 2) NOT NULL,
    overtime_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    remarks VARCHAR(255),
    marked_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_att_site_lab_date UNIQUE (site_id, labourer_id, attendance_date),
    CONSTRAINT fk_att_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT fk_att_site FOREIGN KEY (site_id) REFERENCES sites (id) ON DELETE CASCADE,
    CONSTRAINT fk_att_labour FOREIGN KEY (labourer_id) REFERENCES labourers (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. ATTENDANCE CORRECTIONS (AUDIT TRAIL FOR MODIFIED ATTENDANCE)
CREATE TABLE IF NOT EXISTS attendance_corrections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_record_id BIGINT NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    previous_overtime_hours DECIMAL(4, 2) NOT NULL,
    new_overtime_hours DECIMAL(4, 2) NOT NULL,
    previous_calculated_wage DECIMAL(10, 2) NOT NULL,
    new_calculated_wage DECIMAL(10, 2) NOT NULL,
    reason TEXT NOT NULL,
    modified_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ac_att FOREIGN KEY (attendance_record_id) REFERENCES attendance_records (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. ADVANCES TABLE
CREATE TABLE IF NOT EXISTS advances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    site_id BIGINT NULL,
    labourer_id BIGINT NOT NULL,
    advance_code VARCHAR(30) NOT NULL,
    advance_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'CASH',
    advance_type VARCHAR(50) NOT NULL DEFAULT 'PERSONAL_ADVANCE',
    reference_number VARCHAR(80),
    purpose VARCHAR(255),
    approved_by VARCHAR(50),
    recovered_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    recovery_status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    CONSTRAINT uq_adv_bus_code UNIQUE (business_id, advance_code),
    CONSTRAINT fk_adv_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT fk_adv_site FOREIGN KEY (site_id) REFERENCES sites (id) ON DELETE SET NULL,
    CONSTRAINT fk_adv_labour FOREIGN KEY (labourer_id) REFERENCES labourers (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. PAYMENTS TABLE
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    site_id BIGINT NULL,
    labourer_id BIGINT NOT NULL,
    payment_code VARCHAR(30) NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'CASH',
    reference_number VARCHAR(80),
    receipt_attachment_url VARCHAR(255),
    remarks TEXT,
    recorded_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_pay_bus_code UNIQUE (business_id, payment_code),
    CONSTRAINT fk_pay_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT fk_pay_site FOREIGN KEY (site_id) REFERENCES sites (id) ON DELETE SET NULL,
    CONSTRAINT fk_pay_labour FOREIGN KEY (labourer_id) REFERENCES labourers (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. PAYROLL RUNS (MONTHLY / PERIODIC PAYROLL AGGREGATE)
CREATE TABLE IF NOT EXISTS payroll_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    site_id BIGINT NULL,
    run_code VARCHAR(30) NOT NULL,
    period_month INT NOT NULL,
    period_year INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_workers INT NOT NULL DEFAULT 0,
    total_gross_earnings DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_advance_deductions DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_other_deductions DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_net_payable DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_paid_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_outstanding_balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    generated_by VARCHAR(50),
    approved_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_pr_bus_code UNIQUE (business_id, run_code),
    CONSTRAINT fk_pr_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_site FOREIGN KEY (site_id) REFERENCES sites (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. PAYROLL ITEMS (INDIVIDUAL WORKER PERIOD LEDGER)
CREATE TABLE IF NOT EXISTS payroll_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_run_id BIGINT NOT NULL,
    labourer_id BIGINT NOT NULL,
    daily_wage_rate DECIMAL(10, 2) NOT NULL,
    present_days DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    half_days DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    absent_days DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    paid_leave_days DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    weekly_off_days DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    holiday_days DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    overtime_hours DECIMAL(6, 2) NOT NULL DEFAULT 0.00,
    overtime_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    base_earnings DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    gross_earnings DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    advance_recovery DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    other_deductions DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    net_payable DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    amount_paid DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    remaining_balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pi_run_labour UNIQUE (payroll_run_id, labourer_id),
    CONSTRAINT fk_pi_run FOREIGN KEY (payroll_run_id) REFERENCES payroll_runs (id) ON DELETE CASCADE,
    CONSTRAINT fk_pi_labour FOREIGN KEY (labourer_id) REFERENCES labourers (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. ADVANCE RECOVERIES (MAPPING ADVANCE TO PAYROLL ITEM DEDUCTION)
CREATE TABLE IF NOT EXISTS advance_recoveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    advance_id BIGINT NOT NULL,
    payroll_item_id BIGINT NOT NULL,
    recovered_amount DECIMAL(10, 2) NOT NULL,
    recovery_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ar_adv FOREIGN KEY (advance_id) REFERENCES advances (id) ON DELETE CASCADE,
    CONSTRAINT fk_ar_pi FOREIGN KEY (payroll_item_id) REFERENCES payroll_items (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. LABOUR TRANSFERS (INTER-SITE WORKFORCE REASSIGNMENT HISTORY)
CREATE TABLE IF NOT EXISTS labour_transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    labourer_id BIGINT NOT NULL,
    from_site_id BIGINT NULL,
    to_site_id BIGINT NOT NULL,
    transfer_date DATE NOT NULL,
    effective_date DATE NOT NULL,
    reason TEXT,
    remarks VARCHAR(255),
    transferred_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lt_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT fk_lt_labour FOREIGN KEY (labourer_id) REFERENCES labourers (id) ON DELETE CASCADE,
    CONSTRAINT fk_lt_from_site FOREIGN KEY (from_site_id) REFERENCES sites (id) ON DELETE SET NULL,
    CONSTRAINT fk_lt_to_site FOREIGN KEY (to_site_id) REFERENCES sites (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. AUDIT LOGS (IMMUTABLE FORENSIC & SYSTEM AUDIT TRAIL)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NULL,
    user_id BIGINT NULL,
    username VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    previous_value LONGTEXT NULL,
    new_value LONGTEXT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_bus_ent (business_id, entity_type, entity_id),
    INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. BUSINESS MODULES (CONFIGURABLE FEATURE TOGGLES PER BUSINESS)
CREATE TABLE IF NOT EXISTS business_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    module_code VARCHAR(40) NOT NULL,
    module_name VARCHAR(100) NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_bm_bus_mod UNIQUE (business_id, module_code),
    CONSTRAINT fk_bm_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. BUSINESS SETTINGS (KEY-VALUE & JSON CONFIGURATION)
CREATE TABLE IF NOT EXISTS business_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_id BIGINT NOT NULL,
    setting_key VARCHAR(80) NOT NULL,
    setting_value TEXT NOT NULL,
    data_type VARCHAR(20) DEFAULT 'STRING',
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    CONSTRAINT uq_bs_bus_key UNIQUE (business_id, setting_key),
    CONSTRAINT fk_bs_bus FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. USER TABLE PREFERENCES (SAVED COLUMN VISIBILITY, SIZES, SORTS)
CREATE TABLE IF NOT EXISTS user_table_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    table_key VARCHAR(60) NOT NULL,
    preferences_json TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_utp_user_tbl UNIQUE (user_id, table_key),
    CONSTRAINT fk_utp_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
