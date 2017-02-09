package purejavaxbox.api;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavaxbox.XboxButton;
import purejavaxbox.XboxController;
import purejavaxbox.XboxControllers;
import reactor.core.Disposable;

import java.util.*;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class SinglePlayerTest
{
    private static final Logger LOG = LoggerFactory.getLogger(SinglePlayerTest.class);

    @Test(expected = IllegalStateException.class)
    public void testBuildWithoutControllers()
    {
        new SinglePlayer.Builder()
                .timing(20.0)
                .get();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithoutTiming()
    {
        new SinglePlayer.Builder()
                .controllers(mock(XboxControllers.class))
                .get();
    }

    /**
     * This test creates and listens to a single player object, and then disposes of the object. It tests creation,
     * timing, and disposal of a controller object.
     *
     * @throws TimeoutException
     * @throws InterruptedException
     */
    @Test
    public void testSinglePlayerLifecycle() throws TimeoutException, InterruptedException
    {
        Map<XboxButton, Number> buttons = new EnumMap<>(XboxButton.class);
        Arrays
                .asList(XboxButton.values())
                .forEach(b -> buttons.put(b, 0));

        XboxController mockedController = mock(XboxController.class);
        when(mockedController.buttons()).thenReturn(buttons);

        XboxControllers controllers = mock(XboxControllers.class);
        doAnswer(a -> {
            Consumer<XboxController> c = a.getArgument(0);
            c.accept(mockedController);
            return null;
        })
                .when(controllers)
                .forEach(any());

        double timing = 10.0;

        SinglePlayer.Builder builder = new SinglePlayer.Builder()
                .timing(timing)
                .controllers(controllers);
        SinglePlayer sp = builder.get();

        Assert.assertNotNull(sp);

        Phaser barrier = new Phaser(5);
        List<Long> data = new ArrayList<>();

        Phaser cancelBarrier = new Phaser(2);
        AtomicReference<Thread> cancelThread = new AtomicReference<>(null);

        Disposable d = sp
                .observe(XboxButton.A)
                .doOnCancel(() -> {
                    cancelThread.set(Thread.currentThread());
                    cancelBarrier.arrive();
                })
                .subscribe(b -> {
                    data.add(System.nanoTime());
                    barrier.arrive();
                });

        barrier.awaitAdvanceInterruptibly(barrier.arrive(), 30, TimeUnit.SECONDS);
        d.dispose();

        long aveTimeNanos = 0L;
        int diffs = data.size() - 2;
        for (int i = diffs; i >= 0; i--)
        {
            long first = data.get(i);
            long last = data.get(i + 1);
            long diff = last - first;

            aveTimeNanos += diff;
        }

        aveTimeNanos /= data.size() - 1;

        long expectedTiming = TimeUnit.SECONDS.toMillis(1);
        expectedTiming = (long) (expectedTiming / timing);

        long actualTiming = TimeUnit.NANOSECONDS.toMillis(aveTimeNanos);

        long lower = expectedTiming - 5;
        long upper = expectedTiming + 5;

        LOG.info("Average timing = {}ms", actualTiming);

        Assert.assertTrue("Timing within 10 millis", lower <= actualTiming && actualTiming <= upper);
        sp.dispose();

        cancelBarrier.awaitAdvanceInterruptibly(cancelBarrier.arrive(), 500, TimeUnit.MILLISECONDS);

        Assert.assertEquals("Check cancel thread is same as test thread.", Thread.currentThread(), cancelThread.get());

        sp.dispose();
        sp.dispose();
        sp.dispose();

        Assert.assertEquals("Barrier was not advanced.", cancelBarrier.getPhase(), 1);
    }

    @Test
    public void controllerFailover() throws TimeoutException, InterruptedException
    {
        Map<XboxButton, Number> buttons = new EnumMap<>(XboxButton.class);
        Arrays
                .asList(XboxButton.values())
                .forEach(b -> buttons.put(b, 0));

        XboxController mockedController = mock(XboxController.class);
        when(mockedController.buttons()).thenReturn(buttons);

        XboxController emptyController = mock(XboxController.class);
        when(emptyController.buttons()).thenReturn(Collections.emptyMap());

        List<XboxController> controllerList = Arrays.asList(emptyController, mockedController, emptyController, emptyController);

        XboxControllers controllers = mock(XboxControllers.class);
        doAnswer(a -> {

            Consumer<XboxController> c = a.getArgument(0);
            for (XboxController controller : controllerList)
            {
                c.accept(controller);
            }
            return null;
        })
                .when(controllers)
                .forEach(any());

        double timing = 10.0;

        SinglePlayer.Builder builder = new SinglePlayer.Builder()
                .timing(timing)
                .controllers(controllers);
        SinglePlayer sp = builder.get();

        Phaser barrier = new Phaser(2);
        sp
                .observeToggle(XboxButton.A)
                .subscribe(n -> barrier.arrive());

        barrier.awaitAdvanceInterruptibly(barrier.arrive(), 500, TimeUnit.MILLISECONDS);
        sp.dispose();
    }
}
