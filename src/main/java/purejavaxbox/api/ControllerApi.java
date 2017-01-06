package purejavaxbox.api;

import purejavaxbox.XboxButton;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This interface provides the entry point for developers to listen to a controller. Default methods in this class
 * provides strategies for observing button values beyond the base {@link #get() listener stream}. <p> Implementations
 * of this interface are responsible for determining the rate at which to poll controllers, as well as the order/etc.
 * </p>
 */
public interface ControllerApi extends Supplier<Flux<Map<XboxButton, Number>>>
{
    /**
     * This function produces a {@link Flux} that can be used to listen to values from a controller. To get data to
     * slow, you must call {@link Flux#subscribe()} at the end of the listening chain. Canceling is supported, and will
     * not affect other clients. <p> In the event that you have a slow consumer, you may want to consume using a
     * different thread. This can be done using {@link Flux#publishOn(reactor.core.scheduler.Scheduler)}. </p>
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
     * Watch for a particular button combination.
     *
     * @param b1      - the first button in the combo.
     * @param b2      - the second button in the combo.
     * @param buttons - the remaining buttons in the combo.
     * @return a flux which will broadcast true a single time when all buttons are pressed. Once the condition is no
     * longer true, a false is sent.
     */
    default Flux<Boolean> observeCombo(XboxButton b1, XboxButton b2, XboxButton... buttons)
    {
        Function<? super Number, ? extends Boolean> mapper = n -> n.intValue() == 1;

        Flux<Boolean> flux = observe(b1).map(mapper);

        flux = flux.zipWith(observe(b2).map(mapper))
                   .map(t -> t.getT1() && t.getT2());

        for (XboxButton button : buttons)
        {
            flux = flux.zipWith(observe(button).map(mapper))
                       .map(t -> t.getT1() && t.getT2());
        }

        return flux.distinctUntilChanged();
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
}
