package com.example.miniproject.dto.Tour;

public class DashboardStatsDTO {

    private long activeTours;
    private long totalTours;
    private double totalRevenue;
    private long pendingBookings;

    public long getActiveTours() { return activeTours; }
    public void setActiveTours(long activeTours) { this.activeTours = activeTours; }

    public long getTotalTours() { return totalTours; }
    public void setTotalTours(long totalTours) { this.totalTours = totalTours; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(long pendingBookings) { this.pendingBookings = pendingBookings; }
}