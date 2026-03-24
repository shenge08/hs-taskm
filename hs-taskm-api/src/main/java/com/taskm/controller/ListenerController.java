package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.entity.Listener;
import com.taskm.service.ListenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监听器管理 REST Controller
 *
 * <p>提供监听器的查询 REST API 接口
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/listeners")
@Tag(name = "监听器管理", description = "事件监听器的查询和管理接口")
public class ListenerController {

    private final ListenerService listenerService;

    @Autowired
    public ListenerController(ListenerService listenerService) {
        this.listenerService = listenerService;
    }

    /**
     * 获取所有监听器列表
     *
     * <p>支持按编程语言或事件类型过滤
     *
     * @param language 编程语言过滤（可选）
     * @param eventType 事件类型过滤（可选）
     * @return 监听器列表
     */
    @GetMapping
    @Operation(summary = "获取监听器列表", description = "获取所有事件监听器，支持按编程语言或事件类型过滤")
    @Parameter(name = "language", description = "编程语言，如 python、javascript、java", required = false)
    @Parameter(name = "eventType", description = "事件类型，如 order_filled、price_alert", required = false)
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<List<Listener>> getAllListeners(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String eventType) {

        if (language != null && !language.isEmpty()) {
            List<Listener> listeners = listenerService.getListenersByLanguage(language);
            return Result.success("Listeners filtered by language: " + language, listeners);
        }

        if (eventType != null && !eventType.isEmpty()) {
            List<Listener> listeners = listenerService.getListenersByEventType(eventType);
            return Result.success("Listeners filtered by event type: " + eventType, listeners);
        }

        List<Listener> listeners = listenerService.getAllListeners();
        return Result.success(listeners);
    }

    /**
     * 根据 ID 获取监听器详情
     *
     * @param id 监听器 ID
     * @return 监听器详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取监听器详情", description = "根据监听器 ID 查询监听器的详细信息")
    @Parameter(name = "id", description = "监听器 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "监听器不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Listener> getListener(@PathVariable Long id) {
        Listener listener = listenerService.getListener(id);
        return Result.success(listener);
    }
}
