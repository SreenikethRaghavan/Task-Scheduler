package main.java.visualisation;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.text.Text;
import main.java.visualisation.ganttchart.ScheduleChart;

import java.util.*;
import java.net.URL;

//todo Ask the group if we want the GanttChart class named to be
// changed or we want to retain it. Also whether we want the fields
// in the GanttChart class should start with underscore

public class VisualisationController implements Initializable {

    // FXML Fields

    @FXML
    private AreaChart<String, Number> _CPUChart;
    @FXML
    private AreaChart<Number, Number> _RAMChart;
    @FXML
    private Text _timeElapsedFigure;
    @FXML
    private Text _activeBranchFigure;
    @FXML
    private Text _visitedStatesFigure;
    @FXML
    private Text _completedSchedulesFigure;
    @FXML
    private VBox _bestScheduleParent;
    @FXML
    private VBox _currentScheduleParent;
    @FXML
    private JFXToggleButton _switchThemeButton;

    // Non-FXML Fields

    private XYChart.Series _CPUSeries;
    private XYChart.Series _RAMSeries;

    private ScheduleChart<Number, String> _currentScheduleChart;
    private ScheduleChart<Number, String> _bestScheduleChart;

    private int _numProcessors;
    private ThemeSwitcher _themeSwitcher;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set initial theme when scene is loaded
        Platform.runLater( () -> {
            Scene scene = _switchThemeButton.getScene();
            _themeSwitcher = new ThemeSwitcher(scene, _currentScheduleChart, _bestScheduleChart, "light-style.css");
        });

        _numProcessors = VisualisationDriver.getNumProcessors();
        // initialise the charts
        setUpCPUChart();
        setUpRAMChart();
        setUpScheduleCharts();

        DisplayUpdater displayUpdater = new DisplayUpdater(_visitedStatesFigure, _completedSchedulesFigure, _activeBranchFigure, _timeElapsedFigure,
                                                           _currentScheduleChart, _bestScheduleChart, _CPUSeries, _RAMSeries);

        InformationPoller informationPoller = new InformationPoller(displayUpdater);
    }


    /**
     * Sets up the RAM chart
     */
    private void setUpRAMChart() {

        // create the series data instance
        _RAMSeries = new XYChart.Series();

        // add the series data to the chart
        _RAMChart.getData().add(_RAMSeries);
    }

    /**
     * Sets up the CPU chart.
     */
    private void setUpCPUChart() {

        // create the series data instance
        _CPUSeries = new XYChart.Series();

        // add the series data to the chart
        _CPUChart.getData().add(_CPUSeries);
    }


    private void setUpScheduleCharts() {
        // Setting up the y-axis
        List<String> processorsList = new ArrayList<>();
        for (int i = 0; i < _numProcessors; i++) {
            processorsList.add("Processor ".concat(Integer.toString(i + 1)));
        }
        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setCategories(FXCollections.observableArrayList(processorsList));
        yAxis.setLabel("Processors");

        // Setting up the x-axis
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");

        // Setting up the Schedule chart object and their parents (containers)
        _currentScheduleChart = new ScheduleChart<>(xAxis, yAxis);
        _bestScheduleChart = new ScheduleChart<>(xAxis, yAxis);

        _bestScheduleChart.setBlockHeight(200 / _numProcessors);
        _currentScheduleChart.setBlockHeight(200 / _numProcessors);


        _bestScheduleParent.getChildren().add(_bestScheduleChart);
        _currentScheduleParent.getChildren().add(_currentScheduleChart);
    }

    @FXML
    public void switchTheme() {
        _themeSwitcher.switchTheme();
    }


    public void shutdown() {
        System.exit(0);
    }
}
