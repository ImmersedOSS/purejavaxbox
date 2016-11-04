package purejavaxbox.windows;

import org.junit.Test;

public class XInput13NativeTest
{
    @Test
    public void testCode() throws InterruptedException
    {
        XInputConnector x13 = new XInputConnector(0);

        XInputControllerState state;

        do
        {
            System.out.println(System.currentTimeMillis() + " " + x13.get());
            Thread.sleep(100);
        } while (true);
    }
}
