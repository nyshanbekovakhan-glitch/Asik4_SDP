package org.example.error;
public class InvalidModuleException extends StationException {
    public InvalidModuleException(String moduleId) {
        super("Invalid module id: '" + moduleId + "' (expected something like MODULE-3)");
    }
}
