package purejavaxbox.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavaxbox.raw.xinput.XInputControllerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class is the entry point into the API.
 * <p>
 * Creating a new list is simple. If your platform is already supported, get the default list using {@link
 * #useDefaults()}. If you have a custom implementation of {@link XboxController}, then provide your custom factory
 * using {@link #createFrom(XboxControllerFactory, XboxControllerFactory...)}. Your libraries will be accessed first
 * before the defaults are consumed.
 */
public class XboxControllers implements Iterable<XboxController>
{
    private static final Logger LOG = LoggerFactory.getLogger(XboxControllers.class);
    private static final XboxControllerFactory[] DEFAULTS = new XboxControllerFactory[]{new XInputControllerFactory()};
    private List<XboxController> controllers;

    private XboxControllers(List<XboxController> controllers)
    {
        this.controllers = controllers;
    }

    private static final XboxControllers createWithDefaultsFrom(XboxControllerFactory... includedFactories)
    {
        for (XboxControllerFactory factory : includedFactories)
        {
            LOG.info("Loading controllers from factory with id = {}.", factory.getId());

            List<XboxController> controllers = factory.get();

            if (!controllers.isEmpty())
            {
                return new XboxControllers(controllers);
            }
        }

        LOG.info("No controllers found! The returned object will have no controllers.");
        return new XboxControllers(Collections.emptyList());
    }

    /**
     * Creates a new set of controllers from the default factories.
     *
     * @return a new set of controllers.
     * @see #createFrom(XboxControllerFactory, XboxControllerFactory...)
     */
    public static final XboxControllers useDefaults()
    {
        return createWithDefaultsFrom(DEFAULTS);
    }

    /**
     * Creates a new set of controllers. The provided factories are utilized first, followed by the default factories.
     *
     * @param includedFactories - factories that attempt to create controllers for the user.
     * @return a new set of controllers.
     * @see #useDefaults()
     */
    public static final XboxControllers createFrom(XboxControllerFactory first, XboxControllerFactory... includedFactories)
    {
        XboxControllerFactory[] newData = new XboxControllerFactory[includedFactories.length + DEFAULTS.length + 1];

        newData[0] = first;
        System.arraycopy(includedFactories, 1, newData, 0, includedFactories.length);

        for (int i = 0; i < DEFAULTS.length; i++)
        {
            newData[newData.length - i - 1] = DEFAULTS[i];
        }
        return createWithDefaultsFrom(newData);
    }

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
        return Collections
                .unmodifiableList(controllers)
                .iterator();
    }
}
