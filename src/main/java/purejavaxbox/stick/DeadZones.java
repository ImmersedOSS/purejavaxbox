package purejavaxbox.stick;

import purejavaxbox.ButtonMapper;
import purejavaxbox.XboxButton;

public class DeadZones
{
    private static final double SHORT_ELEMENTS = Math.pow(2.0, Short.SIZE);
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
            if (sHorizontal <= innerDeadZone || outerDeadZone <= sHorizontal)
            {
                buttons.put(horizontalKey, 0.0);
            }
            if (sVertical <= innerDeadZone || outerDeadZone <= sVertical)
            {
                buttons.put(horizontalKey, 0.0);
            }
        };
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
            boolean withinDeadZone = !(innerDeadZone <= magnitude && magnitude <= outerDeadZone);

            if (withinDeadZone)
            {
                buttons.put(horizontalKey, 0.0);
                buttons.put(verticalKey, 0.0);
            }
        };
    }

    public ButtonMapper scaledRadialDeadZone()
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
                double scalar = (magnitude - innerDeadZone) / (1 - innerDeadZone);
                buttons.put(horizontalKey, sHorizontal * scalar);
                buttons.put(verticalKey, sVertical * scalar);
            }
        };
    }
}
