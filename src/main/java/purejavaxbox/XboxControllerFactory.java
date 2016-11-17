package purejavaxbox;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * A supplier that provides the list of controllers through the {@link #get()} method. Each implementation of a
 * controller set on a platform should provide one of these factories.
 */
@FunctionalInterface
public interface XboxControllerFactory extends Supplier<List<XboxController>>
{
    /**
     * Produces the list of controllers available based on the native backend. If the system does not support this
     * interface, then this method will return an empty list.
     * <p>
     * It is an error for implementations to throw an Exception.
     *
     * @return a list with controller objects or {@link Collections#emptyList()}.
     */
    @Override
    List<XboxController> get();

    /**
     * This method is used to identify the supplier in the event of a failure. By default, it is the package name of the
     * implementation class.
     *
     * @return the package name.
     */
    default String getId()
    {
        return getClass().getPackage()
                         .getName();
    }
}
