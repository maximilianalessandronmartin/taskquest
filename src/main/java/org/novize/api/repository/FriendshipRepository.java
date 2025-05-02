package org.novize.api.repository;

import org.novize.api.enums.FriendshipStatus;
import org.novize.api.model.Friendship;
import org.novize.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, String> {
    List<Friendship> findByFriendAndStatus(User friend, FriendshipStatus status);

    List<Friendship> findByUserAndStatus(User user, FriendshipStatus status);

    // Custom query methods can be defined here if needed
    // For example, to find friendships by user ID or status
    // List<Friendship> findByUserUsernameAndStatus(String username, FriendshipStatus status);
    
}
