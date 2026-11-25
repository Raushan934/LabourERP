package in.gov.labourerp.config;

import in.gov.labourerp.entity.*;
import in.gov.labourerp.repository.*;
import in.gov.labourerp.service.SupabaseAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;
    private final BusinessUserRepository businessUserRepository;
    private final MasterCategoryRepository masterCategoryRepository;
    private final MasterValueRepository masterValueRepository;
    private final SiteRepository siteRepository;
    private final LabourerRepository labourerRepository;
    private final BusinessModuleRepository businessModuleRepository;
    private final BusinessSettingRepository businessSettingRepository;
    private final PasswordEncoder passwordEncoder;
    private final SupabaseAuthService supabaseAuthService;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           BusinessRepository businessRepository,
                           BusinessUserRepository businessUserRepository,
                           MasterCategoryRepository masterCategoryRepository,
                           MasterValueRepository masterValueRepository,
                           SiteRepository siteRepository,
                           LabourerRepository labourerRepository,
                           BusinessModuleRepository businessModuleRepository,
                           BusinessSettingRepository businessSettingRepository,
                           PasswordEncoder passwordEncoder,
                           SupabaseAuthService supabaseAuthService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.businessRepository = businessRepository;
        this.businessUserRepository = businessUserRepository;
        this.masterCategoryRepository = masterCategoryRepository;
        this.masterValueRepository = masterValueRepository;
        this.siteRepository = siteRepository;
        this.labourerRepository = labourerRepository;
        this.businessModuleRepository = businessModuleRepository;
        this.businessSettingRepository = businessSettingRepository;
        this.passwordEncoder = passwordEncoder;
        this.supabaseAuthService = supabaseAuthService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Enterprise Data & Supabase Security initialization...");
        seedRoles();
        seedUsers();
        seedMasterCategories();
        seedBusinesses();
        seedMasterValues();
        seedSites();
        seedLabourers();
        ensureUserSiteAssignments();
        log.info("Enterprise Data & Supabase Security initialization completed successfully.");
    }

    private void seedRoles() {
        createRoleIfMissing("ROLE_ADMIN", "Administrator / Owner", "Full administrative authority across all businesses and system modules");
        createRoleIfMissing("ROLE_MANAGER", "Operations Manager", "Operational control over assigned businesses and sites");
        createRoleIfMissing("ROLE_SITE_SUPERVISOR", "Site Supervisor", "On-site workforce supervision and daily muster attendance logging");
        createRoleIfMissing("ROLE_ACCOUNTANT", "Financial Accountant", "Disbursements, advances, wage settlement and statutory ledger verification");
    }

    private Role createRoleIfMissing(String code, String name, String desc) {
        return roleRepository.findByRoleCode(code).orElseGet(() -> roleRepository.save(new Role(code, name, desc)));
    }

    private void seedUsers() {
        Role adminRole = roleRepository.findByRoleCode("ROLE_ADMIN").orElseThrow();
        Role mgrRole = roleRepository.findByRoleCode("ROLE_MANAGER").orElseThrow();
        Role supRole = roleRepository.findByRoleCode("ROLE_SITE_SUPERVISOR").orElseThrow();
        Role accRole = roleRepository.findByRoleCode("ROLE_ACCOUNTANT").orElseThrow();

        createUserIfMissing("admin", "admin@labourerp.com", "Admin@123", "Rajesh Verma (Managing Director)", "+91 9876543210", adminRole);
        createUserIfMissing("manager", "manager@labourerp.com", "Manager@123", "Vikram Singh (Operations Manager)", "+91 9876543211", mgrRole);
        createUserIfMissing("supervisor", "supervisor@labourerp.com", "Supervisor@123", "Ramesh Yadav (Site Supervisor)", "+91 9876543212", supRole);
        createUserIfMissing("accountant", "accountant@labourerp.com", "Accountant@123", "Deepak Sharma (Head Accountant)", "+91 9876543213", accRole);
    }

    private void createUserIfMissing(String username, String email, String rawPassword, String fullName, String phone, Role role) {
        Optional<User> existing = userRepository.findByUsername(username).or(() -> userRepository.findByEmail(email));
        User user;
        if (existing.isEmpty()) {
            user = new User(username, email, passwordEncoder.encode(rawPassword), fullName, phone, "ACTIVE");
            user.getRoles().add(role);
            user = userRepository.save(user);
            log.info("Created user in public.users: {}", username);
        } else {
            user = existing.get();
            // Ensure role is present
            if (user.getRoles().isEmpty()) {
                user.getRoles().add(role);
                user = userRepository.save(user);
            }
        }

        // Sync with Supabase Auth auth.users table
        try {
            supabaseAuthService.syncUserToSupabaseAuth(email, rawPassword, role.getRoleCode(), fullName);
        } catch (Exception ex) {
            log.debug("Supabase auth sync notice for {}: {}", email, ex.getMessage());
        }
    }

    private void seedMasterCategories() {
        if (masterCategoryRepository.count() > 0) return;
        masterCategoryRepository.save(new MasterCategory("BUSINESS_TYPE", "Business & Organization Types", "Classification of businesses operating under ERP", true));
        masterCategoryRepository.save(new MasterCategory("LABOUR_CATEGORY", "Labour Skill Categories", "Worker functional qualification hierarchy", true));
        masterCategoryRepository.save(new MasterCategory("DESIGNATION", "Workforce Designations", "Operational job titles and organizational roles", true));
        masterCategoryRepository.save(new MasterCategory("ATTENDANCE_STATUS", "Attendance Roster Statuses", "Configurable daily muster status with wage multiplier", true));
        masterCategoryRepository.save(new MasterCategory("PAYMENT_METHOD", "Disbursement Payment Methods", "Banking, digital and cash payment channels", true));
        masterCategoryRepository.save(new MasterCategory("ADVANCE_TYPE", "Monetary Advance Categories", "Types of loan advances granted to workforce", true));
        masterCategoryRepository.save(new MasterCategory("SITE_CATEGORY", "Project & Site Categories", "Class of construction / electrical site", true));
        masterCategoryRepository.save(new MasterCategory("SKILL_TYPE", "Technical Work Skills", "Specific domain technical proficiencies", true));
        masterCategoryRepository.save(new MasterCategory("SITE_STATUS", "Site Operational Statuses", "Lifecycle states for sites", true));
        masterCategoryRepository.save(new MasterCategory("LABOUR_STATUS", "Worker Employment Statuses", "Status of workforce members", true));
    }

    private void seedBusinesses() {
        Business b1 = businessRepository.findByBusinessCode("ELEC-CORP-01").orElseGet(() -> {
            Business b = new Business();
            b.setBusinessCode("ELEC-CORP-01");
            b.setBusinessName("Apex Electrical & Power Contracting Corp.");
            b.setBusinessType("ELECTRICAL_CONTRACTOR");
            b.setRegistrationNo("REG/UP/2022/88910");
            b.setGstin("09AAAAA0000A1Z5");
            b.setAddressLine1("Plot No. 42, Commercial Sector");
            b.setAddressLine2("Sector 62 Phase II");
            b.setCity("Noida");
            b.setState("Uttar Pradesh");
            b.setPincode("201309");
            b.setContactEmail("contact@apexelectrical.com");
            b.setContactPhone("+91 120 4567890");
            b.setStatus("ACTIVE");
            b.setCreatedBy("SYSTEM");
            return businessRepository.save(b);
        });

        businessRepository.findByBusinessCode("PETROL-CORP-02").orElseGet(() -> {
            Business b = new Business();
            b.setBusinessCode("PETROL-CORP-02");
            b.setBusinessName("Apex Fuel & Petrol Retail Hub");
            b.setBusinessType("PETROL_PUMP");
            b.setRegistrationNo("REG/DL/2023/11290");
            b.setGstin("07BBBBB1111B2Z6");
            b.setAddressLine1("GT Road Junction");
            b.setAddressLine2("Near Industrial Corridor");
            b.setCity("Ghaziabad");
            b.setState("Uttar Pradesh");
            b.setPincode("201001");
            b.setContactEmail("operations@apexfuel.com");
            b.setContactPhone("+91 120 9876543");
            b.setStatus("ACTIVE");
            b.setCreatedBy("SYSTEM");
            return businessRepository.save(b);
        });

        // Ensure BusinessUser mappings exist for all users
        mapUserToBusinessIfMissing(b1, "admin", "ROLE_ADMIN", true);
        mapUserToBusinessIfMissing(b1, "manager", "ROLE_MANAGER", true);
        mapUserToBusinessIfMissing(b1, "supervisor", "ROLE_SITE_SUPERVISOR", true);
        mapUserToBusinessIfMissing(b1, "accountant", "ROLE_ACCOUNTANT", true);

        // Modules & settings
        if (businessModuleRepository.findByBusinessId(b1.getId()).isEmpty()) {
            String[][] modules = {
                    {"SITES", "Site & Location Management"},
                    {"LABOUR", "Labour Workforce Directory"},
                    {"ATTENDANCE", "Daily Muster Roll & Attendance"},
                    {"PAYROLL", "Wages & Periodic Payroll"},
                    {"ADVANCES", "Workforce Advances & Loans"},
                    {"PAYMENTS", "Disbursements & Payment Register"},
                    {"TRANSFERS", "Inter-Site Labour Transfers"},
                    {"REPORTS", "Reports & Official Statements"},
                    {"CUSTOM_FIELDS", "Custom Field Builder"},
                    {"AUDIT_LOG", "Tamper-Evident Audit History"}
            };
            for (String[] m : modules) {
                businessModuleRepository.save(new BusinessModule(b1, m[0], m[1], true));
            }
        }

        if (businessSettingRepository.findByBusinessId(b1.getId()).isEmpty()) {
            String[][] settings = {
                    {"CURRENCY_SYMBOL", "₹", "STRING", "Official currency symbol for reports and payments"},
                    {"DATE_FORMAT", "DD/MM/YYYY", "STRING", "Administrative date formatting standard"},
                    {"HALF_DAY_PERCENT", "50.00", "DECIMAL", "Percentage of daily wage allocated for half day work"},
                    {"WEEKLY_OFF_DAY", "SUNDAY", "STRING", "Default weekly rest day"},
                    {"WEEKLY_OFF_PAYABLE", "TRUE", "BOOLEAN", "Whether designated weekly off is compensated"},
                    {"OVERTIME_CALC_MODE", "HOURLY_RATE", "STRING", "Overtime computation methodology: HOURLY_RATE or DAILY_PRO_RATA"},
                    {"ADVANCE_MAX_RECOVERY_PERCENT", "35.00", "DECIMAL", "Max % of gross earnings deductible towards advances in single payroll period"}
            };
            for (String[] s : settings) {
                businessSettingRepository.save(new BusinessSetting(b1, s[0], s[1], s[2], s[3]));
            }
        }
    }

    private void mapUserToBusinessIfMissing(Business business, String username, String roleCode, boolean isPrimary) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (businessUserRepository.findByUserId(user.getId()).isEmpty()) {
                Role role = roleRepository.findByRoleCode(roleCode).orElseThrow();
                businessUserRepository.save(new BusinessUser(business, user, role, isPrimary));
            }
        });
    }

    private void seedMasterValues() {
        if (masterValueRepository.count() > 0) return;
        MasterCategory attCat = masterCategoryRepository.findByCategoryCode("ATTENDANCE_STATUS").orElseThrow();
        createMasterValue(attCat, "PRESENT", "Full Day Present", "Full shift completed", "#1E7E34", true, new BigDecimal("100.00"), 1);
        createMasterValue(attCat, "HALF_DAY", "Half Day Work", "Half shift logged", "#D39E00", true, new BigDecimal("50.00"), 2);
        createMasterValue(attCat, "ABSENT", "Absent (Unpaid)", "Not present", "#BD2130", false, BigDecimal.ZERO, 3);
        createMasterValue(attCat, "PAID_LEAVE", "Sanctioned Paid Leave", "Approved paid leave", "#245A85", true, new BigDecimal("100.00"), 4);
        createMasterValue(attCat, "WEEKLY_OFF", "Weekly Mandatory Off", "Rest day", "#5F6B76", true, new BigDecimal("100.00"), 5);
        createMasterValue(attCat, "HOLIDAY", "Gazetted Holiday", "Paid national holiday", "#1B3557", true, new BigDecimal("100.00"), 6);

        MasterCategory lbrCat = masterCategoryRepository.findByCategoryCode("LABOUR_CATEGORY").orElseThrow();
        createMasterValue(lbrCat, "ELECTRICIAN", "Electrician (Skilled)", "Licensed electrical craftsman", "#1B3557", true, new BigDecimal("100.00"), 1);
        createMasterValue(lbrCat, "HELPER", "Helper (Unskilled)", "Assistant labour", "#5F6B76", true, new BigDecimal("100.00"), 2);
        createMasterValue(lbrCat, "TECHNICIAN", "High-Tension Technician", "Substation and HT specialist", "#245A85", true, new BigDecimal("100.00"), 3);
        createMasterValue(lbrCat, "SUPERVISOR", "Electrical Site Supervisor", "Muster and site planner", "#152A45", true, new BigDecimal("100.00"), 4);
        createMasterValue(lbrCat, "SEMI_SKILLED", "Semi-Skilled Assistant", "Junior wireman", "#4B6584", true, new BigDecimal("100.00"), 5);

        MasterCategory desigCat = masterCategoryRepository.findByCategoryCode("DESIGNATION").orElseThrow();
        createMasterValue(desigCat, "SR_ELECTRICIAN", "Senior Electrician", "Lead wiring technician", "#1B3557", true, new BigDecimal("100.00"), 1);
        createMasterValue(desigCat, "JR_ELECTRICIAN", "Junior Electrician", "Field wireman", "#245A85", true, new BigDecimal("100.00"), 2);
        createMasterValue(desigCat, "SITE_HELPER", "Site General Helper", "Preparation and tools", "#5F6B76", true, new BigDecimal("100.00"), 3);
        createMasterValue(desigCat, "PANEL_BOARD_TECH", "Control Panel Specialist", "Panel assembly technician", "#152A45", true, new BigDecimal("100.00"), 4);
        createMasterValue(desigCat, "FOREMAN", "Electrical Foreman", "Crew head", "#1E7E34", true, new BigDecimal("100.00"), 5);

        MasterCategory payCat = masterCategoryRepository.findByCategoryCode("PAYMENT_METHOD").orElseThrow();
        createMasterValue(payCat, "CASH", "Physical Cash Voucher", "Cash voucher at site", "#1B3557", true, new BigDecimal("100.00"), 1);
        createMasterValue(payCat, "BANK_TRANSFER", "Bank NEFT / RTGS / IMPS", "Direct bank transfer", "#245A85", true, new BigDecimal("100.00"), 2);
        createMasterValue(payCat, "UPI", "UPI Digital Payment", "Mobile UPI handle", "#1E7E34", true, new BigDecimal("100.00"), 3);
        createMasterValue(payCat, "CHEQUE", "Bank Cheque / Draft", "Crossed cheque", "#5F6B76", true, new BigDecimal("100.00"), 4);

        MasterCategory advCat = masterCategoryRepository.findByCategoryCode("ADVANCE_TYPE").orElseThrow();
        createMasterValue(advCat, "SALARY_ADVANCE", "Monthly Salary Advance", "Advance against wages", "#1B3557", true, new BigDecimal("100.00"), 1);
        createMasterValue(advCat, "EMERGENCY_ADVANCE", "Medical Emergency", "Urgent medical loan", "#BD2130", true, new BigDecimal("100.00"), 2);
        createMasterValue(advCat, "TRAVEL_ADVANCE", "Relocation / Travel Advance", "Transit assistance", "#245A85", true, new BigDecimal("100.00"), 3);
    }

    private void createMasterValue(MasterCategory cat, String code, String name, String desc, String color, boolean payable, BigDecimal pct, int sort) {
        MasterValue mv = new MasterValue();
        mv.setCategory(cat);
        mv.setCode(code);
        mv.setName(name);
        mv.setDescription(desc);
        mv.setColorCode(color);
        mv.setPayable(payable);
        mv.setWagePercentage(pct);
        mv.setSortOrder(sort);
        mv.setActive(true);
        masterValueRepository.save(mv);
    }

    private void seedSites() {
        Business b1 = businessRepository.findByBusinessCode("ELEC-CORP-01").orElseThrow();
        User manager = userRepository.findByUsername("manager").orElse(null);
        User supervisor = userRepository.findByUsername("supervisor").orElse(null);

        createSiteIfMissing(b1, "SITE-NOI-62", "Sector 62 Substation Expansion Project", "Noida Power Distribution Authority",
                "ELECTRICAL_SUBSTATION", "Plot 10, Sector 62 Electrical Substation", "Noida", "Uttar Pradesh", manager, supervisor);

        createSiteIfMissing(b1, "SITE-DEL-METRO", "Delhi Metro Phase IV Electrification", "Delhi Metro Rail Corp (DMRC)",
                "COMMERCIAL_TRANSIT", "Janakpuri West to RK Ashram Corridor Pier 45-80", "New Delhi", "Delhi", manager, supervisor);

        createSiteIfMissing(b1, "SITE-GUR-CYBER", "Cyber City Tower-E Internal Cabling", "DLF Infrastructure Ltd",
                "COMMERCIAL_BUILDING", "Tower E, DLF Phase II", "Gurugram", "Haryana", manager, supervisor);
    }

    private void createSiteIfMissing(Business b, String code, String name, String client, String cat,
                                     String addr, String city, String state, User mgr, User sup) {
        if (siteRepository.findByBusinessIdAndSiteCode(b.getId(), code).isEmpty()) {
            Site s = new Site();
            s.setBusiness(b);
            s.setSiteCode(code);
            s.setSiteName(name);
            s.setClientName(client);
            s.setSiteCategory(cat);
            s.setLocationAddress(addr);
            s.setCity(city);
            s.setState(state);
            s.setSiteManager(mgr);
            s.setSupervisor(sup);
            s.setStartDate(LocalDate.of(2026, 1, 10));
            s.setExpectedEndDate(LocalDate.of(2026, 12, 31));
            s.setStatus("ACTIVE");
            s.setNotes("Enterprise contracted electrical project");
            s.setCreatedBy("admin");
            siteRepository.save(s);
        }
    }

    private void seedLabourers() {
        Business b1 = businessRepository.findByBusinessCode("ELEC-CORP-01").orElse(null);
        if (b1 == null) return;
        Site s1 = siteRepository.findByBusinessIdAndSiteCode(b1.getId(), "SITE-NOI-62").orElse(null);
        Site s2 = siteRepository.findByBusinessIdAndSiteCode(b1.getId(), "SITE-DEL-METRO").orElse(null);
        Site s3 = siteRepository.findByBusinessIdAndSiteCode(b1.getId(), "SITE-GUR-CYBER").orElse(null);

        if (labourerRepository.count() == 0) {
            createLabourer(b1, "LBR-001", "Rajesh Sharma", "+91 9811223344", "1234-5678-9012",
                    LocalDate.of(2026, 1, 15), "Senior Electrician", "ELECTRICIAN", "SKILLED",
                    new BigDecimal("850.00"), new BigDecimal("110.00"), s1,
                    "9811223344@ybl", "50100234567890", "HDFC0001234", "HDFC Bank", "Rajesh Sharma");

            createLabourer(b1, "LBR-002", "Sunil Kumar", "+91 9811223345", "2345-6789-0123",
                    LocalDate.of(2026, 1, 20), "Site General Helper", "HELPER", "UNSKILLED",
                    new BigDecimal("550.00"), new BigDecimal("70.00"), s1,
                    "9811223345@paytm", "30987654321", "SBIN0004567", "State Bank of India", "Sunil Kumar");

            createLabourer(b1, "LBR-003", "Mohd. Imran", "+91 9811223346", "3456-7890-1234",
                    LocalDate.of(2026, 2, 1), "Control Panel Specialist", "TECHNICIAN", "HIGHLY_SKILLED",
                    new BigDecimal("1100.00"), new BigDecimal("140.00"), s2,
                    "9811223346@okaxis", "918273645012", "UTIB0000890", "Axis Bank", "Mohd Imran");

            createLabourer(b1, "LBR-004", "Amit Patel", "+91 9811223347", "4567-8901-2345",
                    LocalDate.of(2026, 2, 5), "Junior Electrician", "ELECTRICIAN", "SEMI_SKILLED",
                    new BigDecimal("700.00"), new BigDecimal("90.00"), s2,
                    "9811223347@ibl", "102938475610", "ICIC0000102", "ICICI Bank", "Amit Patel");

            createLabourer(b1, "LBR-005", "Dharmendra Yadav", "+91 9811223348", "5678-9012-3456",
                    LocalDate.of(2026, 2, 10), "Electrical Foreman", "SUPERVISOR", "SUPERVISORY",
                    new BigDecimal("1200.00"), new BigDecimal("150.00"), s3,
                    "9811223348@upi", "654321098765", "BARB0NOIDAX", "Bank of Baroda", "Dharmendra Yadav");

            createLabourer(b1, "LBR-006", "Sanjay Verma", "+91 9811223349", "6789-0123-4567",
                    LocalDate.of(2026, 2, 12), "Site General Helper", "HELPER", "UNSKILLED",
                    new BigDecimal("550.00"), new BigDecimal("70.00"), s3,
                    "9811223349@upi", "456789012345", "PUNB0123400", "Punjab National Bank", "Sanjay Verma");
        }

        // Guarantee EVERY existing labourer has their specific UPI ID, bank details, and scannable UPI QR!
        labourerRepository.findAll().forEach(l -> {
            boolean changed = false;
            if (l.getUpiId() == null || l.getUpiId().isBlank()) {
                String phoneDigits = l.getMobileNumber() != null ? l.getMobileNumber().replaceAll("[^0-9]", "") : "";
                String upi = (phoneDigits.length() >= 10 ? phoneDigits.substring(phoneDigits.length() - 10) : l.getLabourCode().toLowerCase()) + "@upi";
                l.setUpiId(upi);
                changed = true;
            }
            if (l.getBankBeneficiaryName() == null || l.getBankBeneficiaryName().isBlank()) {
                l.setBankBeneficiaryName(l.getFullName());
                changed = true;
            }
            if (l.getBankAccountNumber() == null || l.getBankAccountNumber().isBlank()) {
                l.setBankAccountNumber("50100" + (10000000L + l.getId() * 3829L));
                l.setBankIfscCode("HDFC0001234");
                l.setBankName("HDFC Bank");
                changed = true;
            }
            if (l.getQrCodeUrl() == null || l.getQrCodeUrl().isBlank()) {
                String upiUri = "upi://pay?pa=" + l.getUpiId() + "&pn=" + URLEncoder.encode(l.getBankBeneficiaryName(), StandardCharsets.UTF_8) + "&cu=INR";
                String qr = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + URLEncoder.encode(upiUri, StandardCharsets.UTF_8);
                l.setQrCodeUrl(qr);
                changed = true;
            }
            if (changed) {
                labourerRepository.save(l);
            }
        });
    }

    private void createLabourer(Business b, String code, String name, String phone, String aadhaar,
                                LocalDate doj, String desig, String cat, String skill,
                                BigDecimal wage, BigDecimal ot, Site site,
                                String upiId, String bankAcc, String ifsc, String bankName, String beneficiary) {
        Labourer l = new Labourer();
        l.setBusiness(b);
        l.setLabourCode(code);
        l.setFullName(name);
        l.setMobileNumber(phone);
        l.setAadharNumber(aadhaar);
        l.setDateOfJoining(doj);
        l.setDesignation(desig);
        l.setLabourCategory(cat);
        l.setSkillLevel(skill);
        l.setDailyWage(wage);
        l.setOvertimeHourlyRate(ot);
        l.setCurrentSite(site);
        l.setStatus("ACTIVE");
        l.setCreatedBy("admin");
        l.setUpiId(upiId);
        l.setBankAccountNumber(bankAcc);
        l.setBankIfscCode(ifsc);
        l.setBankName(bankName);
        l.setBankBeneficiaryName(beneficiary);

        String upiUri = "upi://pay?pa=" + upiId + "&pn=" + URLEncoder.encode(beneficiary, StandardCharsets.UTF_8) + "&cu=INR";
        String qr = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + URLEncoder.encode(upiUri, StandardCharsets.UTF_8);
        l.setQrCodeUrl(qr);

        labourerRepository.save(l);
    }

    private void ensureUserSiteAssignments() {
        // Depute supervisor to Site 1 (SITE-NOI-62) if unassigned
        userRepository.findByUsername("supervisor").ifPresent(sup -> {
            if (sup.getAssignedSite() == null) {
                siteRepository.findFirstBySiteCode("SITE-NOI-62").ifPresent(s1 -> {
                    sup.setAssignedSite(s1);
                    userRepository.save(sup);
                    log.info("Deputed supervisor to site SITE-NOI-62");
                });
            }
        });

        // Guarantee EVERY user (all system roles and employees) has their specific UPI ID, bank details, and scannable UPI QR!
        userRepository.findAll().forEach(u -> {
            boolean changed = false;
            if (u.getUpiId() == null || u.getUpiId().isBlank()) {
                String phoneDigits = u.getPhone() != null ? u.getPhone().replaceAll("[^0-9]", "") : "";
                String upi = (phoneDigits.length() >= 10 ? phoneDigits.substring(phoneDigits.length() - 10) : u.getUsername().toLowerCase()) + "@upi";
                u.setUpiId(upi);
                changed = true;
            }
            if (u.getBankBeneficiaryName() == null || u.getBankBeneficiaryName().isBlank()) {
                u.setBankBeneficiaryName(u.getFullName());
                changed = true;
            }
            if (u.getBankAccountNumber() == null || u.getBankAccountNumber().isBlank()) {
                u.setBankAccountNumber("3098" + (10000000L + u.getId() * 4721L));
                u.setBankIfscCode("SBIN0004567");
                u.setBankName("State Bank of India");
                changed = true;
            }
            if (u.getQrCodeUrl() == null || u.getQrCodeUrl().isBlank()) {
                String upiUri = "upi://pay?pa=" + u.getUpiId() + "&pn=" + URLEncoder.encode(u.getBankBeneficiaryName(), StandardCharsets.UTF_8) + "&cu=INR";
                String qr = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + URLEncoder.encode(upiUri, StandardCharsets.UTF_8);
                u.setQrCodeUrl(qr);
                changed = true;
            }
            if (changed) {
                userRepository.save(u);
                log.info("Initialized specific UPI QR & bank credentials for user '{}' ({})", u.getUsername(), u.getUpiId());
            }
        });
    }
}
