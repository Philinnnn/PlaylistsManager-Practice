package kz.kstu.kutsinas.batyrkhanov.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class YouTubeVideo {
    private String videoId;
    private String title;
    private String description;
    private String thumbnailUrl;


}
