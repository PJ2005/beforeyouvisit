package com.beforeyouvisit.report;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report submit(Long officeId, String issueType, String details) {
        return reportRepository.save(new Report(officeId, issueType, details.trim()));
    }

    public List<Report> findRecentForOffice(Long officeId) {
        return reportRepository.findTop5ByOfficeIdOrderBySubmittedAtDesc(officeId);
    }
}
