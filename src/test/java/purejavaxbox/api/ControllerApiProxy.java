package purejavaxbox.api;

import purejavaxbox.XboxButton;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import javax.naming.ldap.Control;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public class ControllerApiProxy implements ControllerApi
{
    private Method m;

    private EmitterProcessor<Map<XboxButton, Number>> flux = EmitterProcessor.create(false);
    private BlockingSink<Map<XboxButton, Number>> sink = flux.connectSink();

    public void send(Map<XboxButton, Number> map)
    {
        sink.accept(map);
    }

    public void send(XboxButton button, Number value)
    {
        sink.accept(Collections.singletonMap(button, value));
    }

    @Override
    public Flux<Map<XboxButton, Number>> get()
    {
        return flux;
    }
}
