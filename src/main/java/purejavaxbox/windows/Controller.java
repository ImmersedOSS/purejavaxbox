package purejavaxbox.windows;

import purejavaxbox.XboxButton;
import purejavaxbox.XboxController;

import java.util.Map;

class Controller implements XboxController
{
    @Override
    public Map<XboxButton, Double> poll()
    {
        return null;
    }
}
