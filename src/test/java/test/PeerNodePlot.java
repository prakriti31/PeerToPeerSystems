package test;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeerNodePlot {

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
        for (String file : LATENCY_FILES) {
            try {
                XYChart chart = createChart(file, "Latency");
                String outputFileName = file.replace(".csv", ".png");
                saveChartAsPNG(chart, outputFileName);
                System.out.println("Saved latency chart for " + file + " as " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String file : THROUGHPUT_FILES) {
            try {
                XYChart chart = createChart(file, "Throughput");
                String outputFileName = file.replace(".csv", ".png");
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
            default -> "Benchmark Results";
        };
    }
}
