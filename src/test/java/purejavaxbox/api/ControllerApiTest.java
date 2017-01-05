package purejavaxbox.api;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import purejavaxbox.XboxButton;
import reactor.core.Cancellation;

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

    @Test
    public void testToggle()
    {
        XboxButton button = XboxButton.A;

        AtomicInteger count = new AtomicInteger();
        AtomicBoolean actual = new AtomicBoolean(true);
        Cancellation c = proxy.observeToggle(button)
                              .subscribe(n ->
                              {
                                  count.getAndIncrement();
                                  actual.set(n);
                              });

        Assert.assertEquals("Checking calls", 0, count.get());
        Assert.assertTrue("Ensure true value", actual.get());

        proxy.send(button, 0);
        Assert.assertEquals("Checking calls", 1, count.get());
        Assert.assertFalse("Button is not pressed", actual.get());

        proxy.send(button, 1);
        Assert.assertEquals("Checking calls", 2, count.get());
        Assert.assertTrue("Button is pressed", actual.get());

        proxy.send(button, 1);
        Assert.assertEquals("Checking calls", 2, count.get());
        Assert.assertTrue("Button is pressed", actual.get());

        c.dispose();
    }

    private void sendAndCheck(XboxButton button, Number expected, AtomicReference<Number> actual)
    {
        proxy.send(button, expected);
        Assert.assertEquals("Checking if value sent.", expected, actual.get());
    }
}
