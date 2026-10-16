package org.example.error;
public class NoAirlockAvailableException extends StationException {
    public NoAirlockAvailableException(int capacity) {
        super("No airlock slot available (capacity " + capacity + ")");
    }
}
