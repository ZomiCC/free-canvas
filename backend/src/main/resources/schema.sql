-- 自由画布数据库初始化脚本

CREATE TABLE IF NOT EXISTS project (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200)  NOT NULL COMMENT '项目名称',
    description TEXT          COMMENT '项目描述',
    thumbnail   VARCHAR(500)  COMMENT '缩略图URL',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除, 1=已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

CREATE TABLE IF NOT EXISTS canvas_node (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT        NOT NULL COMMENT '所属项目ID',
    node_type   VARCHAR(50)   NOT NULL COMMENT '节点类型: CHARACTER/SCENE/TEXT/VIDEO',
    label       VARCHAR(200)  COMMENT '节点显示标签',
    pos_x       DOUBLE        NOT NULL DEFAULT 0 COMMENT 'X坐标',
    pos_y       DOUBLE        NOT NULL DEFAULT 0 COMMENT 'Y坐标',
    width       DOUBLE        NOT NULL DEFAULT 200 COMMENT '宽度',
    height      DOUBLE        NOT NULL DEFAULT 150 COMMENT '高度',
    node_data   JSON          COMMENT '节点自定义数据(JSON)',
    style       JSON          COMMENT '节点样式(JSON)',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT       NOT NULL DEFAULT 0,
    INDEX idx_project_id (project_id),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画布节点表';

CREATE TABLE IF NOT EXISTS node_connection (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT        NOT NULL COMMENT '所属项目ID',
    source_node_id  BIGINT        NOT NULL COMMENT '源节点ID',
    target_node_id  BIGINT        NOT NULL COMMENT '目标节点ID',
    source_handle   VARCHAR(100)  COMMENT '源连接点标识',
    target_handle   VARCHAR(100)  COMMENT '目标连接点标识',
    label           VARCHAR(200)  COMMENT '连接线标签',
    style           JSON          COMMENT '连接线样式(JSON)',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,
    INDEX idx_project_id (project_id),
    INDEX idx_source_node (source_node_id),
    INDEX idx_target_node (target_node_id),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (source_node_id) REFERENCES canvas_node(id) ON DELETE CASCADE,
    FOREIGN KEY (target_node_id) REFERENCES canvas_node(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点连接表';

CREATE TABLE IF NOT EXISTS ai_task (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT        COMMENT '所属项目ID',
    node_id         BIGINT        COMMENT '关联节点ID',
    task_type       VARCHAR(50)   NOT NULL COMMENT '任务类型: IMAGE/VIDEO',
    prompt          TEXT          NOT NULL COMMENT '生成提示词',
    params          JSON          COMMENT '生成参数(JSON)',
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/COMPLETED/FAILED',
    result_url      VARCHAR(1000) COMMENT '生成结果URL',
    error_msg       TEXT          COMMENT '错误信息',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_id (project_id),
    INDEX idx_node_id (node_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生成任务表';
