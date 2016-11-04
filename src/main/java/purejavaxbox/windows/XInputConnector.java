package purejavaxbox.windows;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HMODULE;

import java.util.Arrays;

/**
 * This class connects to the provided xbox controller through the XInput interface.
 */
final class XInputConnector
{
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
                HMODULE module = com.sun.jna.platform.win32.Kernel32.INSTANCE.GetModuleHandle(dll);
                return Function.getFunction(kernel32.GetProcAddress(module, 100));
            }
            catch (UnsatisfiedLinkError e)
            {
                // library isn't installed
            }
        }

        throw new IllegalStateException("Count not find the appropriate XInput library. Looked for " + Arrays.toString(DLLS));
    }

    private interface Kernel32 extends Library
    {
        public Pointer GetProcAddress(HMODULE hModule, long lpProcName);
    }

    private XInputControllerState cs = new XInputControllerState();
    private int id;

    /**
     * @param controllerId
     */
    public XInputConnector(int controllerId)
    {
        this.id = controllerId;
    }

    public XInputControllerState get()
    {
        GET_GAMEPAD_STATE.invoke(new Object[]{id, cs});
        return cs;
    }
}
