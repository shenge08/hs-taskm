package com.taskm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskm.entity.Listener;
import com.taskm.exception.ListenerNotFoundException;
import com.taskm.mapper.ListenerMapper;
import com.taskm.service.ListenerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for Listener operations.
 */
@Service
public class ListenerServiceImpl extends ServiceImpl<ListenerMapper, Listener> implements ListenerService {

    @Override
    public List<Listener> getAllListeners() {
        return list();
    }

    @Override
    public Listener getListener(Long id) {
        Listener listener = getById(id);
        if (listener == null) {
            throw new ListenerNotFoundException("Listener not found with id: " + id);
        }
        return listener;
    }

    @Override
    public List<Listener> getListenersByLanguage(String language) {
        QueryWrapper<Listener> wrapper = new QueryWrapper<>();
        wrapper.eq("language", language);
        return list(wrapper);
    }

    @Override
    public List<Listener> getListenersByEventType(String eventType) {
        QueryWrapper<Listener> wrapper = new QueryWrapper<>();
        wrapper.eq("event_type", eventType);
        return list(wrapper);
    }
}
