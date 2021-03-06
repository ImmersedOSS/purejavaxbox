package purejavaxbox.raw.xinput;

import purejavaxbox.raw.XboxController;
import purejavaxbox.raw.XboxControllerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Factory for creating controllers backed using XInput.
 */
public class XInputControllerFactory implements XboxControllerFactory
{
    @Override
    public List<XboxController> get()
    {
        return IntStream
                .range(0, 4)
                .mapToObj(i -> new XInputController(i))
                .collect(Collectors.toList());
    }
}
