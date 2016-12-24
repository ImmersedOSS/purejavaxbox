package purejavaxbox.xinput;

/**
 * Constants used by classes in the XInput implementation.
 */
public final class XInputConstants
{
    /**
     * The API default dead zone for the left joystick. This value is not normalized.
     */
    public static final short LEFT_DZ = 7849;
    /**
     * The API default dead zone value for the right joystick. This value is not normalized.
     */
    public static final short RIGHT_DZ = 8689;
    /**
     * The API default dead zone for triggers.
     */
    public static final int TRIGGER_DZ = 30;

    private XInputConstants()
    {

    }
}
