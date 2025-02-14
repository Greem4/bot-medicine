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

    public GroupScheduleDto upsertGroup(Long groupChatId, String schedulerUrl) {
        var entity = new GroupSchedule(groupChatId, schedulerUrl);
        groupScheduleRepository.save(entity);
        return new GroupScheduleDto(groupChatId, schedulerUrl);
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
