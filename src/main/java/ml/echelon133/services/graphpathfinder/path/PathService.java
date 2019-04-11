package ml.echelon133.services.graphpathfinder.path;

import ml.echelon133.graph.Vertex;
import ml.echelon133.graph.VertexResult;

import java.math.BigDecimal;
import java.util.Map;

public interface PathService {
    Map<Vertex<BigDecimal>, VertexResult<BigDecimal>> calculateShortestPath(String graphId, String startVertexName) throws Exception;
}
