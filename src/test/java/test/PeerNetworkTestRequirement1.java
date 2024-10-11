package test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication // Marks this as a Spring Boot application
public class PeerNetworkTestRequirement1 implements CommandLineRunner { // Implements CommandLineRunner to run code after Spring context is initialized

    private final RestTemplate restTemplate = new RestTemplate(); // RestTemplate for making HTTP requests

    public static void main(String[] args) {
        SpringApplication.run(PeerNetworkTestRequirement1.class, args); // Start Spring Boot application
    }

    @Override
    public void run(String... args) throws Exception { // Method that runs after application startup
        // Start the indexing server (simulated in this test)
        System.out.println("Spring Boot application started.");

        System.out.println("Starting Indexing Server...");
        startIndexingServer(); // Simulates the start of an indexing server

        // Create a thread pool to run peer nodes concurrently
        ExecutorService executor = Executors.newFixedThreadPool(3); // Thread pool for 3 peers

        // Initialize and run 3 peer nodes in parallel on different ports
        executor.submit(() -> runPeerNode("127.0.0.1", 8081, "Peer1")); // Start Peer1
        executor.submit(() -> runPeerNode("127.0.0.1", 8082, "Peer2")); // Start Peer2
        executor.submit(() -> runPeerNode("127.0.0.1", 8083, "Peer3")); // Start Peer3

        // Shutdown executor after tasks completion
        executor.shutdown(); // Gracefully shuts down the thread pool after tasks are done
        executor.awaitTermination(5, TimeUnit.MINUTES); // Waits for up to 5 minutes for tasks to complete
    }

    private void startIndexingServer() {
        // Simulate starting the Indexing Server
        System.out.println("Indexing Server started at http://127.0.0.1:8080/indexing"); // Outputs that the indexing server has started
    }

    private void runPeerNode(String indexServerIp, int indexServerPort, String peerName) {
        try {
            // Initialize the peer node
            System.out.println(peerName + " initializing...");
            String peerId = initializePeerNode(indexServerIp, indexServerPort); // Initializes the peer node with the indexing server

            // Register the peer with the indexing server
            System.out.println(peerName + " registering with Indexing Server...");
            registerPeerWithIndexingServer(peerId, List.of("topicA")); // Register the peer for a specific topic

            // If this is Peer1, create and publish a topic
            if ("Peer1".equals(peerName)) { // Peer1 creates and publishes to topicA
                System.out.println(peerName + " creating topic 'topicA' and publishing message...");
                createTopic(peerId, "topicA"); // Create the topic "topicA"
                publishMessage(peerId, "topicA", "Hello from " + peerName); // Publish a message to "topicA"
            }

            // Subscribe to topicA
            System.out.println(peerName + " subscribing to 'topicA'...");
            subscribeToTopic(peerId, "topicA"); // Subscribe to "topicA"

            // Pull messages from topicA
            System.out.println(peerName + " pulling messages from 'topicA'...");
            pullMessages(peerId, "topicA"); // Retrieve messages from "topicA"

        } catch (Exception e) {
            e.printStackTrace(); // Print error details in case of an exception
        }
    }

    private String initializePeerNode(String indexServerIp, int indexServerPort) {
        String url = "http://localhost:" + indexServerPort + "/peer/initialize?indexServerIp=" + indexServerIp + "&indexServerPort=" + indexServerPort; // Construct the URL for initializing the peer node
        System.out.println("=============");
        System.out.println("POST Request URL: " + url); // Log the request URL
        System.out.println("Request Body: null (No request body needed for this operation)"); // No request body needed

        Map<String, Object> response = restTemplate.postForObject(url, null, Map.class); // Send POST request to initialize the peer node

        System.out.println("Response Body: " + response); // Log the response
        System.out.println("=============");
        String nodeId = (String) response.get("node_id"); // Retrieve the node ID from the response
        System.out.println("Peer Node initialized: " + nodeId); // Log the initialized node ID
        return nodeId; // Return the peer node ID
    }

    private void createTopic(String peerId, String topic) {
        String url = "http://localhost:8081/peer/create_topic"; // URL for creating a topic
        System.out.println("=============");
        System.out.println("POST Request URL: " + url); // Log the request URL
        System.out.println("Request Body: Topic - " + topic); // Log the topic being created

        Map<String, Object> response = restTemplate.postForObject(url, topic, Map.class); // Send POST request to create the topic

        System.out.println("Response Body: " + response); // Log the response
        System.out.println("=============");
        System.out.println("Topic created: " + topic + " on peer " + peerId); // Log topic creation on the peer node
    }

    private void registerPeerWithIndexingServer(String peerId, List<String> topics) {
        String url = "http://localhost:8081/peer/register_with_indexing_server"; // URL for registering the peer with the indexing server
        Map<String, Object> registrationPayload = Map.of("node_id", peerId, "topics", topics); // Create payload containing the peer ID and topics

        System.out.println("=============");
        System.out.println("POST Request URL: " + url); // Log the request URL
        System.out.println("Request Body: " + registrationPayload); // Log the request payload

        Map<String, Object> response = restTemplate.postForObject(url, registrationPayload, Map.class); // Send POST request to register the peer

        System.out.println("Response Body: " + response); // Log the response
        System.out.println("=============");
        System.out.println("Peer " + peerId + " registered with Indexing Server."); // Log peer registration
    }

    private void publishMessage(String peerId, String topic, String message) {
        String url = "http://localhost:8081/peer/publish"; // URL for publishing a message
        Map<String, Object> publishPayload = Map.of("topic", topic, "message", message); // Create payload with the topic and message

        System.out.println("=============");
        System.out.println("POST Request URL: " + url); // Log the request URL
        System.out.println("Request Body: " + publishPayload); // Log the request payload

        Map<String, Object> response = restTemplate.postForObject(url, publishPayload, Map.class); // Send POST request to publish the message

        System.out.println("Response Body: " + response); // Log the response
        System.out.println("=============");
        System.out.println("Message published to " + topic + " by " + peerId); // Log the successful message publication
    }

    private void subscribeToTopic(String peerId, String topic) {
        String url = "http://localhost:8081/peer/subscribe/" + topic; // URL for subscribing to a topic

        System.out.println("=============");
        System.out.println("GET Request URL: " + url); // Log the request URL

        Map<String, Object> response = restTemplate.getForObject(url, Map.class); // Send GET request to subscribe to the topic

        System.out.println("Response Body: " + response); // Log the response
        System.out.println("=============");
        System.out.println("Peer " + peerId + " subscribed to " + topic); // Log the successful subscription
    }

    private void pullMessages(String peerId, String topic) {
        String url = "http://localhost:8081/peer/pull_messages/" + topic; // URL for pulling messages from a topic

        System.out.println("=============");
        System.out.println("GET Request URL: " + url); // Log the request URL

        Map<String, Object> response = restTemplate.getForObject(url, Map.class); // Send GET request to pull messages

        System.out.println("Response Body: " + response); // Log the response
        System.out.println("=============");
        System.out.println("Peer " + peerId + " pulled messages: " + response.get("messages")); // Log the messages pulled from the topic
    }
}
