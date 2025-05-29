package kz.kstu.kutsinas.batyrkhanov.practice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info=@Info(
                title = "Playlist Manager API",
                version = "1.0-SNAPSHOT",
                description = "API containing various instruments for managing playlists in Spotify" +
                        "API uses machine learning methods that optimize the selection of tracks" +
                        "Under your genre preferences and mood," +
                        "There are also functions that allow you to create playlists based on the top of your compositions"
        )
)
@Configuration
public class PlaylistManagerConfiguration {

}
