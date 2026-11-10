package com.beforeyouvisit.office;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Office {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 1200)
    private String description;
    private String timings;
    private String location;
    @Column(length = 1200)
    private String instructions;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "office_documents", joinColumns = @JoinColumn(name = "office_id"))
    @Column(name = "document_name")
    @OrderColumn(name = "document_order")
    private List<String> requiredDocuments = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "office_steps", joinColumns = @JoinColumn(name = "office_id"))
    @Column(name = "step_text")
    @OrderColumn(name = "step_order")
    private List<String> visitSteps = new ArrayList<>();

    protected Office() { }

    public Office(String name, String description, String timings, String location, String instructions,
                  List<String> requiredDocuments, List<String> visitSteps) {
        this.name = name;
        this.description = description;
        this.timings = timings;
        this.location = location;
        this.instructions = instructions;
        this.requiredDocuments = requiredDocuments;
        this.visitSteps = visitSteps;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getTimings() { return timings; }
    public String getLocation() { return location; }
    public String getInstructions() { return instructions; }
    public List<String> getRequiredDocuments() { return requiredDocuments; }
    public List<String> getVisitSteps() { return visitSteps; }
}
