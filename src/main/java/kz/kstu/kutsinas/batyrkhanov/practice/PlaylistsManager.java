package kz.kstu.kutsinas.batyrkhanov.practice;

import kz.kstu.kutsinas.batyrkhanov.practice.config.EnvPropertyLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlaylistsManager {
    public static void main(String[] args){
        EnvPropertyLoader.init();
        SpringApplication.run(PlaylistsManager.class,args);
    }
}
