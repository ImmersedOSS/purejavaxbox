package purejavaxbox.api;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Utility class to prevent duplication and improve readability of {@link ControllerApi}.
 */
final class HelperMethods
{
    /**
     * The predicate created by this function is used to filter out values in the observeAfterDelay(..) functions.
     *
     * @param count     - the number of ticks since the button was first held down.
     * @param timestamp - the timestamp indicating when the buttons(s) were first pressed down.
     * @param timeNanos - the measure of the delay in nanos.
     * @param <T>       - the type of value to consume. Value is unused in the calculation.
     * @return returns true if count == 0, or the button(s) have been held longer than timeNanos.
     */
    static <T> Predicate<T> duringDelay(AtomicLong count, AtomicLong timestamp, long timeNanos)
    {
        return v -> {
            boolean isFirst = count.getAndIncrement() == 0;
            long elapsed = System.nanoTime() - timestamp.get();
            long diff = elapsed - timeNanos;
            boolean timeHasExceededDelay = diff >= 0;
            return isFirst || timeHasExceededDelay;
        };
    }

    private HelperMethods()
    {
        // Utility class
    }
}
