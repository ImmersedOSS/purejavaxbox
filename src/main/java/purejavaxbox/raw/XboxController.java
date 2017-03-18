package purejavaxbox.raw;

import purejavaxbox.XboxButton;

import java.util.Map;

/**
 * The interface representing the controller. Most native controllers are not thread safe. Read the package details for
 * more information.
 */
public interface XboxController
{
    /**
     * Gets the latest state of the controller. This method is not required to be thread-safe, and depends on the
     * implementation. Consult package documentation for each implementation to get the details.There are two types of
     * numeric values to be found in the returned map:
     * <p>
     * <pre>
     * <b>Toggle Buttons:</b> An integer with value of 0 to 1.
     *  0 = OFF
     *  1 = 0N
     * <b>Analog Buttons:</b> A scaled double between 0.0 and 1.0.
     *  0.0 = OFF.
     *  1.0 = Fully pressed.
     * <b>Analog Sticks:</b> A scaled double between -1.0 and 1.0.
     * -1.0 = Left or Down.
     *  0.0 = Centered.
     *  1.0 = Right or Up.
     * </pre>
     * <p>
     * Most libraries define dead zones for analog components (sticks, triggers). Implementations will use the vendor
     * recommended defaults with the ability to adjust them. Consult package documentation for how to configure dead
     * zones for a given library.
     *
     * @return a read-only map containing the state of the values for the controller. If the controller is not
     * available, then this map will be empty.
     */
    Map<XboxButton, Number> buttons();

    /**
     * Enables the rumble pack. Will continue to rumble until stopped.
     *
     * @param lowFrequency  - the percentage of rumble, where 0.0 is off and 1.0 is max rumble.
     * @param highFrequency - the percentage of rumble, where 0.0 is off and 1.0 is max rumble.
     */
    void rumble(double lowFrequency, double highFrequency);
}
