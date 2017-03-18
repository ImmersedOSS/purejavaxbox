package purejavaxbox.api;

import purejavaxbox.XboxButton;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Implementations of this class allow developers to extend the API by manipulating RAW values from the controller
 * output before users receive them. <p> API's are not required to support this functionality. But if they do, then they
 * must be executed in a user-specified order each time the controller is polled. </p>
 *
 * @see ControllerBuilder#mapper(ButtonMapper)
 */
@FunctionalInterface
public interface ButtonMapper extends Consumer<Map<XboxButton, Number>>
{
    /**
     * Manipulates this incoming map. Implementations will define how the map is modified. If the incoming map is empty,
     * this method does nothing.
     *
     * @param buttons - the current set of values for each button polled by the controller. Button Mappers may have been
     *                applied.
     */
    @Override
    default void accept(Map<XboxButton, Number> buttons)
    {
        if (!buttons.isEmpty())
        {
            modifyMapInPlace(buttons);
        }
    }

    /**
     * Performs the actual modification. This method should not be called directly. Use {@link #accept(Map)} instead.
     *
     * @param buttons - the poll of the controller.
     */
    void modifyMapInPlace(Map<XboxButton, Number> buttons);
}
