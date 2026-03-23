package com.taskm.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Custom TypeHandler for JSON fields.
 * Converts between Java Map<String, Object> and PostgreSQL JSONB.
 */
@MappedTypes(Map.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.NULL})
public class JsonTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            // Convert Map to JSON string
            String jsonString = objectMapper.writeValueAsString(parameter);
            // Use setString with Types.OTHER for PostgreSQL JSONB
            ps.setObject(i, jsonString, java.sql.Types.OTHER);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting Map to JSON", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(getJsonObject(rs.getObject(columnName)));
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(getJsonObject(rs.getObject(columnIndex)));
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(getJsonObject(cs.getObject(columnIndex)));
    }

    /**
     * Extract JSON string from PostgreSQL PGobject or regular object.
     */
    private String getJsonObject(Object obj) throws SQLException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof PGobject) {
            PGobject pGobject = (PGobject) obj;
            return pGobject.getValue();
        }
        return obj.toString();
    }

    private Map<String, Object> parseJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to Map", e);
        }
    }
}
