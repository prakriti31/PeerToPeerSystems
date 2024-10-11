// This line specifies which package this class belongs to
package com.example.p2p;

// These lines import necessary Java classes and Spring Framework annotations
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// This annotation marks the class as a REST controller, handling web requests
@RestController
// This annotation sets the base URL path for all methods in this controller
@RequestMapping("/indexing")
public class IndexingServerController {

    // This map stores information about peer nodes and their topics
    private final Map<String, List<String>> peerNodes = new HashMap<>();

    // This map stores performance metrics reported by each peer node
    private final Map<String, Map<String, Object>> collectedMetrics = new HashMap<>();

    // This method handles registration of new peer nodes
    @PostMapping("/register")
    public Map<String, Object> registerNode(@RequestBody Map<String, Object> body) {
        // Extract node ID and topics from the request body
        String nodeId = (String) body.get("node_id");
        List<String> topics = (List<String>) body.get("topics");

        // Store the node and its topics in the peerNodes map
        peerNodes.put(nodeId, topics);

        // Return a success message with the registered node ID
        return Map.of("status", "registered", "node_id", nodeId);
    }

    // This method handles unregistration of peer nodes
    @PostMapping("/unregister")
    public Map<String, Object> unregisterNode(@RequestBody Map<String, Object> body) {
        // Extract node ID from the request body
        String nodeId = (String) body.get("node_id");

        // Check if the node exists in the peerNodes map
        if (peerNodes.containsKey(nodeId)) {
            // Get the topics hosted by the unregistering node
            List<String> topicsToMigrate = peerNodes.get(nodeId);
            // Remove the node from the peerNodes map
            peerNodes.remove(nodeId);

            // Try to find another peer to migrate the topics to
            Optional<String> availablePeer = peerNodes.keySet().stream().findAny();
            if (availablePeer.isPresent()) {
                // If a peer is found, migrate the topics to it
                peerNodes.get(availablePeer.get()).addAll(topicsToMigrate);
                return Map.of("status", "unregistered", "node_id", nodeId, "topics_migrated_to", availablePeer.get());
            }

            // If no peer is found, just delete the topics
            return Map.of("status", "unregistered", "node_id", nodeId, "message", "Topics deleted");
        } else {
            // If the node wasn't found, return an error message
            return Map.of("status", "error", "message", "Node not found");
        }
    }

    // This method handles updating topics for a peer node
    @PostMapping("/update_topics")
    public Map<String, Object> updateTopics(@RequestBody Map<String, Object> body) {
        // Extract node ID and new topics from the request body
        String nodeId = (String) body.get("node_id");
        List<String> topics = (List<String>) body.get("topics");

        // Check if the node exists in the peerNodes map
        if (peerNodes.containsKey(nodeId)) {
            // Update the topics for the node
            peerNodes.put(nodeId, topics);
            return Map.of("status", "updated", "node_id", nodeId);
        } else {
            // If the node wasn't found, return an error message
            return Map.of("status", "error", "message", "Node not found");
        }
    }

    // This method handles querying which node hosts a specific topic
    @GetMapping("/query_topic/{topic}")
    public Map<String, Object> queryTopic(@PathVariable String topic) {
        // Loop through all peer nodes and their topics
        for (Map.Entry<String, List<String>> entry : peerNodes.entrySet()) {
            // If a node hosts the requested topic, return its ID
            if (entry.getValue().contains(topic)) {
                return Map.of("status", "found", "node_id", entry.getKey());
            }
        }
        // If the topic wasn't found, return a not found status
        return Map.of("status", "not_found");
    }

    // This method returns collected metrics for all peer nodes
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        // Simply return the collected metrics
        return Map.of("peer_metrics", collectedMetrics);
    }
}