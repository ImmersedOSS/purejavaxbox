package purejavaxbox.xinput;

import purejavaxbox.XboxController;
import purejavaxbox.XboxControllerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XInputControllerFactory implements XboxControllerFactory
{
    @Override
    public List<XboxController> get()
    {
        return IntStream.range(0, 4)
                        .mapToObj(i -> new XInputController(i))
                        .collect(Collectors.toList());
    }
}
