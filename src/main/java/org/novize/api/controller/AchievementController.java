package org.novize.api.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.dtos.AchievementDto;
import org.novize.api.model.Achievement;
import org.novize.api.services.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling achievement-related requests.
 * This controller provides endpoints for managing achievements.
 */

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {
    private static final Logger logger = LogManager.getLogger(AchievementController.class);
    @Autowired
    AchievementService achievementService;

    /**
     * Endpoint to get an achievement by its ID.
     * @param id The ID of the achievement to retrieve.
     * @return The achievement with the specified ID.
     */
    @GetMapping("{id}")
    public Achievement getById(@PathVariable Long id) {
        return achievementService.getById(id);
    }

    /**
     * Endpoint to get all achievements.
     * @return A list of all achievements of a user.
     */
    @GetMapping()
    public List<Achievement> getAllFromUser() {
        logger.info("Received request to get all achievements");
        return achievementService.getAll();
    }

    /**
     * Endpoint to add a new achievement.
     * @param achievementDto The DTO containing the details of the achievement to add.
     * @return The added achievement.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("add")
    public Achievement add(@RequestBody AchievementDto achievementDto){
        return achievementService.add(achievementDto);
    }

    /**
     * Endpoint to update an existing achievement.
     * @param id The ID of the achievement to update.
     * @param achievementDto The DTO containing the updated details of the achievement.
     * @return The updated achievement.
     */
    @PostMapping("update/{id}")
    public void update(@PathVariable Long id, @RequestBody AchievementDto achievementDto){
        logger.info("Received request to update achievement with ID: {}", id);
        // Map the AchievementDto to the Achievement entity
        // and update the achievement in the database
        Achievement achievment = Achievement.builder()
                .name(achievementDto.getName())
                .description(achievementDto.getDescription())
                .xpRequired(achievementDto.getXpRequired())
                .build();
        achievementService.update(id, achievment);
    }

    /**
     * Endpoint to delete an achievement by its ID.
     * @param id The ID of the achievement to delete.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("delete")
    public void deleteById(@RequestParam Long id){
        achievementService.deleteById(id);
    }
    /**
     * Endpoint to search for an achievement by its name.
     * @param name The name of the achievement to search for.
     * @return The achievement with the specified name.
     */
    @GetMapping("search")
    public Achievement search(@RequestParam String name) {
        return achievementService.searchByName(name);
    }
}