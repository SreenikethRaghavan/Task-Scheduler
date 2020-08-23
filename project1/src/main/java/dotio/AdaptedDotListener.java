package main.java.dotio;

import main.java.dotio.antlr.DOTBaseListener;
import main.java.dotio.antlr.DOTParser;

/**
 * This class extends the class DOTBaseListener that was generated by ANTLR. It creates a TaskGraph object upon
 * construction, and gradually adds nodes and edges to the graph as it parses the DOT file. It has been written to only
 * read nodes and edges from the graph, and ignore any other .dot elements.
 */
public class AdaptedDotListener extends DOTBaseListener {

    private TaskGraph _taskGraph;

    /**
     * Creates the listener and sets it up to update the input task graph when parsing the .dot file.
     * @param graph The TaskGraph object that the .dot syntax will be read into.
     */
    public AdaptedDotListener(TaskGraph graph) {
        super();
        _taskGraph = graph;
    }

    /**
     * Executed every time the parser finishes reading a graph. It sets the name of the task graph.
     * @param ctx The statement context for the graph being read.
     */
    @Override
    public void exitGraph(DOTParser.GraphContext ctx) {
        if (ctx.id() != null) {
            _taskGraph.setName(ctx.id().getText().replaceAll("^\"|\"$", ""));
        }
    }

    /**
     * Executed every time the parser finishes reading a node. It sets the name ("id") of the node, and ensures that the
     * node has been assigned a weight.
     * @param ctx The statement context for the node being read.
     */
    @Override
    public void exitNode_stmt(DOTParser.Node_stmtContext ctx) {
        String taskName = ctx.node_id().id().getText();
        int taskWeight = getWeightFromAttrList(ctx.attr_list());
        _taskGraph.insertTask(new Task(taskName, taskWeight));
    }

    /**
     * Executed every time the parser finishes reading an edge. It sets the name ("id") of the source and destination
     * nodes, and ensures that the edge has been assigned a weight.
     * @param ctx The statement context for the edge being read.
     */
    @Override
    public void exitEdge_stmt(DOTParser.Edge_stmtContext ctx) {
        if (ctx.edgeRHS().node_id().size() != 1) {
            //If an edge involves more than 2 nodes,
            throw new DotIOException("Can only accept edges between 2 nodes.");
        } else {
            DOTParser.Node_idContext nodeList = ctx.node_id();
            String src = ctx.node_id().id().getText();
            String dest = ctx.edgeRHS().node_id(0).id().getText();
            int weight = getWeightFromAttrList(ctx.attr_list());
            _taskGraph.insertDependency(new Dependency(src, dest, weight));
        }
    }

    /**
     * Helper method used to check whether an edge or a node has been assigned a weight, and retrieve the weight.
     * @param ctx The attribute list context for the edge/node.
     * @return The weight of the graph element, parsed to an integer.
     */
    private int getWeightFromAttrList(DOTParser.Attr_listContext ctx) {
        try {
            for (DOTParser.A_listContext attr : ctx.a_list()) {
                if (attr.id(0).getText().equalsIgnoreCase("WEIGHT")) {
                    return Integer.parseInt(attr.id(1).getText());
                }
            }
        } catch (NullPointerException e) {
            throw new DotIOException("Found node/edge with no attributes.");
        }
        throw new DotIOException("Found node/edge without a specified weight attribute.");
    }
}