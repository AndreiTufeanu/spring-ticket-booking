package com.andreitufeanu.backend.chat.mapper;

import com.andreitufeanu.backend.chat.dto.ChatMessageDto;
import com.andreitufeanu.backend.chat.entity.ChatMessage;
import com.andreitufeanu.backend.chat.enums.ChatMessageRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "roleToString")
    ChatMessageDto toResponse(ChatMessage chatMessage);

    @Named("roleToString")
    default String roleToString(ChatMessageRole role) {
        return role.name().toLowerCase();
    }
}