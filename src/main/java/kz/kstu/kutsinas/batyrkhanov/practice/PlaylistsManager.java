package kz.kstu.kutsinas.batyrkhanov.practice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import kz.kstu.kutsinas.batyrkhanov.practice.config.EnvPropertyLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
@SpringBootApplication
public class PlaylistsManager {
    public static void main(String[] args){
        EnvPropertyLoader.init();
        SpringApplication.run(PlaylistsManager.class,args);
    }
}
