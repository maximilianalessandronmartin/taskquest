package org.novize.api.services;

import org.novize.api.model.Friendship;
import org.novize.api.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FriendshipService {
    /**
     * Retrieves all friendships for a given user with status ACCEPTED.
     *
     * @param user the user for whom to fetch friendships
     * @return a list of accepted {@link Friendship} objects
     */
    List<Friendship> getFriendshipsByUser(User user);

    /**
     * Retrieves the list of pending friend requests for a given user.
     *
     * @param user the user for whom to fetch pending friend requests
     * @return a list of {@link Friendship} objects representing the pending friend requests
     */
    List<Friendship> getPendingFriendRequests(User user);

    /**
     * Sends a friend request from one user to another.
     *
     * @param sender the user sending the friend request
     * @param receiver the user receiving the friend request
     */
    void sendFriendRequest(User sender, User receiver);

    /**
     * Accepts a pending friend request.
     *
     * @param friendship the friendship instance to be accepted
     */
    void acceptFriendRequest(Friendship friendship);

    /**
     * Declines a friend request by deleting the associated {@link Friendship} entity.
     *
     * @param friendship the {@link Friendship} instance to be declined
     */
    void declineFriendRequest(Friendship friendship);

    /**
     * Retrieves a {@link Friendship} object based on the provided friendship ID.
     *
     * @param friendshipId the unique identifier of the friendship to retrieve
     * @return the {@link Friendship} object corresponding to the given ID
     */
    Friendship getFriendshipById(String friendshipId);

    /**
     * Checks if the given user is a friend of the current user.
     *
     * @param currentUser the user initiating the friend status check
     * @param friend the user whose friendship status is being checked
     * @return true if the users are friends, false otherwise
     */
    boolean areNotFriends(User currentUser, User friend);


}