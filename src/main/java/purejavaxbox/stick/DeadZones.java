package purejavaxbox.stick;

import purejavaxbox.ButtonMapper;
import purejavaxbox.XboxButton;

public class DeadZones
{
    private static final double SHORT_ELEMENTS = Short.MAX_VALUE;
    private double innerDZ = 0.0;
    private double outerDZ = 1.0;
    private XboxButton verticalKey;
    private XboxButton horizontalKey;

    public DeadZones innerRadius(short radius)
    {
        innerDZ = radius / SHORT_ELEMENTS;
        return this;
    }

    public DeadZones outerRadius(short radius)
    {
        outerDZ = radius / SHORT_ELEMENTS;
        return this;
    }

    public DeadZones leftStick()
    {
        this.verticalKey = XboxButton.LEFT_STICK_VERTICAL;
        this.horizontalKey = XboxButton.LEFT_STICK_HORIZONTAL;
        return this;
    }

    public DeadZones rightStick()
    {
        this.verticalKey = XboxButton.LEFT_STICK_VERTICAL;
        this.horizontalKey = XboxButton.LEFT_STICK_HORIZONTAL;
        return this;
    }

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

            buttons.put(horizontalKey, clip(sHorizontal, innerDeadZone, outerDeadZone));
            buttons.put(verticalKey, clip(sVertical, innerDeadZone, outerDeadZone));
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
}
