package purejavaxbox;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * The implementation of XboxController for the Windows operating system. Supports Windows 7+.
 */
final class XInputController implements XboxController, Pollable
{
    private interface Kernel32 extends Library
    {
        public Pointer GetProcAddress(WinDef.HMODULE hModule, long lpProcName);
    }

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
        GET_GAMEPAD_STATE.invoke(new Object[]{xinputId, controllerStructure});

        Map<XboxButton, Double> poll = new EnumMap<>(XboxButton.class);


    }
}
