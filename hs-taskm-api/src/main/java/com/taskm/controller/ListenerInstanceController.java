package com.taskm.controller;

import com.taskm.dto.CreateListenerInstanceDTO;
import com.taskm.dto.ListenerInstanceVO;
import com.taskm.dto.Result;
import com.taskm.dto.UpdateListenerInstanceDTO;
import com.taskm.service.ListenerInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监听器实例管理 REST Controller
 *
 * <p>提供监听器实例的增删改查 REST API 接口
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/listeners/{listenerId}/instances")
@Tag(name = "监听器实例管理", description = "监听器实例的创建、查询、更新和删除接口")
public class ListenerInstanceController {

    private final ListenerInstanceService listenerInstanceService;

    @Autowired
    public ListenerInstanceController(ListenerInstanceService listenerInstanceService) {
        this.listenerInstanceService = listenerInstanceService;
    }

    /**
     * 创建监听器实例
     *
     * @param listenerId 监听器 ID
     * @param dto 创建请求
     * @return 创建的实例
     */
    @PostMapping
    @Operation(summary = "创建监听器实例", description = "为指定监听器创建一个新的实例配置")
    @Parameter(name = "listenerId", description = "监听器 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "监听器不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ListenerInstanceVO> createInstance(
            @PathVariable Long listenerId,
            @RequestBody @Valid CreateListenerInstanceDTO dto) {

        ListenerInstanceVO instance = listenerInstanceService.createInstance(listenerId, dto);
        return Result.success("Listener instance created successfully", instance);
    }

    /**
     * 获取监听器的所有实例
     *
     * @param listenerId 监听器 ID
     * @return 实例列表
     */
    @GetMapping
    @Operation(summary = "获取监听器实例列表", description = "获取指定监听器的所有实例配置")
    @Parameter(name = "listenerId", description = "监听器 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<ListenerInstanceVO>> getInstances(@PathVariable Long listenerId) {
        List<ListenerInstanceVO> instances = listenerInstanceService.getInstancesByListenerId(listenerId);
        return Result.success(instances);
    }

    /**
     * 根据 ID 获取实例详情
     *
     * @param listenerId 监听器 ID
     * @param instanceId 实例 ID
     * @return 实例详细信息
     */
    @GetMapping("/{instanceId}")
    @Operation(summary = "获取实例详情", description = "根据实例 ID 查询实例的详细信息")
    @Parameter(name = "listenerId", description = "监听器 ID", required = true, example = "1")
    @Parameter(name = "instanceId", description = "实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ListenerInstanceVO> getInstance(
            @PathVariable Long listenerId,
            @PathVariable Long instanceId) {

        ListenerInstanceVO instance = listenerInstanceService.getInstanceById(instanceId);
        return Result.success(instance);
    }

    /**
     * 更新监听器实例
     *
     * @param listenerId 监听器 ID
     * @param instanceId 实例 ID
     * @param dto 更新请求
     * @return 更新后的实例
     */
    @PutMapping("/{instanceId}")
    @Operation(summary = "更新监听器实例", description = "更新指定监听器实例的配置信息")
    @Parameter(name = "listenerId", description = "监听器 ID", required = true, example = "1")
    @Parameter(name = "instanceId", description = "实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<ListenerInstanceVO> updateInstance(
            @PathVariable Long listenerId,
            @PathVariable Long instanceId,
            @RequestBody @Valid UpdateListenerInstanceDTO dto) {

        ListenerInstanceVO instance = listenerInstanceService.updateInstance(instanceId, dto);
        return Result.success("Listener instance updated successfully", instance);
    }

    /**
     * 删除监听器实例
     *
     * @param listenerId 监听器 ID
     * @param instanceId 实例 ID
     * @return 成功消息
     */
    @DeleteMapping("/{instanceId}")
    @Operation(summary = "删除监听器实例", description = "删除指定的监听器实例配置")
    @Parameter(name = "listenerId", description = "监听器 ID", required = true, example = "1")
    @Parameter(name = "instanceId", description = "实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> deleteInstance(
            @PathVariable Long listenerId,
            @PathVariable Long instanceId) {

        listenerInstanceService.deleteInstance(instanceId);
        return Result.success("Listener instance deleted successfully");
    }

    /**
     * 设置默认实例
     *
     * @param listenerId 监听器 ID
     * @param instanceId 实例 ID
     * @return 成功消息
     */
    @PostMapping("/{instanceId}/setDefault")
    @Operation(summary = "设置默认实例", description = "将指定实例设置为监听器的默认实例")
    @Parameter(name = "listenerId", description = "监听器 ID", required = true, example = "1")
    @Parameter(name = "instanceId", description = "实例 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "设置成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "实例不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> setDefaultInstance(
            @PathVariable Long listenerId,
            @PathVariable Long instanceId) {

        listenerInstanceService.setDefaultInstance(instanceId);
        return Result.success("Default instance set successfully");
    }
}
