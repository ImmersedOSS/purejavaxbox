package purejavaxbox;

import java.util.Map;

public interface XboxController
{
    /**
     * Polls the state of the controller.
     *
     * @return a map containing the state of the values for the controller.
     */
    Map<XboxButton, Double> poll();
}
