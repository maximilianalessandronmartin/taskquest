package org.novize.api.dtos.friendship;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.novize.api.dtos.UserDto;
import org.novize.api.enums.FriendshipStatus;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipDto {
    private String id;
    private UserDto requester;
    private UserDto addressee;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
}
