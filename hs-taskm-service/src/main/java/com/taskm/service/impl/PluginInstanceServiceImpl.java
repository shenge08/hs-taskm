package com.taskm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taskm.dto.CreatePluginInstanceDTO;
import com.taskm.dto.PluginInstanceVO;
import com.taskm.dto.UpdatePluginInstanceDTO;
import com.taskm.entity.DataPlugin;
import com.taskm.entity.DataPluginInstance;
import com.taskm.exception.DataPluginNotFoundException;
import com.taskm.exception.InstanceInUseException;
import com.taskm.exception.InstanceNameConflictException;
import com.taskm.exception.PluginInstanceNotFoundException;
import com.taskm.mapper.DataPluginInstanceMapper;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.mapper.TaskMapper;
import com.taskm.service.PluginInstanceService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * Service implementation for plugin instance management.
 */
@Service
public class PluginInstanceServiceImpl implements PluginInstanceService {

    private final DataPluginInstanceMapper instanceMapper;
    private final DataPluginMapper pluginMapper;
    private final TaskMapper taskMapper;

    @Autowired
    public PluginInstanceServiceImpl(
            DataPluginInstanceMapper instanceMapper,
            DataPluginMapper pluginMapper,
            TaskMapper taskMapper) {
        this.instanceMapper = instanceMapper;
        this.pluginMapper = pluginMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional
    public PluginInstanceVO createInstance(Long pluginId, CreatePluginInstanceDTO dto) {
        // Verify plugin exists
        DataPlugin plugin = pluginMapper.selectById(pluginId);
        if (plugin == null) {
            throw new DataPluginNotFoundException("Plugin not found with id: " + pluginId);
        }

        // Check instance name uniqueness
        QueryWrapper<DataPluginInstance> nameCheckWrapper = new QueryWrapper<>();
        nameCheckWrapper.eq("plugin_id", pluginId)
                .eq("name", dto.getName());
        DataPluginInstance existing = instanceMapper.selectOne(nameCheckWrapper);
        if (existing != null) {
            throw new InstanceNameConflictException(
                    "Instance name '" + dto.getName() + "' already exists for plugin " + pluginId);
        }

        // Create instance
        DataPluginInstance instance = new DataPluginInstance();
        instance.setPluginId(pluginId);
        instance.setName(dto.getName());
        instance.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        if(instance.getIsDefault()){
            List<Map<String, Object>> metadata = plugin.getMetadata();
            if(!CollectionUtils.isEmpty( metadata)){
                Map<String,Object> config = new HashMap<>();
                for (Map<String, Object> entry : metadata){
                    config.put(entry.get("key").toString(), entry.get("defaultValue"));
                }
                instance.setConfig(config);
            }
        }else{
            instance.setConfig(dto.getConfig());
        }
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());

        instanceMapper.insert(instance);

        return toVO(instance);
    }

    @Override
    public List<PluginInstanceVO> getInstancesByPluginId(Long pluginId) {
        // Verify plugin exists
        DataPlugin plugin = pluginMapper.selectById(pluginId);
        if (plugin == null) {
            throw new DataPluginNotFoundException("Plugin not found with id: " + pluginId);
        }

        QueryWrapper<DataPluginInstance> wrapper = new QueryWrapper<>();
        wrapper.eq("plugin_id", pluginId);
        List<DataPluginInstance> instances = instanceMapper.selectList(wrapper);

        return instances.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public PluginInstanceVO getInstanceById(Long instanceId) {
        DataPluginInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new PluginInstanceNotFoundException("Instance not found with id: " + instanceId);
        }
        return toVO(instance);
    }

    @Override
    @Transactional
    public PluginInstanceVO updateInstance(Long instanceId, UpdatePluginInstanceDTO dto) {
        DataPluginInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new PluginInstanceNotFoundException("Instance not found with id: " + instanceId);
        }

        // Update fields if provided
        if (dto.getName() != null) {
            // Check name uniqueness
            QueryWrapper<DataPluginInstance> nameCheckWrapper = new QueryWrapper<>();
            nameCheckWrapper.eq("plugin_id", instance.getPluginId())
                    .eq("name", dto.getName())
                    .ne("id", instanceId);
            DataPluginInstance existing = instanceMapper.selectOne(nameCheckWrapper);
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
        DataPluginInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new PluginInstanceNotFoundException("Instance not found with id: " + instanceId);
        }

        // Check if instance is in use by any task
        QueryWrapper<com.taskm.entity.Task> taskCheckWrapper = new QueryWrapper<>();
        taskCheckWrapper.eq("plugin_instance_id", instanceId);
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
        DataPluginInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new PluginInstanceNotFoundException("Instance not found with id: " + instanceId);
        }

        // Remove default flag from all instances of the same plugin
        QueryWrapper<DataPluginInstance> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("plugin_id", instance.getPluginId())
                .eq("is_default", true);

        DataPluginInstance updateEntity = new DataPluginInstance();
        updateEntity.setIsDefault(false);
        instanceMapper.update(updateEntity, updateWrapper);

        // Set current instance as default
        instance.setIsDefault(true);
        instanceMapper.updateById(instance);
    }

    @Override
    public DataPluginInstance getDefaultInstance(Long pluginId) {
        QueryWrapper<DataPluginInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("plugin_id", pluginId)
                .eq("is_default", true)
                .last("LIMIT 1");

        return instanceMapper.selectOne(queryWrapper);
    }

    /**
     * Convert entity to VO.
     */
    private PluginInstanceVO toVO(DataPluginInstance entity) {
        PluginInstanceVO vo = new PluginInstanceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
