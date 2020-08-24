package main.java.validitychecker;

import main.java.dotio.Dependency;
import main.java.dotio.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class ValidityChecker {

    private ArrayList<Task> _tasks;
    private ArrayList<Dependency> _dependencies;
    private HashMap<String, Integer> _bestStartTimeMap;
    private HashMap<String, Integer> _bestProcessorMap;
    private HashMap<String, Integer> _taskStartTime;

    public ValidityChecker(ArrayList<Task> tasks, ArrayList<Dependency> dependencies, HashMap<String, Integer> bestStartTimeMap, HashMap<String, Integer> bestProcessorMap) {
        _tasks = tasks;
        _dependencies = dependencies;
        _bestStartTimeMap = bestStartTimeMap;
        _bestProcessorMap = bestProcessorMap;
        _taskStartTime = new HashMap<String, Integer>();

        // intialise the task start time
        for (Task task : tasks) {
            _taskStartTime.put(task.getName(), task.getTaskTime());
        }
    }

    public boolean check() {

        for (Dependency dependency : _dependencies) {
            String source = dependency.getSource();
            String dest = dependency.getDest();
            int communicationTime = dependency.getCommunicationTime();
            int delay = 0;

            // if they are on different processors, add a delay
            if (!_bestProcessorMap.get(source).equals(_bestProcessorMap.get(dest))) {
                delay = communicationTime;
            }

            int parentEndTime = _bestStartTimeMap.get(source) + _taskStartTime.get(source) + delay;
            int childStartTime = _bestStartTimeMap.get(dest);

            // check if the destination starts before the parent finishes
            if (childStartTime < parentEndTime) {
                // invalid
                return false;
            }
        }

        // check for overlap between tasks
        for (Task taskA : _tasks) {
            for (Task taskB : _tasks) {
                // if the tasks are the same, continue
                if (taskA.equals(taskB)) {
                    continue;
                }

                String nameA = taskA.getName();
                String nameB = taskB.getName();

                // check if start time overlaps end time and vice versa
                int taskAStartTime = _bestStartTimeMap.get(nameA);
                int taskBStartTime = _bestStartTimeMap.get(nameB);
                int taskAEndTime = taskAStartTime + taskA.getTaskTime();
                int taskBEndTime = taskBStartTime + taskB.getTaskTime();

                // if there is any overlap return false
                if (taskAStartTime < taskBEndTime && taskBStartTime < taskAEndTime) {
                    return false;
                }
            }
        }

        return true;
    }
}
