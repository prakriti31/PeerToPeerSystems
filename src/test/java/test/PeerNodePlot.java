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

public class PeerNodePlot {

    private static final String BASE_DIR = "peernodetests";

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
                XYChart chart = createChart(Paths.get(BASE_DIR, file).toString(), "latency");
                String outputFileName = Paths.get(BASE_DIR, file.replace(".csv", ".png")).toString();
                saveChartAsPNG(chart, outputFileName);
                System.out.println("Saved latency chart for " + file + " as " + outputFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (String file : THROUGHPUT_FILES) {
            try {
                XYChart chart = createChart(Paths.get(BASE_DIR, file).toString(), "throughput");
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
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        int numClients = Integer.parseInt(parts[0].trim());
                        double value = Double.parseDouble(parts[1].trim());
                        if (Double.isFinite(value)) {
                            numClientsList.add(numClients);
                            valuesList.add(value);
                        } else {
                            System.err.println("Skipping line with infinite value: " + line);
                        }
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
                .yAxisTitle(chartType.substring(0, 1).toUpperCase() + chartType.substring(1) + " (seconds)")
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
        String apiName = getApiName(fileName);
        return apiName + "_" + chartType;
    }

    private static String getApiName(String fileName) {
        if (fileName.contains("initialize")) return "Initialize";
        if (fileName.contains("publish")) return "Publish";
        if (fileName.contains("subscribe")) return "Subscribe";
        if (fileName.contains("pull_messages")) return "Pull Messages";
        if (fileName.contains("register_with_indexing_server")) return "Register with Indexing Server";
        if (fileName.contains("create_topic")) return "Create Topic";
        if (fileName.contains("report_metrics")) return "Report Metrics";
        if (fileName.contains("event_log")) return "Event Log";
        return "Unknown API";
    }
}