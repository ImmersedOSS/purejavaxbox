package purejavaxbox.util;

public final class BitUtil
{
    private BitUtil()
    {

    }

    /**
     * Extracts the value of a bit in a short.
     *
     * @param target - the short to extract the bit from.
     * @param index  - the index. Must between 0 and 15. Behavior for values outside of this range is undefined.
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
     * @param index  - the index. Must between 0 and 31. Behavior for values outside of this range is undefined.
     * @return 0 if the bit is not set, 1 if it is.
     */
    public static int getBitFrom(int target, int index)
    {
        int xor = 1 << index;
        return (target & xor) > 0 ? 1 : 0;
    }
}
