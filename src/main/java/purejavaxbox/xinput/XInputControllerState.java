package purejavaxbox.xinput;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the "hidden" input for the controller state.
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
    public int eventCount;
    /**
     * A short whose bits represent the on/off state for the various buttons.
     */
    public short buttons;

    public byte lTrigger;
    public byte rTrigger;

    public short leftStickY;
    public short leftStickX;
    public short rightStickY;
    public short rightStickX;

    @Override
    protected List getFieldOrder()
    {
        return Arrays.asList("eventCount", "buttons", "lTrigger", "rTrigger", "leftStickY", "leftStickX", "rightStickY", "rightStickX");
    }
}
