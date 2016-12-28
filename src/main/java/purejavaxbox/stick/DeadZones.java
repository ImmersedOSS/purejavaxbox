package purejavaxbox.stick;

import purejavaxbox.ButtonMapper;
import purejavaxbox.XboxButton;

/**
 * Builder-style class for creating dead zones for analog components. Currently supports sticks and triggers.
 */
public class DeadZones
{
    private static final double SHORT_ELEMENTS = Short.MAX_VALUE;
    private double innerDZ = 0.0;
    private double outerDZ = 1.0;
    private XboxButton verticalKey = XboxButton.LEFT_STICK_VERTICAL;
    private XboxButton horizontalKey = XboxButton.LEFT_STICK_HORIZONTAL;

    /**
     * Sets the inner deadzone. How this value is implemented depends on the dead zone strategy.
     *
     * @param deadZone - the value for the deadzone.
     * @return this
     */
    public DeadZones innerRadius(short deadZone)
    {
        innerDZ = deadZone / SHORT_ELEMENTS;
        return this;
    }

    /**
     * Sets the outer deadzone. How this value is implemented depends on the dead zone strategy.
     *
     * @param deadZone - the value for the deadzone.
     * @return this
     */
    public DeadZones outerRadius(short deadZone)
    {
        outerDZ = deadZone / SHORT_ELEMENTS;
        return this;
    }

    /**
     * Changes the target stick to the left stick.
     *
     * @return - this
     */
    public DeadZones leftStick()
    {
        this.verticalKey = XboxButton.LEFT_STICK_VERTICAL;
        this.horizontalKey = XboxButton.LEFT_STICK_HORIZONTAL;
        return this;
    }

    /**
     * Changes the target stick to the right stick.
     *
     * @return - this
     */
    public DeadZones rightStick()
    {
        this.verticalKey = XboxButton.RIGHT_STICK_VERTICAL;
        this.horizontalKey = XboxButton.RIGHT_STICK_HORIZONTAL;
        return this;
    }

    /**
     * Creates dead zone handling that snaps horizontal and vertical values to 0.0, 1.0, or -1.0 when outside of the
     * specified dead zone.
     *
     * @return the button mapper executing this strategy.
     */
    public ButtonMapper buildAxialDeadZone()
    {
        XboxButton verticalKey = this.verticalKey;
        XboxButton horizontalKey = this.horizontalKey;
        double innerDeadZone = this.innerDZ;
        double outerDeadZone = this.outerDZ;

        return buttons -> {
            double sHorizontal = buttons.get(horizontalKey)
                                        .doubleValue();
            double sVertical = buttons.get(verticalKey)
                                      .doubleValue();

            double magnitude = Math.sqrt(sHorizontal * sHorizontal + sVertical * sVertical);

            double dirHorizontal = sHorizontal / magnitude;
            double dirVertical = sVertical / magnitude;

            magnitude = clip(magnitude, innerDeadZone, outerDeadZone);

            if (!(Double.isNaN(dirHorizontal) || Double.isNaN(dirVertical)))
            {
                buttons.put(horizontalKey, clip(dirHorizontal * magnitude, innerDeadZone, outerDeadZone));
                buttons.put(verticalKey, clip(dirVertical * magnitude, innerDeadZone, outerDeadZone));
            }
        };
    }

    /**
     * This dead zone strategy clips the magnitude of the stick values between 0.0 and 1.0 based on the inner and outer
     * dead zones. Values are not rescaled, so fidelity outside of the dead zone is lost.
     *
     * @return the button mapper executing this strategy.
     */
    public ButtonMapper buildRadialDeadZone()
    {
        XboxButton verticalKey = this.verticalKey;
        XboxButton horizontalKey = this.horizontalKey;
        double innerDeadZone = this.innerDZ;
        double outerDeadZone = this.outerDZ;

        return buttons -> {
            double sHorizontal = buttons.get(horizontalKey)
                                        .doubleValue();
            double sVertical = buttons.get(verticalKey)
                                      .doubleValue();
            double magnitude = Math.sqrt(sHorizontal * sHorizontal + sVertical * sVertical);

            double dirHorizontal = sHorizontal / magnitude;
            double dirVertical = sVertical / magnitude;

            magnitude = clip(magnitude, innerDeadZone, outerDeadZone);

            if (!(Double.isNaN(dirHorizontal) || Double.isNaN(dirVertical)))
            {
                buttons.put(horizontalKey, dirHorizontal * magnitude);
                buttons.put(verticalKey, dirVertical * magnitude);
            }
        };
    }

    /**
     * Similar to {@link #buildRadialDeadZone()}, but rescales the input values between the upper and lower.
     *
     * @return the button mapper executing this strategy.
     */
    public ButtonMapper buildScaledRadialDeadZone()
    {
        XboxButton verticalKey = this.verticalKey;
        XboxButton horizontalKey = this.horizontalKey;
        double innerDeadZone = this.innerDZ;
        double outerDeadZone = this.outerDZ;

        return buttons -> {
            double sHorizontal = buttons.get(horizontalKey)
                                        .doubleValue();
            double sVertical = buttons.get(verticalKey)
                                      .doubleValue();
            double magnitude = Math.sqrt(sHorizontal * sHorizontal + sVertical * sVertical);
            if (magnitude < innerDeadZone)
            {
                buttons.put(horizontalKey, 0.0);
                buttons.put(verticalKey, 0.0);
            }
            else
            {
                double scalar = (magnitude - innerDeadZone) / (1.0 - innerDeadZone);
                buttons.put(horizontalKey, sHorizontal * scalar);
                buttons.put(verticalKey, sVertical * scalar);
            }
        };
    }

    private double clip(double value, double inner, double outer)
    {
        double sign = Math.signum(value);
        double v = Math.abs(value);

        if (Double.isNaN(v) || v < inner)
        {
            return 0.0;
        }

        if (v > outer)
        {
            return 1.0 * sign;
        }

        return value;
    }
}
