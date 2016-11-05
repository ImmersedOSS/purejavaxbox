package purejavaxbox;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Based on https://github.com/DieKatzchen/GuideButtonPoller and https://github.com/bwRavencl/ControllerBuddy
 */
public final class XInputControllerState extends Structure
{
    public int eventCount;
    public short buttons;

    public byte lTrigger;  //Left Trigger
    public byte rTrigger;  //Right Trigger

    public short lJoyY;  //Left Joystick Y
    public short lJoyx;  //Left Joystick X
    public short rJoyY;  //Right Joystick Y
    public short rJoyX;  //Right Joystick X

    @Override
    protected List getFieldOrder()
    {
        return Arrays.asList(new String[]{"eventCount", "buttons", "lTrigger", "rTrigger", "lJoyY", "lJoyx", "rJoyY", "rJoyX"});
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
}
