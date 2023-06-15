package org.resource.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(APIException.class)
    public ResponseEntity<?> handleException(APIException exception) {
        ErrorDetails errorDetails = new ErrorDetails(exception.getStatusCode().toString(), exception.getReason());
        return new ResponseEntity<>(errorDetails, exception.getStatusCode());
    }
}
