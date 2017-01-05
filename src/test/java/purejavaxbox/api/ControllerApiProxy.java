package purejavaxbox.api;

import java.util.Collections;
import java.util.Map;

import purejavaxbox.XboxButton;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

final class ControllerApiProxy implements ControllerApi
{
    private EmitterProcessor<Map<XboxButton, Number>> flux = EmitterProcessor.create(false);
    private BlockingSink<Map<XboxButton, Number>> sink = flux.connectSink();

    @Override
    public Flux<Map<XboxButton, Number>> get()
    {
        return flux;
    }

    public void send(Map<XboxButton, Number> map)
    {
        sink.accept(map);
    }

    public void send(XboxButton button, Number value)
    {
        sink.accept(Collections.singletonMap(button, value));
    }
}
