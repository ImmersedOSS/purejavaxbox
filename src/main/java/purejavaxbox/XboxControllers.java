package purejavaxbox;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public enum XboxControllers implements Iterable<XboxController>
{
    INSTANCE;

    private List<XboxController> controllers = Collections.emptyList();
    private List<XboxController> view = Collections.unmodifiableList(controllers);

    public List<XboxController> getControllers()
    {
        return view;
    }

    @Override
    public Iterator<XboxController> iterator()
    {
        return view.iterator();
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
