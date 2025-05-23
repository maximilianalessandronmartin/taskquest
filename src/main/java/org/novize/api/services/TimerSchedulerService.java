package org.novize.api.services;

import org.novize.api.dtos.timer.TimerUpdateDto;
import org.novize.api.model.Task;
import org.novize.api.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

@Service
@EnableScheduling
public class TimerSchedulerService {

    private static final Logger logger = Logger.getLogger(TimerSchedulerService.class.getName());
    // Toleranzbereich für Timer-Ablauf (z.B. 100 ms)
    private static final long TIMER_COMPLETION_TOLERANCE = 1000L;
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TimerNotificationService timerNotificationService;



    /**
     * Aktualisiert alle aktiven Timer in regelmäßigen Abständen (1 Sekunde)
     * und sendet die aktualisierten Werte an die Clients über WebSocket.
     */
    @Scheduled(fixedRate = 1000)
    @Transactional
    public void updateActiveTimers() {
        logger.info("Timer scheduler running at " + LocalDateTime.now());

        try {

            List<Task> activeTasks = taskRepository.findByTimerActiveTrue();
            if (!activeTasks.isEmpty()) {
                logger.info("Updating " + activeTasks.size() + " active timers");
            }

            LocalDateTime now = LocalDateTime.now();

            for (Task task : activeTasks) {
                updateTaskTimer(task, now);
            }

        } catch (Exception e) {
            logger.severe("Fehler bei der Timer-Aktualisierung: " + e.getMessage());
        }
    }

    /**
     * Aktualisiert den Timer eines einzelnen Tasks und sendet das Update an die Clients.
     */
    private void updateTaskTimer(Task task, LocalDateTime now) {
        // Logging für Debugging
        logger.info("Aktualisiere Timer für Task: " + task.getId() + " - " + task.getName() +
                " - verbleibende Zeit: " + task.getRemainingTimeMillis() + " ms");

        // Vorherigen Wert speichern, um zu überprüfen, ob der Timer gerade abgelaufen ist
        long previousRemainingMillis = task.getRemainingTimeMillis();

        // Verbleibende Zeit aktualisieren
        if (task.getLastTimerUpdateTimestamp() != null && Boolean.TRUE.equals(task.getTimerActive())) {
            long millisElapsed = ChronoUnit.MILLIS.between(
                    task.getLastTimerUpdateTimestamp(), now);

            // Nur aktualisieren, wenn Zeit vergangen ist
            if (millisElapsed > 0) {
                long newRemainingTime = Math.max(0L, task.getRemainingTimeMillis() - millisElapsed);

                // Wichtig: Überprüfen, ob der Timer gerade abgelaufen ist (mit Toleranzbereich)
                boolean justCompleted = (previousRemainingMillis > 0 &&
                        newRemainingTime <= TIMER_COMPLETION_TOLERANCE);
                // Timer-Status aktualisieren
                task.setRemainingTimeMillis(newRemainingTime);

                // Timer automatisch stoppen, wenn Zeit abgelaufen ist
                if (newRemainingTime <= TIMER_COMPLETION_TOLERANCE) {
                    // Als abgelaufen behandeln
                    task.setRemainingTimeMillis(0L);
                    task.setTimerActive(false);
                    logger.info("Timer-Prüfung: Task ID=" + task.getId() +
                            ", previousTime=" + previousRemainingMillis +
                            ", newTime=" + newRemainingTime +
                            ", justCompleted=" + justCompleted);


                    // Wenn der Timer gerade jetzt abgelaufen ist, Benachrichtigungen senden
                    if (justCompleted) {
                        logger.info("Timer abgelaufen für Task: " + task.getId() + " - " + task.getName());

                        // Benachrichtigung an Eigentümer und geteilte Benutzer senden
                        timerNotificationService.sendTimerCompletedNotification(task);
                    }
                }

                // Zeitstempel aktualisieren
                task.setLastTimerUpdateTimestamp(now);
                Task savedTask = taskRepository.save(task);

                // WebSocket-Update senden
               sendTimerUpdate(savedTask);

                // Zusätzliches Logging
                logger.info("Timer aktualisiert: " + task.getId() + " - verbleibende Zeit jetzt: " +
                        task.getRemainingTimeMillis() + " ms, aktiv: " + task.getTimerActive());
            }
        } else if (task.getLastTimerUpdateTimestamp() == null) {
            // Falls der Zeitstempel null ist, setzen wir ihn auf jetzt
            task.setLastTimerUpdateTimestamp(now);
            taskRepository.save(task);
        }
    }

    /**
     * Sendet ein Timer-Update über WebSocket an die Clients.
     */
    private void sendTimerUpdate(Task task) {
        TimerUpdateDto updateDto = new TimerUpdateDto();
        updateDto.setRemainingTimeMillis(task.getRemainingTimeMillis());
        updateDto.setTimerActive(task.getTimerActive());
        timerNotificationService.sendTimerUpdate(task.getId(), updateDto);
    }
}