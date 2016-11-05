package purejavaxbox;

import org.junit.Test;
import purejavaxbox.XInputConnector;
import purejavaxbox.XInputControllerState;

public class XInputConnectorTest
{
    @Test
    public void testCode() throws InterruptedException
    {
        XInputConnector xinput = new XInputConnector(0);

        XInputControllerState state;
        String out;
        do
        {
            out = xinput.get()
                        .toString();
            System.out.println(System.currentTimeMillis() + " " + out);
            Thread.sleep(100);
        } while (!"1111000000000000".equals(out));
    }
}
