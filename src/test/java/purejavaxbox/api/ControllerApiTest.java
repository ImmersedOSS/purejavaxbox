package purejavaxbox.api;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavaxbox.XboxButton;
import reactor.core.Disposable;

import java.util.*;
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
    public void testObserveSimulatingNoController()
    {
        XboxButton button = XboxButton.LEFT_STICK_BUTTON;

        AtomicInteger count = new AtomicInteger(0);
        Disposable c = proxy
                .observe(button)
                .subscribe(n -> count.getAndIncrement());

        proxy.send(Collections.emptyMap());
        proxy.send(button, 1.0);

        Assert.assertEquals("Empty map ignored.", 1, count.get());

        c.dispose();
    }

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
    public void testObserveCombo()
    {
        XboxButton b1 = XboxButton.A;
        XboxButton b2 = XboxButton.B;
        XboxButton b3 = XboxButton.X;

        Map<XboxButton, Number> values = new EnumMap<>(XboxButton.class);

        Arrays
                .asList(b1, b2, b3)
                .forEach(b -> values.put(b, 0));

        AtomicReference<Boolean> actual = new AtomicReference<>(null);
        Disposable c = proxy
                .observe(b1, b2, b3)
                .subscribe(b -> actual.set(b));

        Assert.assertNull("No values sent yet.", actual.get());

        proxy.sendAll(values);
        Assert.assertFalse("No buttons pressed", actual.get());

        values.put(b1, 1);
        proxy.sendAll(values);
        Assert.assertFalse("Only 1 pressed", actual.get());

        values.put(b2, 1);
        proxy.sendAll(values);
        Assert.assertFalse("Only 2 pressed", actual.get());

        values.put(b3, 1);
        proxy.sendAll(values);
        Assert.assertTrue("All pressed", actual.get());

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
    public void testToggleCombo()
    {
        XboxButton b1 = XboxButton.RIGHT_BUMPER;
        XboxButton b2 = XboxButton.X;
        XboxButton b3 = XboxButton.Y;

        Map<XboxButton, Number> values = new EnumMap<>(XboxButton.class);

        Arrays
                .asList(b1, b2, b3)
                .forEach(b -> values.put(b, 0));

        AtomicInteger count = new AtomicInteger();
        AtomicReference<Boolean> actual = new AtomicReference<>(null);

        Disposable c = proxy
                .observeToggle(b1, b2, b3)
                .subscribe(n -> {
                    count.getAndIncrement();
                    actual.set(n);
                });

        Assert.assertNull("Nothing has happened", actual.get());

        proxy.sendAll(values);
        Assert.assertEquals("Checking calls", 1, count.get());
        Assert.assertFalse("No buttons pressed", actual.get());

        values.put(b1, 1);
        proxy.sendAll(values);
        Assert.assertEquals("Checking calls", 1, count.get());
        Assert.assertFalse("Only 1 pressed", actual.get());

        values.put(b2, 1);
        proxy.sendAll(values);
        Assert.assertEquals("Checking calls", 1, count.get());
        Assert.assertFalse("Only 2 pressed", actual.get());

        values.put(b3, 1);
        proxy.sendAll(values);
        Assert.assertEquals("Checking calls", 2, count.get());
        Assert.assertTrue("All pressed", actual.get());

        values.put(b3, 0);
        proxy.sendAll(values);
        Assert.assertEquals("Checking calls", 3, count.get());
        Assert.assertFalse("One released pressed", actual.get());

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
    public void testAfterDelayWhenSpammingButton()
    {
        XboxButton button = XboxButton.LEFT_BUMPER;

        AtomicInteger value = new AtomicInteger(0);

        Disposable c = proxy
                .observeAfterDelay(1, TimeUnit.SECONDS, button)
                .subscribe(n -> value.getAndIncrement());

        proxy.send(button, 1);
        Assert.assertEquals("First press.", 1, value.get());

        proxy.send(button, 0);
        proxy.send(button, 1);
        Assert.assertEquals("Second press.", 2, value.get());

        proxy.send(button, 0);
        proxy.send(button, 1);
        Assert.assertEquals("Third press.", 3, value.get());

        c.dispose();
    }

    @Test
    public void testAfterDelayWhenSpammingButtons()
    {
        XboxButton b1 = XboxButton.DPAD_DOWN;
        XboxButton b2 = XboxButton.DPAD_RIGHT;
        XboxButton b3 = XboxButton.RIGHT_STICK_BUTTON;
        List<XboxButton> buttons = Arrays.asList(b1, b2, b3);

        AtomicInteger value = new AtomicInteger(0);

        Disposable c = proxy
                .observeAfterDelay(1, TimeUnit.SECONDS, b1, b2, b3)
                .subscribe(n -> value.getAndIncrement());

        proxy.sendAll(buttons, 1);
        Assert.assertEquals("First press.", 1, value.get());

        proxy.sendAll(buttons, 0);
        proxy.sendAll(buttons, 1);
        Assert.assertEquals("Second press.", 2, value.get());

        proxy.sendAll(buttons, 0);
        proxy.sendAll(buttons, 1);
        Assert.assertEquals("Third press.", 3, value.get());

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

    @Test
    public void testObserveHeld()
    {
        XboxButton button = XboxButton.RIGHT_BUMPER;

        AtomicInteger count = new AtomicInteger();
        AtomicReference<Boolean> actual = new AtomicReference<>(null);

        Disposable c = proxy
                .observeHeld(50, TimeUnit.MILLISECONDS, button)
                .subscribe(b -> {
                    count.getAndIncrement();
                    actual.set(b);
                });

        testHeldGeneric(count, actual, i -> proxy.send(button, i), Boolean.TRUE, Boolean.FALSE);
        c.dispose();
    }

    @Test
    public void testObserveHeldCombo()
    {
        XboxButton b1 = XboxButton.RIGHT_BUMPER;
        XboxButton b2 = XboxButton.LEFT_BUMPER;
        XboxButton b3 = XboxButton.A;
        XboxButton b4 = XboxButton.B;

        AtomicInteger count = new AtomicInteger();
        AtomicReference<Boolean> actual = new AtomicReference<>(null);

        Disposable c = proxy
                .observeHeld(50, TimeUnit.MILLISECONDS, b1, b2, b3)
                .subscribe(b -> {
                    count.getAndIncrement();
                    actual.set(b);
                });

        testHeldGeneric(count, actual, i -> proxy.sendAll(Arrays.asList(b1, b2, b3, b4), i), Boolean.TRUE, Boolean.FALSE);
        c.dispose();
    }

    private <T> void testHeldGeneric(AtomicInteger count, AtomicReference<T> actual, Consumer<Integer> buttonToggle, T on, T off)
    {
        Assert.assertEquals("Checking calls", 0, count.get());
        Assert.assertNotEquals("No value sent.", off, actual.get());
        Assert.assertNotEquals("No value sent.", on, actual.get());

        buttonToggle.accept(0);

        Assert.assertEquals("Checking calls", 1, count.get());
        Assert.assertEquals("Value is off.", off, actual.get());

        spinTest(count, actual, buttonToggle, on);
    }

    private <T> void testAfterDelayGeneric(AtomicInteger count, AtomicReference<T> actual, Consumer<Integer> buttonToggle, T on, T off)
    {
        Assert.assertEquals("Checking calls", 0, count.get());
        Assert.assertNotEquals("No value sent.", off, actual.get());
        Assert.assertNotEquals("No value sent.", on, actual.get());

        buttonToggle.accept(1);

        Assert.assertEquals("Checking calls", 1, count.get());
        Assert.assertEquals("Value is on.", on, actual.get());

        spinTest(count, actual, buttonToggle, on);
    }

    private <T> void spinTest(AtomicInteger count, AtomicReference<T> actual, Consumer<Integer> buttonToggle, T on)
    {
        buttonToggle.accept(1);
        long extraCount = 0;

        while (count.get() == 1)
        {
            buttonToggle.accept(1);
            extraCount++;
        }

        StackTraceElement[] element = Thread
                .currentThread()
                .getStackTrace();
        LOG.info("Count during delay = {} for {}", extraCount, element[3].getMethodName());
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
