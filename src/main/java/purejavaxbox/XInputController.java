package purejavaxbox;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wincon;
import purejavaxbox.util.BitUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static purejavaxbox.XboxButton.*;

/**
 * The implementation of XboxController for the Windows operating system. Supports Windows 7+.
 * <p>
 * On Windows 7, you must install XBox Accessories, which will install the xinput1_3.dll. Windows 8 and 10 come with
 * XInput1_4.dll by default.
 */
final class XInputController implements XboxController
{
    private static interface Kernel32Ext extends WinNT, Wincon
    {
        Kernel32Ext INSTANCE = (Kernel32Ext) Native.loadLibrary("Kernel32.dll", Kernel32Ext.class);

        /**
         * Retrieves the address of an exported function or variable from the specified dynamic-link library (DLL).
         *
         * @param hModule    A handle to the DLL module that contains the function or variable. The LoadLibrary,
         *                   LoadLibraryEx, LoadPackagedLibrary, or GetModuleHandle function returns this handle. The
         *                   GetProcAddress function does not retrieve addresses from modules that were loaded using the
         *                   LOAD_LIBRARY_AS_DATAFILE flag. For more information, see LoadLibraryEx.
         * @param lpProcName The function or variable name, or the function's ordinal value. If this parameter is an
         *                   ordinal value, it must be in the low-order word; the high-order word must be zero.
         * @return If the function succeeds, the return value is the address of the exported function or variable. If
         * the function fails, the return value is NULL. To get extended error information, call GetLastError.
         * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/ms683212(v=vs.85).aspx">MSDN
         * Documentation</a>
         */
        Pointer GetProcAddress(WinDef.HMODULE hModule, long lpProcName);
    }

    /**
     * unsigned short up : 1, down : 1, left : 1, right : 1, start : 1, back : 1, l3 : 1, r3 : 1, lButton : 1, rButton :
     * 1, guideButton : 1, unknown : 1, aButton : 1, bButton : 1, xButton : 1, yButton : 1; // button state bitfield
     */
    private static final XboxButton[] INDEX_ORDER = {DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, START, BACK, LEFT_STICK_BUTTON, RIGHT_STICK_BUTTON, LEFT_BUMPER, RIGHT_BUMPER, GUIDE, UNKNOWN, A, B, X, Y};

    private static final String[] DLLS = {"Xinput1_4.dll", "xinput1_3.dll"};

    private static final Library DLL;
    private static final Function GET_GAMEPAD_STATE;

    static
    {
        Library dllLib = null;
        Function function = null;

        for (String dll : DLLS)
        {
            try
            {
                dllLib = (Library) Native.loadLibrary(dll, Library.class);
                WinDef.HMODULE module = Kernel32.INSTANCE.GetModuleHandle(dll);
                Pointer functionPtr = Kernel32Ext.INSTANCE.GetProcAddress(module, 100);
                function = Function.getFunction(functionPtr);
            }
            catch (UnsatisfiedLinkError e)
            {
                // move on to the next
            }
        }

        if (dllLib == null)
        {
            throw new IllegalStateException();
        }

        DLL = dllLib;
        GET_GAMEPAD_STATE = function;
    }

    private XInputControllerState controllerStructure = new XInputControllerState();
    private int xinputId;

    XInputController(int xinputId)
    {
        this.xinputId = xinputId;
    }

    @Override
    public Map<XboxButton, Number> buttons()
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
        return anErrorOccured ? Collections.emptyMap() : poll;
    }

    @Override
    public void rumble(double value)
    {

    }
}
