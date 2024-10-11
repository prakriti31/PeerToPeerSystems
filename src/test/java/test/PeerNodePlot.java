package test;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PeerNodePlot {

    // Directory containing the benchmark CSV files
    private static final String DIRECTORY = "peernodetests/";

    // List of latency benchmark files
    private static final String[] LATENCY_FILES = {
            "initialize_latency.csv",
            "publish_latency.csv",
            "subscribe_latency.csv",
            "pull_messages_latency.csv",
            "register_with_indexing_server_latency.csv",
            "create_topic_latency.csv",
            "report_metrics_latency.csv",
            "event_log_latency.csv"
    };

    // List of throughput benchmark files
    private static final String[] THROUGHPUT_FILES = {
            "initialize_throughput.csv",
            "publish_throughput.csv",
            "subscribe_throughput.csv",
            "pull_messages_throughput.csv",
            "register_with_indexing_server_throughput.csv",
            "create_topic_throughput.csv",
            "report_metrics_throughput.csv",
            "event_log_throughput.csv"
    };

    public static void main(String[] args) {
        // Process each latency file to create and save corresponding charts
        for (String file : LATENCY_FILES) {
            try {
                // Create the chart for latency data
                XYChart chart = createChart(DIRECTORY + file, "Latency");
                // Save the chart as a PNG image
                String outputFileName = DIRECTORY + file.replace(".csv", ".png");
                saveChartAsPNG(chart, outputFileName);
                System.out.println("Saved latency chart for " + file + " as " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace();  // Handle any I/O exceptions
            }
        }

        // Process each throughput file to create and save corresponding charts
        for (String file : THROUGHPUT_FILES) {
            try {
                // Create the chart for throughput data
                XYChart chart = createChart(DIRECTORY + file, "Throughput");
                // Save the chart as a PNG image
                String outputFileName = DIRECTORY + file.replace(".csv", ".png");
                saveChartAsPNG(chart, outputFileName);
                System.out.println("Saved throughput chart for " + file + " as " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace();  // Handle any I/O exceptions
            }
        }
    }

    /**
     * Reads data from a CSV file and creates an XYChart.
     * @param fileName The name of the CSV file.
     * @param chartType The type of chart (e.g., "Latency", "Throughput").
     * @return The created XYChart object.
     * @throws IOException If there is an error reading the file.
     */
    private static XYChart createChart(String fileName, String chartType) throws IOException {
        // Lists to hold the number of clients and the corresponding values (latency or throughput)
        List<Integer> numClientsList = new ArrayList<>();
        List<Double> valuesList = new ArrayList<>();

        // Read the CSV file
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            // Skip header
            br.readLine();

            // Read the rest of the lines and extract data
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        // Parse the number of clients and the corresponding value
                        int numClients = Integer.parseInt(parts[0].trim());
                        double value = Double.parseDouble(parts[1].trim());
                        numClientsList.add(numClients);
                        valuesList.add(value);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping line due to number format issue: " + line);
                    }
                } else {
                    System.err.println("Skipping line due to incorrect format: " + line);
                }
            }
        }

        // Ensure that there is data to plot
        if (numClientsList.isEmpty() || valuesList.isEmpty()) {
            throw new IllegalArgumentException("Y-Axis data cannot be empty!!!");
        }

        // Create a new XYChart with the appropriate labels and theme
        XYChart chart = new XYChartBuilder()
                .width(800)  // Width of the chart
                .height(600) // Height of the chart
                .title(getChartTitle(fileName, chartType)) // Title of the chart
                .xAxisTitle("Number of Clients") // X-axis label
                .yAxisTitle(chartType + " (seconds)") // Y-axis label
                .theme(Styler.ChartTheme.Matlab) // Chart theme (can be customized)
                .build();

        // Add the data series to the chart
        chart.addSeries(chartType + " Values", numClientsList, valuesList);

        return chart;
    }

    /**
     * Saves a chart as a PNG file.
     * @param chart The XYChart object to be saved.
     * @param fileName The name of the PNG file.
     */
    private static void saveChartAsPNG(XYChart chart, String fileName) {
        try {
            // Encode and save the chart as a PNG file
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            e.printStackTrace();  // Handle any I/O exceptions
        }
    }

    /**
     * Determines the title of the chart based on the file name and chart type.
     * @param fileName The name of the CSV file.
     * @param chartType The type of chart (e.g., "Latency", "Throughput").
     * @return The appropriate title for the chart.
     */
    private static String getChartTitle(String fileName, String chartType) {
        // Use a switch statement to map each file to a meaningful chart title
        return switch (fileName) {
            case "initialize_latency.csv" -> "Initialize Latency Benchmark";
            case "publish_latency.csv" -> "Publish Latency Benchmark";
            case "subscribe_latency.csv" -> "Subscribe Latency Benchmark";
            case "pull_messages_latency.csv" -> "Pull Messages Latency Benchmark";
            case "register_with_indexing_server_latency.csv" -> "Register with Indexing Server Latency Benchmark";
            case "create_topic_latency.csv" -> "Create Topic Latency Benchmark";
            case "report_metrics_latency.csv" -> "Report Metrics Latency Benchmark";
            case "event_log_latency.csv" -> "Event Log Latency Benchmark";
            case "initialize_throughput.csv" -> "Initialize Throughput Benchmark";
            case "publish_throughput.csv" -> "Publish Throughput Benchmark";
            case "subscribe_throughput.csv" -> "Subscribe Throughput Benchmark";
            case "pull_messages_throughput.csv" -> "Pull Messages Throughput Benchmark";
            case "register_with_indexing_server_throughput.csv" -> "Register with Indexing Server Throughput Benchmark";
            case "create_topic_throughput.csv" -> "Create Topic Throughput Benchmark";
            case "report_metrics_throughput.csv" -> "Report Metrics Throughput Benchmark";
            case "event_log_throughput.csv" -> "Event Log Throughput Benchmark";
            default -> "Benchmark Results";  // Default title if none match
        };
    }
}
