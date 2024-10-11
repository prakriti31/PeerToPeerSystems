package com.example.p2p;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/indexing")
public class IndexingServerController {

    // Stores peer nodes and the topics they host
    private final Map<String, List<String>> peerNodes = new HashMap<>();

    // Stores metrics reported by each peer node
    private final Map<String, Map<String, Object>> collectedMetrics = new HashMap<>();

    @PostMapping("/register")
    public Map<String, Object> registerNode(@RequestBody Map<String, Object> body) {
        String nodeId = (String) body.get("node_id");
        List<String> topics = (List<String>) body.get("topics");

        peerNodes.put(nodeId, topics);

        return Map.of("status", "registered", "node_id", nodeId);
    }

    @PostMapping("/unregister")
    public Map<String, Object> unregisterNode(@RequestBody Map<String, Object> body) {
        String nodeId = (String) body.get("node_id");

        if (peerNodes.containsKey(nodeId)) {
            List<String> topicsToMigrate = peerNodes.get(nodeId);
            peerNodes.remove(nodeId);

            // Migrate topics to another peer, if any topics are being hosted
            Optional<String> availablePeer = peerNodes.keySet().stream().findAny();
            if (availablePeer.isPresent()) {
                peerNodes.get(availablePeer.get()).addAll(topicsToMigrate);
                return Map.of("status", "unregistered", "node_id", nodeId, "topics_migrated_to", availablePeer.get());
            }

            return Map.of("status", "unregistered", "node_id", nodeId, "message", "Topics deleted");
        } else {
            return Map.of("status", "error", "message", "Node not found");
        }
    }

    @PostMapping("/update_topics")
    public Map<String, Object> updateTopics(@RequestBody Map<String, Object> body) {
        String nodeId = (String) body.get("node_id");
        List<String> topics = (List<String>) body.get("topics");

        if (peerNodes.containsKey(nodeId)) {
            peerNodes.put(nodeId, topics);
            return Map.of("status", "updated", "node_id", nodeId);
        } else {
            return Map.of("status", "error", "message", "Node not found");
        }
    }

    @GetMapping("/query_topic/{topic}")
    public Map<String, Object> queryTopic(@PathVariable String topic) {
        for (Map.Entry<String, List<String>> entry : peerNodes.entrySet()) {
            if (entry.getValue().contains(topic)) {
                return Map.of("status", "found", "node_id", entry.getKey());
            }
        }
        return Map.of("status", "not_found");
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        // Return collected metrics for all peers
        return Map.of("peer_metrics", collectedMetrics);
    }
}
