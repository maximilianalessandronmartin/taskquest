package org.novize.api.services;

import org.novize.api.dtos.timer.TimerUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TimerNotificationService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendTimerUpdate(String taskId, TimerUpdateDto timerUpdate) {
        messagingTemplate.convertAndSend(
                "/topic/task/" + taskId + "/timer",
                timerUpdate
        );
    }
}
