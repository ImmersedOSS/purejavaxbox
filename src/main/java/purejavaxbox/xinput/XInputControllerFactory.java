package purejavaxbox.xinput;

import purejavaxbox.ButtonMapper;
import purejavaxbox.XboxController;
import purejavaxbox.XboxControllerFactory;
import purejavaxbox.stick.DeadZones;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static purejavaxbox.xinput.XInputConstants.*;

/**
 * Factory for creating controllers backed using XInput. This factory supports {@link #createWith(ButtonMapper[]) button
 * mappers}.
 */
public class XInputControllerFactory implements XboxControllerFactory
{
    /**
     * Creates a new factory.
     *
     * @return an object capable of creating XInputControllers.
     */
    public static XInputControllerFactory create()
    {
        return new XInputControllerFactory(new ButtonMapper[0]);
    }

    public static XInputControllerFactory createWithDeadZones()
    {
        DeadZones deadZones = new DeadZones().outerRadius(Short.MAX_VALUE);
        ButtonMapper leftStick = deadZones.innerRadius(LEFT_DZ)
                                          .leftStick()
                                          .buildRadialDeadZone();

        ButtonMapper rightStick = deadZones.innerRadius(RIGHT_DZ)
                                           .rightStick()
                                           .buildRadialDeadZone();

        return createWith(varargs(leftStick, rightStick));
    }

    private static ButtonMapper[] varargs(ButtonMapper... array)
    {
        return array;
    }

    /**
     * Creates a factory with custom button mappers.
     *
     * @param mappers - the custom mappers to apply.
     * @return a new factory capable of creating XInputControllers
     */
    public static XInputControllerFactory createWith(ButtonMapper[] mappers)
    {
        if (mappers == null)
        {
            return create();
        }

        return new XInputControllerFactory(mappers);
    }

    private ButtonMapper[] mappers;

    XInputControllerFactory(ButtonMapper[] mappers)
    {
        this.mappers = mappers;
    }

    @Override
    public List<XboxController> get()
    {
        return IntStream.range(0, 4)
                        .mapToObj(i -> new XInputController(i, mappers))
                        .collect(Collectors.toList());
    }
}
