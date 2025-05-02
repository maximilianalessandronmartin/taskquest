package org.novize.api.services;


import org.novize.api.dtos.AchievementDto;
import org.novize.api.model.Achievement;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AchievementService {


    Achievement getById(Long id);

    Achievement add(AchievementDto achievementDto);

    Achievement update(Long id, Achievement achievement);

    Achievement searchByName(String name);

    void deleteById(Long id);

    List<Achievement> getAll();
}
