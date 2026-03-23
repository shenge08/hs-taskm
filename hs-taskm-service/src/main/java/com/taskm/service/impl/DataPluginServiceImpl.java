package com.taskm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskm.entity.DataPlugin;
import com.taskm.exception.DataPluginNotFoundException;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.service.DataPluginService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for DataPlugin operations.
 */
@Service
public class DataPluginServiceImpl extends ServiceImpl<DataPluginMapper, DataPlugin> implements DataPluginService {

    @Override
    public List<DataPlugin> getAllPlugins() {
        return list();
    }

    @Override
    public DataPlugin getPlugin(Long id) {
        DataPlugin plugin = getById(id);
        if (plugin == null) {
            throw new DataPluginNotFoundException("DataPlugin not found with id: " + id);
        }
        return plugin;
    }

    @Override
    public List<DataPlugin> getPluginsByLanguage(String language) {
        QueryWrapper<DataPlugin> wrapper = new QueryWrapper<>();
        wrapper.eq("language", language);
        return list(wrapper);
    }

    @Override
    public List<DataPlugin> getPluginsByType(String pluginType) {
        QueryWrapper<DataPlugin> wrapper = new QueryWrapper<>();
        wrapper.eq("plugin_type", pluginType);
        return list(wrapper);
    }

    @Override
    public Map<String, Object> testPlugin(Long id, Map<String, Object> testParams) {
        // TODO: Implement plugin testing in Tracer Bullet 3
        DataPlugin plugin = getPlugin(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Plugin testing not yet implemented");
        result.put("pluginId", id);
        result.put("pluginName", plugin.getName());
        result.put("executionTime", 0L);

        return result;
    }
}
