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

/**
 * An implementation of {@link ControllerApi} which supports exactly 1 controller. Objects of this type share a single
 * daemon thread called controller-polling-thread, which will be used to poll controllers. <p> This object will poll
 * each controller, starting with ID 0 through ID 3. If a map with values is produced, then that map is saved and
 * distributed through {@link #get()}. If an empty is produced, then the next controller with the next highest ID is
 * checked. This process repeats until all controllers have been checked. In the event of multiple controllers, lower
 * IDs will always take precedence. </p>
 */
final class SinglePlayer implements ControllerApi
{
    private static final Logger LOG = LoggerFactory.getLogger(SinglePlayer.class);

    private static final ScheduledExecutorService SERVICE = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("controller-polling-thread");
        thread.setDaemon(true);
        return thread;
    });

    private EmitterProcessor<Map<XboxButton, Number>> flux = EmitterProcessor.create(false);
    private BlockingSink<Map<XboxButton, Number>> sink = flux.connectSink();

    private ScheduledFuture<?> task;

    SinglePlayer(long nanos, XboxControllers controllers)
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
     * {@inheritDoc}
     *
     * @throws IllegalStateException if an error occurred while waiting for the task to complete. In theory, this should
     *                               never happen.
     */
    public synchronized void dispose()
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
            catch (InterruptedException e)
            {
                LOG.error("Interrupted while waiting to dispose. Listeners will be removed. Recalling this method should terminate the.", e);
                Thread
                        .currentThread()
                        .interrupt();
                flux.cancelOn(Schedulers.immediate());
            }
            catch (ExecutionException e)
            {
                LOG.error("Waiting for cancel threw an unexpected error. Listeners will still be canceled.", e);
                flux.cancelOn(Schedulers.immediate());
            }
            catch (TimeoutException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }
}
