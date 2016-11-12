package purejavaxbox;

/**
 * Internal class used for calculating common values.
 */
final class ControllerMath
{
    /**
     * Normalizes a stick value between 0 and 1.
     *
     * @param value - the button value, as an unsigned short.
     * @param dz    - offset for actual zero value.
     * @return a normalized value, between 0 and 1.
     */
    static double normalizeTrigger(byte value, int dz)
    {
        double valueDz = Byte.toUnsignedInt(value) - dz;
        double sizeDz = Byte.MAX_VALUE - Byte.MIN_VALUE - dz;

        return Math.max(valueDz / sizeDz, 0.0);
    }

    /**
     * Normalizes a stick value between 0 and 1.
     *
     * @param value - the button value, as an signed short. Negative means left or down. Positive means right or up.
     * @param dz    - the deadzone radial value.
     * @return a normalized value, between 0 and 1.
     */
    static double normalizeStick(short value, int dz)
    {
        double sign = Math.signum(value);
        double max = sign >= 0.0 ? Short.MAX_VALUE : Short.MIN_VALUE;
        double offset = sign * dz;

        double valueDz = value - offset;
        double sizeDz = max - offset;

        return sign * Math.max(valueDz / sizeDz, 0.0);
    }

    /**
     * @param stickX
     * @param stickY
     * @param deadZone
     * @return
     */
    static double magnitude(double stickX, double stickY, int deadZone)
    {
        //determine how far the controller is pushed
        double magnitude = Math.sqrt(stickX * stickX + stickY * stickY);

        //determine the direction the controller is pushed
        double normalizedLX = stickX / magnitude;
        double normalizedLY = stickY / magnitude;

        double normalizedMagnitude = 0;

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

    static double normalizeMagnitude(double magnitude, int deadZone)
    {
        //optionally normalizeTrigger the magnitude with respect to its expected range
        //giving a magnitude value of 0.0 to 1.0
        return magnitude / (Short.MAX_VALUE - deadZone);
    }

    static short scaleToUShort(double normalizedValue)
    {
        return (short) (normalizedValue * (Short.MAX_VALUE - Short.MIN_VALUE));
    }

    private ControllerMath() {}
}
