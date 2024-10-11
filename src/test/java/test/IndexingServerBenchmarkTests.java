// Declare the package for this class, indicating it's part of the 'test' module
package test;

// Import necessary classes for HTTP requests and file handling
import org.springframework.http.ResponseEntity; // Class for handling HTTP responses
import org.springframework.web.client.RestTemplate; // Class to make HTTP requests

import java.io.BufferedWriter; // Class for writing text to a file
import java.io.File; // Class representing file and directory pathnames
import java.io.FileWriter; // Class for writing to a file
import java.io.IOException; // Exception for handling input-output errors
import java.util.HashMap; // HashMap class for storing key-value pairs
import java.util.List; // List interface for handling a collection of elements
import java.util.Map; // Map interface for key-value pairs
import java.util.concurrent.ExecutorService; // Interface for managing a pool of threads
import java.util.concurrent.Executors; // Factory class for creating thread pools
import java.util.concurrent.TimeUnit; // Enumeration for time unit conversion

// Class to benchmark various API endpoints of the indexing server
public class IndexingServerBenchmarkTests {

    // Instance variables for the RestTemplate and base URL
    private final RestTemplate restTemplate; // RestTemplate to make HTTP requests
    private final String baseUrl; // Base URL for the indexing server

    // Constants for benchmark settings
    private static final int THREAD_TERMINATION_TIMEOUT = 1; // Timeout for thread termination in minutes
    private static final String REGISTER_NODE_ENDPOINT = "/indexing/register"; // Endpoint for registering nodes
    private static final String UNREGISTER_NODE_ENDPOINT = "/indexing/unregister"; // Endpoint for unregistering nodes
    private static final String UPDATE_TOPICS_ENDPOINT = "/indexing/update_topics"; // Endpoint for updating topics
    private static final String QUERY_TOPIC_ENDPOINT = "/indexing/query_topic/"; // Endpoint for querying topics
    private static final String METRICS_ENDPOINT = "/indexing/metrics"; // Endpoint for getting metrics
    private static final String CSV_DIRECTORY = "indexingservertests"; // Directory for storing CSV files

    // Constructor for initializing the benchmark tests with a base URL
    public IndexingServerBenchmarkTests(String baseUrl) {
        this.restTemplate = new RestTemplate(); // Initialize RestTemplate
        this.baseUrl = baseUrl; // Set the base URL for requests
        createCSVDirectory(); // Create the directory for CSV files
    }

    // Method to create a directory for storing CSV files
    private void createCSVDirectory() {
        File directory = new File(CSV_DIRECTORY); // Create a File object for the directory
        if (!directory.exists()) { // Check if the directory already exists
            directory.mkdir(); // Create the directory if it doesn't exist
        }
    }

    // Method to write data to a CSV file
    private void writeToCSV(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) { // Open a BufferedWriter for the file
            writer.write(data); // Write data to the file
            writer.newLine(); // Add a new line after writing
        } catch (IOException e) { // Catch any IO exceptions
            e.printStackTrace(); // Print the stack trace for debugging
        }
    }

    // Method to benchmark the register node API
    public double benchmarkRegisterNode(int numClients) throws InterruptedException {
        // Call the generic benchmark method with the appropriate endpoint and CSV file names
        return benchmarkAPI(numClients, REGISTER_NODE_ENDPOINT, CSV_DIRECTORY + "/register_node_latency.csv", CSV_DIRECTORY + "/register_node_throughput.csv");
    }

    // Method to benchmark the unregister node API
    public double benchmarkUnregisterNode(int numClients) throws InterruptedException {
        // Call the generic benchmark method for the unregister endpoint
        return benchmarkAPI(numClients, UNREGISTER_NODE_ENDPOINT, CSV_DIRECTORY + "/unregister_node_latency.csv", CSV_DIRECTORY + "/unregister_node_throughput.csv");
    }

    // Method to benchmark the update topics API
    public double benchmarkUpdateTopics(int numClients) throws InterruptedException {
        // Call the generic benchmark method for the update topics endpoint
        return benchmarkAPI(numClients, UPDATE_TOPICS_ENDPOINT, CSV_DIRECTORY + "/update_topics_latency.csv", CSV_DIRECTORY + "/update_topics_throughput.csv");
    }

    // Method to benchmark the query topic API
    public double benchmarkQueryTopic(int numClients, String topic) throws InterruptedException {
        // Call the generic benchmark method for the query topic endpoint with the specified topic
        return benchmarkAPI(numClients, QUERY_TOPIC_ENDPOINT + topic, CSV_DIRECTORY + "/query_topic_latency.csv", CSV_DIRECTORY + "/query_topic_throughput.csv");
    }

    // Method to benchmark the get metrics API
    public double benchmarkGetMetrics(int numClients) throws InterruptedException {
        // Call the generic benchmark method for the get metrics endpoint
        return benchmarkAPI(numClients, METRICS_ENDPOINT, CSV_DIRECTORY + "/get_metrics_latency.csv", CSV_DIRECTORY + "/get_metrics_throughput.csv");
    }

    // Generic method to benchmark an API endpoint
    private double benchmarkAPI(int numClients, String endpoint, String latencyCsvFileName, String throughputCsvFileName) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numClients); // Create a thread pool with a fixed number of threads
        long startTime = System.currentTimeMillis(); // Record the start time
        long[] totalLatency = {0}; // Using an array to hold the total latency, allowing modification in the inner class

        // Submit tasks to the executor for the specified number of clients
        for (int i = 0; i < numClients; i++) {
            executor.submit(() -> {
                long requestStartTime = System.currentTimeMillis(); // Record the start time for the request
                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + endpoint, createNodeRequestBody(), String.class); // Make the API call
                long requestEndTime = System.currentTimeMillis(); // Record the end time for the request
                long requestLatency = requestEndTime - requestStartTime; // Calculate the latency for the request

                synchronized (this) { // Synchronize access to totalLatency to avoid race conditions
                    totalLatency[0] += requestLatency; // Update the total latency in the array
                }
            });
        }

        executor.shutdown(); // Shutdown the executor to stop accepting new tasks
        executor.awaitTermination(THREAD_TERMINATION_TIMEOUT, TimeUnit.MINUTES); // Wait for existing tasks to finish
        long endTime = System.currentTimeMillis(); // Record the end time for the benchmarking process

        // Calculate the total time taken for the benchmark in seconds
        double timeTakenInSeconds = (endTime - startTime) / 1000.0;
        System.out.println("Clients: " + numClients + " | Time taken for " + endpoint + ": " + timeTakenInSeconds + " seconds");

        // Calculate and save average latency
        double averageLatency = (double) totalLatency[0] / numClients; // Calculate average latency
        writeToCSV(latencyCsvFileName, numClients + "," + averageLatency); // Write average latency to CSV

        // Calculate and save throughput
        double throughput = numClients / timeTakenInSeconds; // Calculate throughput in requests per second
        writeToCSV(throughputCsvFileName, numClients + "," + throughput); // Write throughput to CSV

        return averageLatency; // Return average latency for further processing if needed
    }

    // Method to create a request body for registering a node
    private Map<String, Object> createNodeRequestBody() {
        Map<String, Object> requestBody = new HashMap<>(); // Create a new HashMap for the request body
        requestBody.put("node_id", "peerNode1"); // Add node ID to the request body
        requestBody.put("topics", List.of("topic1", "topic2")); // Add a list of topics to the request body
        return requestBody; // Return the completed request body
    }

    // Main method to run the benchmark tests
    public static void main(String[] args) throws InterruptedException {
        IndexingServerBenchmarkTests benchmarkTestsClient = new IndexingServerBenchmarkTests("http://localhost:8080"); // Initialize the benchmark tests with the server URL

        final int initialClients = 1; // Initial number of clients for benchmarking
        final int maxClients = 8; // Maximum number of clients to benchmark
        final int increment = 1; // Increment value for increasing the number of clients

        // Create CSV files with headers in the specified directory
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

        // Loop to run benchmarks with increasing numbers of clients
        for (int numClients = initialClients; numClients <= maxClients; numClients += increment) {
            System.out.println("\nRunning benchmark with " + numClients + " clients:"); // Print the current number of clients being tested

            // Call benchmark methods for each API endpoint with the current number of clients
            benchmarkTestsClient.benchmarkRegisterNode(numClients);
            benchmarkTestsClient.benchmarkUnregisterNode(numClients);
            benchmarkTestsClient.benchmarkUpdateTopics(numClients);
            benchmarkTestsClient.benchmarkQueryTopic(numClients, "topic1");
            benchmarkTestsClient.benchmarkGetMetrics(numClients);
        }
    }

    // Method to create a CSV file with headers
    private static void createCSVFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) { // Open a BufferedWriter for the file
            writer.write("Number of Clients,Time Taken (seconds)"); // Write the header to the CSV
            writer.newLine(); // Add a new line after the header
        } catch (IOException e) { // Catch any IO exceptions
            e.printStackTrace(); // Print the stack trace for debugging
        }
    }
}
