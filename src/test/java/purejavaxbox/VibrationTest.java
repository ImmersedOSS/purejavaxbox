package purejavaxbox;

import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VibrationTest
{
    private static final Logger LOG = LoggerFactory.getLogger(VibrationTest.class);
    private static final ScheduledExecutorService THREAD = Executors.newSingleThreadScheduledExecutor();

    @AfterClass
    public static void cleanup() throws InterruptedException
    {
        THREAD.shutdown();
        THREAD.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    public void testVibration() throws InterruptedException
    {
        LOG.info("Performing vibration test:");
        LOG.info("  Left trigger adjusts low frequency motor (vibration is strong and slower).");
        LOG.info("  Right trigger adjusts high frequency motor (vibration is weak and fast).");
        LOG.info("  Press the A button to complete the test.");

        final Phaser barrier = new Phaser(2);

        THREAD.schedule(() -> runTest(barrier), 50, TimeUnit.MILLISECONDS);

        barrier.arriveAndAwaitAdvance();
    }

    private void runTest(Phaser barrier)
    {
        XboxController ctrl = XboxControllers.INSTANCE.getController(0);
        Map<XboxButton, Number> buttons = ctrl.buttons();

        if (buttons.getOrDefault(XboxButton.A, 0)
                   .intValue() == 1)
        {
            barrier.arrive();
            return;
        }

        double vibrateLow = buttons.getOrDefault(XboxButton.LEFT_TRIGGER, 0.0)
                                   .doubleValue();
        double vibrateHigh = buttons.getOrDefault(XboxButton.RIGHT_TRIGGER, 0.0)
                                    .doubleValue();
        ctrl.rumble(vibrateLow, vibrateHigh);

        THREAD.schedule(() -> runTest(barrier), 50, TimeUnit.MILLISECONDS);
    }
}
