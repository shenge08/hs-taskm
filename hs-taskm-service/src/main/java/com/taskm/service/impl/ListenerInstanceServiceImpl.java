package com.taskm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taskm.dto.CreateListenerInstanceDTO;
import com.taskm.dto.ListenerInstanceVO;
import com.taskm.dto.UpdateListenerInstanceDTO;
import com.taskm.entity.Listener;
import com.taskm.entity.ListenerInstance;
import com.taskm.exception.InstanceInUseException;
import com.taskm.exception.InstanceNameConflictException;
import com.taskm.exception.ListenerInstanceNotFoundException;
import com.taskm.exception.ListenerNotFoundException;
import com.taskm.mapper.ListenerInstanceMapper;
import com.taskm.mapper.ListenerMapper;
import com.taskm.mapper.TaskMapper;
import com.taskm.service.ListenerInstanceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for listener instance management.
 */
@Service
public class ListenerInstanceServiceImpl implements ListenerInstanceService {

    private final ListenerInstanceMapper instanceMapper;
    private final ListenerMapper listenerMapper;
    private final TaskMapper taskMapper;

    @Autowired
    public ListenerInstanceServiceImpl(
            ListenerInstanceMapper instanceMapper,
            ListenerMapper listenerMapper,
            TaskMapper taskMapper) {
        this.instanceMapper = instanceMapper;
        this.listenerMapper = listenerMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional
    public ListenerInstanceVO createInstance(Long listenerId, CreateListenerInstanceDTO dto) {
        // Verify listener exists
        Listener listener = listenerMapper.selectById(listenerId);
        if (listener == null) {
            throw new ListenerNotFoundException("Listener not found with id: " + listenerId);
        }

        // Check instance name uniqueness
        QueryWrapper<ListenerInstance> nameCheckWrapper = new QueryWrapper<>();
        nameCheckWrapper.eq("listener_id", listenerId)
                .eq("name", dto.getName());
        ListenerInstance existing = instanceMapper.selectOne(nameCheckWrapper);
        if (existing != null) {
            throw new InstanceNameConflictException(
                    "Instance name '" + dto.getName() + "' already exists for listener " + listenerId);
        }

        // Create instance
        ListenerInstance instance = new ListenerInstance();
        instance.setListenerId(listenerId);
        instance.setName(dto.getName());
        instance.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        instance.setConfig(dto.getConfig());

        instanceMapper.insert(instance);

        return toVO(instance);
    }

    @Override
    public List<ListenerInstanceVO> getInstancesByListenerId(Long listenerId) {
        // Verify listener exists
        Listener listener = listenerMapper.selectById(listenerId);
        if (listener == null) {
            throw new ListenerNotFoundException("Listener not found with id: " + listenerId);
        }

        QueryWrapper<ListenerInstance> wrapper = new QueryWrapper<>();
        wrapper.eq("listener_id", listenerId);
        List<ListenerInstance> instances = instanceMapper.selectList(wrapper);

        return instances.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public ListenerInstanceVO getInstanceById(Long instanceId) {
        ListenerInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new ListenerInstanceNotFoundException("Instance not found with id: " + instanceId);
        }
        return toVO(instance);
    }

    @Override
    @Transactional
    public ListenerInstanceVO updateInstance(Long instanceId, UpdateListenerInstanceDTO dto) {
        ListenerInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new ListenerInstanceNotFoundException("Instance not found with id: " + instanceId);
        }

        // Update fields if provided
        if (dto.getName() != null) {
            // Check name uniqueness
            QueryWrapper<ListenerInstance> nameCheckWrapper = new QueryWrapper<>();
            nameCheckWrapper.eq("listener_id", instance.getListenerId())
                    .eq("name", dto.getName())
                    .ne("id", instanceId);
            ListenerInstance existing = instanceMapper.selectOne(nameCheckWrapper);
            if (existing != null) {
                throw new InstanceNameConflictException(
                        "Instance name '" + dto.getName() + "' already exists");
            }
            instance.setName(dto.getName());
        }

        if (dto.getIsDefault() != null) {
            instance.setIsDefault(dto.getIsDefault());
        }

        if (dto.getConfig() != null) {
            instance.setConfig(dto.getConfig());
        }

        if (dto.getStatus() != null) {
            instance.setStatus(dto.getStatus());
        }

        if (dto.getContainerId() != null) {
            instance.setContainerId(dto.getContainerId());
        }

        instanceMapper.updateById(instance);

        return toVO(instance);
    }

    @Override
    @Transactional
    public void deleteInstance(Long instanceId) {
        ListenerInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new ListenerInstanceNotFoundException("Instance not found with id: " + instanceId);
        }

        // Check if instance is in use by any task
        QueryWrapper<com.taskm.entity.Task> taskCheckWrapper = new QueryWrapper<>();
        taskCheckWrapper.eq("listener_instance_id", instanceId);
        Long taskCount = taskMapper.selectCount(taskCheckWrapper);
        if (taskCount > 0) {
            throw new InstanceInUseException(
                    "Cannot delete instance " + instanceId + " - it is being used by " + taskCount + " task(s)");
        }

        instanceMapper.deleteById(instanceId);
    }

    @Override
    @Transactional
    public void setDefaultInstance(Long instanceId) {
        ListenerInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new ListenerInstanceNotFoundException("Instance not found with id: " + instanceId);
        }

        // Remove default flag from all instances of the same listener
        QueryWrapper<ListenerInstance> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("listener_id", instance.getListenerId())
                .eq("is_default", true);

        ListenerInstance updateEntity = new ListenerInstance();
        updateEntity.setIsDefault(false);
        instanceMapper.update(updateEntity, updateWrapper);

        // Set current instance as default
        instance.setIsDefault(true);
        instanceMapper.updateById(instance);
    }

    @Override
    public ListenerInstance getDefaultInstance(Long listenerId) {
        QueryWrapper<ListenerInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("listener_id", listenerId)
                .eq("is_default", true)
                .last("LIMIT 1");

        return instanceMapper.selectOne(queryWrapper);
    }

    /**
     * Convert entity to VO.
     */
    private ListenerInstanceVO toVO(ListenerInstance entity) {
        ListenerInstanceVO vo = new ListenerInstanceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
