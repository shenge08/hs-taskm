package com.taskm.controller;

import com.taskm.dto.CreatePluginInstanceDTO;
import com.taskm.dto.PluginInstanceVO;
import com.taskm.dto.Result;
import com.taskm.dto.UpdatePluginInstanceDTO;
import com.taskm.service.PluginInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 插件实例管理 REST Controller
 *
 * <p>提供插件实例的增删改查 REST API 接口
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/plugins/{pluginId}/instances")
@Tag(name = "插件实例管理", description = "插件实例的创建、查询、更新和删除接口")
public class PluginInstanceController {

    private final PluginInstanceService pluginInstanceService;

    @Autowired
    public PluginInstanceController(PluginInstanceService pluginInstanceService) {
        this.pluginInstanceService = pluginInstanceService;
    }

    /**
     * 创建插件实例
     *
     * @param pluginId 插件 ID
     * @param dto 创建请求
     * @return 创建的实例
     */
    @PostMapping
    @Operation(summary = "创建插件实例", description = "为指定插件创建一个新的实例配置")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "插件不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<PluginInstanceVO> createInstance(
            @PathVariable Long pluginId,
            @RequestBody @Valid CreatePluginInstanceDTO dto) {

        PluginInstanceVO instance = pluginInstanceService.createInstance(pluginId, dto);
        return Result.success("Plugin instance created successfully", instance);
    }

    /**
     * 获取插件的所有实例
     *
     * @param pluginId 插件 ID
     * @return 实例列表
     */
    @GetMapping
    @Operation(summary = "获取插件实例列表", description = "获取指定插件的所有实例配置")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<PluginInstanceVO>> getInstances(@PathVariable Long pluginId) {
        List<PluginInstanceVO> instances = pluginInstanceService.getInstancesByPluginId(pluginId);
        return Result.success(instances);
    }

    /**
     * 根据 ID 获取实例详情
     *
     * @param pluginId 插件 ID
     * @param instanceId 实例 ID
     * @return 实例详细信息
     */
    @GetMapping("/{instanceId}")
    @Operation(summary = "获取实例详情", description = "根据实例 ID 查询实例的详细信息")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @Parameter(name = "instanceId", description = "实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<PluginInstanceVO> getInstance(
            @PathVariable Long pluginId,
            @PathVariable Long instanceId) {

        PluginInstanceVO instance = pluginInstanceService.getInstanceById(instanceId);
        return Result.success(instance);
    }

    /**
     * 更新插件实例
     *
     * @param pluginId 插件 ID
     * @param instanceId 实例 ID
     * @param dto 更新请求
     * @return 更新后的实例
     */
    @PutMapping("/{instanceId}")
    @Operation(summary = "更新插件实例", description = "更新指定插件实例的配置信息")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @Parameter(name = "instanceId", description = "实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<PluginInstanceVO> updateInstance(
            @PathVariable Long pluginId,
            @PathVariable Long instanceId,
            @RequestBody @Valid UpdatePluginInstanceDTO dto) {

        PluginInstanceVO instance = pluginInstanceService.updateInstance(instanceId, dto);
        return Result.success("Plugin instance updated successfully", instance);
    }

    /**
     * 删除插件实例
     *
     * @param pluginId 插件 ID
     * @param instanceId 实例 ID
     * @return 成功消息
     */
    @DeleteMapping("/{instanceId}")
    @Operation(summary = "删除插件实例", description = "删除指定的插件实例配置")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @Parameter(name = "instanceId", description = "实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> deleteInstance(
            @PathVariable Long pluginId,
            @PathVariable Long instanceId) {

        pluginInstanceService.deleteInstance(instanceId);
        return Result.success("Plugin instance deleted successfully");
    }

    /**
     * 设置默认实例
     *
     * @param pluginId 插件 ID
     * @param instanceId 实例 ID
     * @return 成功消息
     */
    @PostMapping("/{instanceId}/setDefault")
    @Operation(summary = "设置默认实例", description = "将指定实例设置为插件的默认实例")
    @Parameter(name = "pluginId", description = "插件 ID", required = true, example = "1")
    @Parameter(name = "instanceId", description = "实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "设置成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> setDefaultInstance(
            @PathVariable Long pluginId,
            @PathVariable Long instanceId) {

        pluginInstanceService.setDefaultInstance(instanceId);
        return Result.success("Default instance set successfully");
    }
}
