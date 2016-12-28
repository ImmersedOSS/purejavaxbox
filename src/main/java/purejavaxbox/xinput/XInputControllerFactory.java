package purejavaxbox.xinput;

import purejavaxbox.ButtonMapper;
import purejavaxbox.XboxController;
import purejavaxbox.XboxControllerFactory;
import purejavaxbox.analog.StickDeadZones;
import purejavaxbox.analog.TriggerDeadZones;

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
        StickDeadZones deadZones = new StickDeadZones().outerDeadZone(Short.MAX_VALUE);
        ButtonMapper leftStick = deadZones.innerDeadZone(LEFT_DZ)
                                          .leftStick()
                                          .buildScaledRadialDeadZone();

        ButtonMapper rightStick = deadZones.innerDeadZone(RIGHT_DZ)
                                           .rightStick()
                                           .buildScaledRadialDeadZone();

        TriggerDeadZones triggerDZ = new TriggerDeadZones();
        ButtonMapper leftTrigger = triggerDZ.innerDeadZone(TRIGGER_DZ)
                                            .leftTrigger()
                                            .buildLinearScalar();
        ButtonMapper rightTrigger = triggerDZ.innerDeadZone(TRIGGER_DZ)
                                             .rightTrigger()
                                             .buildLinearScalar();

        return createWith(varargs(leftStick, rightStick, leftTrigger, rightTrigger));
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
