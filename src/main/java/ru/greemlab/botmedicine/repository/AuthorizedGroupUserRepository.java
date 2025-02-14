package ru.greemlab.botmedicine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.greemlab.botmedicine.entity.AuthorizedGroupUSer;
import ru.greemlab.botmedicine.entity.AuthorizedGroupUserKey;

import java.util.List;

@Repository
public interface AuthorizedGroupUserRepository extends JpaRepository<AuthorizedGroupUSer, AuthorizedGroupUserKey> {

    @Query("SELECT u.groupChatId FROM AuthorizedGroupUSer u WHERE u.userId = :userId")
    List<Long> findAllGroupsByUserId(Long userId);
}
