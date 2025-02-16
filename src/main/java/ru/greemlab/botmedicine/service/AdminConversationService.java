package ru.greemlab.botmedicine.service;

import org.springframework.stereotype.Service;
import ru.greemlab.botmedicine.admin.AdminState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminConversationService {

    private final Map<Long, AdminState> adminStates = new ConcurrentHashMap<>();

    public void waitForSetGroupData(Long adminId) {
        adminStates.put(adminId, AdminState.WAITING_FOR_SETGROUP_DATE);
    }

    public void waitForSetScheduleUrl(Long adminId) {
        adminStates.put(adminId, AdminState.WAITING_FOR_UPDATESCHEDULE);
    }

    public void waitForRemoveGroupData(Long adminId) {
        adminStates.put(adminId, AdminState.WAITING_FOR_REMOVEGPOUP_DATE);
    }

    public void waitForRemoveUserData(Long adminId) {
        adminStates.put(adminId, AdminState.WAITING_FOR_REMOVEUSER_DATE);
    }

    public AdminState getCurrentState(Long adminId) {
        return adminStates.getOrDefault(adminId, AdminState.NONE);
    }

    public void clearState(Long adminId) {
        adminStates.remove(adminId);
    }
}
