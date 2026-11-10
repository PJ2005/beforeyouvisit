package com.beforeyouvisit.web;

import com.beforeyouvisit.office.Office;
import com.beforeyouvisit.office.OfficeService;
import com.beforeyouvisit.report.ReportService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OfficeController {
    private final OfficeService officeService;
    private final ReportService reportService;
    private final String appVersion;

    public OfficeController(OfficeService officeService, ReportService reportService,
                            @Value("${app.version:local}") String appVersion) {
        this.officeService = officeService;
        this.reportService = reportService;
        this.appVersion = appVersion;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("offices", officeService.findAll());
        model.addAttribute("appVersion", appVersion);
        return "index";
    }

    @GetMapping("/offices/{id}")
    public String office(@PathVariable Long id, Model model) {
        model.addAttribute("office", officeService.findById(id));
        model.addAttribute("reportForm", new ReportForm(id, "", ""));
        return "office-details";
    }

    @PostMapping("/reports")
    public String report(@jakarta.validation.Valid @ModelAttribute("reportForm") ReportForm form,
                         BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("reportError", "Please choose an issue and describe what needs updating.");
        } else {
            reportService.submit(form.officeId(), form.issueType(), form.details());
            redirectAttributes.addFlashAttribute("reportSuccess", "Thanks — your report has been saved for review.");
        }
        return "redirect:/offices/" + form.officeId();
    }

    public record ReportForm(@NotNull Long officeId, @NotBlank String issueType, @NotBlank String details) { }
}
