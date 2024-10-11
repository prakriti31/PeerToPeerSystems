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

    // Constants
    private static final int THREAD_TERMINATION_TIMEOUT = 1; // in minutes
    private static final String INITIALIZE_ENDPOINT = "/peer/initialize";
    private static final String PUBLISH_ENDPOINT = "/peer/publish";
    private static final String SUBSCRIBE_ENDPOINT = "/peer/subscribe/";
    private static final String PULL_MESSAGES_ENDPOINT = "/peer/pull_messages/";
    private static final String REGISTER_WITH_INDEXING_SERVER_ENDPOINT = "/peer/register_with_indexing_server";
    private static final String CREATE_TOPIC_ENDPOINT = "/peer/create_topic";
    private static final String REPORT_METRICS_ENDPOINT = "/peer/report_metrics";
    private static final String EVENT_LOG_ENDPOINT = "/peer/event_log";

    public PeerNodeBenchmarkTests(String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        createOutputDirectory(); // Create the output directory at initialization
    }

    private void createOutputDirectory() {
        File directory = new File(OUTPUT_DIR);
        if (!directory.exists()) {
            directory.mkdir(); // Create the directory if it does not exist
        }
    }

    private void writeToCSV(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "/" + fileName, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double benchmarkInitialize(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, INITIALIZE_ENDPOINT, "initialize_latency.csv", "initialize_throughput.csv");
    }

    public double benchmarkPublish(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, PUBLISH_ENDPOINT, "publish_latency.csv", "publish_throughput.csv");
    }

    public double benchmarkSubscribe(int numClients, String topic) throws InterruptedException {
        return benchmarkAPI(numClients, SUBSCRIBE_ENDPOINT + topic, "subscribe_latency.csv", "subscribe_throughput.csv");
    }

    public double benchmarkPullMessages(int numClients, String topic) throws InterruptedException {
        return benchmarkAPI(numClients, PULL_MESSAGES_ENDPOINT + topic, "pull_messages_latency.csv", "pull_messages_throughput.csv");
    }

    public double benchmarkRegisterWithIndexingServer(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, REGISTER_WITH_INDEXING_SERVER_ENDPOINT, "register_with_indexing_server_latency.csv", "register_with_indexing_server_throughput.csv");
    }

    public double benchmarkCreateTopic(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, CREATE_TOPIC_ENDPOINT, "create_topic_latency.csv", "create_topic_throughput.csv");
    }

    public double benchmarkReportMetrics(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, REPORT_METRICS_ENDPOINT, "report_metrics_latency.csv", "report_metrics_throughput.csv");
    }

    public double benchmarkEventLog(int numClients) throws InterruptedException {
        return benchmarkAPI(numClients, EVENT_LOG_ENDPOINT, "event_log_latency.csv", "event_log_throughput.csv");
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
        PeerNodeBenchmarkTests benchmarkTestsClient = new PeerNodeBenchmarkTests("http://localhost:8080");

        final int initialClients = 1;
        final int maxClients = 8; // Increase as needed
        final int increment = 1;

        // Create CSV files with headers in the specified directory
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

        for (int numClients = initialClients; numClients <= maxClients; numClients += increment) {
            System.out.println("\nRunning benchmark with " + numClients + " clients:");

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

    private static void createCSVFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_DIR + "/" + fileName))) {
            writer.write("Number of Clients,Time Taken (seconds)");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
