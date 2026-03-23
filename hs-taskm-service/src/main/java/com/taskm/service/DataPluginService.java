package com.taskm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taskm.entity.DataPlugin;

import java.util.List;
import java.util.Map;

/**
 * Service interface for DataPlugin operations.
 */
public interface DataPluginService extends IService<DataPlugin> {

    /**
     * Get all data plugins.
     *
     * @return list of all plugins
     */
    List<DataPlugin> getAllPlugins();

    /**
     * Get data plugin by ID.
     *
     * @param id plugin ID
     * @return the plugin
     * @throws com.taskm.exception.DataPluginNotFoundException if plugin not found
     */
    DataPlugin getPlugin(Long id);

    /**
     * Get plugins by programming language.
     *
     * @param language programming language (python, javascript, java)
     * @return list of plugins
     */
    List<DataPlugin> getPluginsByLanguage(String language);

    /**
     * Get plugins by plugin type.
     *
     * @param pluginType plugin type (market_data, news, analytics)
     * @return list of plugins
     */
    List<DataPlugin> getPluginsByType(String pluginType);

    /**
     * Test plugin execution with provided parameters.
     *
     * @param id plugin ID
     * @param testParams test parameters for the plugin
     * @return test result (success status, data, execution time)
     */
    Map<String, Object> testPlugin(Long id, Map<String, Object> testParams);
}
