package in.gov.labourerp.repository;

import in.gov.labourerp.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByBusinessIdAndAttendanceDate(Long businessId, LocalDate attendanceDate);

    List<AttendanceRecord> findBySiteIdAndAttendanceDate(Long siteId, LocalDate attendanceDate);

    Optional<AttendanceRecord> findBySiteIdAndLabourerIdAndAttendanceDate(Long siteId, Long labourerId, LocalDate attendanceDate);

    List<AttendanceRecord> findByLabourerIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(Long labourerId, LocalDate startDate, LocalDate endDate);

    List<AttendanceRecord> findBySiteIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(Long siteId, LocalDate startDate, LocalDate endDate);

    List<AttendanceRecord> findByBusinessIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(Long businessId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.business.id = :businessId AND a.attendanceDate = :date AND a.status = :status")
    long countByBusinessAndDateAndStatus(@Param("businessId") Long businessId, @Param("date") LocalDate date, @Param("status") String status);

    @Query("SELECT COUNT(a) FROM AttendanceRecord a WHERE a.site.id = :siteId AND a.attendanceDate = :date AND a.status = :status")
    long countBySiteAndDateAndStatus(@Param("siteId") Long siteId, @Param("date") LocalDate date, @Param("status") String status);
}
