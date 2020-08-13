package main.java.scheduler;

import main.java.dotio.Dependency;
import main.java.dotio.Task;
import main.java.dotio.TaskGraph;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Takes the graph from the dotIO.
 * currentState and bestState will be constantly updated
 *
 * @// TODO: 10/08/20 getcurrentstate, getbeststate, dfs methods.
 */
public abstract class Scheduler {
    TaskGraph input;
    public int numProcessors;
    Schedule currentState;
    Schedule bestState;
    ThreadPoolExecutor threadPool;
    HashMap<String, Integer> startTimeMap = new HashMap<>();
    HashMap<String, Integer> processorMap = new HashMap<>();
    HashMap<String, Node> nodeMap = new HashMap<>();
    HashMap<Node, List<Edge>> edgeMap = new HashMap<>();

    public abstract void execute();

    public HashMap<String, Integer> getStartTimeMap() {
        return startTimeMap;
    }

    public HashMap<String, Integer> getProcessorMap() {
        return processorMap;
    }
}
