package purejavaxbox;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

enum Polling
{
    INSTANCE;

    private ScheduledExecutorService pollThread = Executors.newSingleThreadScheduledExecutor();
    private int pollMillis;

    private Polling()
    {
        String pollsPerSecond = System.getProperties()
                                      .getOrDefault("purejavaxbox.pollsPerSecond", "20")
                                      .toString();

        pollMillis = 1000 / Integer.valueOf(pollsPerSecond);
    }

    void register(Pollable[] pollableList)
    {
        pollThread.scheduleAtFixedRate(() -> {
            for (Pollable pollable : pollableList)
            {
                pollable.poll();
            }
        }, 0, pollMillis, TimeUnit.MILLISECONDS);
    }
}
