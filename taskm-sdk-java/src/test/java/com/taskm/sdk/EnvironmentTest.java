package com.taskm.sdk;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Environment utility class.
 */
class EnvironmentTest {

    private String originalTaskId;
    private String originalLogType;
    private String originalPort;
    private String originalPrice;
    private String originalEnabled;
    private String originalDisabled;
    private String originalFlag;
    private String originalTaskmApiKey;
    private String originalTaskmTimeout;

    @BeforeEach
    void setUp() {
        // Save original environment variables
        originalTaskId = System.getenv("TASK_ID");
        originalLogType = System.getenv("LOG_TYPE");
        originalPort = System.getenv("PORT");
        originalPrice = System.getenv("PRICE");
        originalEnabled = System.getenv("ENABLED");
        originalDisabled = System.getenv("DISABLED");
        originalFlag = System.getenv("FLAG");
        originalTaskmApiKey = System.getenv("TASKM_API_KEY");
        originalTaskmTimeout = System.getenv("TASKM_TIMEOUT");

        // Set test environment variables
        setEnv("TASK_ID", "123");
        setEnv("LOG_TYPE", "strategy");
        setEnv("PORT", "8080");
        setEnv("PRICE", "100.5");
        setEnv("ENABLED", "true");
        setEnv("DISABLED", "false");
        setEnv("FLAG", "1");
        setEnv("TASKM_API_KEY", "secret-key");
        setEnv("TASKM_TIMEOUT", "30");
    }

    @AfterEach
    void tearDown() {
        // Restore original environment variables
        // Note: In Java, we cannot actually unset environment variables,
        // so we just set them back to their original values
        setEnv("TASK_ID", originalTaskId);
        setEnv("LOG_TYPE", originalLogType);
        setEnv("PORT", originalPort);
        setEnv("PRICE", originalPrice);
        setEnv("ENABLED", originalEnabled);
        setEnv("DISABLED", originalDisabled);
        setEnv("FLAG", originalFlag);
        setEnv("TASKM_API_KEY", originalTaskmApiKey);
        setEnv("TASKM_TIMEOUT", originalTaskmTimeout);
    }

    // Helper method to set environment variable
    private void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            // Use reflection to modify the environment
            Class<?>[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> writableEnv = null;
            for (Class<?> cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Object field = cl.getDeclaredField("m").get(env);
                    writableEnv = (Map<String, String>) field;
                    break;
                }
            }
            if (writableEnv != null) {
                if (value == null) {
                    writableEnv.remove(key);
                } else {
                    writableEnv.put(key, value);
                }
            }
        } catch (Exception e) {
            // If reflection fails, ignore
        }
    }

    @Test
    void testGetString() {
        assertEquals("strategy", Environment.get("LOG_TYPE"));
        assertEquals("strategy", Environment.get("LOG_TYPE", "default"));
        assertEquals("default", Environment.get("NON_EXISTENT", "default"));
        assertNull(Environment.get("NON_EXISTENT"));
    }

    @Test
    void testGetInt() {
        assertEquals(123, Environment.getInt("TASK_ID", 0));
        assertEquals(8080, Environment.getInt("PORT", 0));
        assertEquals(0, Environment.getInt("NON_EXISTENT", 0));
        assertEquals(42, Environment.getInt("NON_EXISTENT", 42));
    }

    @Test
    void testGetLong() {
        assertEquals(123L, Environment.getLong("TASK_ID", 0L));
        assertEquals(8080L, Environment.getLong("PORT", 0L));
        assertEquals(999L, Environment.getLong("NON_EXISTENT", 999L));
    }

    @Test
    void testGetFloat() {
        assertEquals(100.5f, Environment.getFloat("PRICE", 0.0f), 0.001f);
        assertEquals(0.0f, Environment.getFloat("NON_EXISTENT", 0.0f), 0.001f);
        assertEquals(3.14f, Environment.getFloat("NON_EXISTENT", 3.14f), 0.001f);
    }

    @Test
    void testGetDouble() {
        assertEquals(100.5, Environment.getDouble("PRICE", 0.0), 0.001);
        assertEquals(0.0, Environment.getDouble("NON_EXISTENT", 0.0), 0.001);
        assertEquals(3.14, Environment.getDouble("NON_EXISTENT", 3.14), 0.001);
    }

    @Test
    void testGetBoolean() {
        assertTrue(Environment.getBoolean("ENABLED", false));
        assertFalse(Environment.getBoolean("DISABLED", true));
        assertTrue(Environment.getBoolean("FLAG", false));
        assertFalse(Environment.getBoolean("NON_EXISTENT", false));
        assertTrue(Environment.getBoolean("NON_EXISTENT", true));
    }

    @Test
    void testGetRequired() {
        assertEquals("strategy", Environment.getRequired("LOG_TYPE"));
        assertEquals("123", Environment.getRequired("TASK_ID"));
    }

    @Test
    void testGetRequiredThrowsWhenNotSet() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Environment.getRequired("NON_EXISTENT")
        );
        assertTrue(exception.getMessage().contains("is not set"));
    }

    @Test
    void testGetRequiredInt() {
        assertEquals(123, Environment.getRequiredInt("TASK_ID"));
        assertEquals(8080, Environment.getRequiredInt("PORT"));
    }

    @Test
    void testGetRequiredLong() {
        assertEquals(123L, Environment.getRequiredLong("TASK_ID"));
        assertEquals(8080L, Environment.getRequiredLong("PORT"));
    }

    @Test
    void testGetRequiredDouble() {
        assertEquals(100.5, Environment.getRequiredDouble("PRICE"), 0.001);
    }

    @Test
    void testGetRequiredBoolean() {
        assertTrue(Environment.getRequiredBoolean("ENABLED"));
        assertFalse(Environment.getRequiredBoolean("DISABLED"));
        assertTrue(Environment.getRequiredBoolean("FLAG"));
    }

    @Test
    void testGetAll() {
        Map<String, String> allVars = Environment.getAll();
        assertTrue(allVars.containsKey("TASK_ID"));
        assertTrue(allVars.containsKey("LOG_TYPE"));
        assertTrue(allVars.containsKey("PORT"));
    }

    @Test
    void testGetAllWithPrefix() {
        Map<String, String> taskmVars = Environment.getAll("TASKM_");
        assertTrue(taskmVars.containsKey("TASKM_API_KEY"));
        assertTrue(taskmVars.containsKey("TASKM_TIMEOUT"));
        assertFalse(taskmVars.containsKey("LOG_TYPE"));
        assertFalse(taskmVars.containsKey("PORT"));
    }

    @Test
    void testExists() {
        assertTrue(Environment.exists("TASK_ID"));
        assertTrue(Environment.exists("LOG_TYPE"));
        assertFalse(Environment.exists("NON_EXISTENT"));
    }

    @Test
    void testExistsAny() {
        assertTrue(Environment.existsAny("TASK_ID", "NON_EXISTENT"));
        assertTrue(Environment.existsAny("NON_EXISTENT", "LOG_TYPE"));
        assertFalse(Environment.existsAny("NON_EXISTENT", "ANOTHER_NON_EXISTENT"));
        assertFalse(Environment.existsAny());
    }

    @Test
    void testExistsAll() {
        assertTrue(Environment.existsAll("TASK_ID", "LOG_TYPE"));
        assertFalse(Environment.existsAll("TASK_ID", "NON_EXISTENT"));
        assertFalse(Environment.existsAll("NON_EXISTENT"));
        assertTrue(Environment.existsAll());
    }

    @Test
    void testGetList() {
        setEnv("COMMA_SEPARATED", "a,b,c");
        var list = Environment.getList("COMMA_SEPARATED", ",");
        assertEquals(3, list.size());
        assertTrue(list.contains("a"));
        assertTrue(list.contains("b"));
        assertTrue(list.contains("c"));
    }

    @Test
    void testGetCommaSeparatedList() {
        setEnv("COMMA_SEPARATED", "x,y,z");
        var list = Environment.getCommaSeparatedList("COMMA_SEPARATED");
        assertEquals(3, list.size());
        assertTrue(list.contains("x"));
        assertTrue(list.contains("y"));
        assertTrue(list.contains("z"));
    }

    @Test
    void testInvalidIntConversion() {
        assertThrows(IllegalArgumentException.class,
            () -> Environment.getInt("LOG_TYPE", 0));
    }

    @Test
    void testInvalidLongConversion() {
        assertThrows(IllegalArgumentException.class,
            () -> Environment.getLong("LOG_TYPE", 0L));
    }

    @Test
    void testInvalidDoubleConversion() {
        assertThrows(IllegalArgumentException.class,
            () -> Environment.getDouble("LOG_TYPE", 0.0));
    }

    @Test
    void testInvalidBooleanConversion() {
        assertThrows(IllegalArgumentException.class,
            () -> Environment.getBoolean("LOG_TYPE", false));
    }

    @Test
    void testBooleanVariousFormats() {
        // Test true values
        String[] trueValues = {"true", "True", "TRUE", "1", "yes", "YES", "on", "ON"};
        for (String value : trueValues) {
            setEnv("TEST_BOOL", value);
            assertTrue(Environment.getBoolean("TEST_BOOL", false),
                "Failed for value: " + value);
        }

        // Test false values
        String[] falseValues = {"false", "False", "FALSE", "0", "no", "NO", "off", "OFF", ""};
        for (String value : falseValues) {
            setEnv("TEST_BOOL", value);
            assertFalse(Environment.getBoolean("TEST_BOOL", true),
                "Failed for value: " + value);
        }
    }

    @Test
    void testUtilityClassCannotBeInstantiated() {
        assertThrows(UnsupportedOperationException.class,
            () -> {
                // Use reflection to bypass private constructor check
                var constructor = Environment.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            });
    }
}
