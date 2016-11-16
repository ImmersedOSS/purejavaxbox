package purejavaxbox.xinput;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

import static purejavaxbox.ControllerMath.*;

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

    public short getButton()
    {
        return buttons;
    }

    public double leftStickXNormalized()
    {
        return normalizeStick(leftStickX, LEFT_STICK_DEADZONE);
    }

    public double leftStickYNormalized()
    {
        return normalizeStick(leftStickY, LEFT_STICK_DEADZONE);
    }

    public double rightStickXNormalized()
    {
        return normalizeStick(rightStickX, RIGHT_STICK_DEADZONE);
    }

    public double rightStickYNormalized()
    {
        return normalizeStick(rightStickY, RIGHT_STICK_DEADZONE);
    }

    public double leftTriggerNormalized()
    {
        return normalizeTrigger(lTrigger, TRIGGER_THRESHOLD);
    }

    public double rightTriggerNormalized()
    {
        return normalizeTrigger(rTrigger, TRIGGER_THRESHOLD);
    }

    public double leftStickMagnitude() { return magnitude(leftStickX, leftStickY, LEFT_STICK_DEADZONE);}

    public double rightStickMagnitude() { return magnitude(rightStickX, rightStickY, RIGHT_STICK_DEADZONE);}
}
