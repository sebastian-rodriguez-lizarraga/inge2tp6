package inge2.dataflow.pointstoanalysis;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashSet;
import java.util.Set;

public class PointsToAnalysis extends ForwardFlowAnalysis<Unit, PointsToGraph> {

    private PointsToGraph lastPointsToGraph;

    public PointsToAnalysis(UnitGraph graph) {
        super(graph);
        doAnalysis();
    }

    public PointsToGraph getLastPointsToGraph() {
        return lastPointsToGraph;
    }

    /**
     * This method is called for each unit in the control flow graph.
     * @param in the input flow
     * @param unit the current node
     * @param out the returned flow
     */
    @Override
    protected void flowThrough(PointsToGraph in, Unit unit, PointsToGraph out) {
        out.copy(in);

        PointsToVisitor visitor = new PointsToVisitor(out);
        unit.apply(visitor);

        this.lastPointsToGraph = out;
    }

    @Override
    protected PointsToGraph newInitialFlow() {
        return new PointsToGraph();
    }

    /**
     * This method merges the two input flows into a single output flow.
     * @param input1 the first input flow
     * @param input2 the second input flow
     * @param output the returned flow
     */
    @Override
    protected void merge(PointsToGraph input1, PointsToGraph input2, PointsToGraph output) {
        output.copy(input1);
        output.union(input2);
    }

    @Override
    protected void copy(PointsToGraph source, PointsToGraph dest) {
        dest.copy(source);
    }

    /**
     * Retorna true si alguno de los objetos apuntados por leftVariableName y rightVariableName coinciden.
     * @param leftVariableName
     * @param rightVariableName
     * @return
     */
    public boolean mayAlias(String leftVariableName, String rightVariableName) {
        Set<Node> nX = lastPointsToGraph.getNodesForVariable(leftVariableName);
        Set<Node> nY = lastPointsToGraph.getNodesForVariable(rightVariableName);
        for (Node x : nX) {
            for (Node y : nY) {
                if (x.equals(y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retorna true si alguno de los objetos apuntados por leftVariableName.fieldName y rightVariableName coinciden.
     * @param leftVariableName
     * @param fieldName
     * @param rightVariableName
     * @return
     */
    public boolean mayAlias(String leftVariableName, String fieldName, String rightVariableName) {
        Set<Node> nX = lastPointsToGraph.getNodesForVariable(leftVariableName);
        Set<Node> n = new HashSet<Node>();
        for (Node node : nX) {
            n.addAll(lastPointsToGraph.getReachableNodesByField(node,fieldName));
        }
        Set<Node> nY = lastPointsToGraph.getNodesForVariable(rightVariableName);
        for (Node m : n) {
            for (Node y : nY) {
                if (m.equals(y)) {
                    return true;
                }
            }
        }
        return false;
    }
}
