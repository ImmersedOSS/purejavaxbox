package purejavaxbox;

import org.junit.Test;

import java.util.Map;

public class VibrationTest
{
    @Test
    public void testVibration() throws InterruptedException
    {
        XboxController ctrl = XboxControllers.INSTANCE.getController(0);

        int a = 0;
        while (a != 1)
        {
            Map<XboxButton, Number> buttons = ctrl.buttons();

            a = buttons.getOrDefault(XboxButton.A, 0)
                       .intValue();

            double vibrateLow = buttons.getOrDefault(XboxButton.LEFT_TRIGGER, 0.0)
                                       .doubleValue();
            double vibrateHigh = buttons.getOrDefault(XboxButton.RIGHT_TRIGGER, 0.0)
                                        .doubleValue();
            ctrl.rumble(vibrateLow, vibrateHigh);

            System.out.println(String.format("vb_low=%.3f vb_high=%.3f", vibrateLow, vibrateHigh));

            Thread.sleep(50);
        }
    }
}
