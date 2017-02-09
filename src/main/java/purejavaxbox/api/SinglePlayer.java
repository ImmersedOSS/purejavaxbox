package purejavaxbox.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavaxbox.XboxButton;
import purejavaxbox.XboxControllers;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.*;

/**
 * An implementation of {@link ControllerApi} which supports exactly 1 controller. Objects of this type share a single
 * daemon thread called controller-polling-thread, which will be used to poll controllers. <p> This object will poll
 * each controller, starting with ID 0 through ID 3. If a map with values is produced, then that map is saved and
 * distributed through {@link #get()}. If an empty is produced, then the next controller with the next highest ID is
 * checked. This process repeats until all controllers have been checked. In the event of multiple controllers, lower
 * IDs will always take precedence. </p> To create a {@link SinglePlayer} HelperMethods, use the {@link Builder}.
 */
public class SinglePlayer implements ControllerApi
{
    private static final Logger LOG = LoggerFactory.getLogger(SinglePlayer.class);

    private static final ScheduledExecutorService SERVICE = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("controller-polling-thread");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * This object is used to create {@link SinglePlayer} objects.
     */
    public static final class Builder implements Supplier<SinglePlayer>
    {
        private XboxControllers controllers;
        private long nanos = -1L;

        /**
         * Provide a custom list of controllers. By default, this builder is configured {@link
         * XboxControllers#useDefaults()}.
         *
         * @param controllers - the controller list.
         * @return this.
         */
        public Builder controllers(XboxControllers controllers)
        {
            this.controllers = controllers;
            return this;
        }

        @Override
        public SinglePlayer get()
        {
            checkState(this.controllers != null, "Controllers must be specified.");
            checkState(nanos >= 0L, "FPS must be specified.");
            return new SinglePlayer(this.nanos, controllers);
        }

        /**
         * Provide a custom poll rate. By default, this rate is 20
         *
         * @param fps - a measurement in FPS. This is translated to nanoseconds per event.
         * @return this.
         */
        public Builder timing(double fps)
        {
            this.nanos = (long) (1.0 / fps * TimeUnit.SECONDS.toNanos(1L));
            return this;
        }
    }

    private EmitterProcessor<Map<XboxButton, Number>> flux = EmitterProcessor.create(false);
    private BlockingSink<Map<XboxButton, Number>> sink = flux.connectSink();

    private ScheduledFuture<?> task;

    private SinglePlayer(long nanos, XboxControllers controllers)
    {
        task = SERVICE.scheduleAtFixedRate(() -> {
            AtomicReference<Map<XboxButton, Number>> buttonReference = new AtomicReference<>(Collections.emptyMap());
            controllers.forEach(controller -> {
                if (buttonReference
                        .get()
                        .isEmpty())
                {
                    buttonReference.set(controller.buttons());
                }
            });

            Map<XboxButton, Number> buttonValues = buttonReference.getAndSet(Collections.emptyMap());
            sink.emit(buttonValues);
        }, 0, nanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public Flux<Map<XboxButton, Number>> get()
    {
        return flux;
    }

    /**
     * Terminates all listeners to this controller and removes it from the polling thread. The cancellation signal is
     * delivered on the calling thread.
     * <p>
     * This method blocks while it waits for the polling task to cancel, up to 1 second.
     * <p>
     * Once called, this method does nothing.
     *
     * @throws InterruptedException - if this method is interrupted while waiting for task to cancel.
     * @throws TimeoutException     - if the task was not able to cancel within a reasonable amount of time.
     */
    public synchronized void dispose() throws InterruptedException, TimeoutException
    {
        if (task.cancel(false))
        {
            try
            {
                SERVICE
                        .submit(() -> LOG.info("Disposing single player controller."))
                        .get(1, TimeUnit.SECONDS);
                LOG.info("Task was successfully cancelled.");
                flux.cancelOn(Schedulers.immediate());
            }
            catch (ExecutionException e)
            {
                LOG.error("Waiting for cancel threw an unexpected error. Listeners will still be canceled.", e);
                flux.cancelOn(Schedulers.immediate());
            }
        }
    }
}
