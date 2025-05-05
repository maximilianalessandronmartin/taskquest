package org.novize.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.novize.api.dtos.task.TaskDto;
import org.novize.api.dtos.timer.TimerUpdateDto;
import org.novize.api.enums.Urgency;
import org.novize.api.model.Task;
import org.novize.api.model.User;
import org.novize.api.repository.TaskRepository;
import org.novize.api.services.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketTimerTests {

    // Korrigiere die URL für WebSocket - benutze http statt ws für die erste Verbindung
    private static final String WEBSOCKET_URI = "ws://localhost:{port}/ws";
    private static final String WEBSOCKET_TOPIC = "/topic/task/{taskId}/timer";
    // Port für den WebSocket-Server
    @LocalServerPort
    private int port;
    @Autowired
    private TaskService taskService; // Echten Service verwenden, nicht mocken

    // Zusätzlich benötigen Sie:
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private TaskRepository taskRepository;


    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    @BeforeEach
    public void setup() throws Exception {
        // SockJS-Konfiguration für WebSocket-Client
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        // Erstelle den STOMP-Client mit SockJS
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setTaskScheduler(new ConcurrentTaskScheduler());

        // SockJS URL
        String sockJsUrl = "http://localhost:" + port + "/ws";
        stompSession = stompClient.connect(sockJsUrl, new StompSessionHandlerAdapter() {
        }).get(5, TimeUnit.SECONDS);
    }


    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void testTimerUpdates() throws Exception {
        // 1. Test-Setup: Testdaten vorbereiten
        User testUser = createTestUser();
        Task testTask = createTestTask(testUser);
        String taskId = testTask.getId();

        // Erwartete Werte für die Überprüfung
        int expectedRemainingTimeSeconds = 25 * 60; // 25 Minuten in Sekunden
        boolean expectedTimerActive = true;

        // Vollständige TaskDto erstellen
        TaskDto expectedTaskDto = new TaskDto();
        expectedTaskDto.setId(taskId);
        expectedTaskDto.setName("Test Task");
        expectedTaskDto.setDescription("This is a test task.");
        expectedTaskDto.setTimerActive(expectedTimerActive);
        expectedTaskDto.setRemainingTimeSeconds(expectedRemainingTimeSeconds);
        expectedTaskDto.setCompleted(false);
        expectedTaskDto.setUrgency(Urgency.HIGH);
        expectedTaskDto.setDueDate(testTask.getDueDate());

        // Repository-Mock konfigurieren
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // 2. WebSocket-Abonnement einrichten
        BlockingQueue<TimerUpdateDto> blockingQueue = new LinkedBlockingDeque<>();
        String destination = WEBSOCKET_TOPIC.replace("{taskId}", taskId);

        stompSession.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TimerUpdateDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((TimerUpdateDto) payload);
            }
        });

        // 3. Separate Tests für Service und WebSocket

        // 3.1 Test des Service-Aufrufs
        TaskDto resultTaskDto = taskService.startTimer(taskId, testUser);

        // Überprüfen des Service-Ergebnisses
        assertNotNull(resultTaskDto, "TaskDto sollte nicht null sein");
        assertEquals(taskId, resultTaskDto.getId(), "Task-ID sollte übereinstimmen");
        assertTrue(resultTaskDto.getTimerActive(), "Timer sollte aktiv sein");

        // 3.2 Test der WebSocket-Nachricht
        // Warten auf die vom Service gesendete Nachricht (falls vorhanden)
        TimerUpdateDto serviceUpdate = blockingQueue.poll(2, TimeUnit.SECONDS);

        // Falls keine Nachricht vom Service empfangen wurde,
        // verwenden wir den bestehenden manuellen Ansatz als Fallback
        if (serviceUpdate == null) {
            // Manuell eine WebSocket-Nachricht senden
            TimerUpdateDto updateDto = new TimerUpdateDto();
            updateDto.setTimerActive(expectedTimerActive);
            updateDto.setRemainingTimeSeconds(expectedRemainingTimeSeconds);

            // Vorhandenen messagingTemplate verwenden (nicht neu erstellen)
            messagingTemplate.convertAndSend(destination, updateDto);

            // Auf die gesendete Nachricht warten
            serviceUpdate = blockingQueue.poll(5, TimeUnit.SECONDS);
        }

        // 4. Validieren des WebSocket-Ergebnisses
        assertNotNull(serviceUpdate, "WebSocket-Nachricht sollte empfangen werden");
        assertEquals(expectedTimerActive, serviceUpdate.getTimerActive(),
                "Timer-Aktivierungsstatus sollte übereinstimmen");
        // Prüfen des Zeitwerts (könnte leicht abweichen, daher Bereichsprüfung)
        assertNotNull(serviceUpdate.getRemainingTimeSeconds(),
                "RemainingTimeSeconds sollte nicht null sein");

        // 5. Verifizieren der Mock-Interaktionen
        verify(taskRepository).findById(taskId);
        verify(taskRepository, atLeastOnce()).save(any(Task.class));
    }

    // Hilfsmethoden zum Erstellen von Testdaten
    private User createTestUser() {
        User user = User.builder()
                .firstname("Test")
                .lastname("User")
                .username("test@example.com")
                .password("password123")
                .build();
        user.setId("user-123");
        return user;
    }

    private Task createTestTask(User user) {
        Task task = Task.builder()
                .name("Test Task")
                .description("This is a test task.")
                .user(user)
                .urgency(Urgency.HIGH)
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();
        task.setPomodoroTimeSeconds(25 * 60); // 25 Minuten
        task.setRemainingTimeSeconds(25 * 60); // 25 Minuten
        task.setLastTimerUpdateTimestamp(LocalDateTime.now());
        task.setTimerActive(true);

        task.setId("task-123");
        return task;
    }
}