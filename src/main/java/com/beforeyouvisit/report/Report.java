package com.beforeyouvisit.report;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Entity
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long officeId;
    private String issueType;
    @Column(length = 2000)
    private String details;
    private LocalDateTime submittedAt;

    protected Report() { }

    public Report(Long officeId, String issueType, String details) {
        this.officeId = officeId;
        this.issueType = issueType;
        this.details = details;
        this.submittedAt = LocalDateTime.now();
    }
}
