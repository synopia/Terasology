package org.terasology.logic.behavior;

import org.junit.Test;
import org.terasology.logic.behavior.core.BehaviorState;

import java.util.Arrays;

/**
 * Created by synopia on 11.01.2015.
 */
public class ParallelTest extends CountTest {

    @Test
    public void testAllSuccess() {
        assertBT("{ parallel:[success, success, success]}", Arrays.asList(BehaviorState.SUCCESS, BehaviorState.SUCCESS, BehaviorState.SUCCESS), Arrays.asList(4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3));
    }

    @Test
    public void testAllFail() {
        assertBT("{ parallel:[failure, failure, failure]}", Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE), Arrays.asList(4, 1, 4, 1, 4, 1));
    }

    @Test
    public void testAllRunning() {
        assertBT("{ parallel:[running, running, running]}", Arrays.asList(BehaviorState.RUNNING, BehaviorState.RUNNING, BehaviorState.RUNNING), Arrays.asList(4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3));
    }

    @Test
    public void testFailSuccess() {
        assertBT("{ parallel:[failure, success, success]}", Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE), Arrays.asList(4, 1, 4, 1, 4, 1));
    }

    @Test
    public void testSuccessFail() {
        assertBT("{ parallel:[success, failure, failure]}", Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE), Arrays.asList(4, 1, 2, 4, 1, 2, 4, 1, 2));
    }

    @Test
    public void testRunningFail() {
        assertBT("{ parallel:[running, failure, failure]}", Arrays.asList(BehaviorState.FAILURE, BehaviorState.FAILURE, BehaviorState.FAILURE), Arrays.asList(4, 1, 2, 4, 1, 2, 4, 1, 2));
    }

    @Test
    public void testRunningSuccess() {
        assertBT("{ parallel:[running, success, success]}", Arrays.asList(BehaviorState.RUNNING, BehaviorState.RUNNING, BehaviorState.RUNNING), Arrays.asList(4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3));
    }

}
