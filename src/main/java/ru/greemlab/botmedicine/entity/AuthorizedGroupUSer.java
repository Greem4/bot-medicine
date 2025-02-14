package ru.greemlab.botmedicine.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "authorized_group_user")
@IdClass(AuthorizedGroupUSer.class)
public class AuthorizedGroupUSer {

    @Id
    private Long groupChatId;

    @Id
    private Long userId;
}
