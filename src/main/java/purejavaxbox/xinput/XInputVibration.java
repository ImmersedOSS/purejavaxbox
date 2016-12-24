package purejavaxbox.xinput;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Vibration structure used to set the vibration levels of the controller. This structure is mutable by java, and can
 * reused.
 */
public class XInputVibration extends Structure
{
    public short wLeftMotorSpeed = 0;
    public short wRightMotorSpeed = 0;

    /**
     * Do not instantiate this class.
     */
    XInputVibration()
    {
        // Only the API uses this method.
    }

    @Override
    protected List getFieldOrder()
    {
        return Arrays.asList("wLeftMotorSpeed", "wRightMotorSpeed");
    }
}
