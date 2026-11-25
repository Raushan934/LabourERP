package in.gov.labourerp.service;

import in.gov.labourerp.dto.business.BusinessDto;
import in.gov.labourerp.dto.business.BusinessModuleDto;
import in.gov.labourerp.dto.business.BusinessSettingDto;
import in.gov.labourerp.entity.*;
import in.gov.labourerp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessModuleRepository businessModuleRepository;
    private final BusinessSettingRepository businessSettingRepository;
    private final BusinessUserRepository businessUserRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public BusinessService(BusinessRepository businessRepository,
                           BusinessModuleRepository businessModuleRepository,
                           BusinessSettingRepository businessSettingRepository,
                           BusinessUserRepository businessUserRepository,
                           RoleRepository roleRepository,
                           UserRepository userRepository,
                           AuditLogService auditLogService) {
        this.businessRepository = businessRepository;
        this.businessModuleRepository = businessModuleRepository;
        this.businessSettingRepository = businessSettingRepository;
        this.businessUserRepository = businessUserRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<BusinessDto> getAllBusinesses() {
        return businessRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BusinessDto getBusinessById(Long id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + id));
        return mapToDto(business);
    }

    @Transactional
    public BusinessDto createBusiness(BusinessDto dto, String username, String ipAddress) {
        if (businessRepository.existsByBusinessCode(dto.getBusinessCode())) {
            throw new RuntimeException("Business code already exists: " + dto.getBusinessCode());
        }

        Business business = new Business();
        business.setBusinessCode(dto.getBusinessCode());
        business.setBusinessName(dto.getBusinessName());
        business.setBusinessType(dto.getBusinessType() != null ? dto.getBusinessType() : "ELECTRICAL_CONTRACTOR");
        business.setRegistrationNo(dto.getRegistrationNo());
        business.setGstin(dto.getGstin());
        business.setAddressLine1(dto.getAddressLine1());
        business.setAddressLine2(dto.getAddressLine2());
        business.setCity(dto.getCity());
        business.setState(dto.getState());
        business.setPincode(dto.getPincode());
        business.setContactEmail(dto.getContactEmail());
        business.setContactPhone(dto.getContactPhone());
        business.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        business.setCreatedBy(username);

        Business saved = businessRepository.save(business);

        // Seed default core modules for the new business
        initializeDefaultModules(saved);

        // Seed default settings for the new business
        initializeDefaultSettings(saved);

        // Map creator admin user to the new business
        userRepository.findByUsername(username).ifPresent(user -> {
            Role adminRole = roleRepository.findByRoleCode("ROLE_ADMIN").orElse(null);
            if (adminRole != null) {
                BusinessUser bu = new BusinessUser(saved, user, adminRole, false);
                businessUserRepository.save(bu);
            }
        });

        auditLogService.logAction(
                saved.getId(), null, username, "CREATE", "BUSINESS",
                saved.getId().toString(), null, "Created new business entity: " + saved.getBusinessName(), ipAddress
        );

        return mapToDto(saved);
    }

    @Transactional
    public BusinessDto updateBusiness(Long id, BusinessDto dto, String username, String ipAddress) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + id));

        business.setBusinessName(dto.getBusinessName());
        business.setBusinessType(dto.getBusinessType());
        business.setRegistrationNo(dto.getRegistrationNo());
        business.setGstin(dto.getGstin());
        business.setAddressLine1(dto.getAddressLine1());
        business.setAddressLine2(dto.getAddressLine2());
        business.setCity(dto.getCity());
        business.setState(dto.getState());
        business.setPincode(dto.getPincode());
        business.setContactEmail(dto.getContactEmail());
        business.setContactPhone(dto.getContactPhone());
        business.setStatus(dto.getStatus());
        business.setUpdatedBy(username);

        Business updated = businessRepository.save(business);

        auditLogService.logAction(
                updated.getId(), null, username, "UPDATE", "BUSINESS",
                updated.getId().toString(), null, "Updated business details: " + updated.getBusinessName(), ipAddress
        );

        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public List<BusinessModuleDto> getBusinessModules(Long businessId) {
        return businessModuleRepository.findByBusinessId(businessId).stream()
                .map(bm -> new BusinessModuleDto(bm.getId(), bm.getModuleCode(), bm.getModuleName(), bm.isEnabled()))
                .collect(Collectors.toList());
    }

    @Transactional
    public BusinessModuleDto toggleBusinessModule(Long businessId, String moduleCode, boolean enabled, String username) {
        BusinessModule module = businessModuleRepository.findByBusinessIdAndModuleCode(businessId, moduleCode)
                .orElseThrow(() -> new RuntimeException("Module not found: " + moduleCode));
        module.setEnabled(enabled);
        BusinessModule saved = businessModuleRepository.save(module);
        return new BusinessModuleDto(saved.getId(), saved.getModuleCode(), saved.getModuleName(), saved.isEnabled());
    }

    @Transactional(readOnly = true)
    public List<BusinessSettingDto> getBusinessSettings(Long businessId) {
        return businessSettingRepository.findByBusinessId(businessId).stream()
                .map(bs -> new BusinessSettingDto(bs.getId(), bs.getSettingKey(), bs.getSettingValue(), bs.getDataType(), bs.getDescription()))
                .collect(Collectors.toList());
    }

    @Transactional
    public BusinessSettingDto updateBusinessSetting(Long businessId, String settingKey, String settingValue, String username) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + businessId));

        BusinessSetting setting = businessSettingRepository.findByBusinessIdAndSettingKey(businessId, settingKey)
                .orElse(new BusinessSetting(business, settingKey, settingValue, "STRING", "Custom setting"));

        setting.setSettingValue(settingValue);
        setting.setUpdatedBy(username);
        BusinessSetting saved = businessSettingRepository.save(setting);

        return new BusinessSettingDto(saved.getId(), saved.getSettingKey(), saved.getSettingValue(), saved.getDataType(), saved.getDescription());
    }

    private void initializeDefaultModules(Business business) {
        String[][] defaultModules = {
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
        for (String[] mod : defaultModules) {
            businessModuleRepository.save(new BusinessModule(business, mod[0], mod[1], true));
        }
    }

    private void initializeDefaultSettings(Business business) {
        String[][] defaultSettings = {
                {"CURRENCY_SYMBOL", "₹", "STRING", "Official currency symbol for reports and payments"},
                {"DATE_FORMAT", "DD/MM/YYYY", "STRING", "Administrative date formatting standard"},
                {"HALF_DAY_PERCENT", "50.00", "DECIMAL", "Percentage of daily wage allocated for half day work"},
                {"WEEKLY_OFF_DAY", "SUNDAY", "STRING", "Default weekly rest day"},
                {"WEEKLY_OFF_PAYABLE", "TRUE", "BOOLEAN", "Whether designated weekly off is compensated"},
                {"OVERTIME_CALC_MODE", "HOURLY_RATE", "STRING", "Overtime computation methodology: HOURLY_RATE or DAILY_PRO_RATA"},
                {"ADVANCE_MAX_RECOVERY_PERCENT", "35.00", "DECIMAL", "Max % of gross earnings deductible towards advances in single payroll period"}
        };
        for (String[] set : defaultSettings) {
            businessSettingRepository.save(new BusinessSetting(business, set[0], set[1], set[2], set[3]));
        }
    }

    private BusinessDto mapToDto(Business b) {
        BusinessDto dto = new BusinessDto();
        dto.setId(b.getId());
        dto.setBusinessCode(b.getBusinessCode());
        dto.setBusinessName(b.getBusinessName());
        dto.setBusinessType(b.getBusinessType());
        dto.setRegistrationNo(b.getRegistrationNo());
        dto.setGstin(b.getGstin());
        dto.setAddressLine1(b.getAddressLine1());
        dto.setAddressLine2(b.getAddressLine2());
        dto.setCity(b.getCity());
        dto.setState(b.getState());
        dto.setPincode(b.getPincode());
        dto.setContactEmail(b.getContactEmail());
        dto.setContactPhone(b.getContactPhone());
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }
}
