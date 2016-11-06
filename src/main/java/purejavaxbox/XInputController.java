package purejavaxbox;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import purejavaxbox.util.BitUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static purejavaxbox.XboxButton.*;

/**
 * The implementation of XboxController for the Windows operating system. Supports Windows 7+.
 */
final class XInputController implements XboxController, Pollable
{
    private interface Kernel32 extends Library
    {
        public Pointer GetProcAddress(WinDef.HMODULE hModule, long lpProcName);
    }

    /**
     * unsigned short up : 1, down : 1, left : 1, right : 1, start : 1, back : 1, l3 : 1, r3 : 1, lButton : 1, rButton :
     * 1, guideButton : 1, unknown : 1, aButton : 1, bButton : 1, xButton : 1, yButton : 1; // button state bitfield
     */
    private static final XboxButton[] INDEX_ORDER = {DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, START, BACK, LEFT_STICK_BUTTON, RIGHT_STICK_BUTTON, LEFT_BUMPER, RIGHT_BUMPER, GUIDE, UNKNOWN, A, B, X, Y};

    private static final String[] DLLS = {"Xinput1_4.dll", "xinput1_3.dll"};
    private static final Function GET_GAMEPAD_STATE = findFunctionInDll();

    private static final Function findFunctionInDll()
    {
        Kernel32 kernel32 = (Kernel32) Native.loadLibrary(Kernel32.class);

        for (String dll : DLLS)
        {
            try
            {
                Native.loadLibrary(dll, Library.class);
                WinDef.HMODULE module = com.sun.jna.platform.win32.Kernel32.INSTANCE.GetModuleHandle(dll);
                return Function.getFunction(kernel32.GetProcAddress(module, 100));
            }
            catch (UnsatisfiedLinkError e)
            {
                // library isn't installed
            }
        }

        throw new IllegalStateException("Count not find the appropriate XInput library. Looked for " + Arrays.toString(DLLS));
    }

    private Map<XboxButton, Number> lastPoll = Collections.emptyMap();
    private XInputControllerState controllerStructure = new XInputControllerState();
    private int xinputId;

    XInputController(int xinputId)
    {
        this.xinputId = xinputId;
    }

    @Override
    public Map<XboxButton, Number> buttons()
    {
        return lastPoll;
    }

    @Override
    public void rumble(double value)
    {

    }

    @Override
    public void poll()
    {
        int controllerStatus = GET_GAMEPAD_STATE.invokeInt(new Object[]{xinputId, controllerStructure});

        Map<XboxButton, Number> poll = new EnumMap<>(XboxButton.class);

        short btns = controllerStructure.buttons;

        for (int i = 0; i < INDEX_ORDER.length; i++)
        {
            XboxButton button = INDEX_ORDER[i];
            poll.put(button, BitUtil.getBitFrom(btns, i));
        }

        poll.put(XboxButton.LEFT_STICK_X, controllerStructure.leftStickXNormalized());
        poll.put(XboxButton.LEFT_STICK_Y, controllerStructure.leftStickYNormalized());
        poll.put(XboxButton.RIGHT_STICK_X, controllerStructure.rightStickXNormalized());
        poll.put(XboxButton.RIGHT_STICK_Y, controllerStructure.rightStickYNormalized());

        poll.put(XboxButton.LEFT_TRIGGER, controllerStructure.leftTriggerNormalized());
        poll.put(XboxButton.RIGHT_TRIGGER, controllerStructure.rightTriggerNormalized());

        boolean anErrorOccured = controllerStatus != 0;
        this.lastPoll = anErrorOccured ? Collections.emptyMap() : poll;
    }
}
