package purejavaxbox.api;

import purejavaxbox.raw.XboxControllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for generating {@link ControllerApi} objects to use throughout the system. This class built
 * like a builder class.
 */
public final class ControllerBuilder
{
    private XboxControllers controllers;
    private long nanos = -1L;

    private List<ButtonMapper> mappers = Collections.emptyList();

    /**
     * Provide a custom list of controllers. By default, this ControllerBuilder is configured {@link
     * XboxControllers#useDefaults()}.
     *
     * @param controllers - the controller list.
     * @return this.
     */
    public ControllerBuilder controllers(XboxControllers controllers)
    {
        this.controllers = controllers;
        return this;
    }

    /**
     * Provide a custom poll rate. By default, this rate is 20
     *
     * @param fps - a measurement in FPS. This is translated to nanoseconds per event.
     * @return this.
     */
    public ControllerBuilder timing(double fps)
    {
        if (fps > 0.0)
        {
            this.nanos = fpsToNanos(fps);
        }
        return this;
    }

    private long fpsToNanos(double fps)
    {
        return (long) (1.0 / fps * TimeUnit.SECONDS.toNanos(1L));
    }

    /**
     * Adds a mapper to this controller.
     *
     * @param mapper - the mapper.
     * @return this.
     */
    public ControllerBuilder mapper(ButtonMapper mapper)
    {
        if (mappers.isEmpty())
        {
            mappers = new ArrayList<>();
        }
        mappers.add(mapper);
        return this;
    }

    /**
     * Adds a collection of mappers to this controller.
     *
     * @param mappers - the collection of mappers.
     * @return this.
     */
    public ControllerBuilder mappers(List<ButtonMapper> mappers)
    {
        mappers.forEach(this::mapper);
        return this;
    }

    /**
     * Adds a collection of mappers to this controller.
     *
     * @param m1 - the first mapper.
     * @param m2 - the second mapper.
     * @param m  - any remaining mappers.
     * @return this.
     */
    public ControllerBuilder mappers(ButtonMapper m1, ButtonMapper m2, ButtonMapper... m)
    {
        Arrays
                .asList(m)
                .forEach(this::mapper);
        return mapper(m1).mapper(m2);
    }

    public ControllerApi player1()
    {
        XboxControllers controllers = this.controllers == null ? XboxControllers.useDefaults() : this.controllers;
        long pollingInNanos = this.nanos <= 0L ? fpsToNanos(20.0) : this.nanos;

        return new PreProcessedControllerApi(new SinglePlayer(pollingInNanos, controllers), mappers);
    }
}
