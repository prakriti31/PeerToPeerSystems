package test;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

public class PeerNetworkTestRequirement2 {

    // Constants for indexing server URL, number of requests per node, and configuration for concurrent nodes and topics
    private static final String INDEXING_SERVER_URL = "http://localhost:8080/indexing"; // Base URL for the indexing server
    private static final int REQUESTS_PER_NODE = 1000; // Number of requests each peer node will make
    private static final int[] CONCURRENT_NODES = {2, 4, 8, 10, 12, 14, 16, 18, 20}; // Array of peer node counts to test with
    private static final int NUM_TOPICS = 1000; // Number of topics to be pre-registered in the indexing server

    private static final RestTemplate restTemplate = new RestTemplate(); // RestTemplate object for making HTTP requests

    public static void main(String[] args) throws Exception {
        // Initialize the indexing server by registering topics
        initializeIndexingServer();

        // Lists to hold the number of peer nodes and their corresponding average response times for charting
        List<Integer> peerCounts = new ArrayList<>();
        List<Double> avgResponseTimes = new ArrayList<>();

        // Loop through the various peer node counts and run tests
        for (int numNodes : CONCURRENT_NODES) {
            System.out.println("Initializing loop for " + numNodes + " peers...");

            // Run test for current number of nodes and capture the average response time
            double avgResponseTime = runTest(numNodes);
            System.out.printf("Average response time for %d concurrent nodes: %.2f ms%n%n", numNodes, avgResponseTime);

            // Add data for plotting (peer node count and average response time)
            peerCounts.add(numNodes);
            avgResponseTimes.add(avgResponseTime);
        }

        // After all tests, plot the results in a chart
        XYChart chart = createChart(peerCounts, avgResponseTimes);

        // Save the generated chart as a PNG image
        saveChartAsPNG(chart, "peer_network_performance.png");
        System.out.println("Saved chart as peer_network_performance.png");
    }

    // Method to initialize the indexing server by registering 1000 topics
    private static void initializeIndexingServer() {
        for (int i = 1; i <= NUM_TOPICS; i++) {
            // Generate a topic name for each iteration
            String topic = "topic" + i;

            // Register the topic with the indexing server by making a POST request
            restTemplate.postForObject(
                    INDEXING_SERVER_URL + "/register",
                    Map.of("node_id", "peer" + i, "topics", List.of(topic)),
                    Map.class
            );
        }
        // Print out confirmation that the indexing server has been initialized with topics
        System.out.println("Indexing server initialized with " + NUM_TOPICS + " topics.");
    }

    // Method to run the performance test for a given number of nodes
    static double runTest(int numNodes) throws Exception {
        // ExecutorService to handle multithreading and concurrency (fixed thread pool with numNodes threads)
        ExecutorService executor = Executors.newFixedThreadPool(numNodes);

        // List of tasks, each representing a peer making a number of requests
        List<Callable<List<Long>>> tasks = new ArrayList<>();

        // For each peer node, add a task to perform requests
        for (int i = 0; i < numNodes; i++) {
            tasks.add(() -> {
                List<Long> responseTimes = new ArrayList<>();

                // Each peer makes REQUESTS_PER_NODE number of requests
                for (int j = 0; j < REQUESTS_PER_NODE; j++) {
                    String topic = "topic" + (j % NUM_TOPICS + 1); // Select topic in round-robin fashion

                    // Record the start time
                    long startTime = System.nanoTime();

                    // Perform GET request to query the topic from the indexing server
                    restTemplate.getForObject(INDEXING_SERVER_URL + "/query_topic/" + topic, Map.class);

                    // Record the end time
                    long endTime = System.nanoTime();

                    // Calculate the response time in milliseconds and add it to the list
                    responseTimes.add((endTime - startTime) / 1_000_000); // Convert nanoseconds to milliseconds
                }
                // Return the list of response times for this peer
                return responseTimes;
            });
        }

        // Invoke all tasks and collect their futures (each future holds the result of a peer's task)
        List<Future<List<Long>>> results = executor.invokeAll(tasks);
        executor.shutdown(); // Shut down the executor after tasks are submitted

        // Aggregate all the response times from all peers
        List<Long> allResponseTimes = new ArrayList<>();
        for (Future<List<Long>> result : results) {
            allResponseTimes.addAll(result.get()); // Collect response times from each future
        }

        // Calculate and return the average response time across all peers
        return allResponseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    // Method to create an XY chart from peer counts and their corresponding average response times
    private static XYChart createChart(List<Integer> peerCounts, List<Double> avgResponseTimes) {
        // Build the XYChart with title, x-axis, and y-axis labels
        XYChart chart = new XYChartBuilder()
                .width(800) // Chart width
                .height(600) // Chart height
                .title("Peer Network Performance") // Chart title
                .xAxisTitle("Number of Peers") // X-axis label
                .yAxisTitle("Average Response Time (ms)") // Y-axis label
                .theme(Styler.ChartTheme.Matlab) // Use MATLAB theme for styling
                .build();

        // Add the data series to the chart (X: peer counts, Y: average response times)
        chart.addSeries("Response Time", peerCounts, avgResponseTimes);
        return chart; // Return the created chart
    }

    // Method to save the XYChart as a PNG image
    private static void saveChartAsPNG(XYChart chart, String fileName) {
        try {
            // Use BitmapEncoder to save the chart as a PNG file
            BitmapEncoder.saveBitmap(chart, fileName, BitmapEncoder.BitmapFormat.PNG);
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace if an error occurs while saving the file
        }
    }
}
