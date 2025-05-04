package org.novize.api.repository;

import org.novize.api.enums.FriendshipStatus;
import org.novize.api.model.Friendship;
import org.novize.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, String> {
    List<Friendship> findByFriendAndStatus(User friend, FriendshipStatus status);

    List<Friendship> findByUserAndStatus(User user, FriendshipStatus status);

    boolean existsByUserAndFriendAndStatus(User currentUser, User friend, FriendshipStatus friendshipStatus);

    @Query("SELECT f FROM Friendship f WHERE " +
            "((f.user = :user AND f.friend = :friend) OR " +
            "(f.user = :friend AND f.friend = :user)) AND " +
            "f.status = :status")
    Optional<Friendship> findAcceptedFriendship(
            @Param("user") User user,
            @Param("friend") User friend,
            @Param("status") FriendshipStatus status);

    Optional<Friendship> findByUserAndFriend(User sender, User receiver);
}
