package kz.kstu.kutsinas.batyrkhanov.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackSearchQuery {
    private String trackName;
    private String artistName;
}
