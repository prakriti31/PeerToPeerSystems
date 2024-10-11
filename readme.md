# Peer-to-Peer System in Java

## Table of Contents
- [Introduction](#introduction)
- [Tools Used](#tools-used)
- [What is a Peer-to-Peer System?](#what-is-a-peer-to-peer-system)
- [Structure of the APIs](#structure-of-the-apis)
- [API Overview](#api-overview)
- [Testing Results](#testing-results)
- [Conclusion](#conclusion)
- [How to Run the Project](#how-to-run-the-project)

## Introduction
This project implements a Peer-to-Peer (P2P) System in Java, focusing on efficient communication, fault tolerance, and high availability.

## Tools Used
- Maven
- Gradle
- XYChart
- Spring Boot
- Java

## What is a Peer-to-Peer System?
A Peer-to-Peer (P2P) system is a decentralized network architecture where participants (peers) interact and share resources directly without relying on a central server. Key characteristics include:
- Decentralization
- Distributed Resources
- Scalability
- Fault Tolerance

## Structure of the APIs
The system includes 13 API endpoints, extending the functionality of a Publisher-Subscriber System.

## API Overview
1. Initialize Peer Node
2. Register Peer with Indexing Server
3. Create Topic on Peer Node
4. Publish Message to Topic
5. Subscribe to a Topic
6. Pull Messages from a Topic
7. Unregister Peer Node
8. Query Topic from Indexing Server
9. Report Metrics to Indexing Server
10. Get Event Log of Peer
11. Get Metrics from Indexing Server

## Testing Results
### Requirement 1: Deploying Multiple Peers
- At least 3 peers and 1 indexing server were set up.
- All APIs were tested for proper functionality.
- Multiple peer nodes successfully published and subscribed to topics simultaneously.

### Requirement 2: Measuring Response Time
- Tested with varying numbers of concurrent peer nodes (2, 4, 8, 10, 12, 14, 16, 18, 20).
- Each node made 1000 requests.
- The indexing server was configured to hold 1000 topics.
- Results were graphed to show the relationship between the number of concurrent nodes and average response time.

### Requirement 3: Benchmarking Latency and Throughput
- Started with 1 peer and 1 indexing server.
- Increased the number of peers to 8.
- Graphed results for latency and throughput of each API.

## Conclusion
The project successfully implemented a robust P2P messaging system with:
- Efficient communication
- Fault tolerance
- High availability
- Performance benchmarking
- Network monitoring and analytics

## How to Run the Project
1. Clone the repository:

git clone https://github.com/prakriti31/PeerToPeerSystems.git

2. Build the project:

mvn clean install
text
3. Run tests:

mvn test
text

### Prerequisites for Running Tests
- For IndexingServerTests:
- Start the Spring Boot server on port 8080 using P2PSystemApplication.java
- Run the indexing server plot file to generate graphs from CSVs 
- For PeerNodeTests and PeerNodePlot:
- Follow a similar approach as IndexingServerTests
- For requirements1 file:
- Stop/Kill any process running on port 8080
- Initialize 3 peers on different ports:
 ```
 cd target/
 java -jar p2p-systegitm-0.0.1-SNAPSHOT.jar --server.port=8081
 java -jar p2p-system-0.0.1-SNAPSHOT.jar --server.port=8082
 java -jar p2p-system-0.0.1-SNAPSHOT.jar --server.port=8083
 ```
- For requirements2 file:
- Initialize 20 peers on ports 8081 to 8100 and then trigger the file
- To initialize APIs:
- Run P2PSystemApplication.java
- Use the provided cURLs to interact with the system

For more detailed information, please refer to the full documentation.