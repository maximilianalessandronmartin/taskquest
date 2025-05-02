package org.novize.api.services;

import org.novize.api.enums.FriendshipStatus;
import org.novize.api.model.Friendship;
import org.novize.api.model.User;
import org.novize.api.repository.FriendshipRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the FriendshipService interface to manage friendships between users.
 * This service provides methods to handle friend requests, accept or decline them,
 * and retrieve friendship-related data.
 */
@Service
public class FriendshipServiceImpl implements FriendshipService {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendshipRepository friendshipRepository;


    // Method to get all friends of a user
    public List<Friendship> getFriendshipsByUser(User user) {
        return friendshipRepository.findByUserAndStatus(user, FriendshipStatus.ACCEPTED);
    }

    /**
     * Retrieves the list of pending friend requests for a given user.
     *
     * @param user the user for whom to fetch pending friend requests
     * @return a list of {@link Friendship} objects representing the pending friend requests
     */
    public List<Friendship> getPendingFriendRequests(User user) {
        return friendshipRepository.findByFriendAndStatus(user, FriendshipStatus.PENDING);
    }


    /**
     * Sends a friend request from one user to another.
     * This method creates a new {@link Friendship} instance with a status of PENDING
     * and persists it to the database.
     *
     * @param sender the user sending the friend request
     * @param receiver the user receiving the friend request
     */
    public void sendFriendRequest(User sender, User receiver) {
        // Check if the sender and receiver are the same
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself.");
        }
        Friendship friendship = new Friendship();
        friendship.setUser(sender);
        friendship.setFriend(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);
    }


    /**
     * Accepts a pending friend request by updating its status to ACCEPTED
     * and saving the updated friendship instance in the repository.
     *
     * @param friendship the friendship instance representing the pending friend request
     *                    to be accepted
     */
    public void acceptFriendRequest(Friendship friendship) {
            // Check if the friendship is already accepted or is null
            if (friendship == null || friendship.getStatus() != FriendshipStatus.PENDING) {
                throw new IllegalArgumentException("Friend request is not pending or does not exist.");
            }
            // Update the status to ACCEPTED
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            // Save the updated friendship instance
            friendshipRepository.save(friendship);
    }

    /**
     * Declines a friend request by deleting the associated {@link Friendship} entity from the repository.
     *
     * @param friendship the {@link Friendship} instance representing the friend request to be declined
     */

    public void declineFriendRequest(Friendship friendship) {

        friendshipRepository.delete(friendship);
    }

    /**
     * Retrieves a {@link Friendship} object based on the provided friendship ID.
     *
     * @param friendshipId the unique identifier of the friendship to retrieve
     * @return the {@link Friendship} object corresponding to the given ID
     */
    public Friendship getFriendshipById(String friendshipId) {
        return friendshipRepository.getReferenceById(friendshipId);
    }



}