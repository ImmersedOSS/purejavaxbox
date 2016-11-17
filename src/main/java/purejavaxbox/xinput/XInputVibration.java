package purejavaxbox.xinput;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class XInputVibration extends Structure
{
    public short wLeftMotorSpeed = 0;
    public short wRightMotorSpeed = 0;

    @Override
    protected List getFieldOrder()
    {
        return Arrays.asList("wLeftMotorSpeed", "wRightMotorSpeed");
    }
}
