package in.gov.labourerp.service;

import in.gov.labourerp.dto.finance.PaymentDto;
import in.gov.labourerp.entity.Business;
import in.gov.labourerp.entity.Labourer;
import in.gov.labourerp.entity.Payment;
import in.gov.labourerp.entity.Site;
import in.gov.labourerp.repository.BusinessRepository;
import in.gov.labourerp.repository.LabourerRepository;
import in.gov.labourerp.repository.PaymentRepository;
import in.gov.labourerp.repository.SiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BusinessRepository businessRepository;
    private final LabourerRepository labourerRepository;
    private final SiteRepository siteRepository;
    private final AuditLogService auditLogService;

    public PaymentService(PaymentRepository paymentRepository,
                          BusinessRepository businessRepository,
                          LabourerRepository labourerRepository,
                          SiteRepository siteRepository,
                          AuditLogService auditLogService) {
        this.paymentRepository = paymentRepository;
        this.businessRepository = businessRepository;
        this.labourerRepository = labourerRepository;
        this.siteRepository = siteRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getPayments(Long businessId, Long labourerId, Long siteId,
                                        String paymentMethod, LocalDate startDate, LocalDate endDate,
                                        Pageable pageable) {
        Page<Payment> page = paymentRepository.findByFilters(businessId, labourerId, siteId, paymentMethod, startDate, endDate, pageable);
        return page.map(this::mapToDto);
    }

    @Transactional
    public PaymentDto recordPayment(PaymentDto dto, String username, String ipAddress) {
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + dto.getBusinessId()));

        Labourer labourer = labourerRepository.findById(dto.getLabourerId())
                .orElseThrow(() -> new RuntimeException("Labourer not found with ID: " + dto.getLabourerId()));

        Payment payment = new Payment();
        payment.setBusiness(business);
        payment.setLabourer(labourer);

        if (dto.getSiteId() != null) {
            siteRepository.findById(dto.getSiteId()).ifPresent(payment::setSite);
        } else if (labourer.getCurrentSite() != null) {
            payment.setSite(labourer.getCurrentSite());
        }

        String payCode = dto.getPaymentCode() != null && !dto.getPaymentCode().isBlank()
                ? dto.getPaymentCode()
                : "PAY-" + LocalDate.now().getYear() + "-" + (System.currentTimeMillis() % 100000);

        payment.setPaymentCode(payCode);
        payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDate.now());
        payment.setAmount(dto.getAmount());
        payment.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "CASH");
        payment.setReferenceNumber(dto.getReferenceNumber());
        payment.setReceiptAttachmentUrl(dto.getReceiptAttachmentUrl());
        payment.setRemarks(dto.getRemarks());
        payment.setRecordedBy(username);

        Payment saved = paymentRepository.save(payment);

        auditLogService.logAction(
                dto.getBusinessId(), null, username, "CREATE", "PAYMENT",
                saved.getId().toString(), null,
                "Recorded payment disbursement of ₹" + saved.getAmount() + " to worker: " + labourer.getFullName() + " (" + saved.getPaymentCode() + ")",
                ipAddress
        );

        return mapToDto(saved);
    }

    private PaymentDto mapToDto(Payment p) {
        PaymentDto dto = new PaymentDto();
        dto.setId(p.getId());
        dto.setBusinessId(p.getBusiness().getId());
        if (p.getSite() != null) {
            dto.setSiteId(p.getSite().getId());
            dto.setSiteName(p.getSite().getSiteName());
        }
        dto.setLabourerId(p.getLabourer().getId());
        dto.setLabourCode(p.getLabourer().getLabourCode());
        dto.setLabourName(p.getLabourer().getFullName());
        dto.setPaymentCode(p.getPaymentCode());
        dto.setPaymentDate(p.getPaymentDate());
        dto.setAmount(p.getAmount());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setReferenceNumber(p.getReferenceNumber());
        dto.setReceiptAttachmentUrl(p.getReceiptAttachmentUrl());
        dto.setRemarks(p.getRemarks());
        dto.setRecordedBy(p.getRecordedBy());
        return dto;
    }
}
