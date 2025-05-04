package org.novize.api.bootstrap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.dtos.AchievementDto;
import org.novize.api.model.Achievement;
import org.novize.api.repository.AchievementRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.*;

import java.util.List;

/**
 * This class is responsible for seeding the database with achievements.
 * It reads the achievements from a json file and saves them to the database.
 * Only achievements that are not already in the database will be saved.
 */

@Component
public class AchievementSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final Logger logger = LogManager.getLogger(AchievementSeeder.class);
    private final AchievementRepository achievementRepository;

    public AchievementSeeder(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadAchievements();
    }

    private void loadAchievements() {
        try {
            // Ressource aus dem Klassenpfad laden, nicht von der Festplatte
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("achievements.json");

            if (inputStream == null) {
                logger.error("Konnte achievements.json nicht finden");
                throw new RuntimeException("achievements.json nicht gefunden");
            }

            // Reader aus dem InputStream erstellen
            Reader reader = new InputStreamReader(inputStream);

            // JSON in Objekte konvertieren
            List<Achievement> achievements = new Gson().fromJson(reader, new TypeToken<List<Achievement>>() {
            }.getType());

            for (Achievement achievement : achievements) {
                // Überprüfung ob Achievement existiert
                if (achievementRepository.findByNameIgnoreCase(achievement.getName()) == null) {
                    achievementRepository.save(achievement);
                    logger.info("Achievement {} zur Datenbank hinzugefügt", achievement.getName());
                } else {
                    logger.info("Achievement {} existiert bereits in der Datenbank", achievement.getName());
                }
            }

            // Stream schließen
            inputStream.close();
        } catch (IOException e) {
            logger.error("Fehler beim Laden der Achievements: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
