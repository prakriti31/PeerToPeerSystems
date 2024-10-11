package test;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

public class PeerNetworkTestRequirement2 {

    private static final String INDEXING_SERVER_URL = "http://localhost:8080/indexing";
    private static final int REQUESTS_PER_NODE = 1000;
    private static final int[] CONCURRENT_NODES = {2, 4, 8, 10, 12, 14, 16, 18, 20};
    private static final int NUM_TOPICS = 1000;

    private static final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) throws Exception {
        initializeIndexingServer();

        List<Integer> peerCounts = new ArrayList<>();
        List<Double> avgResponseTimes = new ArrayList<>();

        for (int numNodes : CONCURRENT_NODES) {
            System.out.println("Initializing loop for " + numNodes + " peers...");
            double avgResponseTime = runTest(numNodes);
            System.out.printf("Average response time for %d concurrent nodes: %.2f ms%n%n", numNodes, avgResponseTime);

            // Add data for plotting
            peerCounts.add(numNodes);
            avgResponseTimes.add(avgResponseTime);
        }

        // Plot the results
        XYChart chart = createChart(peerCounts, avgResponseTimes);
        saveChartAsPNG(chart, "peer_network_performance.png");
        System.out.println("Saved chart as peer_network_performance.png");
    }

    private static void initializeIndexingServer() {
        for (int i = 1; i <= NUM_TOPICS; i++) {
            String topic = "topic" + i;
            restTemplate.postForObject(INDEXING_SERVER_URL + "/register",
                    Map.of("node_id", "peer" + i, "topics", List.of(topic)),
                    Map.class);
        }
        System.out.println("Indexing server initialized with " + NUM_TOPICS + " topics.");
    }

    static double runTest(int numNodes) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(numNodes);
        List<Callable<List<Long>>> tasks = new ArrayList<>();

        for (int i = 0; i < numNodes; i++) {
            tasks.add(() -> {
                List<Long> responseTimes = new ArrayList<>();
                for (int j = 0; j < REQUESTS_PER_NODE; j++) {
                    String topic = "topic" + (j % NUM_TOPICS + 1);
                    long startTime = System.nanoTime();
                    restTemplate.getForObject(INDEXING_SERVER_URL + "/query_topic/" + topic, Map.class);
                    long endTime = System.nanoTime();
                    responseTimes.add((endTime - startTime) / 1_000_000); // Convert to milliseconds
                }
                return responseTimes;
            });
        }

        List<Future<List<Long>>> results = executor.invokeAll(tasks);
        executor.shutdown();

        List<Long> allResponseTimes = new ArrayList<>();
        for (Future<List<Long>> result : results) {
            allResponseTimes.addAll(result.get());
        }

        return allResponseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    // Method to create a chart
    private static XYChart createChart(List<Integer> peerCounts, List<Double> avgResponseTimes) {
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Peer Network Performance")
                .xAxisTitle("Number of Peers")
                .yAxisTitle("Average Response Time (ms)")
                .theme(Styler.ChartTheme.Matlab)
                .build();

        chart.addSeries("Response Time", peerCounts, avgResponseTimes);
        return chart;
    }

    // Method to save chart as PNG
    private static void saveChartAsPNG(XYChart chart, String fileName) {
        try {
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.PNG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
