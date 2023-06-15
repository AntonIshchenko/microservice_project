package org.resource.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class APIException extends ResponseStatusException {

    public APIException(HttpStatusCode status, String reason) {
        super(status, reason);
    }

}
