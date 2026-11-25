package in.gov.labourerp.repository;

import in.gov.labourerp.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE (cast(:businessId as Long) IS NULL OR a.businessId = :businessId) " +
           "AND (cast(:entityType as String) IS NULL OR a.entityType = :entityType) " +
           "AND (cast(:actionType as String) IS NULL OR a.actionType = :actionType) " +
           "AND (cast(:startDate as LocalDateTime) IS NULL OR a.createdAt >= :startDate) " +
           "AND (cast(:endDate as LocalDateTime) IS NULL OR a.createdAt <= :endDate) " +
           "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByFilters(@Param("businessId") Long businessId,
                                @Param("entityType") String entityType,
                                @Param("actionType") String actionType,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable);
}
