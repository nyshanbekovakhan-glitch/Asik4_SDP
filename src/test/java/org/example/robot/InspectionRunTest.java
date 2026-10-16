package org.example.robot;

import org.example.error.IllegalStatusTransitionException;
import org.example.robot.InspectionRun.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InspectionRunTest {

    private InspectionRun aFreshRun() {
        return new InspectionRun("RUN-1", "MODULE-4");
    }

    private InspectionRun aRunningRun() {
        InspectionRun run = aFreshRun();
        run.start();
        return run;
    }

    @Test
    void newRunIsQueuedWithNoDefects() {
        InspectionRun run = aFreshRun();
        assertEquals(Status.QUEUED, run.getStatus());
        assertEquals(0, run.getDefectCount());
    }

    @Test
    void queuedRunCanStart() {
        assertEquals(Status.RUNNING, aRunningRun().getStatus());
    }

    @Test
    void runningRunCanCompleteAndKeepsTheDefectCount() {
        InspectionRun run = aRunningRun();
        run.complete(2);
        assertEquals(Status.COMPLETED, run.getStatus());
        assertEquals(2, run.getDefectCount());
    }

    @Test
    void aRunCannotCompleteBeforeItStarts() {
        assertThrows(IllegalStatusTransitionException.class, () -> aFreshRun().complete(1));
    }

    @Test
    void aRunCannotStartTwice() {
        InspectionRun run = aRunningRun();
        assertThrows(IllegalStatusTransitionException.class, run::start);
    }

    @Test
    void queuedAndRunningRunsCanBeAborted() {
        InspectionRun queued = aFreshRun();
        queued.abort();
        assertEquals(Status.ABORTED, queued.getStatus());

        InspectionRun running = aRunningRun();
        running.abort();
        assertEquals(Status.ABORTED, running.getStatus());
    }

    @Test
    void abortingACompletedRunThrowsItsResultAway() {
        InspectionRun run = aRunningRun();
        run.complete(3);

        run.abort();

        assertEquals(Status.ABORTED, run.getStatus());
        assertEquals(0, run.getDefectCount());
    }

    @Test
    void anAbortedRunCannotBeAbortedAgain() {
        InspectionRun run = aFreshRun();
        run.abort();
        assertThrows(IllegalStatusTransitionException.class, run::abort);
    }

    @Test
    void toStringShowsIdModuleStatusAndDefects() {
        assertEquals("RUN-1 [MODULE-4, QUEUED, defects=0]", aFreshRun().toString());
    }
}