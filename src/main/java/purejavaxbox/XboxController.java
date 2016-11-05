package purejavaxbox;

import java.util.Map;

/**
 * The interface representing the controller.
 */
public interface XboxController
{
    /**
     * Gets the latest state of the controller. This method is thread-safe and may be called at any frequency.
     *
     * @return a read-only map containing the state of the values for the controller. If the controller is not
     * available, then this map will be empty.
     */
    Map<XboxButton, Number> buttons();

    /**
     * Enables the rumble pack. Will continue to rumble until stopped.
     *
     * @param value - the percentage of rumble, where 0.0 is off and 1.0 is max rumble.
     */
    void rumble(double value);
}
