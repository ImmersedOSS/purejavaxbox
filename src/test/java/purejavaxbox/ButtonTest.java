package purejavaxbox;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class ButtonTest
{
    private XboxControllers controllers = XboxControllers.useDefaults();

    @Test
    public void testCode() throws InterruptedException
    {
        int size = controllers.size();
        int index = 0;
        XboxController controller = controllers.getController(index);

        Map<XboxButton, Number> buttons = Collections.emptyMap();
        int value = 0;
        int count = 0;
        do
        {
            buttons = controller.buttons();

            value = buttons.getOrDefault(XboxButton.A, 0)
                           .intValue();

            System.out.print("CTRL #" + index + ": ");

            if (buttons.isEmpty())
            {
                System.out.print("Empty ");
                index = (index + 1) % size;
                controller = controllers.getController(index);
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
