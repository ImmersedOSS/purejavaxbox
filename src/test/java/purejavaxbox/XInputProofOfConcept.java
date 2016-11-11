package purejavaxbox;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Wincon;

import java.util.Arrays;
import java.util.List;

public class XInputProofOfConcept
{
    public static class ControllerStruct extends Structure implements Structure.ByValue
    {
        public int eventCount; //event counter, increases with every controller event,
        public short buttons;    // all buttons
        public byte lTrigger;  //Left Trigger
        public byte rTrigger;  //Right Trigger
        public short lJoyY;  //Left Joystick Y
        public short lJoyX;  //Left Joystick X
        public short rJoyY;  //Right Joystick Y
        public short rJoyX;  //Right Joystick X

        @Override
        protected List<String> getFieldOrder()
        {
            String[] content = new String[]{"eventCount", "buttons", "lTrigger", "rTrigger", "lJoyY", "lJoyX", "rJoyY", "rJoyX"};
            return Arrays.asList(content);
        }
    }

    private static interface Kernel32Ext extends WinNT, Wincon
    {
        Kernel32Ext INSTANCE = (Kernel32Ext) Native.loadLibrary("Kernel32.dll", Kernel32Ext.class);

        WinDef.LPVOID GetProcAddress(WinDef.HMODULE hModule, long lpProcName);
    }

    private static WinDef.HMODULE HMODULE(WinDef.HINSTANCE i)
    {
        WinDef.HMODULE module = new WinDef.HMODULE();
        module.getPointer()
              .setPointer(0L, i.getPointer());
        return module;
    }

    public static void main(String[] args) throws InterruptedException
    {
        Kernel32Ext kernel32Ext = Kernel32Ext.INSTANCE;
        Kernel32 kernel32 = Kernel32.INSTANCE;

        //First create an HINSTANCE of the xinput1_3.dll.  Probably should use system variables to find it
        //but whatever.

        String lib = "C:\\Windows\\System32\\xinput1_4.dll";
        Native.loadLibrary(lib, Library.class);

        WinDef.HMODULE module = kernel32.GetModuleHandle(lib);

        //Get the address of ordinal 100.
        WinDef.LPVOID lpfnGetProcessID = kernel32Ext.GetProcAddress(module, 100L);


        //typedef the function. It takes an int and a pointer to a ControllerStruct and returns an error code
        //as an int.  it's 0 for no error and 1167 for "controller not present".  presumably there are others
        //but I never saw them.  It won't cause a crash on error, it just won't update the data.
        Function getControllerData = Function.getFunction(lpfnGetProcessID.getPointer());

        //Assign it to getControllerData for easier use
        //        pICFUNC getControllerData;
        //        getControllerData = pICFUNC(lpfnGetProcessID);

        //Create in an instance of the ControllerStruct
        ControllerStruct buttons = new ControllerStruct();
        int count = 0;
        Object[] content = new Object[]{0, buttons};

        while (true) //Infinite polling loop
        {
            int err = getControllerData.invokeInt(content);  //call the function with the controller number(zero based) and
            //the pointer to the ControllerStruct.

            System.err.println(String.format("%d %d %d %d %d", err, count++, buttons.size(), buttons.eventCount, buttons.buttons));
        }
        //in a real program you should release the dll by calling FreeLibrary(hGetProcIDDLL) to prevent memory
        //leaks, but since there's no way of cleanly exiting this program, I'm not sure where to put it
    }
}
