package com.example.miniproject.dto.Admin;

import java.time.LocalDateTime;

// ใช้กับ /api/admin/reports/by-manager/{id} และ /api/admin/reports/by-owner/{id}
// จงใจ "แบน" ข้อมูลออกมาเป็น field ตรง ๆ แทนที่จะส่ง Report entity ทั้งก้อน
// (ซึ่งมี tour/homestay -> communitymanager/owner -> tours/homestays[] ที่ผูกกลับไปกลับมา
//  ไม่มี @JsonIgnore กัน ทำให้ Jackson serialize วนลูปไม่รู้จบ)
public class ReportListItemDto {

    private String reportId;
    private String reason;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private String evidenceImage;

    // ข้อมูลรายการที่ถูกรายงาน (มีได้แค่อย่างใดอย่างหนึ่ง: ทัวร์ หรือ โฮมสเตย์)
    private String targetType; // "TOUR" หรือ "HOMESTAY"
    private String targetId;
    private String targetName;

    public ReportListItemDto(String reportId, String reason, String description, String status,
            LocalDateTime createdAt, String evidenceImage,
            String targetType, String targetId, String targetName) {
        this.reportId = reportId;
        this.reason = reason;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.evidenceImage = evidenceImage;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
    }

    public String getReportId() {
        return reportId;
    }

    public String getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getEvidenceImage() {
        return evidenceImage;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }
}