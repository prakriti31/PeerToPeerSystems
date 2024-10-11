package test;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IndexingServerPlot {

    private static final String BASE_DIR = "indexingservertests"; // Directory for CSV and PNG files

    private static final String[] LATENCY_FILES = {
            "register_node_latency.csv",
            "unregister_node_latency.csv",
            "update_topics_latency.csv",
            "query_topic_latency.csv",
            "get_metrics_latency.csv"
    };

    private static final String[] THROUGHPUT_FILES = {
            "register_node_throughput.csv",
            "unregister_node_throughput.csv",
            "update_topics_throughput.csv",
            "query_topic_throughput.csv",
            "get_metrics_throughput.csv"
    };

    public static void main(String[] args) {
        for (String file : LATENCY_FILES) {
            try {
                XYChart chart = createChart(Paths.get(BASE_DIR, file).toString(), "Latency");
                String outputFileName = Paths.get(BASE_DIR, file.replace(".csv", ".png")).toString();
                saveChartAsPNG(chart, outputFileName);
                System.out.println("Saved latency chart for " + file + " as " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String file : THROUGHPUT_FILES) {
            try {
                XYChart chart = createChart(Paths.get(BASE_DIR, file).toString(), "Throughput");
                String outputFileName = Paths.get(BASE_DIR, file.replace(".csv", ".png")).toString();
                saveChartAsPNG(chart, outputFileName);
                System.out.println("Saved throughput chart for " + file + " as " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static XYChart createChart(String fileName, String chartType) throws IOException {
        List<Integer> numClientsList = new ArrayList<>();
        List<Double> valuesList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            // Skip header
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
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

        if (numClientsList.isEmpty() || valuesList.isEmpty()) {
            throw new IllegalArgumentException("Y-Axis data cannot be empty!!!");
        }

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title(getChartTitle(fileName, chartType))
                .xAxisTitle("Number of Clients")
                .yAxisTitle(chartType + " (seconds)")
                .theme(Styler.ChartTheme.Matlab)
                .build();

        chart.addSeries(chartType + " Values", numClientsList, valuesList);

        return chart;
    }

    private static void saveChartAsPNG(XYChart chart, String fileName) {
        try {
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getChartTitle(String fileName, String chartType) {
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
            default -> "Benchmark Results";
        };
    }
}
