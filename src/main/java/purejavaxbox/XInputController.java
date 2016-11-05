package purejavaxbox;

import java.util.Collections;
import java.util.Map;

/**
 * The implementation of XboxController for the Windows operating system. Supports Windows 7+.
 */
final class XInputController implements XboxController, Pollable
{
    private XInputConnector connector;
    private Map<XboxButton, Double> lastPoll = Collections.emptyMap();

    XInputController(int xinputId)
    {
        connector = new XInputConnector(xinputId);
    }

    @Override
    public Map<XboxButton, Double> buttons()
    {
        return lastPoll;
    }

    @Override
    public void rumble(double value)
    {

    }

    @Override
    public void poll()
    {
        connector.poll();
    }
}
