package com.taskm.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taskm.entity.DataPluginInstance;
import com.taskm.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that verifies DataPluginInstanceMapper CRUD operations.
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
class DataPluginInstanceMapperTest {

    @Autowired
    private DataPluginInstanceMapper mapper;

    @Test
    void mapperIsProperlyInjected() {
        // Then: Mapper should be injected
        assertThat(mapper).isNotNull();
    }

    @Test
    void canInsertAndQueryById() {
        // Given
        DataPluginInstance instance = new DataPluginInstance();
        instance.setPluginId(1L);
        instance.setName("test-instance");
        instance.setIsDefault(false);

        Map<String, Object> config = new HashMap<>();
        config.put("apiKey", "test-key");
        config.put("timeout", 30);
        instance.setConfig(config);

        instance.setStatus("STOPPED");
        instance.setCreatedAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());

        // When - Insert
        int insertResult = mapper.insert(instance);
        assertThat(insertResult).isGreaterThan(0);

        // Then - Query by ID
        DataPluginInstance result = mapper.selectById(instance.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getPluginId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("test-instance");
        assertThat(result.getIsDefault()).isFalse();
        assertThat(result.getConfig()).isNotNull();
        assertThat(result.getConfig().get("apiKey")).isEqualTo("test-key");
        assertThat(result.getConfig().get("timeout")).isEqualTo(30);
        assertThat(result.getStatus()).isEqualTo("STOPPED");
    }

    @Test
    void canUpdateInstance() {
        // Given - Insert an instance
        DataPluginInstance instance = new DataPluginInstance();
        instance.setPluginId(1L);
        instance.setName("test-instance");
        instance.setConfig(new HashMap<>());
        instance.setStatus("STOPPED");
        mapper.insert(instance);

        // When - Update
        instance.setIsDefault(true);
        instance.setStatus("RUNNING");
        instance.setContainerId("container-123");

        Map<String, Object> newConfig = new HashMap<>();
        newConfig.put("apiKey", "new-key");
        instance.setConfig(newConfig);

        int updateResult = mapper.updateById(instance);
        assertThat(updateResult).isGreaterThan(0);

        // Then - Verify update
        DataPluginInstance result = mapper.selectById(instance.getId());
        assertThat(result.getIsDefault()).isTrue();
        assertThat(result.getStatus()).isEqualTo("RUNNING");
        assertThat(result.getContainerId()).isEqualTo("container-123");
        assertThat(result.getConfig().get("apiKey")).isEqualTo("new-key");
    }

    @Test
    void canDeleteInstance() {
        // Given - Insert an instance
        DataPluginInstance instance = new DataPluginInstance();
        instance.setPluginId(1L);
        instance.setName("test-instance");
        instance.setConfig(new HashMap<>());
        instance.setStatus("STOPPED");
        mapper.insert(instance);
        Long instanceId = instance.getId();

        // When - Delete
        int deleteResult = mapper.deleteById(instanceId);
        assertThat(deleteResult).isGreaterThan(0);

        // Then - Verify deletion
        DataPluginInstance result = mapper.selectById(instanceId);
        assertThat(result).isNull();
    }

    @Test
    void canQueryByPluginId() {
        // Given - Insert two instances for the same plugin
        DataPluginInstance instance1 = new DataPluginInstance();
        instance1.setPluginId(10L);
        instance1.setName("instance-1");
        instance1.setConfig(new HashMap<>());
        instance1.setStatus("RUNNING");
        mapper.insert(instance1);

        DataPluginInstance instance2 = new DataPluginInstance();
        instance2.setPluginId(10L);
        instance2.setName("instance-2");
        instance2.setConfig(new HashMap<>());
        instance2.setStatus("STOPPED");
        mapper.insert(instance2);

        // When - Query by plugin_id
        QueryWrapper<DataPluginInstance> wrapper = new QueryWrapper<>();
        wrapper.eq("plugin_id", 10L);

        java.util.List<DataPluginInstance> results = mapper.selectList(wrapper);

        // Then - Should find both instances
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getPluginId()).isEqualTo(10L);
        assertThat(results.get(1).getPluginId()).isEqualTo(10L);
    }

    @Test
    void canQueryDefaultInstance() {
        // Given - Insert multiple instances, one is default
        DataPluginInstance instance1 = new DataPluginInstance();
        instance1.setPluginId(20L);
        instance1.setName("instance-1");
        instance1.setIsDefault(false);
        instance1.setConfig(new HashMap<>());
        instance1.setStatus("RUNNING");
        mapper.insert(instance1);

        DataPluginInstance instance2 = new DataPluginInstance();
        instance2.setPluginId(20L);
        instance2.setName("instance-2");
        instance2.setIsDefault(true);
        instance2.setConfig(new HashMap<>());
        instance2.setStatus("RUNNING");
        mapper.insert(instance2);

        // When - Query default instance
        QueryWrapper<DataPluginInstance> wrapper = new QueryWrapper<>();
        wrapper.eq("plugin_id", 20L);
        wrapper.eq("is_default", true);

        DataPluginInstance result = mapper.selectOne(wrapper);

        // Then - Should find the default instance
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("instance-2");
        assertThat(result.getIsDefault()).isTrue();
    }
}
