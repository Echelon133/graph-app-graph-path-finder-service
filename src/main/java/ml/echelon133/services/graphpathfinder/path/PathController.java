package ml.echelon133.services.graphpathfinder.path;

import ml.echelon133.graph.Vertex;
import ml.echelon133.graph.VertexResult;
import ml.echelon133.services.graphpathfinder.path.exception.RequiredParameterNotGivenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/graphs/{id}")
public class PathController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathController.class);

    private PathService pathService;

    @Autowired
    public PathController(PathService pathService) {
        this.pathService = pathService;
    }

    @PostMapping("/paths")
    public ResponseEntity<Map<Vertex<BigDecimal>, VertexResult<BigDecimal>>> calcPath(@PathVariable String id, @RequestParam(required = false) String startFrom) throws Exception {

        // startFrom param 'required' set to false, so that we can handle its content our own way
        if (startFrom == null || startFrom.isBlank() || startFrom.isEmpty()) {
            LOGGER.debug(String.format("Attempt of calculating paths for graph with ID %s without giving 'startFrom' param value", id));
            throw new RequiredParameterNotGivenException("Parameter 'startFrom' is required to proceed with the request");
        }

        Map<Vertex<BigDecimal>, VertexResult<BigDecimal>> result = pathService.calculateShortestPath(id, startFrom);

        LOGGER.debug(String.format("About to return calculated paths of a graph with ID %s (startFrom=%s)", id, startFrom));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
