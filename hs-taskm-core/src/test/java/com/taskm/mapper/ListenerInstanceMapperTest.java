package com.taskm.mapper;

import com.taskm.entity.ListenerInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for ListenerInstanceMapper.
 * Tests CRUD operations for listener instances.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ListenerInstanceMapperTest {

    @Autowired
    private ListenerInstanceMapper mapper;

    private ListenerInstance testInstance;

    @BeforeEach
    void setUp() {
        // Clean up database
        mapper.delete(null);

        // Create test data
        testInstance = new ListenerInstance();
        testInstance.setListenerId(1L);
        testInstance.setName("test-listener-instance");
        testInstance.setIsDefault(false);

        Map<String, Object> config = new HashMap<>();
        config.put("webhookUrl", "https://example.com/webhook");
        config.put("retryCount", 3);
        testInstance.setConfig(config);
    }

    @Test
    void mapperIsProperlyInjected() {
        assertThat(mapper).isNotNull();
    }

    @Test
    void canInsertAndQueryById() {
        // When - Insert
        int insertResult = mapper.insert(testInstance);
        assertThat(insertResult).isGreaterThan(0);
        assertThat(testInstance.getId()).isNotNull();

        // Then - Query by ID
        ListenerInstance result = mapper.selectById(testInstance.getId());
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("test-listener-instance");
        assertThat(result.getListenerId()).isEqualTo(1L);
        assertThat(result.getConfig()).isNotNull();
        assertThat(result.getConfig().get("webhookUrl")).isEqualTo("https://example.com/webhook");
    }

    @Test
    void canUpdateInstance() {
        // Given - Insert an instance first
        mapper.insert(testInstance);

        // When - Update config
        Map<String, Object> newConfig = new HashMap<>();
        newConfig.put("webhookUrl", "https://updated.com/webhook");
        newConfig.put("retryCount", 5);
        testInstance.setConfig(newConfig);
        testInstance.setStatus("RUNNING");

        int updateResult = mapper.updateById(testInstance);
        assertThat(updateResult).isGreaterThan(0);

        // Then - Verify update
        ListenerInstance result = mapper.selectById(testInstance.getId());
        assertThat(result.getConfig().get("webhookUrl")).isEqualTo("https://updated.com/webhook");
        assertThat(result.getConfig().get("retryCount")).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo("RUNNING");
    }

    @Test
    void canDeleteInstance() {
        // Given - Insert an instance
        mapper.insert(testInstance);
        Long id = testInstance.getId();

        // When - Delete
        int deleteResult = mapper.deleteById(id);
        assertThat(deleteResult).isGreaterThan(0);

        // Then - Verify deletion
        ListenerInstance deleted = mapper.selectById(id);
        assertThat(deleted).isNull();
    }

    @Test
    void canQueryByListenerId() {
        // Given - Insert multiple instances for the same listener
        ListenerInstance instance1 = new ListenerInstance();
        instance1.setListenerId(1L);
        instance1.setName("instance-1");
        instance1.setIsDefault(true);
        instance1.setConfig(new HashMap<>());
        mapper.insert(instance1);

        ListenerInstance instance2 = new ListenerInstance();
        instance2.setListenerId(1L);
        instance2.setName("instance-2");
        instance2.setIsDefault(false);
        instance2.setConfig(new HashMap<>());
        mapper.insert(instance2);

        // When - Query by listenerId
        List<ListenerInstance> results = mapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ListenerInstance>()
                .eq("listener_id", 1L)
        );

        // Then - Verify results
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getListenerId()).isEqualTo(1L);
        assertThat(results.get(1).getListenerId()).isEqualTo(1L);
    }

    @Test
    void canQueryDefaultInstance() {
        // Given - Insert default and non-default instances
        ListenerInstance defaultInstance = new ListenerInstance();
        defaultInstance.setListenerId(1L);
        defaultInstance.setName("default-instance");
        defaultInstance.setIsDefault(true);
        defaultInstance.setConfig(new HashMap<>());
        mapper.insert(defaultInstance);

        ListenerInstance nonDefaultInstance = new ListenerInstance();
        nonDefaultInstance.setListenerId(1L);
        nonDefaultInstance.setName("non-default-instance");
        nonDefaultInstance.setIsDefault(false);
        nonDefaultInstance.setConfig(new HashMap<>());
        mapper.insert(nonDefaultInstance);

        // When - Query for default instance
        List<ListenerInstance> results = mapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ListenerInstance>()
                .eq("listener_id", 1L)
                .eq("is_default", true)
        );

        // Then - Verify only default instance is returned
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("default-instance");
        assertThat(results.get(0).getIsDefault()).isTrue();
    }
}
