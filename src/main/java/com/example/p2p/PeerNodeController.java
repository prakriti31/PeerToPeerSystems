package com.example.p2p;

import jakarta.annotation.PreDestroy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/peer")
@EnableAsync
public class PeerNodeController {

    private static final Logger logger = LoggerFactory.getLogger(PeerNodeController.class);

    // Static counter for generating peer IDs
    private static final AtomicInteger peerCounter = new AtomicInteger(1);

    private String nodeId;
    private String indexingServerUrl;

    // List of topics hosted by this peer node
    private List<String> topics = new ArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();

    // Stores subscribers for each topic
    private final Map<String, List<String>> topicSubscribers = new HashMap<>();

    // Stores messages for each topic
    private final Map<String, List<String>> topicMessages = new HashMap<>();

    // Event log for the peer node
    private final List<String> eventLog = new ArrayList<>();

    /**
     * Initialize the peer node with nodeId, indexing server URL, and listening port.
     * @param indexServerIp IP address of the indexing server.
     * @param indexServerPort Port number of the indexing server.
     * @return Initialization status.
     */
    @PostMapping("/initialize")
    public Map<String, Object> initialize(
            @RequestParam String indexServerIp,
            @RequestParam int indexServerPort) {

        // Generate a new peer ID each time this method is called
        this.nodeId = "peer" + peerCounter.getAndIncrement();
        this.indexingServerUrl = "http://" + indexServerIp + ":" + indexServerPort + "/indexing";

        logger.info("Peer node initialized with ID: " + nodeId + ", Indexing Server: " + indexingServerUrl);

        return Map.of("status", "initialized", "node_id", nodeId);
    }

    /**
     * Publish a message to a topic hosted by this peer.
     * @param body Request body containing the topic and message.
     * @return A map with the status of the operation.
     */
    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestBody Map<String, Object> body) {
        String topic = (String) body.get("topic");
        String message = (String) body.get("message");

        // Check if the topic is hosted by this peer
        if (!topics.contains(topic)) {
            logger.warn("Attempt to publish to non-hosted topic: " + topic);
            return Map.of("status", "error", "message", "Topic not hosted here");
        }

        // Store the message for this topic
        topicMessages.computeIfAbsent(topic, k -> new ArrayList<>()).add(message);

        logger.info("Message published to topic " + topic + ": " + message);
        return Map.of("status", "published", "topic", topic);
    }

    /**
     * Subscribe to a topic by querying the indexing server and forwarding to the correct peer.
     * @param topic The topic to subscribe to.
     * @return A map with the status of the subscription.
     */
    @GetMapping("/subscribe/{topic}")
    public Map<String, Object> subscribe(@PathVariable String topic) {
        String queryUrl = indexingServerUrl + "/query_topic/" + topic;
        Map<String, Object> response = restTemplate.getForObject(queryUrl, Map.class);

        if ("found".equals(response.get("status"))) {
            String hostingNodeId = (String) response.get("node_id");

            // If this node hosts the topic, register the subscription
            if (hostingNodeId.equals(this.nodeId)) {
                logger.info("Subscribed to topic " + topic + " on this node");
                topicSubscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(this.nodeId);
                return Map.of("status", "subscribed", "topic", topic);
            } else {
                // Forward subscription request to the peer hosting this topic
                return forwardSubscription(hostingNodeId, topic);
            }
        }

        logger.warn("Subscription to topic failed, topic not found: " + topic);
        return Map.of("status", "error", "message", "Topic not found");
    }

    /**
     * Forward subscription request to the hosting node.
     */
    private Map<String, Object> forwardSubscription(String nodeId, String topic) {
        String peerUrl = "http://localhost:8081/peer/subscribe/" + topic;

        try {
            Map<String, Object> response = restTemplate.getForObject(peerUrl, Map.class);
            logger.info("Forwarded subscription request for topic " + topic + " to node " + nodeId);
            return response;
        } catch (Exception e) {
            logger.error("Failed to forward subscription to node " + nodeId, e);
            return Map.of("status", "error", "message", "Failed to forward subscription");
        }
    }

    /**
     * Pull messages for a specific topic.
     * @param topic The topic to pull messages from.
     * @return A map containing the list of messages.
     */
    @GetMapping("/pull_messages/{topic}")
    public Map<String, Object> pullMessages(@PathVariable String topic) {
        // Check if the topic is hosted by this peer node
        if (!topics.contains(topic)) {
            return Map.of("status", "error", "message", "Topic not hosted here");
        }

        // Get the messages for this topic
        List<String> messages = topicMessages.getOrDefault(topic, new ArrayList<>());

        // Return error if there are no messages to pull
        if (messages.isEmpty()) {
            return Map.of("status", "error", "message", "No messages available");
        }

        // Clear the messages after returning them
        topicMessages.put(topic, new ArrayList<>());  // Clear the messages list for this topic

        // Return the pulled messages
        return Map.of("status", "success", "messages", messages);
    }

    /**
     * Register this peer node with the central indexing server.
     * @return A map with the status of the registration.
     */
    @PostMapping("/register_with_indexing_server")
    public Map<String, Object> registerWithIndexingServer() {
        Map<String, Object> registrationPayload = Map.of(
                "node_id", nodeId,
                "topics", topics
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(indexingServerUrl + "/register", registrationPayload, Map.class);
            logger.info("Registered with indexing server: " + indexingServerUrl);
            return response;
        } catch (Exception e) {
            logger.error("Failed to register with indexing server", e);
            return Map.of("status", "error", "message", "Failed to register with the indexing server");
        }
    }

    /**
     * Create a new topic on this peer node.
     * @param topicName The name of the topic to be created.
     * @return A map with the status of the topic creation.
     */
    @PostMapping("/create_topic")
    public Map<String, Object> createTopic(@RequestBody String topicName) {
        topics.add(topicName);
        logger.info("Created topic: " + topicName);
        registerWithIndexingServer(); // Automatically notify indexing server

        return Map.of("status", "created", "topic", topicName);
    }

    @PostMapping("/report_metrics")
    public Map<String, Object> reportMetrics(@RequestBody Map<String, Object> metrics) {
        // Send metrics to the indexing server or save them to a database
        // metrics could contain latency, bandwidth, etc.
        logger.info("Reporting metrics from node " + nodeId + ": " + metrics);

        // Optionally, store metrics in a data structure for analytics
        return Map.of("status", "metrics_reported");
    }

    /**
     * Gracefully unregister from the indexing server when the peer node is shutting down.
     */
    @PreDestroy
    public void onShutdown() {
        logger.info("Received shutdown signal, unregistering from indexing server...");
        logEvent("Shutdown signal received", "Unregistering from server");

        Map<String, Object> unregisterPayload = Map.of("node_id", nodeId);
        restTemplate.postForObject(indexingServerUrl + "/unregister", unregisterPayload, Map.class);

        logger.info("Unregistered from indexing server");
        logEvent("Unregistered from indexing server", nodeId);
    }

    private void logEvent(String event, String details) {
        String logEntry = LocalDateTime.now() + " - Event: " + event + ", Details: " + details;
        eventLog.add(logEntry);
        logger.info(logEntry);
    }

    @GetMapping("/event_log")
    public Map<String, Object> getEventLog() {
        return Map.of("event_log", eventLog);
    }
}