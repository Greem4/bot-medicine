package ru.greemlab.botmedicine.dto;

public record AuthorizedGroupUserDto(
        Long groupChatId,
        Long userId
) {
}
