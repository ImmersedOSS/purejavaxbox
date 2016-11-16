package purejavaxbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavaxbox.xinput.XInputController;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Provides access to all XBox controllers connected to this PC.
 */
public class XboxControllers implements Iterable<XboxController>
{
    private static final Logger LOG = LoggerFactory.getLogger(XboxControllers.class);

    private static final List<Supplier<List<XboxController>>> DEFAULTS = Arrays.asList(XInputController.findAll());

    public static final XboxControllers createFrom(Supplier<List<XboxController>>... includedFactories)
    {
        for (Supplier<List<XboxController>> factory : includedFactories)
        {
            try
            {
                return new XboxControllers(factory.get());
            }
            catch (Exception e)
            {
                LOG.info("Encountered an error while creating controllers from {}. Enable DEBUG for stacktrace.", factory);
                LOG.debug("", e);
            }
        }

        return new XboxControllers(Collections.emptyList());
    }

    public static final XboxControllers createWithDefaultsFrom(Supplier<List<XboxController>>... includedFactories)
    {
        Supplier<List<XboxController>>[] newData = Arrays.copyOf(includedFactories, includedFactories.length + DEFAULTS.size());

        for (int i = 0; i < DEFAULTS.size(); i++)
        {
            newData[newData.length - i - 1] = DEFAULTS.get(i);
        }
        return createFrom(newData);
    }

    private List<XboxController> controllers;

    /**
     * Provides access to one of the controllers in the system.
     *
     * @param id 0 to {@link XboxControllers#size() size - 1}.
     * @return a reference to the controller. Note that the controller does not have to be connected for this method to
     * work.
     */
    public XboxController getController(int id)
    {
        return controllers.get(id);
    }

    /**
     * The number of controllers currently supported by this system.
     *
     * @return the count of controllers.
     */
    public int size()
    {
        return controllers.size();
    }

    @Override
    public Iterator<XboxController> iterator()
    {
        return Collections.unmodifiableList(controllers)
                          .iterator();
    }

    private XboxControllers(List<XboxController> controllers)
    {
        this.controllers = controllers;
    }
}
