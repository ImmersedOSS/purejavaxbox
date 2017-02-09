package purejavaxbox.api;

import purejavaxbox.XboxButton;

import java.util.Arrays;
import java.util.List;

/**
 * Builder-style class that can create {@link ButtonMapper mappers} for supporting dead zones for analog components.
 * Currently supports sticks.
 */
public class StickDeadZones
{
    private static final double SHORT_ELEMENTS = Short.MAX_VALUE;
    private double innerDZ = 0.0;
    private double outerDZ = 1.0;
    private XboxButton vk = XboxButton.LEFT_STICK_VERTICAL;
    private XboxButton hk = XboxButton.LEFT_STICK_HORIZONTAL;

    /**
     * Sets the inner deadzone. How this value is implemented depends on the dead zone strategy.
     *
     * @param deadZone - the value for the deadzone.
     * @return this
     */
    public StickDeadZones innerDeadZone(short deadZone)
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
    public StickDeadZones outerDeadZone(short deadZone)
    {
        outerDZ = deadZone / SHORT_ELEMENTS;
        return this;
    }

    /**
     * Changes the target analog to the left analog.
     *
     * @return - this
     */
    public StickDeadZones leftStick()
    {
        this.vk = XboxButton.LEFT_STICK_VERTICAL;
        this.hk = XboxButton.LEFT_STICK_HORIZONTAL;
        return this;
    }

    /**
     * Changes the target analog to the right analog.
     *
     * @return - this
     */
    public StickDeadZones rightStick()
    {
        this.vk = XboxButton.RIGHT_STICK_VERTICAL;
        this.hk = XboxButton.RIGHT_STICK_HORIZONTAL;
        return this;
    }

    public List<ButtonMapper> usingScaledRadialDeadZone()
    {
        ButtonMapper left = leftStick().buildScaledRadialDeadZone();
        ButtonMapper right = rightStick().buildScaledRadialDeadZone();
        return Arrays.asList(left, right);
    }

    /**
     * Creates dead zone handling that snaps horizontal and vertical values to 0.0, 1.0, or -1.0 when outside of the
     * specified dead zone. Ensures that the magnitude never exceeds 1.0.
     *
     * @return the button mapper executing this strategy.
     */
    public ButtonMapper buildAxialDeadZone()
    {
        XboxButton verticalKey = this.vk;
        XboxButton horizontalKey = this.hk;
        double innerDeadZone = this.innerDZ;
        double outerDeadZone = this.outerDZ;

        return buttons ->
        {
            double sHorizontal = buttons
                    .get(horizontalKey)
                    .doubleValue();
            double sVertical = buttons
                    .get(verticalKey)
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
     * This dead zone strategy clips the magnitude of the analog values between 0.0 and 1.0 based on the inner and outer
     * dead zones. Values are not rescaled, so fidelity outside of the dead zone is lost.
     *
     * @return the button mapper executing this strategy.
     */
    public ButtonMapper buildRadialDeadZone()
    {
        XboxButton verticalKey = this.vk;
        XboxButton horizontalKey = this.hk;
        double innerDeadZone = this.innerDZ;
        double outerDeadZone = this.outerDZ;

        return buttons ->
        {
            double sHorizontal = buttons
                    .get(horizontalKey)
                    .doubleValue();
            double sVertical = buttons
                    .get(verticalKey)
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
        XboxButton verticalKey = this.vk;
        XboxButton horizontalKey = this.hk;
        double innerDeadZone = this.innerDZ;
        double outerDeadZone = this.outerDZ;

        return buttons ->
        {
            double sHorizontal = buttons
                    .get(horizontalKey)
                    .doubleValue();
            double sVertical = buttons
                    .get(verticalKey)
                    .doubleValue();
            double magnitude = Math.sqrt(sHorizontal * sHorizontal + sVertical * sVertical);

            double dirHorizontal = sHorizontal / magnitude;
            double dirVertical = sVertical / magnitude;

            magnitude = clip(magnitude, innerDeadZone, outerDeadZone);

            if (!(Double.isNaN(dirHorizontal) || Double.isNaN(dirVertical)) && magnitude > innerDeadZone)
            {
                double legalRange = outerDeadZone - innerDeadZone;
                double normalizedMag = Math.min(1.0f, (magnitude - innerDeadZone) / legalRange);
                double scalar = normalizedMag / magnitude;

                buttons.put(horizontalKey, dirHorizontal * scalar);
                buttons.put(verticalKey, dirVertical * scalar);
            }
            else
            {
                buttons.put(horizontalKey, 0.0);
                buttons.put(verticalKey, 0.0);
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
