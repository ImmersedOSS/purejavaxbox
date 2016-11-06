package purejavaxbox;

import java.util.*;

public enum XboxControllers implements Iterable<XboxController>
{
    INSTANCE;

    private List<XboxController> controllers = new ArrayList<>();

    public XboxController getController(int id)
    {
        return controllers.get(id);
    }

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
                XInputController[] xInputDevice = new XInputController[4];

                for (int i = 0; i < xInputDevice.length; i++)
                {
                    xInputDevice[i] = new XInputController(i);
                }
                List<XInputController> xInputDevices = Arrays.asList(xInputDevice);
                this.controllers.addAll(xInputDevices);
                Polling.INSTANCE.register(xInputDevice);
            }
            catch (IllegalStateException e)
            {
                // not windows
            }
        }
    }
}
