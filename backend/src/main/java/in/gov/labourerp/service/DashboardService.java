package in.gov.labourerp.service;

import in.gov.labourerp.dto.dashboard.DashboardSummaryDto;
import in.gov.labourerp.dto.dashboard.SiteSummaryDto;
import in.gov.labourerp.entity.AttendanceRecord;
import in.gov.labourerp.entity.Site;
import in.gov.labourerp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final BusinessRepository businessRepository;
    private final SiteRepository siteRepository;
    private final LabourerRepository labourerRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AdvanceRepository advanceRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(BusinessRepository businessRepository,
                            SiteRepository siteRepository,
                            LabourerRepository labourerRepository,
                            AttendanceRecordRepository attendanceRecordRepository,
                            AdvanceRepository advanceRepository,
                            PaymentRepository paymentRepository) {
        this.businessRepository = businessRepository;
        this.siteRepository = siteRepository;
        this.labourerRepository = labourerRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.advanceRepository = advanceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(Long businessId) {
        DashboardSummaryDto dto = new DashboardSummaryDto();

        dto.setTotalBusinesses(businessRepository.count());
        dto.setActiveBusinesses(businessRepository.findByStatus("ACTIVE").size());

        long activeSites = siteRepository.countByBusinessIdAndStatus(businessId, "ACTIVE");
        dto.setTotalActiveSites(activeSites);

        long activeLabourers = labourerRepository.countByBusinessIdAndStatus(businessId, "ACTIVE");
        dto.setTotalActiveLabourers(activeLabourers);

        dto.setTotalSupervisors(2); // In sample / registered roles

        // Today's attendance
        LocalDate today = LocalDate.now();
        long present = attendanceRecordRepository.countByBusinessAndDateAndStatus(businessId, today, "PRESENT");
        long halfDay = attendanceRecordRepository.countByBusinessAndDateAndStatus(businessId, today, "HALF_DAY");
        long absent = attendanceRecordRepository.countByBusinessAndDateAndStatus(businessId, today, "ABSENT");
        long paidLeave = attendanceRecordRepository.countByBusinessAndDateAndStatus(businessId, today, "PAID_LEAVE");

        long totalMarked = present + halfDay + absent + paidLeave;
        long notSubmitted = Math.max(0, activeLabourers - totalMarked);

        dto.setTodayPresent(present);
        dto.setTodayHalfDay(halfDay);
        dto.setTodayAbsent(absent);
        dto.setTodayPaidLeave(paidLeave);
        dto.setTodayNotSubmitted(notSubmitted);

        // Current Month Financial Overview
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        List<AttendanceRecord> monthRecords = attendanceRecordRepository
                .findByBusinessIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(businessId, startOfMonth, endOfMonth);

        BigDecimal monthWages = BigDecimal.ZERO;
        for (AttendanceRecord ar : monthRecords) {
            monthWages = monthWages.add(ar.getCalculatedWage()).add(ar.getOvertimeAmount());
        }
        dto.setPeriodTotalWages(monthWages);

        BigDecimal paymentsMade = paymentRepository.calculateTotalPaymentsByBusiness(businessId, startOfMonth, endOfMonth);
        dto.setPeriodPaymentsMade(paymentsMade != null ? paymentsMade : BigDecimal.ZERO);

        BigDecimal outstandingWages = monthWages.subtract(dto.getPeriodPaymentsMade());
        dto.setPeriodOutstandingWages(outstandingWages.max(BigDecimal.ZERO));

        BigDecimal outstandingAdvances = advanceRepository.calculateTotalOutstandingAdvanceByBusiness(businessId);
        dto.setTotalOutstandingAdvances(outstandingAdvances != null ? outstandingAdvances : BigDecimal.ZERO);
        dto.setPendingPaymentAmount(outstandingWages.max(BigDecimal.ZERO));

        // Site Overview Table
        List<Site> sites = siteRepository.findByBusinessId(businessId);
        List<SiteSummaryDto> siteSummaries = new ArrayList<>();

        for (Site site : sites) {
            long siteLabourCount = labourerRepository.countActiveBySite(businessId, site.getId());
            long sitePresent = attendanceRecordRepository.countBySiteAndDateAndStatus(site.getId(), today, "PRESENT");
            long siteAbsent = attendanceRecordRepository.countBySiteAndDateAndStatus(site.getId(), today, "ABSENT");
            long siteHalfDay = attendanceRecordRepository.countBySiteAndDateAndStatus(site.getId(), today, "HALF_DAY");

            siteSummaries.add(new SiteSummaryDto(
                    site.getId(),
                    site.getSiteCode(),
                    site.getSiteName(),
                    siteLabourCount,
                    sitePresent,
                    siteAbsent,
                    siteHalfDay,
                    site.getStatus()
            ));
        }
        dto.setSiteSummaries(siteSummaries);

        return dto;
    }
}
