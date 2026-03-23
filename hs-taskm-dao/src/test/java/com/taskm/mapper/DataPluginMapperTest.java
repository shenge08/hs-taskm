package com.taskm.mapper;

import com.taskm.entity.DataPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DataPluginMapper.
 * Tests CRUD operations for data plugins.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataPluginMapperTest {

    @Autowired
    private DataPluginMapper dataPluginMapper;

    private DataPlugin testPlugin1;
    private DataPlugin testPlugin2;

    @BeforeEach
    void setUp() {
        // Clean up database
        dataPluginMapper.delete(null);

        // Create test data
        testPlugin1 = new DataPlugin();
        testPlugin1.setName("stock-quote-plugin");
        testPlugin1.setDescription("Get stock quotes from Yahoo Finance");
        testPlugin1.setPluginType("market_data");
        testPlugin1.setLanguage("python");
        testPlugin1.setCode("def get_stock_quote(symbol): return {'symbol': symbol, 'price': 100.0}");

        Map<String, Object> config1 = new HashMap<>();
        config1.put("name", "symbol");
        config1.put("type", "string");
        config1.put("default", "AAPL");
        config1.put("required", true);
        Map<String, Object> params1 = new HashMap<>();
        params1.put("parameters", List.of(config1));
        testPlugin1.setConfigParameters(params1);

        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("author", "John Doe");
        metadata1.put("version", "1.0");
        metadata1.put("description", "Fetch real-time stock quotes");
        testPlugin1.setMetadata(metadata1);

        testPlugin2 = new DataPlugin();
        testPlugin2.setName("news-sentiment-plugin");
        testPlugin2.setDescription("Analyze news sentiment for stocks");
        testPlugin2.setPluginType("analytics");
        testPlugin2.setLanguage("javascript");
        testPlugin2.setCode("function analyzeSentiment(symbol) { return { symbol, sentiment: 'positive' }; }");

        Map<String, Object> config2 = new HashMap<>();
        config2.put("name", "symbol");
        config2.put("type", "string");
        config2.put("required", true);
        Map<String, Object> params2 = new HashMap<>();
        params2.put("parameters", List.of(config2));
        testPlugin2.setConfigParameters(params2);

        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("author", "Jane Smith");
        metadata2.put("version", "2.0");
        metadata2.put("description", "Analyze news sentiment");
        testPlugin2.setMetadata(metadata2);
    }

    @Test
    void canInsertPlugin() {
        int result = dataPluginMapper.insert(testPlugin1);
        assertEquals(1, result);
        assertNotNull(testPlugin1.getId());
    }

    @Test
    void canSelectById() {
        dataPluginMapper.insert(testPlugin1);
        DataPlugin found = dataPluginMapper.selectById(testPlugin1.getId());
        assertNotNull(found);
        assertEquals("stock-quote-plugin", found.getName());
        assertEquals("python", found.getLanguage());
    }

    @Test
    void canSelectAll() {
        dataPluginMapper.insert(testPlugin1);
        dataPluginMapper.insert(testPlugin2);

        List<DataPlugin> plugins = dataPluginMapper.selectList(null);
        assertEquals(2, plugins.size());
    }

    @Test
    void canFilterByLanguage() {
        dataPluginMapper.insert(testPlugin1);
        dataPluginMapper.insert(testPlugin2);

        List<DataPlugin> pythonPlugins = dataPluginMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataPlugin>()
                .eq("language", "python")
        );
        assertEquals(1, pythonPlugins.size());
        assertEquals("stock-quote-plugin", pythonPlugins.get(0).getName());
    }

    @Test
    void canUpdatePlugin() {
        dataPluginMapper.insert(testPlugin1);
        testPlugin1.setDescription("Updated description");
        int result = dataPluginMapper.updateById(testPlugin1);
        assertEquals(1, result);

        DataPlugin updated = dataPluginMapper.selectById(testPlugin1.getId());
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    void canDeletePlugin() {
        dataPluginMapper.insert(testPlugin1);
        Long id = testPlugin1.getId();
        int result = dataPluginMapper.deleteById(id);
        assertEquals(1, result);

        DataPlugin deleted = dataPluginMapper.selectById(id);
        assertNull(deleted);
    }

    @Test
    void canQueryJsonbFields() {
        // Insert plugin with JSONB fields
        dataPluginMapper.insert(testPlugin1);

        // Query by ID
        DataPlugin found = dataPluginMapper.selectById(testPlugin1.getId());
        assertNotNull(found);
        assertNotNull(found.getConfigParameters(), "configParameters should not be null");
        assertNotNull(found.getMetadata(), "metadata should not be null");
        assertEquals("John Doe", found.getMetadata().get("author"));
        assertEquals("1.0", found.getMetadata().get("version"));
    }
}
