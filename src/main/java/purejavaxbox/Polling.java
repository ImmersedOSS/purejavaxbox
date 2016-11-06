package purejavaxbox;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This thread polls all available {@link Polling} at the specified rate. You can use the
 * <code>-Dpurejavaxbox.pollsPerSecond</code> option to poll at a different rate.
 */
enum Polling
{
    INSTANCE;

    private ScheduledExecutorService pollThread = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("Xbox Polling Service");
        t.setUncaughtExceptionHandler((te, e) -> e.printStackTrace());
        return t;
    });
    private int pollMillis;

    private Polling()
    {
        String pollsPerSecond = System.getProperties()
                                      .getOrDefault("purejavaxbox.pollsPerSecond", "20")
                                      .toString();

        pollMillis = 1000 / Integer.valueOf(pollsPerSecond);
    }

    void registerAll(Pollable[] pollableList)
    {
        pollThread.scheduleAtFixedRate(() -> {
            for (Pollable pollable : pollableList)
            {
                pollable.poll();
            }
        }, 0, pollMillis, TimeUnit.MILLISECONDS);
    }
}
