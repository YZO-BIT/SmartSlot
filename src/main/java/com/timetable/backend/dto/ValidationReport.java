package com.timetable.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidationReport {
    private boolean isFeasible;
    private List<String> errorMessages;

    public ValidationReport() {
        this.isFeasible = true;
        this.errorMessages = new ArrayList<>();
    }

    public ValidationReport(boolean isFeasible, List<String> errorMessages) {
        this.isFeasible = isFeasible;
        this.errorMessages = errorMessages;
    }

    public boolean isFeasible() {
        return isFeasible;
    }

    public void setFeasible(boolean feasible) {
        isFeasible = feasible;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public void addError(String errorMessage) {
        this.errorMessages.add(errorMessage);
        this.isFeasible = false;
    }

    @Override
    public String toString() {
        return "ValidationReport{" +
                "isFeasible=" + isFeasible +
                ", errorMessages=" + errorMessages +
                '}';
    }
}
