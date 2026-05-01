package com.example.miniproject.dto.Member;



import java.util.List;

public class SearchResponse {

    private List<ActivitypostDTO> activities;
    private List<TourDTO> tours;
    private List<HomestayDTO> homestays;

    public SearchResponse() {}

    public List<ActivitypostDTO> getActivities() {
        return activities;
    }

    public void setActivities(List<ActivitypostDTO> activities) {
        this.activities = activities;
    }

    public List<TourDTO> getTours() {
        return tours;
    }

    public void setTours(List<TourDTO> tours) {
        this.tours = tours;
    }

    public List<HomestayDTO> getHomestays() {
        return homestays;
    }

    public void setHomestays(List<HomestayDTO> homestays) {
        this.homestays = homestays;
    }
}