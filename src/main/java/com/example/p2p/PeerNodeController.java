package com.example.p2p;

import jakarta.annotation.PreDestroy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/peer")
@EnableAsync
public class PeerNodeController {

    private static final Logger logger = LoggerFactory.getLogger(PeerNodeController.class);

    private static final AtomicInteger peerCounter = new AtomicInteger(1);

    private String nodeId;
    private String indexingServerUrl;

    private List<String> topics = new ArrayList<>();
    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, List<String>> topicSubscribers = new HashMap<>();
    private final Map<String, List<String>> topicMessages = new HashMap<>();

    private final List<String> eventLog = new CopyOnWriteArrayList<>();

    @PostMapping("/initialize")
    public Map<String, Object> initialize(
            @RequestParam String indexServerIp,
            @RequestParam int indexServerPort) {

        this.nodeId = "peer" + peerCounter.getAndIncrement();
        this.indexingServerUrl = "http://" + indexServerIp + ":" + indexServerPort + "/indexing";

        logger.info("Peer node initialized with ID: " + nodeId + ", Indexing Server: " + indexingServerUrl);
        logEvent("Peer Initialized", "ID: " + nodeId);

        return Map.of("status", "initialized", "node_id", nodeId);
    }

    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestBody Map<String, Object> body) {
        String topic = (String) body.get("topic");
        String message = (String) body.get("message");

        if (!topics.contains(topic)) {
            logger.warn("Attempt to publish to non-hosted topic: " + topic);
            return Map.of("status", "error", "message", "Topic not hosted here");
        }

        topicMessages.computeIfAbsent(topic, k -> new ArrayList<>()).add(message);

        logger.info("Message published to topic " + topic + ": " + message);
        logEvent("Message Published", "Topic: " + topic + ", Message: " + message);

        return Map.of("status", "published", "topic", topic);
    }

    @GetMapping("/subscribe/{topic}")
    public Map<String, Object> subscribe(@PathVariable String topic) {
        String queryUrl = indexingServerUrl + "/query_topic/" + topic;
        Map<String, Object> response = restTemplate.getForObject(queryUrl, Map.class);

        if ("found".equals(response.get("status"))) {
            String hostingNodeId = (String) response.get("node_id");

            if (hostingNodeId.equals(this.nodeId)) {
                logger.info("Subscribed to topic " + topic + " on this node");
                topicSubscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(this.nodeId);
                logEvent("Subscribed to Topic", "Topic: " + topic);
                return Map.of("status", "subscribed", "topic", topic);
            } else {
                return forwardSubscription(hostingNodeId, topic);
            }
        }

        logger.warn("Subscription to topic failed, topic not found: " + topic);
        return Map.of("status", "error", "message", "Topic not found");
    }

    private Map<String, Object> forwardSubscription(String nodeId, String topic) {
        String peerUrl = "http://localhost:8081/peer/subscribe/" + topic;

        try {
            Map<String, Object> response = restTemplate.getForObject(peerUrl, Map.class);
            logger.info("Forwarded subscription request for topic " + topic + " to node " + nodeId);
            logEvent("Forwarded Subscription", "Topic: " + topic + ", Node: " + nodeId);
            return response;
        } catch (Exception e) {
            logger.error("Failed to forward subscription to node " + nodeId, e);
            return Map.of("status", "error", "message", "Failed to forward subscription");
        }
    }

    @GetMapping("/pull_messages/{topic}")
    public Map<String, Object> pullMessages(@PathVariable String topic) {
        if (!topics.contains(topic)) {
            return Map.of("status", "error", "message", "Topic not hosted here");
        }

        List<String> messages = topicMessages.getOrDefault(topic, new ArrayList<>());

        if (messages.isEmpty()) {
            return Map.of("status", "error", "message", "No messages available");
        }

        topicMessages.put(topic, new ArrayList<>());

        logEvent("Messages Pulled", "Topic: " + topic);
        return Map.of("status", "success", "messages", messages);
    }

    @PostMapping("/register_with_indexing_server")
    public Map<String, Object> registerWithIndexingServer() {
        Map<String, Object> registrationPayload = Map.of(
                "node_id", nodeId,
                "topics", topics
        );

        try {
            Map<String, Object> response = restTemplate.postForObject(indexingServerUrl + "/register", registrationPayload, Map.class);
            logger.info("Registered with indexing server: " + indexingServerUrl);
            logEvent("Registered with Indexing Server", "Node ID: " + nodeId);
            return response;
        } catch (Exception e) {
            logger.error("Failed to register with indexing server", e);
            return Map.of("status", "error", "message", "Failed to register with the indexing server");
        }
    }

    @PostMapping("/create_topic")
    public Map<String, Object> createTopic(@RequestBody String topicName) {
        topics.add(topicName);
        logger.info("Created topic: " + topicName);
        logEvent("Created Topic", topicName);
        registerWithIndexingServer();

        return Map.of("status", "created", "topic", topicName);
    }

    @PostMapping("/report_metrics")
    public Map<String, Object> reportMetrics(@RequestBody Map<String, Object> metrics) {
        logger.info("Reporting metrics from node " + nodeId + ": " + metrics);
        logEvent("Metrics Reported", metrics.toString());

        return Map.of("status", "metrics_reported");
    }

    @GetMapping("/get_metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("node_id", nodeId);
        metrics.put("number_of_topics", topics.size());
        metrics.put("topics", topics);
        metrics.put("number_of_subscribers", topicSubscribers.values().stream().mapToInt(List::size).sum());
        metrics.put("number_of_messages", topicMessages.values().stream().mapToInt(List::size).sum());

        logEvent("Metrics Retrieved", "Node: " + nodeId);
        logger.info("Metrics retrieved for node " + nodeId);

        return Map.of("status", "success", "metrics", metrics);
    }

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
        logger.info("Log Event: " + logEntry);
    }

    @GetMapping("/event_log")
    public Map<String, Object> getEventLog() {
        return Map.of("event_log", eventLog);
    }
}
