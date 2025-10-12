package com.example.project_chat.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {
    private final Set<Integer> onlineUserIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void userOnline(Integer userId) {
        onlineUserIds.add(userId);
    }

    public void userOffline(Integer userId) {
        onlineUserIds.remove(userId);
    }

    public boolean isUserOnline(Integer userId) {
        return onlineUserIds.contains(userId);
    }

    public Set<Integer> getOnlineUserIds() {
        return onlineUserIds;
    }
}
