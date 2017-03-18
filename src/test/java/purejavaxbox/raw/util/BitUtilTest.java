package purejavaxbox.raw.util;

import org.junit.Assert;
import org.junit.Test;

public class BitUtilTest
{
    @Test
    public void testBitExtractionForShorts()
    {
        int size = Short.MAX_VALUE - Short.MIN_VALUE;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i <= size; i++)
        {
            short value = (short) (Short.MIN_VALUE + i);
            int ushort = Short.toUnsignedInt(value);
            String binaryString = Integer.toBinaryString(ushort);
            long binaryStringAsNumber = Long.parseLong(binaryString);
            String expected = String.format("%016d", binaryStringAsNumber);

            createResultFrom(value, Short.SIZE, builder);
            String actual = builder.toString();

            Assert.assertEquals("For value " + ushort, expected, actual);
        }
    }

    private void createResultFrom(short value, int size, StringBuilder result)
    {
        result.delete(0, result.length());

        for (int i = size - 1; i >= 0; i--)
        {
            result.append(BitUtil.getBitFrom(value, i));
        }
    }
}
