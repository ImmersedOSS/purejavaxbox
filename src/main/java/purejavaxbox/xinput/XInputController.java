package purejavaxbox.xinput;

import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavaxbox.XboxButton;
import purejavaxbox.XboxController;
import purejavaxbox.util.BitUtil;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static purejavaxbox.XboxButton.*;
import static purejavaxbox.xinput.XInputConstants.*;

/**
 * The implementation of XboxController for the Windows operating system. Supports Windows 7+.
 * <p>
 * On Windows 7, you must install XBox Accessories, which will install the xinput1_3.dll. Windows 8 and 10 come with
 * XInput1_4.dll by default.
 */
final class XInputController implements XboxController
{
    private static final Logger LOG = LoggerFactory.getLogger(XInputController.class);


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

        DLL = dllLib;
        GET_GAMEPAD_STATE_FUNC = function;
    }

    private static double normalizeTrigger(byte value, int dz)
    {
        double valueDz = (double) Byte.toUnsignedInt(value) - dz;
        double sizeDz = (double) Byte.MAX_VALUE - Byte.MIN_VALUE - dz;

        return Math.max(valueDz / sizeDz, 0.0);
    }

    private static double normalizeStick(short value, short deadZone)
    {
        double decimal = Math.abs(value);

        double maxValue = Short.MAX_VALUE - deadZone;
        double clipped = decimal < deadZone ? 0.0 : decimal - deadZone;
        clipped = Math.min(clipped, maxValue);

        return clipped / maxValue * Math.signum(value);
    }

    private static double magnitude(short x, short y, short deadZone)
    {
        return Math.sqrt(x * x + y * y);
    }

    private static double normalizedMagnitude(double magnitude, double deadZone)
    {
        //check if the controller is outside a circular dead zone
        if (magnitude > deadZone)
        {
            //clip the magnitude at its expected maximum value
            if (magnitude > Short.MAX_VALUE)
            {
                magnitude = Short.MAX_VALUE;
            }

            //adjust magnitude relative to the end of the dead zone
            magnitude -= deadZone;

            //optionally normalize the magnitude with respect to its expected range
            //giving a magnitude value of 0.0 to 1.0
            return magnitude / (Short.MAX_VALUE - deadZone);
        }

        return 0.0;
    }

    private static double normalizedMagnitude(Map<XboxButton, Number> poll, XboxButton x, XboxButton y)
    {
        double v1 = poll.get(x)
                        .doubleValue();
        double v2 = poll.get(y)
                        .doubleValue();

        return Math.sqrt(v1 * v1 + v2 * v2);
    }

    private static double normalizeStick(short value, double magnitude, short deadZone)
    {
        if (magnitude > deadZone)
        {
            return value / magnitude * normalizedMagnitude(magnitude, deadZone);
        }

        return 0.0;
    }

    private static short scaleToUShort(double normalizedValue)
    {
        return (short) (normalizedValue * (Short.MAX_VALUE - Short.MIN_VALUE));
    }

    private int xinputId;

    private XInputControllerState controllerState = new XInputControllerState();
    private XInputVibration vibrationBuffer = new XInputVibration();

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

        poll.put(LEFT_TRIGGER, normalizeTrigger(controllerState.lTrigger, TRIGGER_DZ));
        poll.put(RIGHT_TRIGGER, normalizeTrigger(controllerState.rTrigger, TRIGGER_DZ));

        poll.put(LEFT_STICK_HORIZONTAL, normalizeStick(controllerState.leftStickY, LEFT_DZ));
        poll.put(LEFT_STICK_VERTICAL, normalizeStick(controllerState.leftStickX, LEFT_DZ));
        poll.put(RIGHT_STICK_HORIZONTAL, normalizeStick(controllerState.rightStickY, RIGHT_DZ));
        poll.put(RIGHT_STICK_VERTICAL, normalizeStick(controllerState.rightStickX, RIGHT_DZ));

        poll.put(LEFT_STICK_HORIZONTAL, normalizeStick(controllerState.leftStickY, LEFT_DZ));
        poll.put(LEFT_STICK_VERTICAL, normalizeStick(controllerState.leftStickX, LEFT_DZ));

        poll.put(RIGHT_STICK_HORIZONTAL, normalizeStick(controllerState.rightStickY, RIGHT_DZ));
        poll.put(RIGHT_STICK_VERTICAL, normalizeStick(controllerState.rightStickX, RIGHT_DZ));

        boolean anErrorOccurred = controllerStatus != 0;
        return anErrorOccurred ? Collections.emptyMap() : poll;
    }

    @Override
    public void rumble(double lowFrequency, double highFrequency)
    {
        vibrationBuffer.wLeftMotorSpeed = scaleToUShort(lowFrequency);
        vibrationBuffer.wRightMotorSpeed = scaleToUShort(highFrequency);

        DLL.XInputSetState(xinputId, vibrationBuffer);
    }
}
