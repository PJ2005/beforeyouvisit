package com.beforeyouvisit.office;

public class OfficeNotFoundException extends RuntimeException {
    public OfficeNotFoundException(Long id) {
        super("Office " + id + " was not found");
    }
}
