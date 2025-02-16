package ru.greemlab.botmedicine.dto;

public record AuthorizedGroupUserDto(
        String userName,
        Long userId,
        Long groupChatId
) {
}
