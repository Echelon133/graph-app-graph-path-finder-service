package ml.echelon133.services.graphpathfinder.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graphs/{id}")
public class PathController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathController.class);

    public PathController() {}

    @PostMapping("/paths")
    public ResponseEntity<String> calcPaths(@PathVariable String id, @RequestParam(required = false) String startFrom) throws Exception {

        // startFrom param 'required' set to false, so that we can handle its content our own way
        if (startFrom == null || startFrom.isBlank() || startFrom.isEmpty()) {
            LOGGER.debug(String.format("Attempt of calculating paths for graph with ID %s without giving 'startFrom' param value", id));
            throw new RequiredParameterNotGivenException("Parameter 'startFrom' is required to proceed with the request");
        }

        return new ResponseEntity<>("test", HttpStatus.OK);
    }
}
