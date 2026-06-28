package com.example.miniproject.dto.Tour;

public class MonthlyRevenueDTO {

    private int month;      // 1-12
    private String label;   // "ม.ค.", "ก.พ.", ...
    private double revenue;

    public MonthlyRevenueDTO(int month, double revenue) {
        this.month = month;
        this.revenue = revenue;
        this.label = MONTH_LABELS[month - 1];
    }

    private static final String[] MONTH_LABELS = {
        "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
        "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
    };

    public int getMonth()       { return month; }
    public String getLabel()    { return label; }
    public double getRevenue()  { return revenue; }
}