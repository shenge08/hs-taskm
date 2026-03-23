package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.entity.DataPlugin;
import com.taskm.service.DataPluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for DataPlugin management.
 * Provides endpoints for querying and testing data plugins.
 */
@RestController
@RequestMapping("/api/plugins")
public class DataPluginController {

    private final DataPluginService dataPluginService;

    @Autowired
    public DataPluginController(DataPluginService dataPluginService) {
        this.dataPluginService = dataPluginService;
    }

    /**
     * Get all data plugins.
     * Optionally filter by programming language or plugin type.
     *
     * @param language optional language filter
     * @param pluginType optional plugin type filter
     * @return list of plugins
     */
    @GetMapping
    public Result<List<DataPlugin>> getAllPlugins(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String pluginType) {

        if (language != null && !language.isEmpty()) {
            List<DataPlugin> plugins = dataPluginService.getPluginsByLanguage(language);
            return Result.success("Plugins filtered by language: " + language, plugins);
        }

        if (pluginType != null && !pluginType.isEmpty()) {
            List<DataPlugin> plugins = dataPluginService.getPluginsByType(pluginType);
            return Result.success("Plugins filtered by type: " + pluginType, plugins);
        }

        List<DataPlugin> plugins = dataPluginService.getAllPlugins();
        return Result.success(plugins);
    }

    /**
     * Get plugin by ID.
     *
     * @param id plugin ID
     * @return plugin details
     */
    @GetMapping("/{id}")
    public Result<DataPlugin> getPlugin(@PathVariable Long id) {
        DataPlugin plugin = dataPluginService.getPlugin(id);
        return Result.success(plugin);
    }

    /**
     * Test plugin execution with provided parameters.
     *
     * @param id plugin ID
     * @param testParams test parameters for the plugin
     * @return test result (success status, data, execution time)
     */
    @PostMapping("/{id}/test")
    public Result<Map<String, Object>> testPlugin(
            @PathVariable Long id,
            @RequestBody Map<String, Object> testParams) {

        Map<String, Object> result = dataPluginService.testPlugin(id, testParams);
        return Result.success("Plugin test completed", result);
    }
}
