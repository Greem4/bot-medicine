package ru.greemlab.botmedicine.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "authorized_group_user")
@IdClass(AuthorizedGroupUSer.class)
public class AuthorizedGroupUSer {

    @Id
    private String userName;

    @Id
    private Long userId;

    @Id
    private Long groupChatId;
}
