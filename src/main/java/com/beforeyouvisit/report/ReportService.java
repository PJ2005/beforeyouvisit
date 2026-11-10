package com.beforeyouvisit.report;

import org.springframework.stereotype.Service;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public void submit(Long officeId, String issueType, String details) {
        reportRepository.save(new Report(officeId, issueType, details.trim()));
    }
}
