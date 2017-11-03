package purejavaxbox.api;

import purejavaxbox.XboxButton;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ControllerApiProxy implements ControllerApi
{
    private EmitterProcessor<Map<XboxButton, Number>> flux = EmitterProcessor.create(false);
    private FluxSink<Map<XboxButton, Number>> sink = flux.sink();

    @Override
    public Flux<Map<XboxButton, Number>> get()
    {
        return flux;
    }

    public void send(Map<XboxButton, Number> map)
    {
        sink.next(map);
    }

    public void send(XboxButton button, Number value)
    {
        Map<XboxButton, Number> map = Collections.singletonMap(button, value);
        sink.next(map);
    }

    public void sendAll(List<XboxButton> buttons, Number value)
    {
        Map<XboxButton, Number> map = new HashMap<>();
        buttons.forEach(b -> map.put(b, value));
        sink.next(map);
    }

    public void sendAll(Map<XboxButton, Number> buttons)
    {
        sink.next(buttons);
    }

    @Override
    public void dispose()
    {
    }
}
