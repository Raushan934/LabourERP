package in.gov.labourerp.service;

import in.gov.labourerp.dto.report.LabourStatementDto;
import in.gov.labourerp.dto.report.SiteMonthlyReportDto;
import in.gov.labourerp.entity.*;
import in.gov.labourerp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final LabourerRepository labourerRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AdvanceRepository advanceRepository;
    private final PaymentRepository paymentRepository;
    private final SiteRepository siteRepository;

    public ReportService(LabourerRepository labourerRepository,
                         AttendanceRecordRepository attendanceRecordRepository,
                         AdvanceRepository advanceRepository,
                         PaymentRepository paymentRepository,
                         SiteRepository siteRepository) {
        this.labourerRepository = labourerRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.advanceRepository = advanceRepository;
        this.paymentRepository = paymentRepository;
        this.siteRepository = siteRepository;
    }

    @Transactional(readOnly = true)
    public LabourStatementDto getLabourStatement(Long labourerId, LocalDate startDate, LocalDate endDate) {
        Labourer labourer = labourerRepository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found with ID: " + labourerId));

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        LabourStatementDto dto = new LabourStatementDto();
        dto.setLabourerId(labourer.getId());
        dto.setLabourCode(labourer.getLabourCode());
        dto.setFullName(labourer.getFullName());
        dto.setDesignation(labourer.getDesignation());
        dto.setLabourCategory(labourer.getLabourCategory());
        dto.setDailyWage(labourer.getDailyWage());
        dto.setBusinessName(labourer.getBusiness().getBusinessName());
        dto.setCurrentSiteName(labourer.getCurrentSite() != null ? labourer.getCurrentSite().getSiteName() : "Unassigned");
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        List<AttendanceRecord> attendanceList = attendanceRecordRepository
                .findByLabourerIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(labourerId, startDate, endDate);

        BigDecimal presentDays = BigDecimal.ZERO;
        BigDecimal halfDays = BigDecimal.ZERO;
        BigDecimal absentDays = BigDecimal.ZERO;
        BigDecimal paidLeaves = BigDecimal.ZERO;
        BigDecimal otHours = BigDecimal.ZERO;
        BigDecimal grossEarnings = BigDecimal.ZERO;

        List<LabourStatementDto.AttendanceItemReport> attReports = new ArrayList<>();
        for (AttendanceRecord ar : attendanceList) {
            switch (ar.getStatus().toUpperCase()) {
                case "PRESENT": presentDays = presentDays.add(BigDecimal.ONE); break;
                case "HALF_DAY": halfDays = halfDays.add(BigDecimal.ONE); break;
                case "ABSENT": absentDays = absentDays.add(BigDecimal.ONE); break;
                case "PAID_LEAVE":
                case "WEEKLY_OFF":
                case "HOLIDAY": paidLeaves = paidLeaves.add(BigDecimal.ONE); break;
            }
            otHours = otHours.add(ar.getOvertimeHours());
            BigDecimal dayTotal = ar.getCalculatedWage().add(ar.getOvertimeAmount());
            grossEarnings = grossEarnings.add(dayTotal);

            attReports.add(new LabourStatementDto.AttendanceItemReport(
                    ar.getAttendanceDate(),
                    ar.getSite().getSiteName(),
                    ar.getStatus(),
                    ar.getOvertimeHours(),
                    dayTotal
            ));
        }
        dto.setTotalPresentDays(presentDays);
        dto.setTotalHalfDays(halfDays);
        dto.setTotalAbsentDays(absentDays);
        dto.setTotalPaidLeaves(paidLeaves);
        dto.setTotalOvertimeHours(otHours);
        dto.setGrossEarnings(grossEarnings);
        dto.setAttendanceDetails(attReports);

        // Advances
        List<Advance> advances = advanceRepository.findByLabourerId(labourerId);
        BigDecimal totalAdvances = BigDecimal.ZERO;
        BigDecimal totalRecovered = BigDecimal.ZERO;
        List<LabourStatementDto.AdvanceItemReport> advReports = new ArrayList<>();

        for (Advance adv : advances) {
            totalAdvances = totalAdvances.add(adv.getAmount());
            totalRecovered = totalRecovered.add(adv.getRecoveredAmount());
            advReports.add(new LabourStatementDto.AdvanceItemReport(
                    adv.getAdvanceCode(),
                    adv.getAdvanceDate(),
                    adv.getAmount(),
                    adv.getPaymentMethod(),
                    adv.getPurpose(),
                    adv.getRecoveryStatus()
            ));
        }
        dto.setTotalAdvancesReceived(totalAdvances);
        dto.setTotalAdvancesRecovered(totalRecovered);
        dto.setOutstandingAdvanceBalance(totalAdvances.subtract(totalRecovered));
        dto.setAdvanceDetails(advReports);

        // Payments
        List<Payment> payments = paymentRepository.findByLabourerId(labourerId);
        BigDecimal totalPayments = BigDecimal.ZERO;
        List<LabourStatementDto.PaymentItemReport> payReports = new ArrayList<>();

        for (Payment p : payments) {
            totalPayments = totalPayments.add(p.getAmount());
            payReports.add(new LabourStatementDto.PaymentItemReport(
                    p.getPaymentCode(),
                    p.getPaymentDate(),
                    p.getAmount(),
                    p.getPaymentMethod(),
                    p.getReferenceNumber()
            ));
        }
        dto.setTotalPaymentsReceived(totalPayments);
        dto.setPaymentDetails(payReports);

        BigDecimal netPayable = grossEarnings.subtract(dto.getOutstandingAdvanceBalance()).subtract(totalPayments);
        dto.setNetOutstandingPayable(netPayable);

        return dto;
    }

    @Transactional(readOnly = true)
    public SiteMonthlyReportDto getSiteMonthlyReport(Long siteId, int month, int year) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("Site not found with ID: " + siteId));

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        SiteMonthlyReportDto report = new SiteMonthlyReportDto();
        report.setSiteId(site.getId());
        report.setSiteCode(site.getSiteCode());
        report.setSiteName(site.getSiteName());
        report.setClientName(site.getClientName());
        report.setMonth(month);
        report.setYear(year);

        List<Labourer> labourers = labourerRepository.findByBusinessIdAndCurrentSiteId(site.getBusiness().getId(), site.getId());
        report.setTotalAssignedLabour(labourers.size());

        BigDecimal sitePresent = BigDecimal.ZERO;
        BigDecimal siteOt = BigDecimal.ZERO;
        BigDecimal siteWages = BigDecimal.ZERO;
        BigDecimal siteAdvances = BigDecimal.ZERO;
        BigDecimal sitePayments = BigDecimal.ZERO;
        BigDecimal siteBalance = BigDecimal.ZERO;

        List<SiteMonthlyReportDto.LabourRowSummary> rows = new ArrayList<>();

        for (Labourer l : labourers) {
            List<AttendanceRecord> records = attendanceRecordRepository
                    .findByLabourerIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(l.getId(), start, end);

            BigDecimal pDays = BigDecimal.ZERO;
            BigDecimal hDays = BigDecimal.ZERO;
            BigDecimal aDays = BigDecimal.ZERO;
            BigDecimal ot = BigDecimal.ZERO;
            BigDecimal gross = BigDecimal.ZERO;

            for (AttendanceRecord ar : records) {
                if (ar.getSite().getId().equals(siteId)) {
                    if ("PRESENT".equalsIgnoreCase(ar.getStatus())) pDays = pDays.add(BigDecimal.ONE);
                    else if ("HALF_DAY".equalsIgnoreCase(ar.getStatus())) hDays = hDays.add(BigDecimal.ONE);
                    else if ("ABSENT".equalsIgnoreCase(ar.getStatus())) aDays = aDays.add(BigDecimal.ONE);
                    ot = ot.add(ar.getOvertimeHours());
                    gross = gross.add(ar.getCalculatedWage()).add(ar.getOvertimeAmount());
                }
            }

            BigDecimal advDisbursed = advanceRepository.calculateOutstandingAdvanceByLabourer(l.getId());
            if (advDisbursed == null) advDisbursed = BigDecimal.ZERO;

            BigDecimal payMade = paymentRepository.calculateTotalPaymentsByLabourer(l.getId(), start, end);
            if (payMade == null) payMade = BigDecimal.ZERO;

            BigDecimal bal = gross.subtract(payMade).setScale(2, RoundingMode.HALF_UP);

            SiteMonthlyReportDto.LabourRowSummary row = new SiteMonthlyReportDto.LabourRowSummary();
            row.setLabourerId(l.getId());
            row.setLabourCode(l.getLabourCode());
            row.setFullName(l.getFullName());
            row.setDesignation(l.getDesignation());
            row.setDailyWage(l.getDailyWage());
            row.setPresentDays(pDays);
            row.setHalfDays(hDays);
            row.setAbsentDays(aDays);
            row.setOvertimeHours(ot);
            row.setGrossEarnings(gross);
            row.setAdvancesDisbursed(advDisbursed);
            row.setPaymentsMade(payMade);
            row.setBalanceDue(bal);

            rows.add(row);

            sitePresent = sitePresent.add(pDays);
            siteOt = siteOt.add(ot);
            siteWages = siteWages.add(gross);
            siteAdvances = siteAdvances.add(advDisbursed);
            sitePayments = sitePayments.add(payMade);
            siteBalance = siteBalance.add(bal);
        }

        report.setTotalPresentDays(sitePresent);
        report.setTotalOvertimeHours(siteOt);
        report.setTotalGrossWages(siteWages);
        report.setTotalAdvancesDisbursed(siteAdvances);
        report.setTotalPaymentsMade(sitePayments);
        report.setTotalOutstandingBalance(siteBalance);
        report.setWorkerRows(rows);

        return report;
    }

    public String generateCsvStatement(LabourStatementDto statement) {
        StringBuilder sb = new StringBuilder();
        sb.append("OFFICIAL LABOUR STATEMENT\n");
        sb.append("Business:,").append(statement.getBusinessName()).append("\n");
        sb.append("Worker ID:,").append(statement.getLabourCode()).append("\n");
        sb.append("Worker Name:,").append(statement.getFullName()).append("\n");
        sb.append("Designation:,").append(statement.getDesignation()).append("\n");
        sb.append("Category:,").append(statement.getLabourCategory()).append("\n");
        sb.append("Period:,").append(statement.getStartDate()).append(" to ").append(statement.getEndDate()).append("\n\n");

        sb.append("FINANCIAL SUMMARY\n");
        sb.append("Gross Earnings (Rs),").append(statement.getGrossEarnings()).append("\n");
        sb.append("Total Advances Disbursed (Rs),").append(statement.getTotalAdvancesReceived()).append("\n");
        sb.append("Outstanding Advance (Rs),").append(statement.getOutstandingAdvanceBalance()).append("\n");
        sb.append("Total Payments Received (Rs),").append(statement.getTotalPaymentsReceived()).append("\n");
        sb.append("Net Outstanding Payable (Rs),").append(statement.getNetOutstandingPayable()).append("\n\n");

        sb.append("DAILY ATTENDANCE LOG\n");
        sb.append("Date,Site,Status,Overtime Hours,Wage Earned (Rs)\n");
        if (statement.getAttendanceDetails() != null) {
            for (LabourStatementDto.AttendanceItemReport r : statement.getAttendanceDetails()) {
                sb.append(r.getDate()).append(",\"")
                  .append(r.getSiteName()).append("\",")
                  .append(r.getStatus()).append(",")
                  .append(r.getOvertimeHours()).append(",")
                  .append(r.getWageEarned()).append("\n");
            }
        }
        return sb.toString();
    }
}
