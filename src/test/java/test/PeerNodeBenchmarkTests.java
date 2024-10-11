package test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PeerNodeBenchmarkTests {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private static final String OUTPUT_DIR = "peernodetests"; // Directory for CSV files

    // Constants defining API endpoints and thread timeout
    private static final int THREAD_TERMINATION_TIMEOUT = 1; // Timeout for threads (in minutes)
    private static final String INITIALIZE_ENDPOINT = "/peer/initialize"; // Initialize peer node endpoint
    private static final String PUBLISH_ENDPOINT = "/peer/publish"; // Publish topic endpoint
    private static final String SUBSCRIBE_ENDPOINT = "/peer/subscribe/"; // Subscribe to topic endpoint
    private static final String PULL_MESSAGES_ENDPOINT = "/peer/pull_messages/"; // Pull messages from topic endpoint
    private static final String REGISTER_WITH_INDEXING_SERVER_ENDPOINT = "/peer/register_with_indexing_server"; // Register peer with indexing server
    private static final String CREATE_TOPIC_ENDPOINT = "/peer/create_topic"; // Create new topic endpoint
    private static final String REPORT_METRICS_ENDPOINT = "/peer/report_metrics"; // Report metrics endpoint
    private static final String EVENT_LOG_ENDPOINT = "/peer/event_log"; // Log events endpoint

    // Constructor to initialize the base URL and RestTemplate object for HTTP requests
    public PeerNodeBenchmarkTests(String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        createOutputDirectory(); // Create the output directory at initialization
    }

    // Create the output directory if it doesn't exist
    private void createOutputDirectory() {
        File directory = new File(OUTPUT_DIR);
        if (!directory.exists()) {
            directory.mkdir(); // Create the directory if it does not exist
        }
    }

    // Method to write latency and throughput data to CSV files
    private void writeToCSV(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "/" + fileName, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Benchmark the initialize API
    public double benchmarkInitialize(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, INITIALIZE_ENDPOINT, "initialize_latency.csv", "initialize_throughput.csv");
    }

    // Benchmark the publish API
    public double benchmarkPublish(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, PUBLISH_ENDPOINT, "publish_latency.csv", "publish_throughput.csv");
    }

    // Benchmark the subscribe API for a given topic
    public double benchmarkSubscribe(int numClients, String topic) throws InterruptedException {
        return benchmarkAPI(numClients, SUBSCRIBE_ENDPOINT + topic, "subscribe_latency.csv", "subscribe_throughput.csv");
    }

    // Benchmark the pull messages API for a given topic
    public double benchmarkPullMessages(int numClients, String topic) throws InterruptedException {
        return benchmarkAPI(numClients, PULL_MESSAGES_ENDPOINT + topic, "pull_messages_latency.csv", "pull_messages_throughput.csv");
    }

    // Benchmark the register with indexing server API
    public double benchmarkRegisterWithIndexingServer(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, REGISTER_WITH_INDEXING_SERVER_ENDPOINT, "register_with_indexing_server_latency.csv", "register_with_indexing_server_throughput.csv");
    }

    // Benchmark the create topic API
    public double benchmarkCreateTopic(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, CREATE_TOPIC_ENDPOINT, "create_topic_latency.csv", "create_topic_throughput.csv");
    }

    // Benchmark the report metrics API
    public double benchmarkReportMetrics(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, REPORT_METRICS_ENDPOINT, "report_metrics_latency.csv", "report_metrics_throughput.csv");
    }

    // Benchmark the event log API
    public double benchmarkEventLog(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, EVENT_LOG_ENDPOINT, "event_log_latency.csv", "event_log_throughput.csv");
    }

    // General method to benchmark an API for a given number of clients and record latency/throughput
    private double benchmarkAPI(int numClients, String endpoint, String latencyCsvFileName, String throughputCsvFileName) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numClients); // Thread pool for concurrent clients
        long startTime = System.currentTimeMillis(); // Record start time
        long[] totalLatency = {0}; // Array to hold total latency for synchronization

        // Submit tasks for each client to execute the API request
        for (int i = 0; i < numClients; i++) {
            executor.submit(() -> {
                long requestStartTime = System.currentTimeMillis(); // Record request start time
                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + endpoint, createNodeRequestBody(), String.class);
                long requestEndTime = System.currentTimeMillis(); // Record request end time
                long requestLatency = requestEndTime - requestStartTime; // Calculate request latency

                // Synchronize the update of total latency across threads
                synchronized (this) {
                    totalLatency[0] += requestLatency;
                }
            });
        }

        executor.shutdown(); // Shutdown the executor
        executor.awaitTermination(THREAD_TERMINATION_TIMEOUT, TimeUnit.MINUTES); // Wait for tasks to finish
        long endTime = System.currentTimeMillis(); // Record end time

        double timeTakenInSeconds = (endTime - startTime) / 1000.0; // Calculate time taken in seconds
        System.out.println("Clients: " + numClients + " | Time taken for " + endpoint + ": " + timeTakenInSeconds + " seconds");

        // Calculate and save average latency
        double averageLatency = (double) totalLatency[0] / numClients;
        writeToCSV(latencyCsvFileName, numClients + "," + averageLatency);

        // Calculate and save throughput (requests per second)
        double throughput = numClients / timeTakenInSeconds;
        writeToCSV(throughputCsvFileName, numClients + "," + throughput);

        return averageLatency; // Return average latency for further use
    }

    // Create the body of the request for the peer node
    private Map<String, Object> createNodeRequestBody() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("node_id", "peerNode1");
        requestBody.put("topics", List.of("topic1", "topic2")); // Example topics
        return requestBody;
    }

    // Main method to run benchmarks for various APIs
    public static void main(String[] args) throws InterruptedException {
        PeerNodeBenchmarkTests benchmarkTestsClient = new PeerNodeBenchmarkTests("http://localhost:8080"); // Set base URL for requests

        // Configure the number of clients for benchmarking
        final int initialClients = 1;
        final int maxClients = 8; // Increase this as needed
        final int increment = 1;

        // Create CSV files with headers for latency and throughput data
        createCSVFile("initialize_latency.csv");
        createCSVFile("initialize_throughput.csv");
        createCSVFile("publish_latency.csv");
        createCSVFile("publish_throughput.csv");
        createCSVFile("subscribe_latency.csv");
        createCSVFile("subscribe_throughput.csv");
        createCSVFile("pull_messages_latency.csv");
        createCSVFile("pull_messages_throughput.csv");
        createCSVFile("register_with_indexing_server_latency.csv");
        createCSVFile("register_with_indexing_server_throughput.csv");
        createCSVFile("create_topic_latency.csv");
        createCSVFile("create_topic_throughput.csv");
        createCSVFile("report_metrics_latency.csv");
        createCSVFile("report_metrics_throughput.csv");
        createCSVFile("event_log_latency.csv");
        createCSVFile("event_log_throughput.csv");

        // Run the benchmarks for each API with increasing number of clients
        for (int numClients = initialClients; numClients <= maxClients; numClients += increment) {
            System.out.println("\nRunning benchmark with " + numClients + " clients:");

            // Run benchmarks for each API and save results to CSV files
            benchmarkTestsClient.benchmarkInitialize(numClients);
            benchmarkTestsClient.benchmarkPublish(numClients);
            benchmarkTestsClient.benchmarkSubscribe(numClients, "topic1");
            benchmarkTestsClient.benchmarkPullMessages(numClients, "topic1");
            benchmarkTestsClient.benchmarkRegisterWithIndexingServer(numClients);
            benchmarkTestsClient.benchmarkCreateTopic(numClients);
            benchmarkTestsClient.benchmarkReportMetrics(numClients);
            benchmarkTestsClient.benchmarkEventLog(numClients);
        }
    }

    // Utility method to create CSV files with headers for recording data
    private static void createCSVFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "/" + fileName))) {
            writer.write("Number of Clients,Time Taken (seconds)");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}