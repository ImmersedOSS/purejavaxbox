package purejavaxbox.analog;

import purejavaxbox.ButtonMapper;
import purejavaxbox.XboxButton;

/**
 * This class provides builders for creating trigger dead zones.
 */
public class TriggerDeadZones
{
    public static final double UBYTE_MAX_VALUE = Math.pow(2.0, Byte.SIZE);

    private XboxButton key = XboxButton.LEFT_TRIGGER;
    private double innerDZ = 0.0;
    private double outerDZ = 1.0;

    /**
     * Sets the target trigger to be the left trigger.
     *
     * @return this.
     */
    public TriggerDeadZones leftTrigger()
    {
        key = XboxButton.LEFT_TRIGGER;
        return this;
    }

    /**
     * Sets the target trigger to be the right trigger.
     *
     * @return this.
     */
    public TriggerDeadZones rightTrigger()
    {
        key = XboxButton.RIGHT_TRIGGER;
        return this;
    }

    /**
     * Sets the inner deadzone. How this value is implemented depends on the dead zone strategy.
     *
     * @param deadZone - the value for the deadzone. Values are clipped between 0 and {@link #UBYTE_MAX_VALUE}.
     * @return this
     */
    public TriggerDeadZones innerDeadZone(int deadZone)
    {
        double scaledDeadZone = Math.max(0.0, Math.min(deadZone, UBYTE_MAX_VALUE));

        innerDZ = scaledDeadZone / UBYTE_MAX_VALUE;
        return this;
    }

    /**
     * Sets the outer deadzone. How this value is implemented depends on the dead zone strategy.
     *
     * @param deadZone - the value for the dead zone. Should not be > {@link #UBYTE_MAX_VALUE}
     * @return this
     */
    public TriggerDeadZones outerDeadZone(int deadZone)
    {
        outerDZ = deadZone / UBYTE_MAX_VALUE;
        return this;
    }

    /**
     * Scales the trigger value linearly between 0.0 and 1.0, based on the previously defined dead zones.
     *
     * @return the mapper supporting the above dead zone strategy.
     */
    public ButtonMapper buildLinearScalar()
    {
        XboxButton triggerKey = this.key;

        double innerDeadZone = innerDZ;
        double outerDeadZone = outerDZ;

        return buttons -> {
            double value = buttons.get(triggerKey)
                                  .doubleValue();

            if (value > innerDeadZone)
            {
                double legalRange = outerDeadZone - innerDeadZone;
                double normalizedValue = Math.min(1.0f, (value - innerDeadZone) / legalRange);

                buttons.put(triggerKey, normalizedValue);
            }
            else
            {
                buttons.put(triggerKey, 0.0);
            }
        };
    }
}
