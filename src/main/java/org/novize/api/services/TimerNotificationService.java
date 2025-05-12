package org.novize.api.services;

import org.novize.api.dtos.timer.TimerUpdateDto;
import org.novize.api.enums.NotificationType;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TimerNotificationService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    NotificationService notificationService;

    public void sendTimerUpdate(String taskId, TimerUpdateDto timerUpdate) {
        messagingTemplate.convertAndSend(
                "/topic/task/" + taskId + "/timer",
                timerUpdate
        );
    }

    // Neue Methode zum Senden einer Timer-Abschluss-Benachrichtigung
    public void sendTimerCompletedNotification(Task task) {
        // 1. Benachrichtigung an den Aufgabenersteller
        notificationService.sendNotification(
                task.getUser(),
                NotificationType.TASK_COMPLETED,
                "Der Pomodoro-Timer für die Aufgabe \"" + task.getName() + "\" ist abgelaufen!",
                "{\"taskId\": \"" + task.getId() + "\"}"
        );

        // 2. Benachrichtigungen an alle Benutzer, mit denen die Aufgabe geteilt wurde
        if (!task.getSharedWith().isEmpty()) {
            for (User sharedUser : task.getSharedWith()) {
                notificationService.sendNotification(
                        sharedUser,
                        NotificationType.TASK_COMPLETED,
                        "Der Pomodoro-Timer für die Aufgabe \"" + task.getName() + "\", die mit Ihnen geteilt wurde, ist abgelaufen!",
                        "{\"taskId\": \"" + task.getId() + "\"}"
                );
            }
        }
    }

}
