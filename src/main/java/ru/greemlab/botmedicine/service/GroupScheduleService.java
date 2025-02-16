package ru.greemlab.botmedicine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.greemlab.botmedicine.dto.GroupScheduleDto;
import ru.greemlab.botmedicine.entity.GroupSchedule;
import ru.greemlab.botmedicine.repository.GroupScheduleRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupScheduleService {

    private final GroupScheduleRepository groupScheduleRepository;

    public boolean isGroupRegistered(Long groupChatId) {
        return groupScheduleRepository.existsById(groupChatId);
    }

    public void upsertGroup(Long groupChatId, String schedulerUrl) {
        var entity = new GroupSchedule(groupChatId, schedulerUrl);
        groupScheduleRepository.save(entity);
        new GroupScheduleDto(groupChatId, schedulerUrl);
    }

    public void updateScheduleUrl(Long groupChatId, String schedulerUrl) {
        var entity = groupScheduleRepository
                .findById(groupChatId)
                .orElse(new GroupSchedule(groupChatId, null));
        entity.setScheduleUrl(schedulerUrl);
        groupScheduleRepository.save(entity);
    }

    public void createOrUpdateGroup(Long groupChatId, String schedulerUrl) {
        var entity = groupScheduleRepository
                .findById(groupChatId)
                .orElse(new GroupSchedule(null, schedulerUrl));
        entity.setGroupChatId(groupChatId);
        groupScheduleRepository.save(entity);
    }

    public Optional<String> findSchedulerUrl(Long groupChatId) {
        return groupScheduleRepository.findById(groupChatId)
                .map(GroupSchedule::getScheduleUrl);
    }

    public void deleteGroup(Long groupChatId) {
        if (groupScheduleRepository.existsById(groupChatId)) {
            groupScheduleRepository.deleteById(groupChatId);
        }
    }
}
