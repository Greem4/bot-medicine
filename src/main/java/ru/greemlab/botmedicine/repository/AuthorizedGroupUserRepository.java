package ru.greemlab.botmedicine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.greemlab.botmedicine.entity.AuthorizedGroupUSer;
import ru.greemlab.botmedicine.entity.AuthorizedGroupUserKey;

@Repository
public interface AuthorizedGroupUserRepository extends JpaRepository<AuthorizedGroupUSer, AuthorizedGroupUserKey> {
}
