package ml.echelon133.services.graphpathfinder.path;

import ml.echelon133.graph.Vertex;
import ml.echelon133.graph.VertexResult;
import ml.echelon133.services.graphpathfinder.GraphPathFinderApp;
import ml.echelon133.services.graphpathfinder.path.exception.GraphDoesNotExistException;
import ml.echelon133.services.graphpathfinder.path.exception.GraphDoesNotHaveGivenVertexException;
import ml.echelon133.services.graphpathfinder.path.exception.GraphNotAvailableException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RunWith(MockitoJUnitRunner.class)
public class PathControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PathService pathService;

    @InjectMocks
    private PathController pathController;

    @InjectMocks
    private APIExceptionHandler exceptionHandler;

    private JacksonTester<Map<Vertex<BigDecimal>, VertexResult<BigDecimal>>> jsonPathResult;

    @Before
    public void before() {
        JacksonTester.initFields(this, GraphPathFinderApp.objectMapper());

        // Our test controller needs this converter so that it can use our custom ObjectMapper
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(GraphPathFinderApp.objectMapper());

        mockMvc = MockMvcBuilders
                .standaloneSetup(pathController)
                .setControllerAdvice(exceptionHandler)
                .setMessageConverters(converter)
                .build();
    }

    @Test
    public void calcPathRespondsCorrectlyWhenParamStartFromEmpty() throws Exception {
        String graphId = "test";
        String startFrom = "";

        // When
        MockHttpServletResponse response = mockMvc.perform(post("/api/graphs/" + graphId + "/paths")
                .accept(MediaType.APPLICATION_JSON)
                .param("startFrom", startFrom)).andReturn().getResponse();

        // Then
        assertThat(response.getContentAsString()).contains("Parameter 'startFrom' is required to proceed with the request");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void calcPathRespondsCorrectlyWhenParamStartFromNotGiven() throws Exception {
        String graphId = "test";

        // When
        MockHttpServletResponse response = mockMvc.perform(post("/api/graphs/" + graphId + "/paths")
                .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();

        // Then
        assertThat(response.getContentAsString()).contains("Parameter 'startFrom' is required to proceed with the request");
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void calcPathRespondsCorrectlyWhenGraphDoesNotExist() throws Exception {
        String graphId = "asdf";
        String startFrom = "v1";

        // Given
        String exceptionMsg = String.format("Graph with ID %s does not exist. Cannot find shortest paths", graphId);
        given(pathService.calculateShortestPath(eq(graphId), eq(startFrom))).willThrow(new GraphDoesNotExistException(exceptionMsg));

        // When
        MockHttpServletResponse response = mockMvc.perform(post("/api/graphs/" + graphId + "/paths")
                .accept(MediaType.APPLICATION_JSON)
                .param("startFrom", startFrom)).andReturn().getResponse();

        // Then
        assertThat(response.getContentAsString()).contains(exceptionMsg);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void calcPathRespondsCorrectlyWhenGraphNotAvailable() throws Exception {
        String graphId = "asdf";
        String startFrom = "v1";

        // Given
        String exceptionMsg = String.format("Graph with ID %s is unreachable right now. Try again later", graphId);
        given(pathService.calculateShortestPath(eq(graphId), eq(startFrom))).willThrow(new GraphNotAvailableException(exceptionMsg));

        // When
        MockHttpServletResponse response = mockMvc.perform(post("/api/graphs/" + graphId + "/paths")
                .accept(MediaType.APPLICATION_JSON)
                .param("startFrom", startFrom)).andReturn().getResponse();

        // Then
        assertThat(response.getContentAsString()).contains(exceptionMsg);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void calcPathRespondsCorrectlyWhenGraphDoesNotHaveGivenStartVertex() throws Exception {
        String graphId = "asdf";
        String startFrom = "v1";

        // Given
        String exceptionMsg = String.format("Graph with ID %s does not have a vertex with name %s", graphId, startFrom);
        given(pathService.calculateShortestPath(eq(graphId), eq(startFrom))).willThrow(new GraphDoesNotHaveGivenVertexException(exceptionMsg));

        // When
        MockHttpServletResponse response = mockMvc.perform(post("/api/graphs/" + graphId + "/paths")
                .accept(MediaType.APPLICATION_JSON)
                .param("startFrom", startFrom)).andReturn().getResponse();

        // Then
        assertThat(response.getContentAsString()).contains(exceptionMsg);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
