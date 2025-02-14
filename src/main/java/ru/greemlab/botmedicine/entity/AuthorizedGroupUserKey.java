package ru.greemlab.botmedicine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizedGroupUserKey {

    private Long groupChatId;
    private Long userId;
}
