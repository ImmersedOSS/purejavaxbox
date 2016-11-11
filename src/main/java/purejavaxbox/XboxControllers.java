package purejavaxbox;

import java.util.*;

/**
 * Provides access to all XBox controllers connected to this PC.
 */
public enum XboxControllers implements Iterable<XboxController>
{
    INSTANCE;

    private List<XboxController> controllers = new ArrayList<>();

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

    private XboxControllers()
    {
        checkXInput();
    }

    private void checkXInput()
    {
        if (controllers.isEmpty())
        {
            try
            {
                XInputController[] xInputDeviceArray = new XInputController[4];

                for (int i = 0; i < xInputDeviceArray.length; i++)
                {
                    xInputDeviceArray[i] = new XInputController(i);
                }
                List<XInputController> xInputDevices = Arrays.asList(xInputDeviceArray);
                this.controllers.addAll(xInputDevices);
            }
            catch (IllegalStateException e)
            {
                // not windows
            }
        }
    }
}
