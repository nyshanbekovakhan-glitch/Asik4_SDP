package org.example.robot;

public class LeakSensor {

    public int detect(String moduleId) {
        try {
            return Integer.parseInt(moduleId.substring(moduleId.lastIndexOf('-') + 1)) % 3;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot read module number from " + moduleId);
        }
    }
}