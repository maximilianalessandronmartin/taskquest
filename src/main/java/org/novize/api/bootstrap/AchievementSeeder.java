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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

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
        // Read the json file and convert to a List of achievements
        try {
            Reader reader = new FileReader("src/main/resources/achievements.json");

            // Convert the json file to a list of achievements
            List<Achievement> achievements = new Gson().fromJson(reader, new TypeToken<List<Achievement>>() {
            }.getType());

            for (Achievement achievement : achievements) {
                // Check if the achievement already exists in the database
                if (achievementRepository.findByNameIgnoreCase(achievement.getName()) == null) {
                    // Save the achievement to the database
                    achievementRepository.save(achievement);
                    logger.info("Achievement " + achievement.getName() + " saved to the database");
                } else {
                    logger.info("Achievement " + achievement.getName() + " already exists in the database");
                }
            }



        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}
