package purejavaxbox.windows;

import purejavaxbox.XboxButton;
import purejavaxbox.XboxController;

import java.util.Map;

/**
 * The implementation of XboxController for the Windows operating system. Supports Windows 7+.
 */
final class WindowsController implements XboxController
{
    @Override
    public Map<XboxButton, Double> poll()
    {
        return null;
    }
}
