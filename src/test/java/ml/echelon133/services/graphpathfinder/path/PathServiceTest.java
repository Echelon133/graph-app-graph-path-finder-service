package ml.echelon133.services.graphpathfinder.path;

import feign.FeignException;
import feign.Request;
import feign.Response;
import ml.echelon133.graph.Graph;
import ml.echelon133.graph.Vertex;
import ml.echelon133.graph.VertexResult;
import ml.echelon133.graph.WeightedGraph;
import ml.echelon133.services.graphpathfinder.storageclient.GraphStorageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class PathServiceTest {

    @Mock
    private GraphStorageClient graphClient;

    @InjectMocks
    private PathServiceImpl pathService;


    private FeignException createFeignExceptionWithStatus(Integer status) {
        // Really hacky way to create a FeignException on our own...
        // We need the ability to fake feign client exceptions (ex. other service being down, having no response, etc...)

        // create fake headers
        Map<String, Collection<String>> fakeHeaders = new LinkedHashMap<>();
        fakeHeaders.put("Transfer-Encoding", List.of("chunked"));
        fakeHeaders.put("Date", List.of("Wed,24 Apr 2019 12:00:00 GMT"));
        fakeHeaders.put("Content-Type", List.of("application/json;charset=UTF-8"));

        // build a fake response
        Response resp = Response.builder()
                .status(status)
                .request(
                        Request.create(
                                Request.HttpMethod.GET,
                                "testurl",
                                fakeHeaders,
                                new byte[1],
                                Charset.defaultCharset()
                        )
                )
                .headers(fakeHeaders)
                .build();
        return FeignException.errorStatus("GET", resp);
    }

    @Test
    public void calculateShortestPathReturnsCorrectResult() throws Exception {
        String testGraphId = "abcdefghijklmnoprst";

        // prepare a simple test graph
        Graph<BigDecimal> testGraph = new WeightedGraph<>();
        Vertex<BigDecimal> v1Vertex = new Vertex<>("v1");
        Vertex<BigDecimal> v2Vertex = new Vertex<>("v2");
        Vertex<BigDecimal> v3Vertex = new Vertex<>("v3");
        List.of(v1Vertex, v2Vertex, v3Vertex).forEach(testGraph::addVertex);
        testGraph.addEdge(v1Vertex, v2Vertex, new BigDecimal(20));
        testGraph.addEdge(v2Vertex, v3Vertex, new BigDecimal(30));
        testGraph.addEdge(v3Vertex, v1Vertex, new BigDecimal(40));

        // Given
        given(graphClient.getGraph(eq(testGraphId))).willReturn(testGraph);

        // When
        Map<Vertex<BigDecimal>, VertexResult<BigDecimal>> result = pathService.calculateShortestPath(testGraphId, "v1");

        // Then
        VertexResult<BigDecimal> v1VertexResult = result.get(v1Vertex);
        VertexResult<BigDecimal> v2VertexResult = result.get(v2Vertex);
        VertexResult<BigDecimal> v3VertexResult = result.get(v3Vertex);

        // expected v1 --> sumOfWeights = 0      | pathToVertex = []        | previousVertex = null
        assertThat(v1VertexResult.getSumOfWeights()).isEqualByComparingTo(new BigDecimal(0));
        assertThat(v1VertexResult.getPreviousVertex()).isNull();
        assertThat(v1VertexResult.getPathToVertex()).isEqualTo(Collections.EMPTY_LIST);

        // expected v2 --> sumOfWeights = 20     | pathToVertex = [v1]      | previousVertex = v1
        assertThat(v2VertexResult.getSumOfWeights()).isEqualByComparingTo(new BigDecimal(20));
        assertThat(v2VertexResult.getPreviousVertex()).isEqualTo(v1Vertex);
        assertThat(v2VertexResult.getPathToVertex()).isEqualTo(List.of(v1Vertex));

        // expected v3 --> sumOfWeights = 50     | pathToVertex = [v1, v2]  | previousVertex = v2
        assertThat(v3VertexResult.getSumOfWeights()).isEqualByComparingTo(new BigDecimal(50));
        assertThat(v3VertexResult.getPreviousVertex()).isEqualTo(v2Vertex);
        assertThat(v3VertexResult.getPathToVertex()).isEqualTo(List.of(v1Vertex, v2Vertex));
    }

    @Test
    public void calculateShortestPathThrowsExceptionWhenGraphDoesNotExist() {
        String graphId = "aaaaaaaaaaaaaaaaaaaaaaaaaaa";

        String expectedMsg = String.format("Graph with ID %s does not exist. Cannot find shortest paths", graphId);
        String receivedMsg = "";

        // Given

        // We need to fake other service returning 404 status to our request
        FeignException ex = createFeignExceptionWithStatus(404);
        given(graphClient.getGraph(graphId)).willThrow(ex);

        // When
        try {
            Map<Vertex<BigDecimal>, VertexResult<BigDecimal>> result = pathService.calculateShortestPath(graphId, "v1");
        } catch (Exception e) {
            receivedMsg = e.getMessage();
        }

        // Then
        assertThat(receivedMsg).isEqualTo(expectedMsg);
    }

    @Test
    public void calculateShortestPathThrowsExceptionOnClientServiceError() {
        String graphId = "aaaaaaaaaaaaaaaaaaaaaaaaaaa";

        String expectedMsg = String.format("Graph with ID %s is unreachable right now. Try again later", graphId);
        String receivedMsg = "";

        // Given

        // We need to fake other service returning 500 status to our request
        FeignException ex = createFeignExceptionWithStatus(500);
        given(graphClient.getGraph(graphId)).willThrow(ex);

        // When
        try {
            Map<Vertex<BigDecimal>, VertexResult<BigDecimal>> result = pathService.calculateShortestPath(graphId, "v1");
        } catch (Exception e) {
            receivedMsg = e.getMessage();
        }

        // Then
        assertThat(receivedMsg).isEqualTo(expectedMsg);
    }

    @Test
    public void calculateShortestPathThrowsExceptionWhenVertexNameNotInGraph() {
        String testGraphId = "abcdefghijklmnoprst";
        String vertexName = "v2";

        String expectedMsg = String.format("Graph with ID %s does not have a vertex with name %s", testGraphId, vertexName);
        String receivedMsg = "";

        // prepare a simple test graph
        Graph<BigDecimal> testGraph = new WeightedGraph<>();
        Vertex<BigDecimal> v1Vertex = new Vertex<>("v1");
        testGraph.addVertex(v1Vertex);

        // Given
        given(graphClient.getGraph(eq(testGraphId))).willReturn(testGraph);

        // When
        try {
            Map<Vertex<BigDecimal>, VertexResult<BigDecimal>> result = pathService.calculateShortestPath(testGraphId, "v2");
        } catch (Exception e) {
            receivedMsg = e.getMessage();
        }

        // Then
        assertThat(receivedMsg).isEqualTo(expectedMsg);
    }
}
