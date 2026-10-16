package org.example.error;

public class ChainNotFoundException extends StationException {
    public ChainNotFoundException(String id) {
        super("Chain not found: " + id);
    }
}