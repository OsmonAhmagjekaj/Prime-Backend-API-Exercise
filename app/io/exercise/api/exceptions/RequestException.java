package io.exercise.api.exceptions;

import lombok.Data;
import lombok.ToString;

@ToString
public @Data class RequestException extends Exception {
	private static final long serialVersionUID = 1L;
	// either String or List<String>
    private Object description;
    private int statusCode;

    public RequestException(int statusCode, Object message) {
        super(message.toString());
        this.description = message;
        this.statusCode = statusCode;
    }
    
    @Override
    public String getMessage() {
        return description.toString();
    }

    public int getStatusCode() {
        return statusCode;
    }
}


