package kz.kstu.kutsinas.batyrkhanov.practice.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlaylistRequest {
    private String name;
    private String description;
    private Boolean isPublic;
    private List<TrackSearchQuery> trackRequests;
}