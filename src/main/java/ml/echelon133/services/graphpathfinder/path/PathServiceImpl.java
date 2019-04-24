package ml.echelon133.services.graphpathfinder.path;

import feign.FeignException;
import ml.echelon133.graph.Graph;
import ml.echelon133.graph.ShortestPathSolver;
import ml.echelon133.graph.Vertex;
import ml.echelon133.graph.VertexResult;
import ml.echelon133.services.graphpathfinder.path.exception.GraphDoesNotExistException;
import ml.echelon133.services.graphpathfinder.path.exception.GraphDoesNotHaveGivenVertexException;
import ml.echelon133.services.graphpathfinder.path.exception.GraphNotAvailableException;
import ml.echelon133.services.graphpathfinder.storageclient.GraphStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PathServiceImpl implements PathService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathServiceImpl.class);

    private GraphStorageClient graphStorageClient;

    @Autowired
    public PathServiceImpl(GraphStorageClient graphStorageClient) {
        this.graphStorageClient = graphStorageClient;
    }

    @Override
    public Map<Vertex<BigDecimal>, VertexResult<BigDecimal>> calculateShortestPath(String graphId, String startVertexName) throws Exception {

        Map<Vertex<BigDecimal>, VertexResult<BigDecimal>> result;
        Graph<BigDecimal> graph;

        // get a graph with specified id from our graph-store-service
        try {
            graph = graphStorageClient.getGraph(graphId);
            LOGGER.debug(String.format("Graph with ID %s received and deserialized", graphId));
        } catch (FeignException ex) {

            if (ex.status() == 404) {
                String msg = String.format("Graph with ID %s does not exist. Cannot find shortest paths", graphId);
                LOGGER.debug(msg);
                throw new GraphDoesNotExistException(msg);
            } else {
                // unexpected, the service implementation failed in an unusual way
                String msg = String.format("Graph with ID %s is unreachable right now. Try again later", graphId);
                LOGGER.debug(msg);
                throw new GraphNotAvailableException(msg);
            }
        }

        // find the start vertex with a name that was given as a param to the request ('startFrom')
        Vertex<BigDecimal> startVertex = graph.findVertex(startVertexName);

        // instantiate our solver with the graph we got from graph-store-service
        // at this point this graph is 100% correct (because the deserializer didn't throw any errors)
        ShortestPathSolver<BigDecimal> sps = new ShortestPathSolver<>(graph);

        try {
            result = sps.solveStartingFrom(startVertex);
        } catch (IllegalArgumentException ex) {
            String msg = String.format("Graph with ID %s does not have a vertex with name %s", graphId, startVertexName);
            LOGGER.debug(msg);
            throw new GraphDoesNotHaveGivenVertexException(msg);
        }

        return result;
    }
}
