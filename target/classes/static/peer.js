const baseUrl = 'http://localhost:8080/peer';  // Adjust this to your actual backend URL

// Initialize Peer
function initializePeer() {
    const indexServerIp = document.getElementById("indexServerIp").value;
    const indexServerPort = document.getElementById("indexServerPort").value;

    fetch(`${baseUrl}/initialize?indexServerIp=${indexServerIp}&indexServerPort=${indexServerPort}`, {
        method: 'POST'
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("init-response").innerText = `Peer initialized with ID: ${data.node_id}`;
        })
        .catch(error => console.error("Error:", error));
}

// Create Topic
function createTopic() {
    const topicName = document.getElementById("topicName").value;

    fetch(`${baseUrl}/create_topic`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(topicName)
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("create-response").innerText = `Topic created: ${data.topic}`;
        })
        .catch(error => console.error("Error:", error));
}

// Publish Message
function publishMessage() {
    const topic = document.getElementById("topicToPublish").value;
    const message = document.getElementById("message").value;

    fetch(`${baseUrl}/publish`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ topic, message })
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("publish-response").innerText = `Message published to ${data.topic}`;
        })
        .catch(error => console.error("Error:", error));
}

// Subscribe to Topic
function subscribeTopic() {
    const topic = document.getElementById("topicToSubscribe").value;

    fetch(`${baseUrl}/subscribe/${topic}`, {
        method: 'GET'
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("subscribe-response").innerText = `Subscribed to ${data.topic}`;
        })
        .catch(error => console.error("Error:", error));
}

// Pull Messages
function pullMessages() {
    const topic = document.getElementById("topicToPull").value;

    fetch(`${baseUrl}/pull_messages/${topic}`, {
        method: 'GET'
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("messages-response").innerText = `Messages: ${data.messages.join(", ")}`;
        })
        .catch(error => console.error("Error:", error));
}

// Register Node
function registerNode() {
    fetch(`${baseUrl}/register_with_indexing_server`, {
        method: 'POST'
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("register-response").innerText = `Node registered: ${data.node_id}`;
        })
        .catch(error => console.error("Error:", error));
}

// Unregister Node
function unregisterNode() {
    const nodeId = document.getElementById("unregisterNodeId").value;

    fetch(`${baseUrl}/unregister`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ node_id: nodeId })
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("unregister-response").innerText = `Node unregistered: ${data.node_id}`;
        })
        .catch(error => console.error("Error:", error));
}

// Update Topics
function updateTopics() {
    const nodeId = document.getElementById("updateNodeId").value;
    const topics = document.getElementById("updateTopics").value.split(',');

    fetch(`${baseUrl}/update_topics`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ node_id: nodeId, topics })
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("update-response").innerText = `Topics updated: ${data.topics.join(", ")}`;
        })
        .catch(error => console.error("Error:", error));
}

// Query Topic
function queryTopic() {
    const topic = document.getElementById("queryTopic").value;

    fetch(`${baseUrl}/query_topic/${topic}`, {
        method: 'GET'
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("query-response").innerText = `Query result: ${data.result}`;
        })
        .catch(error => console.error("Error:", error));
}

// Report Metrics
function reportMetrics() {
    const latency = document.getElementById("latency").value;
    const bandwidth = document.getElementById("bandwidth").value;

    fetch(`${baseUrl}/report_metrics`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ latency, bandwidth })
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("metrics-response").innerText = `Metrics reported: Latency - ${data.latency}ms, Bandwidth - ${data.bandwidth}Mbps`;
        })
        .catch(error => console.error("Error:", error));
}

// Get Metrics
function getMetrics() {
    fetch(`${baseUrl}/get_metrics`, {
        method: 'GET'
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById("get-metrics-response").innerText = `Metrics: Latency - ${data.latency}ms, Bandwidth - ${data.bandwidth}Mbps`;
        })
        .catch(error => console.error("Error:", error));
}
