package test;

import org.knowm.xchart.BitmapEncoder; // Used to save charts as image files
import org.knowm.xchart.XYChart; // Represents an XY chart (for line charts)
import org.knowm.xchart.XYChartBuilder; // Helps to easily build XY charts
import org.knowm.xchart.style.Styler; // Allows customizing the chart style

import java.io.BufferedReader; // Used for reading text files
import java.io.FileReader; // Used to read files line-by-line
import java.io.IOException; // Handles errors related to file operations
import java.nio.file.Paths; // Simplifies file path creation
import java.util.ArrayList; // Used to store lists of data
import java.util.List; // Generic list class for storing collections

public class IndexingServerPlot {

    // Directory where CSV and PNG files are stored
    private static final String BASE_DIR = "indexingservertests";

    // List of CSV files that store latency data for different operations
    private static final String[] LATENCY_FILES = {
            "register_node_latency.csv",
            "unregister_node_latency.csv",
            "update_topics_latency.csv",
            "query_topic_latency.csv",
            "get_metrics_latency.csv"
    };

    // List of CSV files that store throughput data for different operations
    private static final String[] THROUGHPUT_FILES = {
            "register_node_throughput.csv",
            "unregister_node_throughput.csv",
            "update_topics_throughput.csv",
            "query_topic_throughput.csv",
            "get_metrics_throughput.csv"
    };

    public static void main(String[] args) {
        // Loop through all latency CSV files and generate latency charts
        for (String file : LATENCY_FILES) {
            try {
                // Create a chart from the data in the CSV file
                XYChart chart = createChart(Paths.get(BASE_DIR, file).toString(), "Latency");

                // Define the name of the PNG file for the chart
                String outputFileName = Paths.get(BASE_DIR, file.replace(".csv", ".png")).toString();

                // Save the generated chart as a PNG image
                saveChartAsPNG(chart, outputFileName);

                // Print message indicating the chart has been saved
                System.out.println("Saved latency chart for " + file + " as " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace(); // Handle any IO exceptions that occur during file reading or writing
            }
        }

        // Loop through all throughput CSV files and generate throughput charts
        for (String file : THROUGHPUT_FILES) {
            try {
                // Create a chart from the data in the CSV file
                XYChart chart = createChart(Paths.get(BASE_DIR, file).toString(), "Throughput");

                // Define the name of the PNG file for the chart
                String outputFileName = Paths.get(BASE_DIR, file.replace(".csv", ".png")).toString();

                // Save the generated chart as a PNG image
                saveChartAsPNG(chart, outputFileName);

                // Print message indicating the chart has been saved
                System.out.println("Saved throughput chart for " + file + " as " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace(); // Handle any IO exceptions that occur during file reading or writing
            }
        }
    }

    // Creates an XY chart from the data in the specified CSV file
    private static XYChart createChart(String fileName, String chartType) throws IOException {
        List<Integer> numClientsList = new ArrayList<>(); // List to store the number of clients
        List<Double> valuesList = new ArrayList<>(); // List to store the corresponding latency/throughput values

        // Open the CSV file for reading
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            // Skip the header line of the CSV file
            br.readLine();

            // Read each line of the CSV file
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(","); // Split the line into columns by comma

                // Ensure there are at least two parts (numClients, value)
                if (parts.length >= 2) {
                    try {
                        // Parse the number of clients and the latency/throughput value
                        int numClients = Integer.parseInt(parts[0].trim());
                        double value = Double.parseDouble(parts[1].trim());

                        // Add parsed data to the lists
                        numClientsList.add(numClients);
                        valuesList.add(value);
                    } catch (NumberFormatException e) {
                        // If parsing fails, print a warning and skip the line
                        System.err.println("Skipping line due to number format issue: " + line);
                    }
                } else {
                    // Print warning if the line is incorrectly formatted
                    System.err.println("Skipping line due to incorrect format: " + line);
                }
            }
        }

        // Throw an exception if no valid data was found in the CSV file
        if (numClientsList.isEmpty() || valuesList.isEmpty()) {
            throw new IllegalArgumentException("Y-Axis data cannot be empty!!!");
        }

        // Build a new chart with the specified width, height, title, and axis labels
        XYChart chart = new XYChartBuilder()
                .width(800) // Set chart width
                .height(600) // Set chart height
                .title(getChartTitle(fileName, chartType)) // Set chart title dynamically based on the file
                .xAxisTitle("Number of Clients") // Label for X-axis
                .yAxisTitle(chartType + " (seconds)") // Label for Y-axis
                .theme(Styler.ChartTheme.Matlab) // Set chart theme to 'Matlab'
                .build();

        // Add the data series to the chart
        chart.addSeries(chartType + " Values", numClientsList, valuesList);

        return chart; // Return the generated chart
    }

    // Saves the given chart as a PNG file
    private static void saveChartAsPNG(XYChart chart, String fileName) {
        try {
            // Save the chart as a PNG file at the specified location
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            e.printStackTrace(); // Handle any IO exceptions that occur during file saving
        }
    }

    // Returns a chart title based on the file name and chart type (Latency or Throughput)
    private static String getChartTitle(String fileName, String chartType) {
        // Use a switch statement to determine the correct title based on the file name
        return switch (fileName) {
            case "register_node_latency.csv" -> "Register Node Latency Benchmark";
            case "unregister_node_latency.csv" -> "Unregister Node Latency Benchmark";
            case "update_topics_latency.csv" -> "Update Topics Latency Benchmark";
            case "query_topic_latency.csv" -> "Query Topic Latency Benchmark";
            case "get_metrics_latency.csv" -> "Get Metrics Latency Benchmark";
            case "register_node_throughput.csv" -> "Register Node Throughput Benchmark";
            case "unregister_node_throughput.csv" -> "Unregister Node Throughput Benchmark";
            case "update_topics_throughput.csv" -> "Update Topics Throughput Benchmark";
            case "query_topic_throughput.csv" -> "Query Topic Throughput Benchmark";
            case "get_metrics_throughput.csv" -> "Get Metrics Throughput Benchmark";
            default -> "Benchmark Results"; // Default title for unknown files
        };
    }
}
