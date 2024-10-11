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

public class IndexingServerBenchmarkTests {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    // Constants
    private static final int THREAD_TERMINATION_TIMEOUT = 1; // in minutes
    private static final String REGISTER_NODE_ENDPOINT = "/indexing/register";
    private static final String UNREGISTER_NODE_ENDPOINT = "/indexing/unregister";
    private static final String UPDATE_TOPICS_ENDPOINT = "/indexing/update_topics";
    private static final String QUERY_TOPIC_ENDPOINT = "/indexing/query_topic/";
    private static final String METRICS_ENDPOINT = "/indexing/metrics";
    private static final String CSV_DIRECTORY = "indexingservertests"; // Directory for CSV files

    public IndexingServerBenchmarkTests(String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        createCSVDirectory(); // Create the directory for CSV files
    }

    private void createCSVDirectory() {
        File directory = new File(CSV_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir(); // Create the directory if it doesn't exist
        }
    }

    private void writeToCSV(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double benchmarkRegisterNode(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, REGISTER_NODE_ENDPOINT, CSV_DIRECTORY + "/register_node_latency.csv", CSV_DIRECTORY + "/register_node_throughput.csv");
    }

    public double benchmarkUnregisterNode(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, UNREGISTER_NODE_ENDPOINT, CSV_DIRECTORY + "/unregister_node_latency.csv", CSV_DIRECTORY + "/unregister_node_throughput.csv");
    }

    public double benchmarkUpdateTopics(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, UPDATE_TOPICS_ENDPOINT, CSV_DIRECTORY + "/update_topics_latency.csv", CSV_DIRECTORY + "/update_topics_throughput.csv");
    }

    public double benchmarkQueryTopic(int numClients, String topic) throws InterruptedException {
        return benchmarkAPI(numClients, QUERY_TOPIC_ENDPOINT + topic, CSV_DIRECTORY + "/query_topic_latency.csv", CSV_DIRECTORY + "/query_topic_throughput.csv");
    }

    public double benchmarkGetMetrics(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, METRICS_ENDPOINT, CSV_DIRECTORY + "/get_metrics_latency.csv", CSV_DIRECTORY + "/get_metrics_throughput.csv");
    }

    private double benchmarkAPI(int numClients, String endpoint, String latencyCsvFileName, String throughputCsvFileName) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        long startTime = System.currentTimeMillis();
        long[] totalLatency = {0}; // Using an array to hold the total latency

        for (int i = 0; i < numClients; i++) {
            executor.submit(() -> {
                long requestStartTime = System.currentTimeMillis();
                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + endpoint, createNodeRequestBody(), String.class);
                long requestEndTime = System.currentTimeMillis();
                long requestLatency = requestEndTime - requestStartTime;

                synchronized (this) { // Synchronize access to totalLatency
                    totalLatency[0] += requestLatency; // Update the latency in the array
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(THREAD_TERMINATION_TIMEOUT, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        double timeTakenInSeconds = (endTime - startTime) / 1000.0;
        System.out.println("Clients: " + numClients + " | Time taken for " + endpoint + ": " + timeTakenInSeconds + " seconds");

        // Calculate and save latency
        double averageLatency = (double) totalLatency[0] / numClients;
        writeToCSV(latencyCsvFileName, numClients + "," + averageLatency);

        // Calculate and save throughput
        double throughput = numClients / timeTakenInSeconds; // requests per second
        writeToCSV(throughputCsvFileName, numClients + "," + throughput);

        return averageLatency; // Return average latency if needed
    }

    private Map<String, Object> createNodeRequestBody() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("node_id", "peerNode1");
        requestBody.put("topics", List.of("topic1", "topic2"));
        return requestBody;
    }

    public static void main(String[] args) throws InterruptedException {
        IndexingServerBenchmarkTests benchmarkTestsClient = new IndexingServerBenchmarkTests("http://localhost:8080");

        final int initialClients = 1;
        final int maxClients = 8; // Increase as needed
        final int increment = 1;

        // Create CSV files with headers in the new folder
        createCSVFile(CSV_DIRECTORY + "/register_node_latency.csv");
        createCSVFile(CSV_DIRECTORY + "/register_node_throughput.csv");
        createCSVFile(CSV_DIRECTORY + "/unregister_node_latency.csv");
        createCSVFile(CSV_DIRECTORY + "/unregister_node_throughput.csv");
        createCSVFile(CSV_DIRECTORY + "/update_topics_latency.csv");
        createCSVFile(CSV_DIRECTORY + "/update_topics_throughput.csv");
        createCSVFile(CSV_DIRECTORY + "/query_topic_latency.csv");
        createCSVFile(CSV_DIRECTORY + "/query_topic_throughput.csv");
        createCSVFile(CSV_DIRECTORY + "/get_metrics_latency.csv");
        createCSVFile(CSV_DIRECTORY + "/get_metrics_throughput.csv");

        for (int numClients = initialClients; numClients <= maxClients; numClients += increment) {
            System.out.println("\nRunning benchmark with " + numClients + " clients:");

            benchmarkTestsClient.benchmarkRegisterNode(numClients);
            benchmarkTestsClient.benchmarkUnregisterNode(numClients);
            benchmarkTestsClient.benchmarkUpdateTopics(numClients);
            benchmarkTestsClient.benchmarkQueryTopic(numClients, "topic1");
            benchmarkTestsClient.benchmarkGetMetrics(numClients);
        }
    }

    private static void createCSVFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Number of Clients,Time Taken (seconds)");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
