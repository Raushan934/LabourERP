-- ============================================================================
-- LABOUR MANAGEMENT ERP SYSTEM - INITIAL MASTER SEED DATA (FLYWAY V2)
-- ============================================================================

-- 1. ROLES
INSERT INTO roles (id, role_code, role_name, description) VALUES
(1, 'ROLE_ADMIN', 'Administrator / Owner', 'Full administrative authority across all businesses and system modules'),
(2, 'ROLE_MANAGER', 'Operations Manager', 'Operational control over assigned businesses and sites'),
(3, 'ROLE_SITE_SUPERVISOR', 'Site Supervisor', 'On-site workforce supervision and daily muster attendance logging'),
(4, 'ROLE_ACCOUNTANT', 'Financial Accountant', 'Disbursements, advances, wage settlement and statutory ledger verification')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- 2. INITIAL USERS
INSERT INTO users (id, username, email, password_hash, full_name, phone, status, created_by) VALUES
(1, 'admin', 'admin@labourerp.com', '$2a$10$eACCYoNOHEqgkDw81cZJ.OCq6z0L1B/8tQzT75wXy81L2fL7K6kfa', 'Rajesh Verma (Managing Director)', '+91 9876543210', 'ACTIVE', 'SYSTEM'),
(2, 'manager', 'manager@labourerp.com', '$2a$10$eACCYoNOHEqgkDw81cZJ.OCq6z0L1B/8tQzT75wXy81L2fL7K6kfa', 'Vikram Singh (Operations Manager)', '+91 9876543211', 'ACTIVE', 'SYSTEM'),
(3, 'supervisor', 'supervisor@labourerp.com', '$2a$10$eACCYoNOHEqgkDw81cZJ.OCq6z0L1B/8tQzT75wXy81L2fL7K6kfa', 'Ramesh Yadav (Site Supervisor)', '+91 9876543212', 'ACTIVE', 'SYSTEM'),
(4, 'accountant', 'accountant@labourerp.com', '$2a$10$eACCYoNOHEqgkDw81cZJ.OCq6z0L1B/8tQzT75wXy81L2fL7K6kfa', 'Deepak Sharma (Head Accountant)', '+91 9876543213', 'ACTIVE', 'SYSTEM')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 3. ASSIGN ROLES TO USERS
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin -> ROLE_ADMIN
(2, 2), -- manager -> ROLE_MANAGER
(3, 3), -- supervisor -> ROLE_SITE_SUPERVISOR
(4, 4)  -- accountant -> ROLE_ACCOUNTANT
ON DUPLICATE KEY UPDATE user_id=VALUES(user_id);

-- 4. MASTER CATEGORIES (FOR DYNAMIC REPOSITORY)
INSERT INTO master_categories (id, category_code, category_name, description, is_system) VALUES
(1, 'BUSINESS_TYPE', 'Business & Organization Types', 'Classification of businesses operating under ERP', TRUE),
(2, 'LABOUR_CATEGORY', 'Labour Skill Categories', 'Worker functional qualification hierarchy', TRUE),
(3, 'DESIGNATION', 'Workforce Designations', 'Operational job titles and organizational roles', TRUE),
(4, 'ATTENDANCE_STATUS', 'Attendance Roster Statuses', 'Configurable daily muster status with wage multiplier', TRUE),
(5, 'PAYMENT_METHOD', 'Disbursement Payment Methods', 'Banking, digital and cash payment channels', TRUE),
(6, 'ADVANCE_TYPE', 'Monetary Advance Categories', 'Types of loan advances granted to workforce', TRUE),
(7, 'SITE_CATEGORY', 'Project & Site Categories', 'Class of construction / electrical site', TRUE),
(8, 'SKILL_TYPE', 'Technical Work Skills', 'Specific domain technical proficiencies', TRUE),
(9, 'SITE_STATUS', 'Site Operational Statuses', 'Lifecycle states for sites', TRUE),
(10, 'LABOUR_STATUS', 'Worker Employment Statuses', 'Status of workforce members', TRUE)
ON DUPLICATE KEY UPDATE category_name=VALUES(category_name);

-- 5. INITIAL BUSINESS: ELECTRICAL CONTRACTOR
INSERT INTO businesses (id, business_code, business_name, business_type, registration_no, gstin, address_line1, address_line2, city, state, pincode, contact_email, contact_phone, status, created_by) VALUES
(1, 'ELEC-CORP-01', 'Apex Electrical & Power Contracting Corp.', 'ELECTRICAL_CONTRACTOR', 'REG/DL/2021/88741', '07AAAAA0000A1Z5', 'Plot No. 42, Commercial Sector', 'Sector 62 Phase II', 'Noida', 'Uttar Pradesh', '201309', 'contact@apexelectrical.com', '+91 120 4567890', 'ACTIVE', 'SYSTEM'),
(2, 'PETROL-CORP-02', 'Apex Fuel & Petrol Retail Hub', 'PETROL_PUMP', 'REG/DL/2023/11290', '07BBBBB1111B2Z6', 'GT Road Junction', 'Near Industrial Corridor', 'Ghaziabad', 'Uttar Pradesh', '201001', 'operations@apexfuel.com', '+91 120 9876543', 'ACTIVE', 'SYSTEM')
ON DUPLICATE KEY UPDATE business_name=VALUES(business_name);

-- 6. MAP USERS TO BUSINESS
INSERT INTO business_users (business_id, user_id, role_id, is_primary) VALUES
(1, 1, 1, TRUE),
(1, 2, 2, TRUE),
(1, 3, 3, TRUE),
(1, 4, 4, TRUE),
(2, 1, 1, FALSE)
ON DUPLICATE KEY UPDATE is_primary=VALUES(is_primary);

-- 7. MASTER VALUES (FULLY CONFIGURABLE VIA UI)
-- Attendance Statuses (Category ID: 4)
INSERT INTO master_values (master_category_id, business_id, code, name, description, color_code, is_payable, wage_percentage, sort_order, is_active) VALUES
(4, NULL, 'PRESENT', 'Full Day Present', 'Full standard work shift completed', '#1E7E34', TRUE, 100.00, 1, TRUE),
(4, NULL, 'HALF_DAY', 'Half Day Work', 'Half shift logged (configurable % calculation)', '#D39E00', TRUE, 50.00, 2, TRUE),
(4, NULL, 'ABSENT', 'Absent (Unpaid)', 'Worker was not present at site', '#BD2130', FALSE, 0.00, 3, TRUE),
(4, NULL, 'PAID_LEAVE', 'Sanctioned Paid Leave', 'Approved leave with full compensation', '#245A85', TRUE, 100.00, 4, TRUE),
(4, NULL, 'WEEKLY_OFF', 'Weekly Mandatory Off', 'Designated weekly rest day', '#5F6B76', TRUE, 100.00, 5, TRUE),
(4, NULL, 'HOLIDAY', 'Gazetted / National Holiday', 'Official paid holiday', '#1B3557', TRUE, 100.00, 6, TRUE);

-- Labour Categories (Category ID: 2)
INSERT INTO master_values (master_category_id, business_id, code, name, description, color_code, is_payable, wage_percentage, sort_order, is_active) VALUES
(2, NULL, 'ELECTRICIAN', 'Electrician (Skilled)', 'Licensed electrician capable of complex conduit and wiring', '#1B3557', TRUE, 100.00, 1, TRUE),
(2, NULL, 'HELPER', 'Helper (Unskilled)', 'General assistant for wire pulling, trenching and carrying', '#5F6B76', TRUE, 100.00, 2, TRUE),
(2, NULL, 'TECHNICIAN', 'High-Tension Technician', 'Substation, transformer and HT cable specialist', '#245A85', TRUE, 100.00, 3, TRUE),
(2, NULL, 'SUPERVISOR', 'Electrical Site Supervisor', 'Responsible for day plan and labour muster roll', '#152A45', TRUE, 100.00, 4, TRUE),
(2, NULL, 'SEMI_SKILLED', 'Semi-Skilled Assistant', 'Junior wireman and tool operator', '#4B6584', TRUE, 100.00, 5, TRUE);

-- Designations (Category ID: 3)
INSERT INTO master_values (master_category_id, business_id, code, name, description, color_code, is_payable, wage_percentage, sort_order, is_active) VALUES
(3, NULL, 'SR_ELECTRICIAN', 'Senior Electrician', 'Lead on-site wiring technician', '#1B3557', TRUE, 100.00, 1, TRUE),
(3, NULL, 'JR_ELECTRICIAN', 'Junior Electrician', 'Field wireman', '#245A85', TRUE, 100.00, 2, TRUE),
(3, NULL, 'SITE_HELPER', 'Site General Helper', 'Site preparation and physical assistance', '#5F6B76', TRUE, 100.00, 3, TRUE),
(3, NULL, 'PANEL_BOARD_TECH', 'Control Panel Specialist', 'Panel wiring and testing technician', '#152A45', TRUE, 100.00, 4, TRUE),
(3, NULL, 'FOREMAN', 'Electrical Foreman', 'Gang leader and technical overseer', '#1E7E34', TRUE, 100.00, 5, TRUE);

-- Payment Methods (Category ID: 5)
INSERT INTO master_values (master_category_id, business_id, code, name, description, color_code, is_payable, wage_percentage, sort_order, is_active) VALUES
(5, NULL, 'CASH', 'Physical Cash Voucher', 'Disbursed directly at site against physical voucher sign', '#1B3557', TRUE, 100.00, 1, TRUE),
(5, NULL, 'BANK_TRANSFER', 'Bank NEFT / RTGS / IMPS', 'Direct bank account electronic transfer', '#245A85', TRUE, 100.00, 2, TRUE),
(5, NULL, 'UPI', 'UPI Digital Payment', 'Direct mobile UPI handle payment with UTR number', '#1E7E34', TRUE, 100.00, 3, TRUE),
(5, NULL, 'CHEQUE', 'Bank Cheque / Draft', 'Account payee crossed cheque disbursement', '#5F6B76', TRUE, 100.00, 4, TRUE);

-- Advance Types (Category ID: 6)
INSERT INTO master_values (master_category_id, business_id, code, name, description, color_code, is_payable, wage_percentage, sort_order, is_active) VALUES
(6, NULL, 'SALARY_ADVANCE', 'Monthly Salary Advance', 'Standard advance against upcoming wages', '#1B3557', TRUE, 100.00, 1, TRUE),
(6, NULL, 'EMERGENCY_ADVANCE', 'Medical / Family Emergency', 'Urgent advance for family medical requirements', '#BD2130', TRUE, 100.00, 2, TRUE),
(6, NULL, 'TRAVEL_ADVANCE', 'Site Relocation / Travel Advance', 'Transport cost assistance for remote projects', '#245A85', TRUE, 100.00, 3, TRUE),
(6, NULL, 'FESTIVAL_ADVANCE', 'Festival Bonus Advance', 'Seasonal festive celebration advance', '#D39E00', TRUE, 100.00, 4, TRUE);

-- 8. DEFAULT SITES FOR BHARAT ELECTRICAL
INSERT INTO sites (id, business_id, site_code, site_name, client_name, site_category, location_address, city, state, site_manager_id, supervisor_id, start_date, expected_end_date, status, notes, created_by) VALUES
(1, 1, 'SITE-NOI-62', 'Sector 62 Substation Expansion Project', 'Noida Power Distribution Authority', 'ELECTRICAL_SUBSTATION', 'Plot 10, Sector 62 Electrical Substation', 'Noida', 'Uttar Pradesh', 2, 3, '2026-01-10', '2026-12-31', 'ACTIVE', 'High-priority electrical grid upgrade project', 'admin'),
(2, 1, 'SITE-DEL-METRO', 'Delhi Metro Phase IV Electrification', 'Delhi Metro Rail Corp (DMRC)', 'COMMERCIAL_TRANSIT', 'Janakpuri West to RK Ashram Corridor Pier 45-80', 'New Delhi', 'Delhi', 2, 3, '2026-03-01', '2027-04-30', 'ACTIVE', 'Night shifts and high safety compliance site', 'admin'),
(3, 1, 'SITE-GUR-CYBER', 'Cyber City Tower-E Internal Cabling', 'DLF Infrastructure Ltd', 'COMMERCIAL_BUILDING', 'Tower E, DLF Phase II', 'Gurugram', 'Haryana', 2, 3, '2026-06-15', '2026-11-30', 'ACTIVE', 'Commercial interior conduit & smart lighting', 'admin')
ON DUPLICATE KEY UPDATE site_name=VALUES(site_name);

-- 9. INITIAL SAMPLE LABOURERS WITH REALISTIC BASELINE WAGES
INSERT INTO labourers (id, business_id, labour_code, full_name, mobile_number, aadhar_number, date_of_joining, designation, labour_category, skill_level, daily_wage, overtime_hourly_rate, current_site_id, status, created_by) VALUES
(1, 1, 'LBR-001', 'Rajesh Sharma', '+91 9811223344', '1234-5678-9012', '2026-01-15', 'Senior Electrician', 'ELECTRICIAN', 'SKILLED', 850.00, 110.00, 1, 'ACTIVE', 'admin'),
(2, 1, 'LBR-002', 'Sunil Kumar', '+91 9811223345', '2345-6789-0123', '2026-01-20', 'Site General Helper', 'HELPER', 'UNSKILLED', 550.00, 70.00, 1, 'ACTIVE', 'admin'),
(3, 1, 'LBR-003', 'Mohd. Imran', '+91 9811223346', '3456-7890-1234', '2026-02-01', 'High-Tension Technician', 'TECHNICIAN', 'HIGHLY_SKILLED', 950.00, 125.00, 1, 'ACTIVE', 'admin'),
(4, 1, 'LBR-004', 'Amit Mandal', '+91 9811223347', '4567-8901-2345', '2026-02-10', 'Junior Electrician', 'ELECTRICIAN', 'SEMI_SKILLED', 750.00, 95.00, 2, 'ACTIVE', 'admin'),
(5, 1, 'LBR-005', 'Dinesh Rawat', '+91 9811223348', '5678-9012-3456', '2026-02-15', 'Site General Helper', 'HELPER', 'UNSKILLED', 550.00, 70.00, 2, 'ACTIVE', 'admin'),
(6, 1, 'LBR-006', 'Suresh Chandra', '+91 9811223349', '6789-0123-4567', '2026-03-01', 'Electrical Foreman', 'SUPERVISOR', 'SKILLED', 1100.00, 140.00, 3, 'ACTIVE', 'admin')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 10. DEFAULT BUSINESS MODULES FOR ELECTRICAL CONTRACTOR
INSERT INTO business_modules (business_id, module_code, module_name, is_enabled) VALUES
(1, 'SITES', 'Site & Location Management', TRUE),
(1, 'LABOUR', 'Labour Workforce Directory', TRUE),
(1, 'ATTENDANCE', 'Daily Muster Roll & Attendance', TRUE),
(1, 'PAYROLL', 'Wages & Periodic Payroll', TRUE),
(1, 'ADVANCES', 'Workforce Advances & Loans', TRUE),
(1, 'PAYMENTS', 'Disbursements & Payment Register', TRUE),
(1, 'TRANSFERS', 'Inter-Site Labour Transfers', TRUE),
(1, 'REPORTS', 'Reports & Official Statements', TRUE),
(1, 'CUSTOM_FIELDS', 'Custom Field Builder', TRUE),
(1, 'AUDIT_LOG', 'Tamper-Evident Audit History', TRUE)
ON DUPLICATE KEY UPDATE is_enabled=VALUES(is_enabled);

-- 11. DEFAULT BUSINESS SETTINGS
INSERT INTO business_settings (business_id, setting_key, setting_value, data_type, description) VALUES
(1, 'CURRENCY_SYMBOL', '₹', 'STRING', 'Official currency symbol for reports and payments'),
(1, 'DATE_FORMAT', 'DD/MM/YYYY', 'STRING', 'Administrative date formatting standard'),
(1, 'HALF_DAY_PERCENT', '50.00', 'DECIMAL', 'Percentage of daily wage allocated for half day work'),
(1, 'WEEKLY_OFF_DAY', 'SUNDAY', 'STRING', 'Default weekly rest day'),
(1, 'WEEKLY_OFF_PAYABLE', 'TRUE', 'BOOLEAN', 'Whether designated weekly off is compensated'),
(1, 'OVERTIME_CALC_MODE', 'HOURLY_RATE', 'STRING', 'Overtime computation methodology: HOURLY_RATE or DAILY_PRO_RATA'),
(1, 'ADVANCE_MAX_RECOVERY_PERCENT', '35.00', 'DECIMAL', 'Max % of gross earnings deductible towards advances in single payroll period')
ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value);
