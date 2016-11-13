package purejavaxbox;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wincon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavaxbox.util.BitUtil;

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
    private static final Logger LOG = LoggerFactory.getLogger(XInputController.class);

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

    private static interface XInput extends Library
    {
        int XInputSetState(int dwUserIndex, XInputVibration pVibration);
    }

    /**
     * unsigned short up : 1, down : 1, left : 1, right : 1, start : 1, back : 1, l3 : 1, r3 : 1, lButton : 1, rButton :
     * 1, guideButton : 1, unknown : 1, aButton : 1, bButton : 1, xButton : 1, yButton : 1; // button state bitfield
     */
    private static final XboxButton[] INDEX_ORDER = {DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, START, BACK, LEFT_STICK_BUTTON, RIGHT_STICK_BUTTON, LEFT_BUMPER, RIGHT_BUMPER, GUIDE, UNKNOWN, A, B, X, Y};

    private static final String[] DLLS = {"Xinput1_4.dll", "xinput1_3.dll"};

    private static final XInput DLL;
    private static final Function GET_GAMEPAD_STATE_FUNC;

    static
    {
        XInput dllLib = null;
        Function function = null;

        for (String dll : DLLS)
        {
            try
            {
                dllLib = (XInput) Native.loadLibrary(dll, XInput.class);
                WinDef.HMODULE module = Kernel32.INSTANCE.GetModuleHandle(dll);
                Pointer functionPtr = Kernel32Ext.INSTANCE.GetProcAddress(module, 100);
                function = Function.getFunction(functionPtr);
            }
            catch (UnsatisfiedLinkError e)
            {
                LOG.debug("The following library was not found {}. Trying next.", dll);
                LOG.trace("", e);
            }
        }

        if (dllLib == null)
        {
            throw new IllegalStateException();
        }

        DLL = dllLib;
        GET_GAMEPAD_STATE_FUNC = function;
    }

    private XInputControllerState controllerState = new XInputControllerState();
    private int xinputId;

    XInputController(int xinputId)
    {
        this.xinputId = xinputId;
    }

    XInputControllerState getControllerState()
    {
        return controllerState;
    }

    @Override
    public Map<XboxButton, Number> buttons()
    {
        int controllerStatus = GET_GAMEPAD_STATE_FUNC.invokeInt(new Object[]{xinputId, controllerState});

        Map<XboxButton, Number> poll = new EnumMap<>(XboxButton.class);

        short btns = controllerState.buttons;

        for (int i = 0; i < INDEX_ORDER.length; i++)
        {
            XboxButton button = INDEX_ORDER[i];
            poll.put(button, BitUtil.getBitFrom(btns, i));
        }

        poll.put(LEFT_STICK_X, controllerState.leftStickXNormalized());
        poll.put(LEFT_STICK_Y, controllerState.leftStickYNormalized());
        poll.put(RIGHT_STICK_X, controllerState.rightStickXNormalized());
        poll.put(RIGHT_STICK_Y, controllerState.rightStickYNormalized());

        poll.put(LEFT_TRIGGER, controllerState.leftTriggerNormalized());
        poll.put(RIGHT_TRIGGER, controllerState.rightTriggerNormalized());

        poll.put(LEFT_STICK_MAG, controllerState.leftStickMagnitude());
        poll.put(RIGHT_STICK_MAG, controllerState.rightStickMagnitude());

        boolean anErrorOccured = controllerStatus != 0;
        return anErrorOccured ? Collections.emptyMap() : poll;
    }

    @Override
    public void rumble(double lowFrequency, double highFrequency)
    {
        XInputVibration vibration = new XInputVibration();
        vibration.wLeftMotorSpeed = ControllerMath.scaleToUShort(lowFrequency);
        vibration.wRightMotorSpeed = ControllerMath.scaleToUShort(highFrequency);

        DLL.XInputSetState(xinputId, vibration);
    }
}
