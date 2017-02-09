package purejavaxbox.api;

import purejavaxbox.XboxButton;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This class can be used to map specific controller
 */
final class PreProcessedControllerApi implements ControllerApi
{
    private ControllerApi parent;
    private Flux<Map<XboxButton, Number>> mappedFlux;

    public PreProcessedControllerApi(ControllerApi parent, List<ButtonMapper> mappers)
    {
        this.parent = parent;
        this.mappedFlux = parent.get();

        if (!mappers.isEmpty())
        {
            mappedFlux = mappedFlux.map(EnumMap::new);
            mappers.forEach(mapper -> mappedFlux = mappedFlux.doOnNext(mapper));
            mappedFlux = mappedFlux.map(Collections::unmodifiableMap);
        }

        this.mappedFlux = mappedFlux.cache(1);
    }

    @Override
    public Flux<Map<XboxButton, Number>> get()
    {
        return mappedFlux;
    }

    @Override
    public void dispose()
    {
        parent.dispose();
    }
}
