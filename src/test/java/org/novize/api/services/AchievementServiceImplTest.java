package org.novize.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.novize.api.dtos.AchievementDto;
import org.novize.api.model.Achievement;
import org.novize.api.repository.AchievementRepository;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AchievementServiceImplTest {

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getById() {
        // Arrange
        Long id = 1L;
        Achievement expectedAchievement = new Achievement();
        when(achievementRepository.findById(id)).thenReturn(java.util.Optional.of(expectedAchievement));

        // Act
        Achievement result = achievementService.getById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAchievement, result);
        verify(achievementRepository).findById(id);
    }

    @Test
    void getAll() {
        // Arrange
        Achievement achievement1 = new Achievement();
        Achievement achievement2 = new Achievement();
        when(achievementRepository.findAll()).thenReturn(java.util.List.of(achievement1, achievement2));

        // Act
        java.util.List<Achievement> result = achievementService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(achievementRepository).findAll();
    }

    @Test
    void add() {
        // Arrange
        AchievementDto achievementDto = AchievementDto.builder()
                .name("Test Achievement")
                .description("Test Description")
                .xpRequired(100)
                .build();

        Achievement expectedAchievement = new Achievement(
                achievementDto.getName(),
                achievementDto.getDescription(),
                achievementDto.getXpRequired()
        );

        when(achievementRepository.findByNameIgnoreCase(achievementDto.getName())).thenReturn(null);
        when(achievementRepository.save(any(Achievement.class))).thenReturn(expectedAchievement);

        // Act
        Achievement result = achievementService.add(achievementDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAchievement, result);
        verify(achievementRepository).findByNameIgnoreCase(achievementDto.getName());
        verify(achievementRepository).save(any(Achievement.class));
    }


    @Test
    void update() {
        // Arrange
        Long id = 1L;
        Achievement existingAchievement = new Achievement();
        existingAchievement.setId(id);
        existingAchievement.setName("Old Name");
        existingAchievement.setDescription("Old Description");
        existingAchievement.setXpRequired(50);

        Achievement updatedAchievement = new Achievement();
        updatedAchievement.setName("New Name");
        updatedAchievement.setDescription("New Description");
        updatedAchievement.setXpRequired(100);

        when(achievementRepository.findById(id)).thenReturn(java.util.Optional.of(existingAchievement));
        when(achievementRepository.save(existingAchievement)).thenReturn(existingAchievement);

        // Act
        Achievement result = achievementService.update(id, updatedAchievement);

        // Assert
        assertNotNull(result);
        assertEquals(updatedAchievement.getName(), result.getName());
        assertEquals(updatedAchievement.getDescription(), result.getDescription());
        assertEquals(updatedAchievement.getXpRequired(), result.getXpRequired());
    }

    @Test
    void searchByName() {
        // Arrange
        String name = "Test Achievement";
        Achievement expectedAchievement = new Achievement();
        when(achievementRepository.findByNameIgnoreCase(name)).thenReturn(expectedAchievement);

        // Act
        Achievement result = achievementService.searchByName(name);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAchievement, result);
        verify(achievementRepository).findByNameIgnoreCase(name);
    }

    @Test
    void deleteById() {
        // Arrange
        Long id = 1L;
        Achievement achievement = new Achievement();
        when(achievementRepository.findById(id)).thenReturn(java.util.Optional.of(achievement));

        // Act
        achievementService.deleteById(id);

        // Assert
        verify(achievementRepository).findById(id);
        verify(achievementRepository).deleteById(id);
    }
}