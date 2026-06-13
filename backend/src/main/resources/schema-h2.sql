-- H2 内存数据库初始化脚本 (开发模式)

CREATE TABLE IF NOT EXISTS project (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200)  NOT NULL,
    description VARCHAR(1000),
    thumbnail   VARCHAR(500),
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT       DEFAULT 0
);

CREATE TABLE IF NOT EXISTS canvas_node (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT        NOT NULL,
    node_type   VARCHAR(50)   NOT NULL,
    label       VARCHAR(200),
    pos_x       DOUBLE        DEFAULT 0,
    pos_y       DOUBLE        DEFAULT 0,
    width       DOUBLE        DEFAULT 200,
    height      DOUBLE        DEFAULT 150,
    node_data   CLOB,
    style       CLOB,
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT       DEFAULT 0,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_canvas_node_project ON canvas_node(project_id);

CREATE TABLE IF NOT EXISTS node_connection (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT        NOT NULL,
    source_node_id  BIGINT        NOT NULL,
    target_node_id  BIGINT        NOT NULL,
    source_handle   VARCHAR(100),
    target_handle   VARCHAR(100),
    label           VARCHAR(200),
    style           CLOB,
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT       DEFAULT 0,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    FOREIGN KEY (source_node_id) REFERENCES canvas_node(id) ON DELETE CASCADE,
    FOREIGN KEY (target_node_id) REFERENCES canvas_node(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_conn_project ON node_connection(project_id);
CREATE INDEX IF NOT EXISTS idx_conn_source ON node_connection(source_node_id);
CREATE INDEX IF NOT EXISTS idx_conn_target ON node_connection(target_node_id);

CREATE TABLE IF NOT EXISTS ai_task (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id      BIGINT,
    node_id         BIGINT,
    task_type       VARCHAR(50)   NOT NULL,
    prompt          CLOB          NOT NULL,
    params          CLOB,
    status          VARCHAR(20)   DEFAULT 'PENDING',
    result_url      VARCHAR(1000),
    error_msg       CLOB,
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_aitask_project ON ai_task(project_id);
CREATE INDEX IF NOT EXISTS idx_aitask_node ON ai_task(node_id);
CREATE INDEX IF NOT EXISTS idx_aitask_status ON ai_task(status);
