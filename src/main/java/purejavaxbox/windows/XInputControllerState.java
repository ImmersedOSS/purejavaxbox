package purejavaxbox.windows;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Based on https://github.com/DieKatzchen/GuideButtonPoller and https://github.com/bwRavencl/ControllerBuddy
 */
public class XInputControllerState extends Structure
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
        return Integer.toHexString(buttons);
    }
}
