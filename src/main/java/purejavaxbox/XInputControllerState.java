package purejavaxbox;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Based on https://github.com/DieKatzchen/GuideButtonPoller and https://github.com/bwRavencl/ControllerBuddy
 */
public final class XInputControllerState extends Structure
{
    private static final int LEFT_THUMB_DEADZONE = 7849;
    private static final int RIGHT_THUMB_DEADZONE = 8689;
    private static final int TRIGGER_THRESHOLD = 30;

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

    @Override
    public String toString()
    {
        int buttons = this.buttons;
        buttons = (buttons << 16) >>> 16;

        String out = Integer.toBinaryString(buttons);
        int leadingCount = 16 - out.length();

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < leadingCount; i++)
        {
            b.append("0");
        }
        b.append(out);
        return b.toString();
    }

    public double leftStickXNormalized()
    {
        return normalize(leftStickX, LEFT_THUMB_DEADZONE);
    }

    public double leftStickYNormalized()
    {
        return normalize(leftStickY, LEFT_THUMB_DEADZONE);
    }

    public double rightStickXNormalized()
    {
        return normalize(rightStickX, RIGHT_THUMB_DEADZONE);
    }

    public double rightStickYNormalized()
    {
        return normalize(rightStickY, RIGHT_THUMB_DEADZONE);
    }

    public double leftTriggerNormalized()
    {
        return normalize(lTrigger, TRIGGER_THRESHOLD);
    }

    public double rightTriggerNormalized()
    {
        return normalize(rTrigger, TRIGGER_THRESHOLD);
    }

    private double normalize(short value, int dz)
    {
        double sign = Math.signum(value);
        double max = sign >= 0.0 ? Short.MAX_VALUE : Short.MIN_VALUE;
        double offset = sign * dz;

        double valueDz = value - offset;
        double sizeDz = max - offset;

        return sign * Math.max(valueDz / sizeDz, 0.0);
    }

    private double normalize(byte value, int dz)
    {
        double valueDz = Byte.toUnsignedInt(value) - dz;
        double sizeDz = Byte.MAX_VALUE - Byte.MIN_VALUE - dz;

        return Math.max(valueDz / sizeDz, 0.0);
    }
}
