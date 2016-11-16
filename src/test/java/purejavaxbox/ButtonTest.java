package purejavaxbox;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class ButtonTest
{
    @Test
    public void testCode() throws InterruptedException
    {
        XboxController controller = XboxControllers.createWithDefaultsFrom()
                                                   .getController(0);

        Map<XboxButton, Number> buttons = Collections.emptyMap();
        int value = 0;
        int count = 0;
        do
        {
            buttons = controller.buttons();

            value = buttons.getOrDefault(XboxButton.A, 0)
                           .intValue();

            if (buttons.isEmpty())
            {
                System.out.print("Empty.");
            }

            buttons.entrySet()
                   .stream()
                   .filter(e -> e.getValue()
                                 .doubleValue() != 0.0)
                   .forEach(e -> System.out.print(e + " "));
            System.out.println(count++);
            Thread.sleep(50);
        } while (value != 1);
    }
}
