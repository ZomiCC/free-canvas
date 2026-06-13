package com.freecanvas.service;

import com.freecanvas.model.dto.ConnectionDTO;
import com.freecanvas.model.dto.NodeDTO;
import com.freecanvas.model.entity.CanvasNode;
import com.freecanvas.model.entity.NodeConnection;
import java.util.List;

public interface NodeService {
    // 节点操作
    List<CanvasNode> getProjectNodes(Long projectId);
    CanvasNode createNode(NodeDTO dto);
    CanvasNode updateNode(Long nodeId, NodeDTO dto);
    void deleteNode(Long nodeId);

    // 连接操作
    List<NodeConnection> getProjectConnections(Long projectId);
    NodeConnection createConnection(ConnectionDTO dto);
    void deleteConnection(Long connectionId);

    // 查询关联节点
    /** 获取与指定节点相连的所有节点 */
    List<CanvasNode> getConnectedNodes(Long nodeId);
}
