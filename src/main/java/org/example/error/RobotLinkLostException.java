package org.example.error;
public class RobotLinkLostException extends StationException {
    public RobotLinkLostException(String moduleId) {
        super("Robot lost its signal while inspecting " + moduleId);
    }
}
