package inge2.dataflow.pointstoanalysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PointsToGraphTest {

    private PointsToGraph graph;
    private Node node1, node2, node3;

    @BeforeEach
    void setUp() {
        graph = new PointsToGraph();
        node1 = new Node("1");
        node2 = new Node("2");
        node3 = new Node("3");
    }

    @Test
    @DisplayName("Clear should empty all collections")
    void testClear() {
        // Add some data first
        graph.nodes.add(node1);
        graph.axis.add(new Axis(node1, "field", node2));
        Set<Node> nodeSet = new HashSet<>();
        nodeSet.add(node1);
        graph.mapping.put("var1", nodeSet);

        // Verify data was added
        assertFalse(graph.nodes.isEmpty());
        assertFalse(graph.axis.isEmpty());
        assertFalse(graph.mapping.isEmpty());

        // Clear and verify everything is empty
        graph.clear();

        assertTrue(graph.nodes.isEmpty());
        assertTrue(graph.axis.isEmpty());
        assertTrue(graph.mapping.isEmpty());
    }

    @Test
    @DisplayName("getNodesForVariable should return correct node set")
    void testGetNodesForVariable() {
        Set<Node> nodeSet = new HashSet<>();
        nodeSet.add(node1);
        nodeSet.add(node2);

        graph.mapping.put("variable1", nodeSet);

        Set<Node> result = graph.getNodesForVariable("variable1");
        assertEquals(nodeSet, result);
        assertTrue(result.contains(node1));
        assertTrue(result.contains(node2));
    }

    @Test
    @DisplayName("getNodesForVariable should return null for non-existent variable")
    void testGetNodesForVariableNonExistent() {
        Set<Node> result = graph.getNodesForVariable("nonExistentVariable");
        assertNull(result);
    }

    @Test
    @DisplayName("setNodesForVariable should add nodes to both nodes set and mapping")
    void testSetNodesForVariable() {
        Set<Node> nodeSet = new HashSet<>();
        nodeSet.add(node1);
        nodeSet.add(node2);

        graph.setNodesForVariable("variable1", nodeSet);

        // Check that nodes were added to the graph's nodes set
        assertTrue(graph.nodes.contains(node1));
        assertTrue(graph.nodes.contains(node2));

        // Check that mapping was updated
        assertEquals(nodeSet, graph.mapping.get("variable1"));
    }

    @Test
    @DisplayName("setNodesForVariable should handle empty node set")
    void testSetNodesForVariableEmpty() {
        Set<Node> emptySet = new HashSet<>();

        graph.setNodesForVariable("variable1", emptySet);

        assertEquals(emptySet, graph.mapping.get("variable1"));
        assertTrue(graph.nodes.isEmpty());
    }

    @Test
    @DisplayName("addEdge should add axis to the graph")
    void testAddEdge() {
        graph.addEdge(node1, "fieldName", node2);

        assertEquals(1, graph.axis.size());

        Axis expectedAxis = new Axis(node1, "fieldName", node2);
        assertTrue(graph.axis.contains(expectedAxis));
    }

    @Test
    @DisplayName("addEdge should handle multiple edges")
    void testAddMultipleEdges() {
        graph.addEdge(node1, "field1", node2);
        graph.addEdge(node2, "field2", node3);
        graph.addEdge(node1, "field3", node3);

        assertEquals(3, graph.axis.size());
    }

    @Test
    @DisplayName("addEdge should not add duplicate edges")
    void testAddDuplicateEdges() {
        graph.addEdge(node1, "field", node2);
        graph.addEdge(node1, "field", node2); // Same edge

        assertEquals(1, graph.axis.size());
    }

    @Test
    @DisplayName("getReachableNodesByField should return correct nodes")
    void testGetReachableNodesByField() {
        graph.addEdge(node1, "field1", node2);
        graph.addEdge(node1, "field1", node3);
        graph.addEdge(node1, "field2", node3);
        graph.addEdge(node2, "field1", node3);

        Set<Node> reachable = graph.getReachableNodesByField(node1, "field1");

        assertEquals(2, reachable.size());
        assertTrue(reachable.contains(node2));
        assertTrue(reachable.contains(node3));
    }

    @Test
    @DisplayName("getReachableNodesByField should return empty set for non-existent field")
    void testGetReachableNodesByFieldNonExistent() {
        graph.addEdge(node1, "field1", node2);

        Set<Node> reachable = graph.getReachableNodesByField(node1, "nonExistentField");

        assertTrue(reachable.isEmpty());
    }

    @Test
    @DisplayName("getReachableNodesByField should return empty set for non-existent node")
    void testGetReachableNodesByFieldNonExistentNode() {
        graph.addEdge(node1, "field1", node2);

        Node nonExistentNode = new Node("999");
        Set<Node> reachable = graph.getReachableNodesByField(nonExistentNode, "field1");

        assertTrue(reachable.isEmpty());
    }

    @Test
    @DisplayName("copy should clear current graph and copy from input")
    void testCopy() {
        // Setup current graph with some data
        graph.nodes.add(node1);
        graph.addEdge(node1, "field", node2);
        Set<Node> nodeSet = new HashSet<>();
        nodeSet.add(node1);
        graph.mapping.put("var1", nodeSet);

        // Create source graph
        PointsToGraph sourceGraph = new PointsToGraph();
        sourceGraph.nodes.add(node2);
        sourceGraph.nodes.add(node3);
        sourceGraph.addEdge(node2, "newField", node3);
        Set<Node> sourceNodeSet = new HashSet<>();
        sourceNodeSet.add(node3);
        sourceGraph.mapping.put("var2", sourceNodeSet);

        // Copy
        graph.copy(sourceGraph);

        // Verify current graph was cleared and replaced with source graph content
        assertEquals(2, graph.nodes.size());
        assertTrue(graph.nodes.contains(node2));
        assertTrue(graph.nodes.contains(node3));
        assertFalse(graph.nodes.contains(node1));

        assertEquals(1, graph.axis.size());
        assertTrue(graph.axis.contains(new Axis(node2, "newField", node3)));

        assertEquals(1, graph.mapping.size());
        assertEquals(sourceNodeSet, graph.mapping.get("var2"));
        assertNull(graph.mapping.get("var1"));
    }

    @Test
    @DisplayName("union should merge two graphs correctly")
    void testUnion() {
        // Setup current graph
        graph.nodes.add(node1);
        graph.addEdge(node1, "field1", node2);
        Set<Node> nodeSet1 = new HashSet<>();
        nodeSet1.add(node1);
        graph.mapping.put("var1", nodeSet1);

        // Create second graph
        PointsToGraph otherGraph = new PointsToGraph();
        otherGraph.nodes.add(node2);
        otherGraph.nodes.add(node3);
        otherGraph.addEdge(node2, "field2", node3);
        Set<Node> nodeSet2 = new HashSet<>();
        nodeSet2.add(node2);
        otherGraph.mapping.put("var2", nodeSet2);

        // Union
        graph.union(otherGraph);

        // Verify merged content
        assertEquals(3, graph.nodes.size());
        assertTrue(graph.nodes.contains(node1));
        assertTrue(graph.nodes.contains(node2));
        assertTrue(graph.nodes.contains(node3));

        assertEquals(2, graph.axis.size());
        assertTrue(graph.axis.contains(new Axis(node1, "field1", node2)));
        assertTrue(graph.axis.contains(new Axis(node2, "field2", node3)));

        assertEquals(2, graph.mapping.size());
        assertEquals(nodeSet1, graph.mapping.get("var1"));
        assertEquals(nodeSet2, graph.mapping.get("var2"));
    }

    @Test
    @DisplayName("union should merge variable mappings correctly when same variable exists in both graphs")
    void testUnionWithOverlappingVariables() {
        // Setup current graph
        Set<Node> nodeSet1 = new HashSet<>();
        nodeSet1.add(node1);
        graph.mapping.put("var1", nodeSet1);

        // Create second graph with same variable name
        PointsToGraph otherGraph = new PointsToGraph();
        Set<Node> nodeSet2 = new HashSet<>();
        nodeSet2.add(node2);
        nodeSet2.add(node3);
        otherGraph.mapping.put("var1", nodeSet2);

        // Union
        graph.union(otherGraph);

        // Verify variable mapping was merged
        Set<Node> mergedNodes = graph.mapping.get("var1");
        assertEquals(3, mergedNodes.size());
        assertTrue(mergedNodes.contains(node1));
        assertTrue(mergedNodes.contains(node2));
        assertTrue(mergedNodes.contains(node3));
    }

    @Test
    @DisplayName("union should handle empty graphs")
    void testUnionWithEmptyGraphs() {
        PointsToGraph emptyGraph = new PointsToGraph();

        // Union with empty graph should not change anything
        graph.union(emptyGraph);

        assertTrue(graph.nodes.isEmpty());
        assertTrue(graph.axis.isEmpty());
        assertTrue(graph.mapping.isEmpty());

        // Add some content and union with empty graph
        graph.nodes.add(node1);
        graph.union(emptyGraph);

        assertEquals(1, graph.nodes.size());
        assertTrue(graph.nodes.contains(node1));
    }

    @Test
    @DisplayName("union should not create duplicate nodes or axes")
    void testUnionNoDuplicates() {
        // Add same content to both graphs
        graph.nodes.add(node1);
        graph.nodes.add(node2);
        graph.addEdge(node1, "field", node2);

        PointsToGraph otherGraph = new PointsToGraph();
        otherGraph.nodes.add(node1);
        otherGraph.nodes.add(node2);
        otherGraph.addEdge(node1, "field", node2);

        graph.union(otherGraph);

        // Should not have duplicates
        assertEquals(2, graph.nodes.size());
        assertEquals(1, graph.axis.size());
    }
}