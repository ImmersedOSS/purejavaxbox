package purejavaxbox.api;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavaxbox.XboxButton;
import reactor.core.Disposable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ControllerApiTest
{
    private static final Logger LOG = LoggerFactory.getLogger(ControllerApiTest.class);
    private ControllerApiProxy proxy = new ControllerApiProxy();

    @Test
    public void testObserve()
    {
        XboxButton button = XboxButton.A;

        AtomicReference<Number> actual = new AtomicReference<>(null);
        Disposable c = proxy
                .observe(button)
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
        Disposable c = proxy
                .observeToggle(button)
                .subscribe(n -> {
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

    @Test
    public void testAfterDelay()
    {
        XboxButton button = XboxButton.B;

        AtomicInteger count = new AtomicInteger();
        AtomicInteger actual = new AtomicInteger(-1);

        Disposable c = proxy
                .observeAfterDelay(50, TimeUnit.MILLISECONDS, button)
                .subscribe(n -> {
                    count.getAndIncrement();
                    actual.set(n.intValue());
                });

        Assert.assertEquals("Checking calls", 0, count.get());
        Assert.assertEquals("No value sent.", -1, actual.get());

        proxy.send(button, 1);

        Assert.assertEquals("Checking calls", 1, count.get());
        Assert.assertEquals("Value is on.", 1, actual.get());

        proxy.send(button, 1);
        long extraCount = 0;

        while (count.get() == 1)
        {
            proxy.send(button, 1);
            extraCount++;
        }

        LOG.info("Count during delay = {}", extraCount);
        Assert.assertTrue("Some values were discarded.", extraCount > 0);
        Assert.assertEquals("Checking calls", 2, count.get());
        Assert.assertEquals("Value is on.", 1, actual.get());

        c.dispose();
    }

    private void sendAndCheck(XboxButton button, Number expected, AtomicReference<Number> actual)
    {
        proxy.send(button, expected);
        Assert.assertEquals("Checking if value sent.", expected, actual.get());
    }
}
