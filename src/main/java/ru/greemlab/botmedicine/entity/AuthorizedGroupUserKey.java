package ru.greemlab.botmedicine.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizedGroupUserKey {

    private String userName;
    private Long userId;
    private Long groupChatId;
}
