package com.beforeyouvisit.office;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OfficeService {
    private final OfficeRepository officeRepository;

    public OfficeService(OfficeRepository officeRepository) {
        this.officeRepository = officeRepository;
    }

    public List<Office> findAll() {
        return officeRepository.findAll();
    }

    public Office findById(Long id) {
        return officeRepository.findAll().stream()
                .filter(office -> id.equals(office.getId()))
                .findFirst()
                .orElseThrow(() -> new OfficeNotFoundException(id));
    }
}
