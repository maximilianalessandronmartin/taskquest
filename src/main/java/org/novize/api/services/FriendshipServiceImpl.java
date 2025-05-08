package org.novize.api.services;

import org.novize.api.enums.FriendshipStatus;
import org.novize.api.enums.NotificationType;
import org.novize.api.exceptions.FriendshipNotFoundException;
import org.novize.api.exceptions.InvalidRequestException;
import org.novize.api.model.Friendship;
import org.novize.api.model.User;
import org.novize.api.repository.FriendshipRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    @Autowired
    private NotificationService notificationService;

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
     * @param sender   the user sending the friend request
     * @param receiver the user receiving the friend request
     */
    public void sendFriendRequest(User sender, User receiver) {
        // Check if the sender and receiver are the same
        if (sender.getId().equals(receiver.getId())) {
            throw new InvalidRequestException("You cannot send a friend request to yourself.");
        }
        // Prüfen, ob bereits eine Anfrage existiert
        Optional<Friendship> existingFriendship = friendshipRepository.findByUserAndFriend(sender, receiver);
        if (existingFriendship.isPresent()) {
            throw new InvalidRequestException("Friend request already exists.");
        }

        Friendship friendship = Friendship
                .builder()
                .user(sender)
                .friend(receiver)
                .status(FriendshipStatus.PENDING)
                .build();


        friendshipRepository.save(friendship);
        // Send a notification to the receiver
        notificationService.sendNotification(
                receiver,
                NotificationType.FRIEND_REQUEST,
                sender.getUsername() + " send you a friend request",
                "{\"senderId\": \"" + sender.getId() + "\"}"
        );

    }


    /**
     * Accepts a pending friend request by updating its status to ACCEPTED
     * and saving the updated friendship instance in the repository.
     *
     * @param friendship the friendship instance representing the pending friend request
     *                   to be accepted
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
        notificationService.sendNotification(
                friendship.getUser(),
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                friendship.getFriend().getFirstname() + " " + friendship.getFriend().getLastname() + " hat Ihre Freundschaftsanfrage angenommen",
                "{\"receiverId\": \"" + friendship.getFriend().getId() + "\"}"
        );

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
     * Retrieves a {@link Friendship} entity by its unique identifier.
     * If the friendship ID is null, empty, or does not correspond to an existing friendship,
     * a {@link FriendshipNotFoundException} is thrown.
     *
     * @param friendshipId the unique identifier of the friendship to retrieve
     * @return the {@link Friendship} entity associated with the provided ID
     * @throws FriendshipNotFoundException if no friendship is found with the specified ID
     */
    public Friendship getFriendshipById(String friendshipId) {
        // Check if the friendship ID is null or empty
        if (friendshipId == null || friendshipId.isEmpty()) {
            throw new IllegalArgumentException("Invalid friendship ID");
        }
        // Retrieve the friendship by ID
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship was not found"));
    }

    /**
     * Checks if two users are friends by verifying if a friendship relation exists
     * between the current user and the given friend with the status set to ACCEPTED.
     *
     * @param currentUser the user initiating the friendship check
     * @param friend      the user whose friendship status with the current user is being checked
     * @return true if the users are friends (friendship exists with status ACCEPTED), false otherwise
     */
    @Override
    public boolean areNotFriends(User currentUser, User friend) {
        return !friendshipRepository.existsByUserAndFriendAndStatus(currentUser, friend, FriendshipStatus.ACCEPTED);
    }


}