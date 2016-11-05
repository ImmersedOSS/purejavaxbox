package purejavaxbox.util;

public final class BitUtil
{
    /**
     * Extracts the value of a bit in a byte.
     *
     * @param target - the short to extract the bit from.
     * @param index  - the index. Must between 1 and 8. Behavior for values outside of this range is undefined.
     * @return 0 if the bit is not set, 1 if it is.
     */
    public static int getBitFrom(byte target, int index)
    {
        return getBitFrom(Byte.toUnsignedInt(target), index);
    }

    /**
     * Extracts the value of a bit in a short.
     *
     * @param target - the short to extract the bit from.
     * @param index  - the index. Must between 1 and 16. Behavior for values outside of this range is undefined.
     * @return 0 if the bit is not set, 1 if it is.
     */
    public static int getBitFrom(short target, int index)
    {
        return getBitFrom(Short.toUnsignedInt(target), index);
    }

    /**
     * Extracts the value of a bit in an int.
     *
     * @param target - the short to extract the bit from.
     * @param index  - the index. Must between 1 and 32. Behavior for values outside of this range is undefined.
     * @return 0 if the bit is not set, 1 if it is.
     */
    public static int getBitFrom(int target, int index)
    {
        int xor = 1 << index - 1;
        return (target & xor) > 0 ? 1 : 0;
    }

    private BitUtil()
    {

    }
}
