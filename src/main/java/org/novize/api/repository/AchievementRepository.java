package org.novize.api.repository;

import org.novize.api.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    Optional<Achievement> findByName(String name);

    Achievement findByNameIgnoreCase(String name);

    void deleteById(Long id);

}
