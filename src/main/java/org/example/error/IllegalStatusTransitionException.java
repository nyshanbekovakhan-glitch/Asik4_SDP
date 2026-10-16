package org.example.error;
public class IllegalStatusTransitionException extends StationException {
    public IllegalStatusTransitionException(String message) {
        super(message);
    }
}
