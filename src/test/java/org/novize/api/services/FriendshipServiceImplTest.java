package org.novize.api.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.novize.api.enums.FriendshipStatus;
import org.novize.api.exceptions.FriendshipNotFoundException;
import org.novize.api.model.Friendship;
import org.novize.api.model.User;
import org.novize.api.repository.FriendshipRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
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

        Friendship friendship = new Friendship();
        friendship.setUser(sender);
        friendship.setFriend(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(userRepository.findById("sender-123")).thenReturn(Optional.of(sender));
        when(userRepository.findById("receiver-456")).thenReturn(Optional.of(receiver));

        friendshipService.sendFriendRequest(sender, receiver);

        verify(friendshipRepository, times(1)).save(Mockito.argThat(savedFriendship ->
                savedFriendship.getUser().equals(sender) &&
                        savedFriendship.getFriend().equals(receiver) &&
                        savedFriendship.getStatus().equals(FriendshipStatus.PENDING)));
    }

    @Test
    void shouldReturnAcceptedFriendshipsByUser() {
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .build();
        user.setId("user-123");

        Friendship acceptedFriendship = new Friendship();
        acceptedFriendship.setUser(user);
        User friend = User.builder()
                .firstname("Jane")
                .lastname("Smith")
                .username("jane.smith@example.com")
                .password("password456")
                .build();
        friend.setId("friend-456");
        acceptedFriendship.setFriend(friend);
        acceptedFriendship.setStatus(FriendshipStatus.ACCEPTED);

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

    }

    @Test
    void shouldThrowExceptionWhenUsersSendRequestToThemselves() {
        User user = User.builder().build();
        user.setId("user-123");

        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.sendFriendRequest(user, user);
        });
    }
}