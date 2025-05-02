package org.novize.api.controller;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.dtos.*;
import org.novize.api.model.User;
import org.novize.api.services.AchievementService;
import org.novize.api.services.TaskService;
import org.novize.api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling synchronization requests.
 * This controller provides endpoints for fetching and syncing data.
 */

@RestController
@RequestMapping("/api/sync")
public class SyncController {
    private static final Logger logger = LogManager.getLogger(SyncController.class);

    /*@Autowired
    private TaskService taskService;


    @Autowired
    private UserService userService;

    @Autowired
    private AchievementService achievementService;

    @GetMapping
    public SyncRequestDto getData(@AuthenticationPrincipal User user) {
        logger.info("Fetching data for user: {}", user.getUsername());
        // Fetch data from the server
        SyncRequestDto syncData = taskService.getData(user);
        //Log fetched data
        logger.info("Fetched sync data: {}", syncData);
        return syncData;
    }


    // Endpoint to sync data to the server
    @PostMapping
    public SyncRequestDto syncData(@AuthenticationPrincipal User user, @RequestBody SyncRequestDto syncRequestDto) {
        logger.info("Syncing data for user: {}", user.getUsername());
        // Process the sync request
        //Log received data
        logger.info("Received sync request: {}", syncRequestDto.getTasks());
        //Update userXp
        return taskService.syncData(user, syncRequestDto);
    }*/






}