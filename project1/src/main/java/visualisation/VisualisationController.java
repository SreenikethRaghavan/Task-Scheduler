package main.java.visualisation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.chart.XYChart.Series;
import main.java.dotio.Task;
import main.java.dotio.TaskGraph;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import main.java.dataretriever.SystemPerformanceRetriever;
import main.java.scheduler.Scheduler;
import main.java.visualisation.ScheduleChart.ExtraData;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//todo Ask the group if we want the GanttChart class named to be
// changed or we want to retain it. Also whether we want the fields
// in the GanttChart class should start with underscore

public class VisualisationController {

    // FXML Fields
    @FXML
    private VBox _bestScheduleParent;
    @FXML
    private VBox _currentScheduleParent;

    // Non-FXML Fields
    private ScheduleChart<Number, String> _currentScheduleChart;
    private ScheduleChart<Number, String> _bestScheduleChart;
    private Scheduler _scheduler;
    private List<Task> _taskList;
    private int _numProcessors;

    @FXML
    void initialize() {
        _scheduler = VisualisationDriver.getScheduler();
        _taskList = VisualisationDriver.getTaskGraph().getTasks();
        _numProcessors = VisualisationDriver.getNumProcessors();
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class VisualisationController implements Initializable {

    private int _refreshRate = 1000;
    private SystemPerformanceRetriever _performanceRetriever;

    private XYChart.Series _CPUSeries;
    private XYChart.Series _RAMSeries;

    private Scheduler _sc;
    private Timer _timer;

    private int _seconds;
    private int _milliseconds;

    @FXML
    private VBox currentSchedule;

    @FXML
    private VBox bestSchedule;

    @FXML
    private VBox statistics;

    @FXML
    private VBox CPUParent;

    @FXML
    private AreaChart<String, Number> CPUChart;

    @FXML
    private VBox RAMParent;

    @FXML
    private AreaChart<Number, Number> RAMChart;

    @FXML
    private Text timeElapsedFigure;

    @FXML
    private Text visitedStatesFigure;

    @FXML
    private Text completedSchedulesFigure;

    /**
     * Updates the refresh rate, yet to be implemented
     * @param refreshRate
     */
    private void updateRefreshRate(int refreshRate) {

        _refreshRate = refreshRate;
    }

    /**
     * starts the timer for the total time and updates every 10 milliseconds.
     */
    private void startTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(0),
                e -> updateTime()),
                new KeyFrame(Duration.millis(10)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Updates the time from the timer.
     */
    private void updateTime() {
        if (_milliseconds < 99) {
            _milliseconds++;
        } else {
            _milliseconds = 0;
            _seconds++;
        }
        timeElapsedFigure.setText(Integer.toString(_seconds).concat(".").concat(Integer.toString(_milliseconds)).concat("s"));
    }

    /**
     * Updates the statistics such as the visited states and the completed schedules
     */
    private void updateStatistics() {

        long visitedStates = _sc.getTotalStatesVisited();
        long completedSchedules = _sc.getCompleteStatesVisited();

        visitedStatesFigure.setText(Long.toString(visitedStates));
        completedSchedulesFigure.setText(Long.toString(completedSchedules));
    }

    /**
     * Sets up the RAM chart
     */
    private void setUpRAMChart() {

        // create the series data instance
        _RAMSeries = new XYChart.Series();

        // add the series data to the chart
        RAMChart.getData().add(_RAMSeries);
    }

    /**
     * Sets up the CPU chart.
     */
    private void setUpCPUChart() {

        // create the series data instance
        _CPUSeries = new XYChart.Series();

        // add the series data to the chart
        CPUChart.getData().add(_CPUSeries);
    }

    /**
     * Adds ram data to the series and updates the chart.
     */
    private void addRAMChartData() {

        // get the machine's CPU Usage data
        long RAMUsageInBytes = _performanceRetriever.getRAMUsageBytes();

        _RAMSeries.getData().add(new XYChart.Data(Integer.toString(_seconds), RAMUsageInBytes));
    }

    /**
     * Adds CPU data to the series and updates the chart.
     */
    private void addCPUChartData() {

        // get the machine's CPU Usage data
        double CPUUsage = _performanceRetriever.getCPUUsagePercent();

        _CPUSeries.getData().add(new XYChart.Data(Integer.toString(_seconds), CPUUsage));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        _sc = VisualisationDriver.sc;
        _performanceRetriever = new SystemPerformanceRetriever();

        // initialise the time to 0.00
        _seconds = 0;
        _milliseconds = 0;

        // start the overall timer
        startTimer();

        // initialise the charts
        setUpCPUChart();
        setUpRAMChart();

        // Setup polling the scheduler
        _timer = new Timer();

        _timer.schedule(new TimerTask() {

            @Override
            public void run() {
                refreshStats();
            }
        }, 1000, 1000);
        chartSetup();
    }

    private void chartSetup() {
        // Setting up the y-axis
        List<String> processorsList = new ArrayList<String>();
        for (int i=0; i < _numProcessors; i++) {
            processorsList.add("Processor "+i+1);
        }
        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setCategories(FXCollections.observableArrayList(processorsList));
        yAxis.setLabel("Processors");

        // Setting up the x-axis
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");

        // Setting up the Schedule chart object and their parents (containers)
        _currentScheduleChart = new ScheduleChart<Number, String>(xAxis, yAxis);
        _bestScheduleChart = new ScheduleChart<Number, String>(xAxis, yAxis);
        _bestScheduleParent.getChildren().add(_bestScheduleChart);
        _currentScheduleParent.getChildren().add(_currentScheduleChart);
    }

    //todo make sure that the following case related to this method is handled: When the scheduler has not found a
    // best schedule yet and this method is called. Either prevent this from happening or handle this situation inside the method
    private void updateScheduleChart() {
        // Create Series objects. Each object will act as a row in the chart
        Series[] seriesArray = new Series[_numProcessors];
        for (int i = 0; i < _numProcessors; i++) {
            seriesArray[i] = new Series();
        }

        // Run through each task, create an XYChart.Data object and put
        // this object in the Series object which corresponds to the processor this task is scheduled on
        for (Task task : _taskList) {
            int processorOfTask = _scheduler.getProcessorMap().get(task.getName());
            int taskStartTime = _scheduler.getStartTimeMap().get(task.getName());
            int taskTime = task.getTaskTime();

            XYChart.Data taskData = new XYChart.Data(taskStartTime, "Processor "+processorOfTask, new ExtraData(taskTime));

            // -1 has been used below because the seriesArray is 0 indexed whereas the processor numbers are 1 indexed
            seriesArray[processorOfTask-1].getData().add(taskData);
        }

        // Put the Series objects generated above in the chart after clearing the chart's existing data
        _bestScheduleChart.getData().clear();
        for (Series series : seriesArray) {
            _bestScheduleChart.getData().add(series);
        }
    }
                updateStatistics();

                // queue tasks on the other thread
                Platform.runLater(new Runnable() {
                    @Override public void run() {
//                        CPUSeries.getData().add(new XYChart.Data<>(Integer.toString(i++),10));
                        addCPUChartData();
                        addRAMChartData();
                    }
                });
            }

        }, _refreshRate, 1000);
    }
}
