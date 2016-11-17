package purejavaxbox;

/**
 * Internal class used for calculating common values.
 */
public final class ControllerMath
{
    private ControllerMath() {}

    /**
     * Normalizes a stick value between 0 and 1.
     *
     * @param value - the button value, as an unsigned short.
     * @param dz    - offset for actual zero value.
     * @return a normalized value, between 0 and 1.
     */
    public static double normalizeTrigger(byte value, int dz)
    {
        double valueDz = (double) Byte.toUnsignedInt(value) - dz;
        double sizeDz = (double) Byte.MAX_VALUE - Byte.MIN_VALUE - dz;

        return Math.max(valueDz / sizeDz, 0.0);
    }

    /**
     * Normalizes a stick value between 0 and 1.
     *
     * @param value - the button value, as an signed short. Negative means left or down. Positive means right or up.
     * @param dz    - the deadzone radial value.
     * @return a normalized value, between 0 and 1.
     */
    public static double normalizeStick(short value, int dz)
    {
        double sign = Math.signum(value);
        double max = sign >= 0.0 ? Short.MAX_VALUE : Short.MIN_VALUE;
        double offset = sign * dz;

        double valueDz = value - offset;
        double sizeDz = max - offset;

        return sign * Math.max(valueDz / sizeDz, 0.0);
    }

    /**
     * Gets the magnitude of the provided
     *
     * @param stickX   - the value for StickX.
     * @param stickY   - the value for StickY.
     * @param deadZone - the value for the dead zone.
     * @return
     */
    public static double magnitude(double stickX, double stickY, int deadZone)
    {
        //determine how far the controller is pushed
        double magnitude = Math.sqrt(stickX * stickX + stickY * stickY);

        //check if the controller is outside a circular dead zone
        if (magnitude > deadZone)
        {
            //clip the magnitude at its expected maximum value
            magnitude = magnitude > Short.MAX_VALUE ? Short.MAX_VALUE : magnitude;

            //adjust magnitude relative to the end of the dead zone
            return magnitude - deadZone;
        }

        return 0.0;
    }

    /**
     * Provides a normalized view of the stick.
     *
     * @param stickValue - the raw value of the stick.
     * @param magnitude  - the {@link #magnitude(double, double, int) magnitude}, as calculated by this class.
     * @return a value between -1.0 and 1.0.
     */
    public static double normalizedStick(double stickValue, double magnitude)
    {
        return stickValue / magnitude;
    }

    public static double normalizeMagnitude(double magnitude, int deadZone)
    {
        return magnitude / (Short.MAX_VALUE - deadZone);
    }

    public static short scaleToUShort(double normalizedValue)
    {
        return (short) (normalizedValue * (Short.MAX_VALUE - Short.MIN_VALUE));
    }
}
