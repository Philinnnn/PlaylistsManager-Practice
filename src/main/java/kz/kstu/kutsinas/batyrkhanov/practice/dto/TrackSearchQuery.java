package kz.kstu.kutsinas.batyrkhanov.practice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackSearchQuery {
    private String trackName;
    private String artistName;
}