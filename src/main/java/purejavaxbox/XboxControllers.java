package purejavaxbox;

import java.util.ArrayList;
import java.util.List;

public enum XboxControllers
{
    INSTANCE;

    private List<XboxControllerFactory<?>> factory = new ArrayList<>();

    public void register(XboxControllerFactory<?> factory)
    {

    }
}
