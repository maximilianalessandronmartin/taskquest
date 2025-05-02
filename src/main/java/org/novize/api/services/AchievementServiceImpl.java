package org.novize.api.services;

import org.novize.api.dtos.AchievementDto;
import org.novize.api.model.Achievement;
import org.novize.api.repository.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AchievementServiceImpl implements AchievementService {
    @Autowired
    AchievementRepository repository;

    /**
     * @param id of the achievement to get
     * @return achievement
     */
    @Override
    public Achievement getById(Long id) {
        Optional<Achievement> optional = repository.findById(id);
        return optional.orElse(null);
    }

    /**
     * @return all achievements
     */

    @Override
    public List<Achievement> getAll() {
       return repository.findAll();
    }

    /**
     *
     * @param achievementDto new achievement data
     * @return created achievement
     */
    @Override
    public Achievement add(AchievementDto achievementDto) {
        if (repository.findByNameIgnoreCase(achievementDto.getName()) == null) {
            var achievement = new Achievement(achievementDto.getName(), achievementDto.getDescription(), achievementDto.getXpRequired());
            return repository.save(achievement);
        }
        throw new RuntimeException();
    }

    /**
     *
     * @param id of the achievement to update
     * @param updatedAchievement new achievement data
     * @return updated achievement
     */
    @Override
    public Achievement update(Long id, Achievement updatedAchievement) {
        Optional<Achievement> existingAchievementOptional = repository.findById(id);
        if (existingAchievementOptional.isPresent()) {
            Achievement existingAchievement = existingAchievementOptional.get();
            existingAchievement.setName(updatedAchievement.getName());
            existingAchievement.setDescription(updatedAchievement.getDescription());
            existingAchievement.setXpRequired(updatedAchievement.getXpRequired());
            return repository.save(existingAchievement);
        } else {
            throw new RuntimeException("Achievement not found with id: " + id);
        }
    }

    @Override
    public Achievement searchByName(String name) {
        return repository.findByNameIgnoreCase(name);
    }

    /**
     * @param id of the achievement to delete
     */
    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }


}
