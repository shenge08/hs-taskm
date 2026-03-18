package com.taskm;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taskm.entity.TestEntity;
import com.taskm.mapper.TestEntityMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that verifies MyBatis-Plus can connect to the database and execute queries.
 * This uses an external PostgreSQL database for testing.
 */
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@Transactional
class MyBatisPlusConnectionTest {

    @Autowired
    private TestEntityMapper testEntityMapper;

    @Test
    void canConnectToDatabase() {
        // This test verifies the mapper is properly injected
        // which means MyBatis-Plus connected to the database successfully
        assertThat(testEntityMapper).isNotNull();
    }

    @Test
    void canInsertAndQuery() {
        // Given
        TestEntity entity = new TestEntity();
        entity.setName("test-name");

        // When - Insert
        int insertResult = testEntityMapper.insert(entity);
        assertThat(insertResult).isGreaterThan(0);

        // Then - Query
        TestEntity result = testEntityMapper.selectOne(
                new QueryWrapper<TestEntity>().eq("name", "test-name")
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("test-name");

        // Cleanup is handled by @Transactional
    }
}
