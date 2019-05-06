# graph-path-finder-service of graph-app

This service takes a directed graph with given ID and executes Dijkstra algorithm on it (starting from
a vertex of that graph specified as an argument).

Custom graph library is used here to:
* deserialize a graph JSON into a graph object
* execute Dijkstra algorithm on that graph object
* serialize the results of that algorithm execution

 
 This service needs to access graph database through another service, since it itself has no database.
 Current implementation gets graphs from **graph-storage-service** via Feign client.