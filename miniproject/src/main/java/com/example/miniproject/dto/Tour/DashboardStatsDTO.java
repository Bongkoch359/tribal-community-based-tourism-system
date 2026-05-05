package com.example.miniproject.dto.Tour;

public class DashboardStatsDTO {
    
    private long totalBookings;
    private long activeTours;
    private long totalTours;
    private long totalPosts;
    private double totalRevenue;
 
    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }
 
    public long getActiveTours() { return activeTours; }
    public void setActiveTours(long activeTours) { this.activeTours = activeTours; }
 
    public long getTotalTours() { return totalTours; }
    public void setTotalTours(long totalTours) { this.totalTours = totalTours; }
 
    public long getTotalPosts() { return totalPosts; }
    public void setTotalPosts(long totalPosts) { this.totalPosts = totalPosts; }
 
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
}
    

