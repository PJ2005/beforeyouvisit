package com.beforeyouvisit.web;

import com.beforeyouvisit.office.Office;
import com.beforeyouvisit.office.OfficeRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SeedData {
    @Bean
    CommandLineRunner loadOffices(OfficeRepository offices) {
        return args -> {
            if (offices.count() == 0) {
                offices.saveAll(List.of(
                    new Office("Registrar", "Academic records, enrollment support and student certificates.",
                        "Mon–Fri, 9:30 AM–4:30 PM", "Main Building, Ground Floor",
                        "Arrive before 4:00 PM for same-day token service. Keep originals and photocopies together.",
                        List.of("Government photo ID", "Student ID or application number", "Completed request form", "One photocopy of supporting records"),
                        List.of("Collect a token at the reception desk.", "Present your documents for verification.", "Submit the form at the assigned counter.", "Keep the acknowledgement receipt until your request is complete.")),
                    new Office("Accounts", "Fee payments, refunds, scholarships and payment receipts.",
                        "Mon–Fri, 10:00 AM–3:30 PM", "Administration Block, First Floor",
                        "Payment counters close 30 minutes before office closing time. Bring transaction details for any payment query.",
                        List.of("Government photo ID", "Student ID", "Fee receipt or transaction reference", "Bank account details for refund requests"),
                        List.of("Check the notice board for the correct counter.", "Take a token and prepare your payment reference.", "Explain the request to the accounts representative.", "Verify all receipt details before leaving.")),
                    new Office("Library", "Membership, borrowing, no-dues certificates and research assistance.",
                        "Mon–Sat, 8:00 AM–7:00 PM", "Learning Centre, East Wing",
                        "Borrowing and no-dues services are available until 6:00 PM. Food and drinks are not permitted inside.",
                        List.of("Library card or student ID", "Government photo ID", "List of items to return (for no-dues)", "Department clearance form (if applicable)"),
                        List.of("Use the self-service kiosk for routine borrowing.", "Visit the help desk for account or no-dues requests.", "Return all borrowed items before requesting clearance.", "Keep the stamped no-dues form in a safe place."))
                ));
            }
        };
    }
}
