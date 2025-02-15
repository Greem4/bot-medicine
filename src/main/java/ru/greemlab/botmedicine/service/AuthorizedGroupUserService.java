package ru.greemlab.botmedicine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.greemlab.botmedicine.dto.AuthorizedGroupUserDto;
import ru.greemlab.botmedicine.entity.AuthorizedGroupUSer;
import ru.greemlab.botmedicine.entity.AuthorizedGroupUserKey;
import ru.greemlab.botmedicine.repository.AuthorizedGroupUserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorizedGroupUserService {

    private final AuthorizedGroupUserRepository authorizedGroupUserRepository;

    public boolean isUserAuthorized(String userName, Long userId, Long groupChatId) {
        var key = new AuthorizedGroupUserKey(userName, userId, groupChatId);
        return authorizedGroupUserRepository.existsById(key);
    }


    public void addUser(String userName, Long userId, Long groupChatId) {
        var key = new AuthorizedGroupUserKey(userName, userId, groupChatId);
        if (!authorizedGroupUserRepository.existsById(key)) {
            authorizedGroupUserRepository.save(new AuthorizedGroupUSer(userName, userId, groupChatId));
        }
        new AuthorizedGroupUserDto(groupChatId, userId);
    }

    public void removeUser(String userName, Long userId, Long groupChatId) {
        var key = new AuthorizedGroupUserKey(userName, userId, groupChatId);
        authorizedGroupUserRepository.deleteById(key);
    }

    public Optional<AuthorizedGroupUserDto> find(String userName, Long userId, Long groupChatId) {
        var key = new AuthorizedGroupUserKey(userName, userId, groupChatId);
        return authorizedGroupUserRepository.findById(key)
                .map(u -> new AuthorizedGroupUserDto(u.getGroupChatId(), u.getUserId()));
    }

    public List<Long> findGroupsForUser(Long userId) {
        return authorizedGroupUserRepository.findAllGroupsByUserId(userId);
    }
}
