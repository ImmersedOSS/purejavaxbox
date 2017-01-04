package purejavaxbox.api;

import purejavaxbox.XboxButton;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public interface ControllerApi extends Supplier<Flux<Map<XboxButton, Number>>>
{
    /**
     * This functions allows clients to connect to the values from a controller. Cancelling is supported, and will not
     * affect other clients.
     *
     * @return the stream of controller values that users can {@link Flux#subscribe() subscribe} to in order to collect
     * data from the controller.
     */
    @Override
    Flux<Map<XboxButton, Number>> get();

    /**
     * Watch a particular button. This method will update as quickly as polling.
     *
     * @param button - the button to observe.
     * @return a flux holding the value.
     */
    default Flux<Number> observe(XboxButton button)
    {
        return get().filter(m -> !m.isEmpty())
                    .map(m -> m.get(button));
    }

    /**
     * Observe the pressing and releasing of a button.
     *
     * @param button - the button we are watching.
     * @return a flux which produces a true/false value if the button is toggled or not.
     */
    default Flux<Boolean> observeToggle(XboxButton button)
    {
        return observe(button).distinctUntilChanged()
                              .map(n -> n.intValue() == 1);
    }

    default Flux<Number> observeHeld(XboxButton button, long time, TimeUnit unit)
    {
        long valueInNanos = unit.toNanos(time);
        AtomicLong timer = new AtomicLong(System.nanoTime());

        return observe(button).doOnNext(n -> {
            if (n.intValue() == 0)
            {
                timer.set(System.nanoTime());
            }
        })
                              .filter(n -> {
                                  long diff = System.nanoTime() - timer.get();

                                  return diff < valueInNanos;
                              });
    }
}
