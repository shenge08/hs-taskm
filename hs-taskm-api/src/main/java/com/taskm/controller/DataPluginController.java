package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.entity.DataPlugin;
import com.taskm.service.DataPluginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据插件管理 REST Controller
 *
 * <p>提供数据插件的查询和测试 REST API 接口
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/plugins")
@Tag(name = "插件管理", description = "数据插件的查询、测试和管理接口")
public class DataPluginController {

    private final DataPluginService dataPluginService;

    @Autowired
    public DataPluginController(DataPluginService dataPluginService) {
        this.dataPluginService = dataPluginService;
    }

    /**
     * 获取所有数据插件列表
     *
     * <p>支持按编程语言或插件类型过滤
     *
     * @param language 编程语言过滤（可选）
     * @param pluginType 插件类型过滤（可选）
     * @return 插件列表
     */
    @GetMapping
    @Operation(summary = "获取插件列表", description = "获取所有数据插件，支持按编程语言或插件类型过滤")
    @Parameter(name = "language", description = "编程语言，如 python、javascript、java", required = false)
    @Parameter(name = "pluginType", description = "插件类型，如 market_data、indicator", required = false)
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
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
     * 根据 ID 获取插件详情
     *
     * @param id 插件 ID
     * @return 插件详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取插件详情", description = "根据插件 ID 查询插件的详细信息")
    @Parameter(name = "id", description = "插件 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "插件不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<DataPlugin> getPlugin(@PathVariable Long id) {
        DataPlugin plugin = dataPluginService.getPlugin(id);
        return Result.success(plugin);
    }

    /**
     * 测试插件执行
     *
     * @param id 插件 ID
     * @param testParams 测试参数
     * @return 测试结果（成功状态、数据、执行时间）
     */
    @PostMapping("/{id}/test")
    @Operation(summary = "测试插件", description = "使用提供的参数测试插件的执行，返回执行结果和性能数据")
    @Parameter(name = "id", description = "插件 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "测试完成"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误或插件执行失败"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "插件不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Map<String, Object>> testPlugin(
            @PathVariable Long id,
            @RequestBody Map<String, Object> testParams) {

        Map<String, Object> result = dataPluginService.testPlugin(id, testParams);
        return Result.success("Plugin test completed", result);
    }
}
