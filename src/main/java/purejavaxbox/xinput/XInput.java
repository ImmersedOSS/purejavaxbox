package purejavaxbox.xinput;

import com.sun.jna.Library;

interface XInput extends Library
{
    int XInputSetState(int dwUserIndex, XInputVibration pVibration);
}