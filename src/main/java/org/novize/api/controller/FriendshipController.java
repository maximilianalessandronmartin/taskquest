package org.novize.api.controller;

import org.novize.api.model.Friendship;
import org.novize.api.model.User;
import org.novize.api.services.FriendshipServiceImpl;
import org.novize.api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Controller for handling friendship-related requests.
 * This controller provides endpoints for managing friendships, including sending, accepting, and declining friend requests.
 */

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    @Autowired
    private FriendshipServiceImpl friendshipServiceImpl;
    @Autowired
    private UserService userService;

    @GetMapping("/pending")
    public List<Friendship> getPendingFriendRequests(@AuthenticationPrincipal User user) {
        // Logik, um die ausstehenden Freundschaftsanfragen zu holen
        return friendshipServiceImpl.getPendingFriendRequests(user);
    }
    @GetMapping("/friends")
    public List<Friendship> getFriends(@AuthenticationPrincipal User user) {
        // Logik, um die Freunde des Benutzers zu holen
        return friendshipServiceImpl.getFriendshipsByUser(user);
    }

    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public void sendFriendRequest(@AuthenticationPrincipal User sender, @RequestParam String receiverMail) {
        // Logik, um den Empfänger zu finden und die Anfrage zu senden
       User receiver = userService.findUserByEmail(receiverMail);
       friendshipServiceImpl.sendFriendRequest(sender, receiver);

    }

    @PostMapping("/accept")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void acceptFriendRequest(@RequestParam String friendshipId) {
        // Logik, um die Anfrage anzunehmen
        var friendship = friendshipServiceImpl.getFriendshipById(friendshipId);
        if (friendship != null) {
            friendshipServiceImpl.acceptFriendRequest(friendship);
        } else {
            // Fehlerbehandlung, wenn die Freundschaftsanfrage nicht gefunden wird
            throw new RuntimeException("Friendship not found");
        }
    }

    @DeleteMapping("/decline")
    @Transactional
    public void declineFriendRequest(@RequestParam String friendshipId) {
        // Logik, um die Anfrage abzulehnen
        var friendship = friendshipServiceImpl.getFriendshipById(friendshipId);
        if (friendship != null) {
            friendshipServiceImpl.declineFriendRequest(friendship);
        } else {
            // Fehlerbehandlung, wenn die Freundschaftsanfrage nicht gefunden wird
            throw new RuntimeException("Friendship not found");
        }
    }
}
