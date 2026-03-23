# HS-TASKM 部署文档

## 概述

本文档详细说明 HS-TASKM 多语言策略容器执行平台的部署流程，包括环境准备、依赖安装、配置说明、构建部署和验证测试。

## 目录

- [环境要求](#环境要求)
- [依赖安装](#依赖安装)
- [配置说明](#配置说明)
- [构建流程](#构建流程)
- [部署步骤](#部署步骤)
- [验证测试](#验证测试)
- [常见问题](#常见问题)
- [运维指南](#运维指南)
- [备份恢复](#备份恢复)

---

## 环境要求

### 硬件要求

| 资源 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2 核 | 4 核+ |
| 内存 | 4 GB | 8 GB+ |
| 磁盘 | 20 GB | 50 GB+ (SSD 推荐) |
| 网络 | 100 Mbps | 1 Gbps |

### 软件要求

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| 操作系统 | Linux (Ubuntu 20.04+, CentOS 7+) | 需要支持 Docker |
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | 2.0+ | 多容器编排（可选） |
| JDK | 17+ | Java 运行环境 |
| Maven | 3.8+ | Java 构建工具 |
| PostgreSQL | 15+ | 数据库 |
| Python | 3.9+ | Python SDK 构建（可选） |
| Git | 2.0+ | 版本控制 |

---

## 依赖安装

### 1. 安装 Docker

#### Ubuntu/Debian

```bash
# 更新包索引
sudo apt-get update

# 安装依赖
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# 添加 Docker 官方 GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# 设置仓库
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker Engine
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 验证安装
sudo docker run hello-world
```

#### CentOS/RHEL

```bash
# 安装依赖
sudo yum install -y yum-utils

# 添加 Docker 仓库
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 安装 Docker
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 验证安装
sudo docker run hello-world
```

### 2. 安装 JDK 17

#### Ubuntu/Debian

```bash
# 安装 OpenJDK 17
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk

# 验证安装
java -version

# 设置 JAVA_HOME（可选）
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' | sudo tee -a /etc/profile
source /etc/profile
```

#### CentOS/RHEL

```bash
# 安装 OpenJDK 17
sudo yum install -y java-17-openjdk-devel

# 验证安装
java -version

# 设置 JAVA_HOME（可选）
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk' | sudo tee -a /etc/profile
source /etc/profile
```

### 3. 安装 Maven

#### Ubuntu/Debian

```bash
sudo apt-get update
sudo apt-get install -y maven

# 验证安装
mvn -version
```

#### CentOS/RHEL

```bash
sudo yum install -y maven

# 验证安装
mvn -version
```

### 4. 安装 PostgreSQL 15

#### 使用官方仓库

```bash
# Ubuntu/Debian
sudo apt-get install -y postgresql-15 postgresql-contrib-15

# CentOS/RHEL
sudo yum install -y postgresql15-server postgresql15-contrib
sudo /usr/pgsql-15/bin/postgresql-15-setup initdb
sudo systemctl start postgresql-15
sudo systemctl enable postgresql-15
```

#### 使用 Docker（推荐）

```bash
# 拉取 PostgreSQL 镜像
docker pull postgres:15

# 创建数据卷
docker volume create pgdata

# 启动 PostgreSQL 容器
docker run -d \
  --name postgres \
  --restart=always \
  -e POSTGRES_PASSWORD=taskm123 \
  -e POSTGRES_USER=taskm \
  -e POSTGRES_DB=taskm \
  -v pgdata:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:15

# 验证运行
docker ps | grep postgres
```

### 5. 安装 Git

```bash
# Ubuntu/Debian
sudo apt-get install -y git

# CentOS/RHEL
sudo yum install -y git

# 验证安装
git --version
```

---

## 配置说明

### 1. 数据库配置

#### 创建数据库和用户

```sql
-- 连接到 PostgreSQL
psql -U postgres

-- 创建数据库
CREATE DATABASE taskm;

-- 创建用户
CREATE USER taskm WITH PASSWORD 'taskm123';

-- 授权
GRANT ALL PRIVILEGES ON DATABASE taskm TO taskm;

-- 退出
\q
```

### 2. 应用配置

创建配置文件 `application-prod.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskm
    username: taskm
    password: taskm123
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

docker:
  default:
    image: python:3.9-slim

monitoring:
  default:
    interval: 30

logs:
  base_dir: /var/log/taskm
```

### 3. Docker 配置

配置 Docker daemon (`/etc/docker/daemon.json`):

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "storage-driver": "overlay2",
  "data-root": "/var/lib/docker",
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com"
  ]
}
```

重启 Docker:

```bash
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 4. 日志目录配置

创建日志目录并设置权限：

```bash
# 创建日志目录
sudo mkdir -p /var/log/taskm/tasks

# 设置权限
sudo chown -R $USER:$USER /var/log/taskm
sudo chmod -R 755 /var/log/taskm
```

---

## 构建流程

### 1. 克隆代码

```bash
# 克隆仓库
git clone https://github.com/your-org/hs-taskm.git
cd hs-taskm

# 查看分支
git branch -a

# 切换到主分支
git checkout main
```

### 2. 编译打包

```bash
# 清理旧的构建
mvn clean

# 编译并跳过测试（快速构建）
mvn clean package -DskipTests

# 完整构建（包含测试）
mvn clean package

# 查看构建产物
ls -lh hs-taskm-api/target/*.jar
```

### 3. 构建 Docker 镜像（可选）

```bash
# 构建 API 服务镜像
cd hs-taskm-api
docker build -t hs-taskm-api:latest .
cd ..

# 查看镜像
docker images | grep hs-taskm
```

---

## 部署步骤

### 方式一：直接运行 JAR

#### 1. 创建运行目录

```bash
sudo mkdir -p /opt/hs-taskm
sudo mkdir -p /var/log/taskm
sudo chown -R $USER:$USER /opt/hs-taskm
```

#### 2. 复制文件

```bash
# 复制 JAR 文件
cp hs-taskm-api/target/hs-taskm-api-*.jar /opt/hs-taskm/

# 复制配置文件
cp hs-taskm-api/src/main/resources/application-prod.yml /opt/hs-taskm/

# 创建启动脚本
cat > /opt/hs-taskm/start.sh << 'EOF'
#!/bin/bash
cd /opt/hs-taskm
java -jar \
  -Xms512m \
  -Xmx2g \
  -Dspring.profiles.active=prod \
  -Dserver.port=8080 \
  hs-taskm-api-*.jar
EOF

chmod +x /opt/hs-taskm/start.sh
```

#### 3. 创建 Systemd 服务

```bash
sudo tee /etc/systemd/system/hs-taskm.service > /dev/null << 'EOF'
[Unit]
Description=HS-TASKM Application
After=network.target postgresql.service docker.service

[Service]
Type=simple
User=your_user
WorkingDirectory=/opt/hs-taskm
ExecStart=/opt/hs-taskm/start.sh
ExecStop=/bin/kill -15 $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=hs-taskm

[Install]
WantedBy=multi-user.target
EOF

# 重载 systemd
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start hs-taskm

# 设置开机自启
sudo systemctl enable hs-taskm

# 查看状态
sudo systemctl status hs-taskm
```

### 方式二：Docker Compose 部署

#### 1. 创建 docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: taskm-postgres
    restart: always
    environment:
      POSTGRES_DB: taskm
      POSTGRES_USER: taskm
      POSTGRES_PASSWORD: taskm123
    volumes:
      - pgdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - taskm-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U taskm"]
      interval: 10s
      timeout: 5s
      retries: 5

  hs-taskm:
    image: hs-taskm-api:latest
    container_name: hs-taskm-app
    restart: always
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/taskm
      SPRING_DATASOURCE_USERNAME: taskm
      SPRING_DATASOURCE_PASSWORD: taskm123
    volumes:
      - /var/log/taskm:/var/log/taskm
      - /var/run/docker.sock:/var/run/docker.sock
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - taskm-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  pgdata:

networks:
  taskm-network:
    driver: bridge
```

#### 2. 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 查看状态
docker-compose ps

# 停止服务
docker-compose down
```

### 方式三：Kubernetes 部署

#### 1. 创建 ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hs-taskm-config
data:
  application.yml: |
    server:
      port: 8080
    spring:
      datasource:
        url: jdbc:postgresql://postgres:5432/taskm
        username: taskm
        password: taskm123
```

#### 2. 创建 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hs-taskm
spec:
  replicas: 2
  selector:
    matchLabels:
      app: hs-taskm
  template:
    metadata:
      labels:
        app: hs-taskm
    spec:
      containers:
      - name: hs-taskm
        image: hs-taskm-api:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        volumeMounts:
        - name: config
          mountPath: /app/config
        - name: logs
          mountPath: /var/log/taskm
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
      volumes:
      - name: config
        configMap:
          name: hs-taskm-config
      - name: logs
        hostPath:
          path: /var/log/taskm
```

#### 3. 创建 Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: hs-taskm-service
spec:
  selector:
    app: hs-taskm
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
  type: LoadBalancer
```

#### 4. 部署到 Kubernetes

```bash
# 应用配置
kubectl apply -f configmap.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# 查看状态
kubectl get pods
kubectl get svc

# 查看日志
kubectl logs -f deployment/hs-taskm
```

---

## 验证测试

### 1. 健康检查

```bash
# 检查应用状态
curl http://localhost:8080/actuator/health

# 预期输出
{
  "status": "UP"
}
```

### 2. API 测试

```bash
# 测试策略查询
curl http://localhost:8080/api/strategies

# 测试插件查询
curl http://localhost:8080/api/plugins

# 测试监听器查询
curl http://localhost:8080/api/listeners

# 测试任务查询
curl http://localhost:8080/api/tasks
```

### 3. 数据库连接测试

```bash
# 连接数据库
psql -h localhost -U taskm -d taskm

# 查看表
\dt

# 查看迁移记录
SELECT * FROM flyway_schema_history;

# 退出
\q
```

### 4. Docker 容器测试

```bash
# 查看 Docker 容器
docker ps

# 查看容器日志
docker logs -f <container_id>

# 测试容器网络
docker exec -it <container_id> ping postgres
```

---

## 常见问题

### 1. 端口冲突

**问题**: 8080 端口被占用

**解决方案**:
```bash
# 查看端口占用
sudo lsof -i :8080

# 修改配置文件中的端口
# application.yml
server:
  port: 8081  # 改为其他端口

# 或使用防火墙转发
sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
```

### 2. 数据库连接失败

**问题**: Could not open connection to database

**解决方案**:
```bash
# 检查 PostgreSQL 状态
sudo systemctl status postgresql

# 检查数据库监听
sudo netstat -tlnp | grep 5432

# 检查防火墙
sudo ufw allow 5432

# 检查 pg_hba.conf
sudo nano /etc/postgresql/15/main/pg_hba.conf
# 添加：
# host    all             all             0.0.0.0/0               md5

# 重启 PostgreSQL
sudo systemctl restart postgresql
```

### 3. Docker 权限问题

**问题**: Got permission denied while trying to connect to the Docker daemon

**解决方案**:
```bash
# 将用户添加到 docker 组
sudo usermod -aG docker $USER

# 刷新组权限
newgrp docker

# 或使用 sudo
sudo docker ps
```

### 4. 内存不足

**问题**: Java heap space error

**解决方案**:
```bash
# 增加 JVM 内存
# 在启动脚本中修改：
java -jar -Xms1g -Xmx4g hs-taskm-api.jar

# 或在 application.yml 中配置：
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
```

### 5. 日志目录权限

**问题**: Failed to create log directory

**解决方案**:
```bash
# 创建并设置权限
sudo mkdir -p /var/log/taskm/tasks
sudo chown -R app_user:app_user /var/log/taskm
sudo chmod -R 755 /var/log/taskm

# 或使用其他路径
# 在 application.yml 中配置：
logs:
  base_dir: ./logs
```

---

## 运维指南

### 1. 日志管理

#### 查看应用日志

```bash
# Systemd 服务日志
sudo journalctl -u hs-taskm -f

# 应用日志文件
tail -f /var/log/taskm/tasks/task_*/strategy.log

# Docker 日志
docker logs -f <container_id>

# Kubernetes 日志
kubectl logs -f deployment/hs-taskm
```

#### 日志轮转

创建 `/etc/logrotate.d/hs-taskm`:

```
/var/log/taskm/**/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 app_user app_user
    postrotate
        systemctl reload hs-taskm > /dev/null 2>&1 || true
    endscript
}
```

### 2. 监控指标

#### JVM 监控

```bash
# 查看 JVM 进程
jps -l

# 查看堆内存
jmap -heap <pid>

# 查看 GC 情况
jstat -gc <pid> 1000

# 线程dump
jstack <pid>
```

#### Spring Boot Actuator

配置 `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env
  endpoint:
    health:
      show-details: always
```

访问监控端点:

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info

# JVM 指标
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# 环境变量
curl http://localhost:8080/actuator/env
```

### 3. 性能优化

#### JVM 参数调优

```bash
# 在启动脚本中添加
JAVA_OPTS="
  -server
  -Xms1g
  -Xmx2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/taskm/
  -Djava.awt.headless=true
"
```

#### 数据库连接池调优

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 4. 容器清理

```bash
# 清理停止的容器
docker container prune -f

# 清理未使用的镜像
docker image prune -a -f

# 清理未使用的卷
docker volume prune -f

# 清理未使用的网络
docker network prune -f

# 清理所有未使用的资源
docker system prune -a --volumes -f
```

### 5. 数据库维护

#### 数据库备份

```bash
# 创建备份脚本
cat > /opt/backup-taskm.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/backups/taskm"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

# 备份数据库
pg_dump -h localhost -U taskm -d taskm | gzip > $BACKUP_DIR/taskm_$DATE.sql.gz

# 保留最近 7 天的备份
find $BACKUP_DIR -name "taskm_*.sql.gz" -mtime +7 -delete

echo "Backup completed: taskm_$DATE.sql.gz"
EOF

chmod +x /opt/backup-taskm.sh

# 添加到 crontab（每天凌晨 2 点执行）
echo "0 2 * * * /opt/backup-taskm.sh" | crontab -
```

#### 数据库恢复

```bash
# 解压备份文件
gunzip /opt/backups/taskm/taskm_20260323_020000.sql.gz

# 恢复数据库
psql -h localhost -U taskm -d taskm < /opt/backups/taskm/taskm_20260323_020000.sql
```

---

## 备份恢复

### 1. 应用备份

#### 配置文件备份

```bash
# 备份配置文件
tar -czf /opt/backups/taskm-config-$(date +%Y%m%d).tar.gz \
  /opt/hs-taskm/application-prod.yml \
  /etc/systemd/system/hs-taskm.service
```

#### 数据备份

```bash
# 备份日志目录
tar -czf /opt/backups/taskm-logs-$(date +%Y%m%d).tar.gz \
  /var/log/taskm/

# 备份数据库
pg_dump -h localhost -U taskm taskm | gzip > \
  /opt/backups/taskm-db-$(date +%Y%m%d).sql.gz
```

### 2. 应用恢复

#### 配置恢复

```bash
# 解压配置文件
tar -xzf /opt/backups/taskm-config-20260323.tar.gz -C /

# 重载 systemd
sudo systemctl daemon-reload

# 重启服务
sudo systemctl restart hs-taskm
```

#### 数据恢复

```bash
# 停止应用
sudo systemctl stop hs-taskm

# 恢复数据库
gunzip < /opt/backups/taskm-db-20260323.sql.gz | \
  psql -h localhost -U taskm taskm

# 启动应用
sudo systemctl start hs-taskm
```

---

## 安全加固

### 1. 防火墙配置

```bash
# Ubuntu UFW
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 8080/tcp  # 应用端口
sudo ufw allow 5432/tcp  # PostgreSQL（可选，限制IP）
sudo ufw enable

# CentOS firewalld
sudo firewall-cmd --permanent --add-port=22/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

### 2. 数据库安全

```bash
# 修改 postgres 密码
sudo -u postgres psql
ALTER USER postgres WITH PASSWORD 'strong_password_here';

# 创建只读用户
CREATE USER taskm_readonly WITH PASSWORD 'readonly_password';
GRANT CONNECT ON DATABASE taskm TO taskm_readonly;
GRANT USAGE ON SCHEMA public TO taskm_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO taskm_readonly;
```

### 3. SSL/TLS 配置

生成自签名证书（测试环境）：

```bash
# 生成证书
openssl req -x509 -newkey rsa:4096 \
  -keyout key.pem -out cert.pem \
  -days 365 -nodes \
  -subj "/CN=localhost"

# 配置 application.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: password
    key-store-type: PKCS12
```

---

## 故障排查

### 1. 应用无法启动

```bash
# 查看服务状态
sudo systemctl status hs-taskm

# 查看日志
sudo journalctl -u hs-taskm -n 100

# 检查端口
sudo netstat -tlnp | grep 8080

# 手动运行测试
cd /opt/hs-taskm
java -jar hs-taskm-api-*.jar
```

### 2. 数据库迁移失败

```bash
# 查看迁移历史
psql -U taskm -d taskm -c "SELECT * FROM flyway_schema_history;"

# 修复失败的迁移
# 1. 删除失败的迁移记录
psql -U taskm -d taskm -c "DELETE FROM flyway_schema_history WHERE version = 'X.X';"

# 2. 重新运行迁移
mvn flyway:migrate
```

### 3. 容器启动失败

```bash
# 查看容器日志
docker logs <container_id>

# 查看容器详情
docker inspect <container_id>

# 进入容器调试
docker exec -it <container_id> /bin/bash

# 查看容器资源使用
docker stats
```

---

## 附录

### A. 目录结构

```
/opt/hs-taskm/
├── hs-taskm-api-*.jar        # 应用 JAR 包
├── application-prod.yml       # 生产配置文件
├── start.sh                   # 启动脚本
└── logs/                      # 日志目录

/var/log/taskm/
└── tasks/
    └── task_{id}/
        ├── strategy.log       # 策略日志
        ├── plugin.log         # 插件日志
        └── listener.log       # 监听器日志
```

### B. 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SERVER_PORT | 应用端口 | 8080 |
| SPRING_PROFILES_ACTIVE | Spring 配置文件 | prod |
| DATABASE_URL | 数据库连接URL | - |
| DATABASE_USERNAME | 数据库用户名 | taskm |
| DATABASE_PASSWORD | 数据库密码 | - |
| LOGS_BASE_DIR | 日志基础目录 | /var/log/taskm |
| DOCKER_DEFAULT_IMAGE | 默认Docker镜像 | python:3.9-slim |

### C. 端口说明

| 端口 | 用途 | 说明 |
|------|------|------|
| 8080 | 应用HTTP端口 | REST API |
| 5432 | PostgreSQL | 数据库 |
| 8080+ | 动态端口 | 插件/监听器容器 |

### D. 相关链接

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Docker 官方文档](https://docs.docker.com/)
- [PostgreSQL 官方文档](https://www.postgresql.org/docs/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)

---

**文档版本**: 1.0
**最后更新**: 2026-03-23
**维护者**: HS-TASKM 开发团队
