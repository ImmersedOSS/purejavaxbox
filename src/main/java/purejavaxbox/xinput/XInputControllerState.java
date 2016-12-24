package purejavaxbox.xinput;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the "hidden" input for the controller state. This is a read-only structure from Java's perspective.
 *
 * @see <a href="https://github.com/DieKatzchen/GuideButtonPoller">GuideButtonPoller on GitHub.</a>
 * @see <a href="https://github.com/bwRavencl/ControllerBuddy">ControllerBuddy on GitHub</a>
 */
public final class XInputControllerState extends Structure
{
    public static final int LEFT_STICK_DEADZONE = 7849;
    public static final int RIGHT_STICK_DEADZONE = 8689;
    public static final int TRIGGER_THRESHOLD = 30;

    /**
     * Increments as buttons are pressed.
     */
    public final int eventCount = 0;
    /**
     * A short whose bits represent the on/off state for the various buttons.
     */
    public final short buttons = 0;

    public final byte lTrigger = 0;
    public final byte rTrigger = 0;

    public final short leftStickY = 0;
    public final short leftStickX = 0;
    public final short rightStickY = 0;
    public final short rightStickX = 0;

    /**
     * Only internal API code should be instantiating this.
     */
    XInputControllerState()
    {
        // Only the API should be creating these.
    }

    @Override
    protected List getFieldOrder()
    {
        return Arrays.asList("eventCount", "buttons", "lTrigger", "rTrigger", "leftStickY", "leftStickX", "rightStickY", "rightStickX");
    }
}
