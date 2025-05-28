package inge2.dataflow;

import inge2.dataflow.pointstoanalysis.Axis;
import inge2.dataflow.pointstoanalysis.Node;
import inge2.dataflow.pointstoanalysis.PointsToAnalysis;
import inge2.dataflow.pointstoanalysis.PointsToGraph;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TallerTest {
    @Test
    public void testUnionPTG() {
        PointsToGraph g1 = new PointsToGraph();
        PointsToGraph g2 = new PointsToGraph();
        Set<Node> n1 = new HashSet<>();
        n1.add(new Node("N1"));
        Set<Node> n2 = new HashSet<>();
        n2.add(new Node("N2"));
        g1.setNodesForVariable("A", n1);
        g2.setNodesForVariable("A", n2);
        g1.union(g2);
        assertEquals(2, g1.getNodesForVariable("A").size());
    }

    @Test
    public void testTarget1() {
        Launcher.analyzeClass("inge2.dataflow.targets.Target1");
        PointsToAnalysis pointsToAnalysis = Launcher.getLastPointsToAnalysis();
        PointsToGraph pointsToGraph = pointsToAnalysis.getLastPointsToGraph();

        Set<Node> xNodes = pointsToGraph.getNodesForVariable("x");
        assertEquals(1, xNodes.size());
    }

    @Test
    public void testTarget2() {
        Launcher.analyzeClass("inge2.dataflow.targets.Target2");
        PointsToAnalysis pointsToAnalysis = Launcher.getLastPointsToAnalysis();

        assertTrue(pointsToAnalysis.mayAlias("x", "f1", "x"), "x.f1 == x? SI");
        assertTrue(pointsToAnalysis.mayAlias("x", "z"), "x may-alias z? SI");
    }

    @Test
    public void testTarget3() {
        Launcher.analyzeClass("inge2.dataflow.targets.Target3");
        PointsToAnalysis pointsToAnalysis = Launcher.getLastPointsToAnalysis();

        assertFalse(pointsToAnalysis.mayAlias("x", "y"), "x may-alias y? NO");
        assertTrue(pointsToAnalysis.mayAlias("x", "f1", "y"), "x.f1 may-alias y? SI");
    }

    @Test
    public void testTarget4() {
        Launcher.analyzeClass("inge2.dataflow.targets.Target4");
        PointsToAnalysis pointsToAnalysis = Launcher.getLastPointsToAnalysis();

        assertFalse(pointsToAnalysis.mayAlias("x", "y"), "x may-alias y? NO");
        assertTrue(pointsToAnalysis.mayAlias("x", "f1", "y"), "x.f1 may-alias y? SI");
    }

    @Test
    public void testTarget5() {
        Launcher.analyzeClass("inge2.dataflow.targets.Target5");
        PointsToAnalysis pointsToAnalysis = Launcher.getLastPointsToAnalysis();

        // Check axis are well calculated

        String[] expectedValuesForAxis = {
                "((7), f1, (8))",
                "((8), f1, (9))",
                "((9), f1, (7))"
        };
        Set<String> expectedAxis = new HashSet<>(Arrays.asList(expectedValuesForAxis));
        Set<String> calculatedAxis = pointsToAnalysis.getLastPointsToGraph().axis.stream().map(Axis::toString).collect(Collectors.toSet());
        assertEquals(expectedAxis, calculatedAxis);

        // Check may-alias points

        assertFalse(pointsToAnalysis.mayAlias("t", "x"), "t may-alias x? NO");
        assertFalse(pointsToAnalysis.mayAlias("t", "z"), "t may-alias z? NO");
        assertTrue(pointsToAnalysis.mayAlias("t", "y"), "t may-alias y? SI");
        assertTrue(pointsToAnalysis.mayAlias("x","f1","y"),"x may-alias y? SI");
        assertTrue(pointsToAnalysis.mayAlias("y","f1","z"),"y may-alias z? SI");
        assertTrue(pointsToAnalysis.mayAlias("z","f1","x"),"z may-alias x? SI");

    }

    @Test
    public void testTarget6() {
        Launcher.analyzeClass("inge2.dataflow.targets.Target6");
        PointsToAnalysis pointsToAnalysis = Launcher.getLastPointsToAnalysis();

        // Check axis are well calculated

        String[] expectedValuesForAxis = {
                "((10), f1, (11))",
                "((7), f1, (8))",
                "((11), f1, (8))"
        };

        Set<String> expectedAxis = new HashSet<>(Arrays.asList(expectedValuesForAxis));
        Set<String> calculatedAxis = pointsToAnalysis.getLastPointsToGraph().axis.stream().map(Axis::toString).collect(Collectors.toSet());
        assertEquals(expectedAxis, calculatedAxis);

        // Check may-alias points

        assertTrue(pointsToAnalysis.mayAlias("a", "c"), "a may-alias c? SI");
        assertFalse(pointsToAnalysis.mayAlias("a", "b"), "a may-alias b? NO");
        assertTrue(pointsToAnalysis.mayAlias("a", "f1", "e"), "a.f1 may-alias e? SI");
        assertTrue(pointsToAnalysis.mayAlias("c", "f1", "d"),"c.f1 may-alias d? SI");
        assertTrue(pointsToAnalysis.mayAlias("d","f1", "b"),"d.f1 may-alias b? SI");
        assertFalse(pointsToAnalysis.mayAlias("a", "f1", "b"), "a.f1 may-alias b? NO");
    }
}