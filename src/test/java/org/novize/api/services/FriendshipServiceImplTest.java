package org.novize.api.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.novize.api.enums.FriendshipStatus;
import org.novize.api.exceptions.FriendshipNotFoundException;
import org.novize.api.exceptions.InvalidRequestException;
import org.novize.api.model.Friendship;
import org.novize.api.model.User;
import org.novize.api.repository.FriendshipRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class FriendshipServiceImplTest {

    @Autowired
    private FriendshipServiceImpl friendshipService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private FriendshipRepository friendshipRepository;

    @Test
    void shouldSendFriendRequest() {
        User sender = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .build();
        sender.setId("sender-123");

        User receiver = User.builder()
                .firstname("Jane")
                .lastname("Smith")
                .username("jane.smith@example.com")
                .password("password456")
                .build();
        receiver.setId("receiver-456");

        Friendship friendship = Friendship.builder()
                .user(sender)
                .friend(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        when(userRepository.findById("sender-123")).thenReturn(Optional.of(sender));
        when(userRepository.findById("receiver-456")).thenReturn(Optional.of(receiver));

        friendshipService.sendFriendRequest(sender, receiver);

        verify(friendshipRepository, times(1)).save(Mockito.argThat(savedFriendship ->
                savedFriendship.getUser().equals(sender) &&
                        savedFriendship.getFriend().equals(receiver) &&
                        savedFriendship.getStatus().equals(FriendshipStatus.PENDING)));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldReturnAcceptedFriendshipsByUser() {
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .build();
        user.setId("user-123");

        Friendship acceptedFriendship = Friendship.builder()
                .user(user)
                .friend(null) // Friend will be set later
                .status(FriendshipStatus.ACCEPTED)
                .build();
        User friend = User.builder()
                .firstname("Jane")
                .lastname("Smith")
                .username("jane.smith@example.com")
                .password("password456")
                .build();
        friend.setId("friend-456");
        acceptedFriendship.setFriend(friend);

        when(friendshipRepository.findByUserAndStatus(user, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of(acceptedFriendship));

        List<Friendship> friendships = friendshipService.getFriendshipsByUser(user);

        verify(friendshipRepository, times(1))
                .findByUserAndStatus(user, FriendshipStatus.ACCEPTED);

        assertEquals(1, friendships.size());
        assertEquals(FriendshipStatus.ACCEPTED, friendships.get(0).getStatus());
        assertEquals("friend-456", friendships.get(0).getFriend().getId());
    }

    @Test
    void shouldThrowExceptionWhenFriendshipNotFound() {
        String friendshipId = "friendship-123";
        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.empty());

        assertThrows(FriendshipNotFoundException.class, () -> {
            friendshipService.getFriendshipById(friendshipId);
        });

        verify(friendshipRepository, times(1)).findById(friendshipId);

    }

    @Test
    void shouldThrowExceptionWhenFriendshipRequestIdIsInValid() {
        String friendshipId = "invalid-id";
        when(friendshipRepository.findById(friendshipId)).thenThrow(new IllegalArgumentException());

        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.getFriendshipById(friendshipId);
        });

        verify(friendshipRepository, times(1)).findById(friendshipId);
    }

    @Test
    void shouldThrowExceptionWhenUsersSendRequestToThemselves() {
        User user = User.builder().build();
        user.setId("user-123");

        assertThrows(InvalidRequestException.class, () -> {
            friendshipService.sendFriendRequest(user, user);
        });
    }

    @Test
    void getFriendshipsByUser() {
        User user = User.builder()
                .firstname("Test")
                .lastname("User")
                .username("test.user@example.com")
                .build();
        user.setId("user-123");

        when(friendshipRepository.findByUserAndStatus(user, FriendshipStatus.ACCEPTED))
                .thenReturn(List.of());

        List<Friendship> friendships = friendshipService.getFriendshipsByUser(user);

        verify(friendshipRepository, times(1))
                .findByUserAndStatus(user, FriendshipStatus.ACCEPTED);
        assertEquals(0, friendships.size());
    }

    @Test
    void getPendingFriendRequests() {
        User user = User.builder()
                .firstname("Test")
                .lastname("User")
                .username("test.user@example.com")
                .build();
        user.setId("user-123");

        when(friendshipRepository.findByFriendAndStatus(user, FriendshipStatus.PENDING))
                .thenReturn(List.of());

        List<Friendship> pendingRequests = friendshipService.getPendingFriendRequests(user);

        verify(friendshipRepository, times(1))
                .findByFriendAndStatus(user, FriendshipStatus.PENDING);
        assertEquals(0, pendingRequests.size());
    }

    @Test
    void sendFriendRequest() {
        User sender = User.builder().build();
        sender.setId("sender-123");
        User receiver = User.builder().build();
        receiver.setId("receiver-456");

        // Mock the repository when saving friendship
        Friendship friendship = Friendship.builder()
                .user(sender)
                .friend(receiver)
                .status(FriendshipStatus.PENDING)
                .build();

        when(friendshipRepository.findByUserAndFriend(sender, receiver))
                .thenReturn(Optional.empty());
        when(friendshipRepository.save(friendship)).thenReturn(friendship);

        friendshipService.sendFriendRequest(sender, receiver);

        verify(friendshipRepository, times(1))
                .findByUserAndFriend(sender, receiver);
        verify(friendshipRepository, times(1)).save(friendship);
    }

    @Test
    void acceptFriendRequest() {
        Friendship friendship = Friendship.builder()
                .status(FriendshipStatus.PENDING)
                .build();
        friendship.setId("friendship-123");

        when(friendshipRepository.findById("friendship-123"))
                .thenReturn(Optional.of(friendship));

        friendshipService.acceptFriendRequest(friendship);

        verify(friendshipRepository, times(1))
                .save(argThat(f -> f.getStatus() == FriendshipStatus.ACCEPTED));
    }

    @Test
    void declineFriendRequest() {
        Friendship friendship = Friendship.builder()
                .status(FriendshipStatus.PENDING)
                .build();
        friendship.setId("friendship-123");

        when(friendshipRepository.findById("friendship-123"))
                .thenReturn(Optional.of(friendship));

        friendshipService.declineFriendRequest(friendship);

        verify(friendshipRepository, times(1)).delete(friendship);
    }

    @Test
    void getFriendshipById() {
        Friendship friendship = Friendship.builder()
                .status(FriendshipStatus.ACCEPTED)
                .build();
        friendship.setId("friendship-123");

        when(friendshipRepository.findById("friendship-123"))
                .thenReturn(Optional.of(friendship));

        Friendship result = friendshipService.getFriendshipById("friendship-123");

        assertEquals(friendship, result);
        verify(friendshipRepository, times(1)).findById("friendship-123");
    }

    @Test
    void areNotFriends() {
        User user1 = User.builder().build();
        user1.setId("user1-123");
        User user2 = User.builder().build();
        user2.setId("user2-456");

        when(friendshipRepository.existsByUserAndFriendAndStatus(user1, user2, FriendshipStatus.ACCEPTED))
                .thenReturn(false);

        boolean result = friendshipService.areNotFriends(user1, user2);

        assertTrue(result);
        verify(friendshipRepository, times(1))
                .existsByUserAndFriendAndStatus(user1, user2, FriendshipStatus.ACCEPTED);
    }
}