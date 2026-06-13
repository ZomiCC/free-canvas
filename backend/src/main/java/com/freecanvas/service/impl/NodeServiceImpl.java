package com.freecanvas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.freecanvas.model.dto.ConnectionDTO;
import com.freecanvas.model.dto.NodeDTO;
import com.freecanvas.model.entity.CanvasNode;
import com.freecanvas.model.entity.NodeConnection;
import com.freecanvas.repository.CanvasNodeRepository;
import com.freecanvas.repository.NodeConnectionRepository;
import com.freecanvas.service.NodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NodeServiceImpl implements NodeService {

    private final CanvasNodeRepository nodeRepository;
    private final NodeConnectionRepository connectionRepository;

    public NodeServiceImpl(CanvasNodeRepository nodeRepository,
                           NodeConnectionRepository connectionRepository) {
        this.nodeRepository = nodeRepository;
        this.connectionRepository = connectionRepository;
    }

    // ==================== 节点操作 ====================

    @Override
    public List<CanvasNode> getProjectNodes(Long projectId) {
        return nodeRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional
    public CanvasNode createNode(NodeDTO dto) {
        CanvasNode node = new CanvasNode();
        node.setProjectId(dto.getProjectId());
        node.setNodeType(dto.getNodeType());
        node.setLabel(dto.getLabel());
        node.setPosX(dto.getPosX());
        node.setPosY(dto.getPosY());
        node.setWidth(dto.getWidth());
        node.setHeight(dto.getHeight());
        node.setNodeData(dto.getNodeData());
        node.setStyle(dto.getStyle());
        nodeRepository.insert(node);
        return node;
    }

    @Override
    @Transactional
    public CanvasNode updateNode(Long nodeId, NodeDTO dto) {
        CanvasNode node = nodeRepository.selectById(nodeId);
        if (node == null) {
            throw new RuntimeException("节点不存在: " + nodeId);
        }
        if (dto.getLabel() != null) node.setLabel(dto.getLabel());
        if (dto.getPosX() != null) node.setPosX(dto.getPosX());
        if (dto.getPosY() != null) node.setPosY(dto.getPosY());
        if (dto.getWidth() != null) node.setWidth(dto.getWidth());
        if (dto.getHeight() != null) node.setHeight(dto.getHeight());
        if (dto.getNodeData() != null) node.setNodeData(dto.getNodeData());
        if (dto.getStyle() != null) node.setStyle(dto.getStyle());
        nodeRepository.updateById(node);
        return node;
    }

    @Override
    @Transactional
    public void deleteNode(Long nodeId) {
        nodeRepository.deleteById(nodeId);
        // 级联删除相关连接
        connectionRepository.delete(
            new LambdaQueryWrapper<NodeConnection>()
                .eq(NodeConnection::getSourceNodeId, nodeId)
                .or()
                .eq(NodeConnection::getTargetNodeId, nodeId)
        );
    }

    // ==================== 连接操作 ====================

    @Override
    public List<NodeConnection> getProjectConnections(Long projectId) {
        return connectionRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional
    public NodeConnection createConnection(ConnectionDTO dto) {
        NodeConnection conn = new NodeConnection();
        conn.setProjectId(dto.getProjectId());
        conn.setSourceNodeId(dto.getSourceNodeId());
        conn.setTargetNodeId(dto.getTargetNodeId());
        conn.setSourceHandle(dto.getSourceHandle());
        conn.setTargetHandle(dto.getTargetHandle());
        conn.setLabel(dto.getLabel());
        conn.setStyle(dto.getStyle());
        connectionRepository.insert(conn);
        return conn;
    }

    @Override
    @Transactional
    public void deleteConnection(Long connectionId) {
        connectionRepository.deleteById(connectionId);
    }

    // ==================== 关联节点查询 ====================

    @Override
    public List<CanvasNode> getConnectedNodes(Long nodeId) {
        // 查询所有连接到该节点的边
        List<NodeConnection> conns = connectionRepository.selectList(
            new LambdaQueryWrapper<NodeConnection>()
                .eq(NodeConnection::getSourceNodeId, nodeId)
                .or()
                .eq(NodeConnection::getTargetNodeId, nodeId)
        );

        // 收集所有相连节点的 ID
        List<Long> connectedIds = conns.stream()
            .map(c -> c.getSourceNodeId().equals(nodeId) ? c.getTargetNodeId() : c.getSourceNodeId())
            .distinct()
            .toList();

        if (connectedIds.isEmpty()) {
            return List.of();
        }

        return nodeRepository.selectBatchIds(connectedIds);
    }
}
