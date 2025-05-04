package org.novize.api.mapper;


import lombok.RequiredArgsConstructor;
import org.novize.api.dtos.friendship.FriendshipDto;
import org.novize.api.model.Friendship;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendshipMapper {
    private final UserMapper userMapper;

    public FriendshipDto toDto(Friendship friendship) {
        return FriendshipDto.builder()
                .id(friendship.getId())
                .requester(userMapper.toDto(friendship.getUser()))
                .addressee(userMapper.toDto(friendship.getFriend()))
                .status(friendship.getStatus())
                .createdAt(friendship.getCreatedAt())
                .build();
    }

}
