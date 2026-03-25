-- 插入示例数据插件记录
-- 用于演示 TaskM 数据插件功能

INSERT INTO data_plugin (
    name,
    description,
    plugin_type,
    language,
    code,
    config_parameters,
    metadata,
    image_name,
    docker_image_id,
    created_at,
    updated_at
) VALUES (
    '期货市场数据插件',
    '获取期货实时行情数据，包括买价、卖价、最新价',
    'SOURCE',
    'java',
    '',
    '[{"key":"symbol","name":"标的代码","type":"string","required":true,"description":"标的代码","defaultValue":""}]'::jsonb,
    '[{"key":"api_url","name":"url地址","type":"string","required":false,"description":"url地址","defaultValue":"http://172.16.90.115:8899/wdd/getBondInfoCompleteCommand"},{"key":"SERVER_PORT","name":"服务端口","type":"string","required":false,"description":"服务端口","defaultValue":"11800"}]'::jsonb,
    'taskm-plugin-future:1.0.0',
    'e1cd34d2397448f93782494ff9bbcdff3e1bad0ed3f36d41b86f655701d49019',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (name) DO UPDATE SET
    config_parameters = EXCLUDED.config_parameters,
    metadata = EXCLUDED.metadata,
    image_name = EXCLUDED.image_name,
    docker_image_id = EXCLUDED.docker_image_id,
    updated_at = CURRENT_TIMESTAMP;

-- 查询插入的记录
SELECT
    id,
    name,
    description,
    plugin_type,
    language,
    docker_image_id,
    created_at
FROM data_plugin
WHERE name = '期货市场数据插件';
