package purejavaxbox.api;

import purejavaxbox.XboxButton;
import purejavaxbox.XboxControllers;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * An implementation of {@link ControllerApi} which supports exactly 1 controller. Objects of this type share a single
 * daemon thread called controller-polling-thread, which will be used to poll controllers. <p> This object will poll
 * each controller, starting with ID 0 through ID 3. If a map with values is produced, then that map is saved and
 * distributed through {@link #get()}. If an empty is produced, then the next controller with the next highest ID is
 * checked. This process repeats until all controllers have been checked. In the event of multiple controllers, lower
 * IDs will always take precedence. </p> To create a {@link SinglePlayer} API, use the {@link Builder}.
 */
public class SinglePlayer implements ControllerApi
{
    private static final ScheduledExecutorService SERVICE = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("controller-polling-thread");
        thread.setDaemon(true);
        return thread;
    });
    private EmitterProcessor<Map<XboxButton, Number>> flux = EmitterProcessor.create(false);
    private BlockingSink<Map<XboxButton, Number>> sink = flux.connectSink();

    private SinglePlayer(long nanos, XboxControllers controllers)
    {
        SERVICE.scheduleAtFixedRate(() -> {
            AtomicReference<Map<XboxButton, Number>> buttonReference = new AtomicReference<>(Collections.emptyMap());
            controllers.forEach(controller -> {
                if (buttonReference.get()
                                   .isEmpty())
                {
                    buttonReference.set(controller.buttons());
                }
            });

            Map<XboxButton, Number> buttonValues = buttonReference.getAndSet(Collections.emptyMap());
            sink.accept(buttonValues);
        }, 0, nanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public Flux<Map<XboxButton, Number>> get()
    {
        return flux;
    }

    /**
     * This object is used to create {@link SinglePlayer} objects. Defaults to 20 FPS.
     */
    public static final class Builder implements Supplier<SinglePlayer>
    {
        private static final double FPS = 20.0;

        private XboxControllers controllers;
        private long nanos;

        /**
         * Set the defaults.
         */
        public Builder()
        {
            controllers(XboxControllers.useDefaults()).timing(FPS);
        }

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
}
