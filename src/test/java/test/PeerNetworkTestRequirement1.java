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

@SpringBootApplication
public class PeerNetworkTestRequirement1 implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(PeerNetworkTestRequirement1.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Start the indexing server (simulated in this test)
        System.out.println("Starting Indexing Server...");
        startIndexingServer();

        // Create a thread pool to run peer nodes concurrently
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Initialize and run 3 peer nodes in parallel on different ports
        executor.submit(() -> runPeerNode("127.0.0.1", 8081, "Peer1"));
        executor.submit(() -> runPeerNode("127.0.0.1", 8082, "Peer2"));
        executor.submit(() -> runPeerNode("127.0.0.1", 8083, "Peer3"));

        // Shutdown executor after tasks completion
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }

    private void startIndexingServer() {
        // Simulate starting the Indexing Server
        System.out.println("Indexing Server started at http://127.0.0.1:8080/indexing");
    }

    private void runPeerNode(String indexServerIp, int indexServerPort, String peerName) {
        try {
            // Initialize the peer node
            System.out.println(peerName + " initializing...");
            String peerId = initializePeerNode(indexServerIp, indexServerPort);

            // Register the peer with the indexing server
            System.out.println(peerName + " registering with Indexing Server...");
            registerPeerWithIndexingServer(peerId, List.of("topicA"));

            // If this is Peer1, create and publish a topic
            if ("Peer1".equals(peerName)) {
                System.out.println(peerName + " creating topic 'topicA' and publishing message...");
                createTopic(peerId, "topicA");
                publishMessage(peerId, "topicA", "Hello from " + peerName);
            }

            // Subscribe to topicA
            System.out.println(peerName + " subscribing to 'topicA'...");
            subscribeToTopic(peerId, "topicA");

            // Pull messages from topicA
            System.out.println(peerName + " pulling messages from 'topicA'...");
            pullMessages(peerId, "topicA");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String initializePeerNode(String indexServerIp, int indexServerPort) {
        String url = "http://localhost:" + indexServerPort + "/peer/initialize?indexServerIp=" + indexServerIp + "&indexServerPort=" + indexServerPort;
        System.out.println("=============");
        System.out.println("POST Request URL: " + url);
        System.out.println("Request Body: null (No request body needed for this operation)");

        Map<String, Object> response = restTemplate.postForObject(url, null, Map.class);

        System.out.println("Response Body: " + response);
        System.out.println("=============");
        String nodeId = (String) response.get("node_id");
        System.out.println("Peer Node initialized: " + nodeId);
        return nodeId;
    }

    private void createTopic(String peerId, String topic) {
        String url = "http://localhost:8081/peer/create_topic"; // Change this to the appropriate peer's port if necessary
        System.out.println("=============");
        System.out.println("POST Request URL: " + url);
        System.out.println("Request Body: Topic - " + topic);

        Map<String, Object> response = restTemplate.postForObject(url, topic, Map.class);

        System.out.println("Response Body: " + response);
        System.out.println("=============");
        System.out.println("Topic created: " + topic + " on peer " + peerId);
    }

    private void registerPeerWithIndexingServer(String peerId, List<String> topics) {
        String url = "http://localhost:8081/peer/register_with_indexing_server"; // Change this to the appropriate peer's port if necessary
        Map<String, Object> registrationPayload = Map.of("node_id", peerId, "topics", topics);

        System.out.println("=============");
        System.out.println("POST Request URL: " + url);
        System.out.println("Request Body: " + registrationPayload);

        Map<String, Object> response = restTemplate.postForObject(url, registrationPayload, Map.class);

        System.out.println("Response Body: " + response);
        System.out.println("=============");
        System.out.println("Peer " + peerId + " registered with Indexing Server.");
    }

    private void publishMessage(String peerId, String topic, String message) {
        String url = "http://localhost:8081/peer/publish"; // Change this to the appropriate peer's port if necessary
        Map<String, Object> publishPayload = Map.of("topic", topic, "message", message);

        System.out.println("=============");
        System.out.println("POST Request URL: " + url);
        System.out.println("Request Body: " + publishPayload);

        Map<String, Object> response = restTemplate.postForObject(url, publishPayload, Map.class);

        System.out.println("Response Body: " + response);
        System.out.println("=============");
        System.out.println("Message published to " + topic + " by " + peerId);
    }

    private void subscribeToTopic(String peerId, String topic) {
        String url = "http://localhost:8081/peer/subscribe/" + topic; // Change this to the appropriate peer's port if necessary

        System.out.println("=============");
        System.out.println("GET Request URL: " + url);

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        System.out.println("Response Body: " + response);
        System.out.println("=============");
        System.out.println("Peer " + peerId + " subscribed to " + topic);
    }

    private void pullMessages(String peerId, String topic) {
        String url = "http://localhost:8081/peer/pull_messages/" + topic; // Change this to the appropriate peer's port if necessary

        System.out.println("=============");
        System.out.println("GET Request URL: " + url);

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        System.out.println("Response Body: " + response);
        System.out.println("=============");
        System.out.println("Peer " + peerId + " pulled messages: " + response.get("messages"));
    }
}
