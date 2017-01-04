package purejavaxbox.api;

import org.junit.Assert;
import org.junit.Test;
import purejavaxbox.XboxButton;
import reactor.core.Cancellation;

import java.util.concurrent.atomic.AtomicReference;

public class ControllerApiTest
{
    private ControllerApiProxy proxy = new ControllerApiProxy();

    @Test
    public void testObserve()
    {
        XboxButton button = XboxButton.A;

        AtomicReference<Number> actual = new AtomicReference<>(null);
        Cancellation c = proxy.observe(button)
                              .subscribe(n -> actual.set(n));

        Assert.assertNull("Should be null.", actual.get());

        sendAndCheck(button, 1, actual);
        sendAndCheck(button, 1, actual);
        sendAndCheck(button, 0, actual);

        c.dispose();
    }

    private void sendAndCheck(XboxButton button, Number expected, AtomicReference<Number> actual)
    {
        proxy.send(button, expected);
        Assert.assertEquals("Checking if value sent.", expected, actual.get());
    }
}
