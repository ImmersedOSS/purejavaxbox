package purejavaxbox.stickapp;

import purejavaxbox.XboxButton;
import purejavaxbox.api.ControllerApi;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

final class StickHistory
{
    private Deque<Tuple3<Long, Number, Number>> xyList = new ArrayDeque<>();
    private Duration time = Duration.ofSeconds(1);

    public StickHistory(Duration duration, ControllerApi controller, XboxButton stickX, XboxButton stickY)
    {
        this.time = duration;

        Flux<Number> x = controller.observe(stickX);
        Flux<Number> y = controller.observe(stickY);

        Flux
                .zip(x, y)
                .map(t -> Tuples.of(System.currentTimeMillis(), t.getT1(), t.getT2()))
                .doOnError(e -> e.printStackTrace())
                .subscribe(this::add);
    }

    public void forEach(Consumer<? super Tuple3<Long, Number, Number>> consumer)
    {
        synchronized (xyList)
        {
            xyList.removeIf(this::isExpired);
            xyList.forEach(consumer);
        }
    }

    private void add(Tuple3<Long, Number, Number> timexy)
    {
        synchronized (xyList)
        {
            xyList.removeIf(this::isExpired);
            xyList.add(timexy);
        }
    }

    private boolean isExpired(Tuple3<Long, Number, Number> buttons)
    {
        long age = buttons.getT1();
        long expiration = System.currentTimeMillis() - time.toMillis();
        return age < expiration;
    }
}
