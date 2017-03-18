package purejavaxbox.api;

import purejavaxbox.XboxButton;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used with {@link ControllerBuilder} to provide support for manipulating controller input before it
 * reaches the target {@link ControllerApi}.
 */
final class PreProcessedControllerApi implements ControllerApi
{
    private ControllerApi parent;
    private Flux<Map<XboxButton, Number>> mappedFlux;

    public PreProcessedControllerApi(ControllerApi parent, List<ButtonMapper> mappers)
    {
        this.parent = parent;
        Flux<Map<XboxButton, Number>> baseFlux = parent.get();

        if (!mappers.isEmpty())
        {
            mappedFlux = baseFlux
                    .filter(m -> !m.isEmpty())
                    .map(EnumMap::new);
            mappers.forEach(mapper -> mappedFlux = mappedFlux.doOnNext(mapper));
            mappedFlux = mappedFlux.map(Collections::unmodifiableMap);

            mappedFlux = mappedFlux.mergeWith(baseFlux.filter(Map::isEmpty));
        }
        else
        {
            this.mappedFlux = baseFlux;
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
