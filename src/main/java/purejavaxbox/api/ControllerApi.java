package purejavaxbox.api;

import purejavaxbox.XboxButton;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import static purejavaxbox.api.HelperMethods.*;

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
     * @return a flux holding the value of the button.
     */
    default Flux<Number> observe(XboxButton button)
    {
        return get()
                .filter(m -> !m.isEmpty())
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
    default Flux<Boolean> observeToggle(XboxButton b1, XboxButton b2, XboxButton... buttons)
    {
        return observe(b1, b2, buttons).distinctUntilChanged();
    }

    /**
     * Observe the state of a button combination. The returned flux will produce true only when all provided buttons are
     * pressed.
     *
     * @param b1      - the  first button in the combination.
     * @param b2      - the second button in the combination.
     * @param buttons - any additional buttons.
     * @return a flux which will broadcast true when all buttons are pressed during each polling event, otherwise false
     * will be broadcast.
     */
    default Flux<Boolean> observe(XboxButton b1, XboxButton b2, XboxButton... buttons)
    {
        Function<? super Number, ? extends Boolean> mapper = n -> n.intValue() == 1;

        Flux<Boolean> flux = observe(b1).map(mapper);

        flux = flux
                .zipWith(observe(b2).map(mapper))
                .map(t -> t.getT1() && t.getT2());

        for (XboxButton button : buttons)
        {
            flux = flux
                    .zipWith(observe(button).map(mapper))
                    .map(t -> t.getT1() && t.getT2());
        }

        return flux;
    }

    /**
     * Observe the pressing and releasing of a button.
     *
     * @param button - the button we are watching.
     * @return a flux which produces a true/false value if the button is toggled or not.
     */
    default Flux<Boolean> observeToggle(XboxButton button)
    {
        return observe(button)
                .distinctUntilChanged()
                .map(n -> n.intValue() == 1);
    }

    /**
     * Observe when a button has been held down for the specified duration.
     *
     * @param duration - measure of time.
     * @param unit     - unit of duration.
     * @param button   - the first button.
     * @return a flux which will broadcast true when the combination of buttons have been held for the specified
     * duration. False is initially broadcast, or if the button is released after this stream has broadcast true.
     */
    default Flux<Boolean> observeHeld(long duration, TimeUnit unit, XboxButton button)
    {
        long timeToHold = unit.toNanos(duration);

        AtomicLong timestamp = new AtomicLong();
        return observe(button)
                .map(n -> n.intValue() == 1)
                .doOnNext(pressed -> {
                    if (!pressed)
                    {
                        timestamp.set(System.nanoTime());
                    }
                })
                .filter(pressed -> System.nanoTime() - timestamp.get() > timeToHold || !pressed)
                .distinctUntilChanged();
    }

    /**
     * Observe when a combination of buttons has been held down for the specified duration.
     *
     * @param duration - measure of time.
     * @param unit     - unit of duration.
     * @param b1       - the first button.
     * @param b2       - the second button.
     * @param buttons  - for three or more buttons.
     * @return a flux which will broadcast true when the combination of buttons have been held for the specified
     * duration. False is initially broadcast, or if one of the buttons is released after this stream has broadcast
     * true.
     */
    default Flux<Boolean> observeHeld(long duration, TimeUnit unit, XboxButton b1, XboxButton b2, XboxButton... buttons)
    {
        Flux<Boolean> observeAll = observe(b1, b2, buttons);

        long timeToHold = unit.toNanos(duration);
        AtomicLong timestamp = new AtomicLong();
        return observeAll
                .doOnNext(pressed -> {
                    if (!pressed)
                    {
                        timestamp.set(System.nanoTime());
                    }
                })
                .filter(pressed -> System.nanoTime() - timestamp.get() > timeToHold || !pressed)
                .distinctUntilChanged();
    }

    /**
     * Notifies listeners when a value is pressed, and then provides a stream of values after the specified duration has
     * elapsed and the button is still being held.
     *
     * @param duration - the amount of time to delay before streaming values.
     * @param unit     - the measure of duration.
     * @return a flux holding the value of the button.
     * @apram button - the button to watch.
     */
    default Flux<Number> observeAfterDelay(long duration, TimeUnit unit, XboxButton button)
    {
        AtomicLong count = new AtomicLong(0);
        AtomicLong timestamp = new AtomicLong(0);

        long time = unit.toNanos(duration);

        return observe(button)
                .doOnNext(n -> timestamp.getAndUpdate(v -> count.get() == 0 ? System.nanoTime() : v))
                .filter(n -> {
                    boolean isPressed = n.intValue() == 1;
                    if (!isPressed)
                    {
                        count.set(0);
                    }
                    return isPressed;
                })
                .filter(duringDelay(count, timestamp, time));
    }

    /**
     * Notifies listeners when a value is pressed, and then provides a stream of values after the specified duration has
     * elapsed and the button is still being held.
     *
     * @param duration - the amount of time to delay before streaming values.
     * @param unit     - the measure of duration.
     * @return a flux holding the value of the button.
     * @apram button - the button to watch.
     */
    default Flux<Boolean> observeAfterDelay(long duration, TimeUnit unit, XboxButton b1, XboxButton b2, XboxButton... buttons)
    {
        AtomicLong count = new AtomicLong(0);
        AtomicLong timestamp = new AtomicLong(0);

        long time = unit.toNanos(duration);

        return observe(b1, b2, buttons)
                .doOnNext(n -> timestamp.getAndUpdate(v -> count.get() == 0 ? System.nanoTime() : v))
                .filter(isPressed -> {
                    if (!isPressed)
                    {
                        count.set(0);
                    }
                    return isPressed;
                })
                .filter(duringDelay(count, timestamp, time));
    }
}
