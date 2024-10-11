[//]: # (# PeerNetworkTestRequirement1.java)

[//]: # (java -jar p2p-system-0.0.1-SNAPSHOT.jar --server.port=8081 &)

[//]: # (java -jar p2p-system-0.0.1-SNAPSHOT.jar --server.port=8082 &)

[//]: # (java -jar p2p-system-0.0.1-SNAPSHOT.jar --server.port=8083 &)

[//]: # ()
[//]: # (To check if the ports are up and running)

[//]: # (netstat -tuln | grep LISTEN)

[//]: # (This will list all active connections and listening ports, helping you confirm that your application instances are indeed running on ports 8081, 8082, and 8083.)

[//]: # ()
[//]: # (Steps to Run Your Application)

[//]: # (Navigate to the Directory)

[//]: # (Open your terminal and navigate to the directory where your JAR file is located:)

[//]: # ()
[//]: # (bash)

[//]: # (Copy code)

[//]: # (cd /path/to/your/jarfile)

[//]: # (Run the Application on Different Ports)

[//]: # (Execute the following commands to run the application on the specified ports. You can do this in separate terminal windows or tabs to run them concurrently:)

[//]: # ()
[//]: # (bash)

[//]: # (Copy code)

[//]: # (java -jar p2p-system-0.0.1-SNAPSHOT.jar --server.port=8081)

[//]: # (bash)

[//]: # (Copy code)

[//]: # (java -jar p2p-system-0.0.1-SNAPSHOT.jar --server.port=8082)

[//]: # (bash)

[//]: # (Copy code)

[//]: # (java -jar p2p-system-0.0.1-SNAPSHOT.jar --server.port=8083)





[//]: # (file: PeerNetworkTestRequirements2)

[//]: # (Explanation:)

[//]: # (Concurrent Peer Nodes:)

[//]: # ()
[//]: # (The code tests with 2, 4, and 8 peer nodes by calling the measureAverageResponseTime function.)

[//]: # (Each peer node runs on its own thread and queries the indexing server.)

[//]: # (Requests per Node:)

[//]: # ()
[//]: # (Each peer node sends 1000 requests to the indexing server. This can be adjusted by modifying the REQUESTS_PER_NODE constant.)

[//]: # (Simulating the Indexing Server:)

[//]: # ()
[//]: # (The server holds 1 million topics. The peers query these topics, which simulates querying topic information from the indexing server.)

[//]: # (Measuring Response Time:)

[//]: # ()
[//]: # (The queryIndexingServer method simulates querying a topic from the indexing server and records the response time for each request.)

[//]: # (Average Response Time:)

[//]: # ()
[//]: # (The average response time per request is calculated and printed for each configuration of peer nodes &#40;2, 4, 8&#41;.)