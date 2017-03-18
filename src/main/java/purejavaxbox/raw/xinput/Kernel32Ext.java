package purejavaxbox.raw.xinput;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wincon;

interface Kernel32Ext extends WinNT, Wincon
{
    final Kernel32Ext INSTANCE = (Kernel32Ext) Native.loadLibrary("Kernel32.dll", Kernel32Ext.class);

    /**
     * Retrieves the address of an exported function or variable from the specified dynamic-link library (DLL).
     *
     * @param hModule    A handle to the DLL module that contains the function or variable. The LoadLibrary,
     *                   LoadLibraryEx, LoadPackagedLibrary, or GetModuleHandle function returns this handle. The
     *                   GetProcAddress function does not retrieve addresses from modules that were loaded using the
     *                   LOAD_LIBRARY_AS_DATAFILE flag. For more information, see LoadLibraryEx.
     * @param lpProcName The function or variable name, or the function's ordinal value. If this parameter is an ordinal
     *                   value, it must be in the low-order word; the high-order word must be zero.
     * @return If the function succeeds, the return value is the address of the exported function or variable. If the
     * function fails, the return value is NULL. To get extended error information, call GetLastError.
     * @see <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/ms683212(v=vs.85).aspx">MSDN
     * Documentation</a>
     */
    Pointer GetProcAddress(WinDef.HMODULE hModule, long lpProcName);
}