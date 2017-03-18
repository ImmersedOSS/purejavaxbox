package purejavaxbox.api;

import purejavaxbox.XboxButton;
import purejavaxbox.raw.XboxControllers;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SinglePlayerTester
{
    public static void main(String[] args)
    {
        long start = System.nanoTime();

        ControllerApi player = new ControllerBuilder()
                .controllers(XboxControllers.useDefaults())
                .timing(100)
                .player1();

        Phaser phaser = new Phaser(2);
        player
                .observeToggle(XboxButton.A)
                .filter(b -> b)
                .subscribe(b -> phaser.arrive());

        AtomicLong time = new AtomicLong(System.nanoTime());
        AtomicLong diff = new AtomicLong(0);

        player
                .observe(XboxButton.LEFT_STICK_VERTICAL)
                .filter(s -> phaser.getPhase() == 0)
                .doOnNext(s -> {
                    long current = System.nanoTime();
                    diff.set(TimeUnit.NANOSECONDS.toMillis(current - time.getAndSet(current)));
                })
                .filter(s -> Math.abs(diff.get() - 10) >= 2)
                .subscribe(s -> System.out.println(String.format("%d %s %s", diff.get(), s, Thread.currentThread())));

        phaser.arriveAndAwaitAdvance();

        long millisInASecond = TimeUnit.SECONDS.toMillis(1L);

        double timeInSeconds = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) / (double) millisInASecond;
        System.out.println(String.format("Time elapsed: %.2fs", timeInSeconds));
    }
}
