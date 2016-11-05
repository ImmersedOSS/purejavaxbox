package purejavaxbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public enum XboxControllers implements Iterable<XboxController>
{
    INSTANCE;

    private List<XboxController> controllers = new ArrayList<XboxController>();
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
}
