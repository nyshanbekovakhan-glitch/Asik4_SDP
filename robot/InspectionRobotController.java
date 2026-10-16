package org.example.robot;

import org.example.error.InspectionNotFoundException;
import org.example.error.RobotBatteryLowException;
import org.example.error.RobotLinkLostException;

import java.util.List;
import java.util.UUID;

public class InspectionRobotController {
    private final InspectionRunRepository repository;
    private final LeakSensor sensor;
    private final int minBattery;

    public InspectionRobotController(InspectionRunRepository repository, LeakSensor sensor, int minBattery) {
        this.repository = repository;
        this.sensor = sensor;
        this.minBattery = minBattery;
    }

    public InspectionRun inspect(String moduleId) {
        int battery = repository.loadBattery();
        if (battery < minBattery) {
            throw new RobotBatteryLowException(battery, minBattery);
        }

        InspectionRun run = new InspectionRun("RUN-" + UUID.randomUUID().toString().substring(0, 8), moduleId);
        repository.save(run);

        run.start();
        repository.save(run);

        if (!repository.loadSignalOnline()) {
            run.abort();
            repository.save(run);
            throw new RobotLinkLostException(moduleId);
        }

        run.complete(sensor.detect(moduleId));
        repository.save(run);
        return run;
    }

    public InspectionRun abort(String runId) {
        InspectionRun run = get(runId);
        if (run.getStatus() == InspectionRun.Status.ABORTED) {
            return run;
        }
        run.abort();
        repository.save(run);
        return run;
    }

    public InspectionRun get(String runId) {
        return repository.findById(runId).orElseThrow(() -> new InspectionNotFoundException(runId));
    }

    public List<InspectionRun> all() {
        return repository.findAll();
    }

    public boolean isSignalOnline() {
        return repository.loadSignalOnline();
    }

    public void setSignalOnline(boolean online) {
        repository.saveSignalOnline(online);
    }

    public int battery() {
        return repository.loadBattery();
    }

    public void setBattery(int level) {
        repository.saveBattery(level);
    }
}