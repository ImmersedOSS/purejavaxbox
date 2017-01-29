package purejavaxbox.api;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavaxbox.XboxButton;
import reactor.core.Disposable;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
        AtomicReference<Integer> actual = new AtomicReference<>(-1);

        Disposable c = proxy
                .observeAfterDelay(50, TimeUnit.MILLISECONDS, button)
                .subscribe(n -> {
                    count.getAndIncrement();
                    actual.set(n.intValue());
                });

        testAfterDelayGeneric(count, actual, i -> proxy.send(button, i), 1, 0);
        c.dispose();
    }

    @Test
    public void testAfterDelayCombo()
    {
        XboxButton b1 = XboxButton.A;
        XboxButton b2 = XboxButton.B;
        XboxButton b3 = XboxButton.Y;
        XboxButton b4 = XboxButton.X;

        AtomicInteger count = new AtomicInteger();
        AtomicReference<Boolean> actual = new AtomicReference<>(null);

        Disposable c = proxy
                .observeAfterDelay(50, TimeUnit.MILLISECONDS, b1, b2, b3, b4)
                .subscribe(b -> {
                    count.getAndIncrement();
                    actual.set(b);
                });

        Consumer<Integer> buttonToggle = i -> proxy.sendAll(Arrays.asList(b1, b2, b3, b4), i);

        testAfterDelayGeneric(count, actual, buttonToggle, true, false);
        c.dispose();
    }

    private <T> void testAfterDelayGeneric(AtomicInteger count, AtomicReference<T> actual, Consumer<Integer> buttonToggle, T on, T off)
    {
        Assert.assertEquals("Checking calls", 0, count.get());
        Assert.assertNotEquals("No value sent.", off, actual.get());
        Assert.assertNotEquals("No value sent.", on, actual.get());

        buttonToggle.accept(1);

        Assert.assertEquals("Checking calls", 1, count.get());
        Assert.assertEquals("Value is on.", on, actual.get());

        buttonToggle.accept(1);
        long extraCount = 0;

        while (count.get() == 1)
        {
            buttonToggle.accept(1);
            extraCount++;
        }

        LOG.info("Count during delay = {}", extraCount);
        Assert.assertTrue("Some values were discarded.", extraCount > 0);
        Assert.assertEquals("Checking calls", 2, count.get());
        Assert.assertEquals("Value is on.", on, actual.get());
    }

    private void sendAndCheck(XboxButton button, Number expected, AtomicReference<Number> actual)
    {
        proxy.send(button, expected);
        Assert.assertEquals("Checking if value sent.", expected, actual.get());
    }
}
