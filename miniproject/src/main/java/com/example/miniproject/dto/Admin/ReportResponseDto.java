package com.example.miniproject.dto.Admin;

public class ReportResponseDto {

    private String reportId;
    private String status;
    private String reason;
    private String message;

    public ReportResponseDto(String reportId, String status, String reason, String message) {
        this.reportId = reportId;
        this.status = status;
        this.reason = reason;
        this.message = message;
    }

    public String getReportId() {
        return reportId;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }
}