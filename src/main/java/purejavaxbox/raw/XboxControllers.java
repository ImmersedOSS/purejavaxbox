package purejavaxbox.raw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * This class is the entry point into the RAW API.
 * <p>
 * Creating a new list is simple. If your platform is already supported, get the default list using {@link
 * #useDefaults()}. If you have a custom implementation of {@link XboxController}, it can be registered through the
 * {@link ServiceLoader} used in this class. Your libraries will be accessed first before the defaults are consumed.
 */
public class XboxControllers implements Iterable<XboxController>
{
    private static final Logger LOG = LoggerFactory.getLogger(XboxControllers.class);
    private List<XboxController> controllers;

    private XboxControllers(List<XboxController> controllers)
    {
        this.controllers = controllers;
    }

    /**
     * Creates a new set of controllers from the factories found on the classpath. This method discovers factories using
     * {@link ServiceLoader}.
     *
     * @return a new set of controllers.
     */
    public static final XboxControllers useDefaults()
    {
        ServiceLoader<XboxControllerFactory> factoriesOnClasspath = ServiceLoader.load(XboxControllerFactory.class);
        for (XboxControllerFactory factory : factoriesOnClasspath)
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
