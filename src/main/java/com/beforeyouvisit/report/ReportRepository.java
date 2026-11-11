package com.beforeyouvisit.report;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findTop5ByOfficeIdOrderBySubmittedAtDesc(Long officeId);
}
