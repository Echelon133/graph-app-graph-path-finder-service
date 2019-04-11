package ml.echelon133.services.graphpathfinder.path;

import ml.echelon133.services.graphpathfinder.path.exception.GraphDoesNotExistException;
import ml.echelon133.services.graphpathfinder.path.exception.GraphDoesNotHaveGivenVertexException;
import ml.echelon133.services.graphpathfinder.path.exception.GraphNotAvailableException;
import ml.echelon133.services.graphpathfinder.path.exception.RequiredParameterNotGivenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class APIExceptionHandler extends ResponseEntityExceptionHandler {

    static class ErrorMessage {
        private Date timestamp;
        private String message;
        private String path;

        ErrorMessage(String message, String path) {
            this.timestamp = new Date();
            this.message = message;
            this.path = path;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }
    }

    @ExceptionHandler(value = RequiredParameterNotGivenException.class)
    protected ResponseEntity<ErrorMessage> handleRequiredParameterNotGivenException(RequiredParameterNotGivenException ex, WebRequest request) {
        ErrorMessage msg = new ErrorMessage(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = GraphDoesNotHaveGivenVertexException.class)
    protected ResponseEntity<ErrorMessage> handleGraphDoesNotHaveGivenVertexException(GraphDoesNotHaveGivenVertexException ex, WebRequest request) {
        ErrorMessage msg = new ErrorMessage(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = GraphDoesNotExistException.class)
    protected ResponseEntity<ErrorMessage> handleGraphDoesNotExistException(GraphDoesNotExistException ex, WebRequest request) {
        ErrorMessage msg = new ErrorMessage(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(msg, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = GraphNotAvailableException.class)
    protected ResponseEntity<ErrorMessage> handleGraphNotAvailableException(GraphNotAvailableException ex, WebRequest request) {
        ErrorMessage msg = new ErrorMessage(ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(msg, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
