package purejavaxbox.api;

import purejavaxbox.XboxButton;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<XboxButton, Number> map = Collections.singletonMap(button, value);
        sink.accept(map);
    }

    public void sendAll(List<XboxButton> buttons, Number value)
    {
        Map<XboxButton, Number> map = new HashMap<>();
        buttons.forEach(b -> map.put(b, value));
        sink.accept(map);
    }

    public void sendAll(Map<XboxButton, Number> buttons)
    {
        sink.accept(buttons);
    }

    @Override
    public void dispose()
    {
    }
}
