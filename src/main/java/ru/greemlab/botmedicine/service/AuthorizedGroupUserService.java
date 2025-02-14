package ru.greemlab.botmedicine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.greemlab.botmedicine.dto.AuthorizedGroupUserDto;
import ru.greemlab.botmedicine.entity.AuthorizedGroupUSer;
import ru.greemlab.botmedicine.entity.AuthorizedGroupUserKey;
import ru.greemlab.botmedicine.repository.AuthorizedGroupUserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorizedGroupUserService {

    private final AuthorizedGroupUserRepository authorizedGroupUserRepository;

    public boolean isUserAuthorized(Long groupChatId, Long userId) {
        var key = new AuthorizedGroupUserKey(groupChatId, userId);
        return authorizedGroupUserRepository.existsById(key);
    }


    public AuthorizedGroupUserDto addUser(Long groupChatId, Long userId) {
        var key = new AuthorizedGroupUserKey(groupChatId, userId);
        if (!authorizedGroupUserRepository.existsById(key)) {
            authorizedGroupUserRepository.save(new AuthorizedGroupUSer(groupChatId, userId));
        }
        return new AuthorizedGroupUserDto(groupChatId, userId);
    }

    public void removeUser(Long groupChatId, Long userId) {
        var key = new AuthorizedGroupUserKey(groupChatId, userId);
        authorizedGroupUserRepository.deleteById(key);
    }

    public Optional<AuthorizedGroupUserDto> find(Long groupChatId, Long userId) {
        var key = new AuthorizedGroupUserKey(groupChatId, userId);
        return authorizedGroupUserRepository.findById(key)
                .map(u -> new AuthorizedGroupUserDto(u.getGroupChatId(), u.getUserId()));
    }
}
