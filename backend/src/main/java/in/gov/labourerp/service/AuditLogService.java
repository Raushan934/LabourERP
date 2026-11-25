package in.gov.labourerp.service;

import in.gov.labourerp.dto.audit.AuditLogDto;
import in.gov.labourerp.entity.AuditLog;
import in.gov.labourerp.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(Long businessId, Long userId, String username, String actionType,
                          String entityType, String entityId, String previousValue,
                          String newValue, String ipAddress) {
        AuditLog log = new AuditLog(businessId, userId, username, actionType, entityType,
                entityId, previousValue, newValue, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogs(Long businessId, String entityType, String actionType,
                                          LocalDateTime startDate, LocalDateTime endDate,
                                          Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByFilters(businessId, entityType, actionType, startDate, endDate, pageable);
        return page.map(this::mapToDto);
    }

    private AuditLogDto mapToDto(AuditLog log) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(log.getId());
        dto.setBusinessId(log.getBusinessId());
        dto.setUserId(log.getUserId());
        dto.setUsername(log.getUsername());
        dto.setActionType(log.getActionType());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setPreviousValue(log.getPreviousValue());
        dto.setNewValue(log.getNewValue());
        dto.setIpAddress(log.getIpAddress());
        dto.setUserAgent(log.getUserAgent());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
