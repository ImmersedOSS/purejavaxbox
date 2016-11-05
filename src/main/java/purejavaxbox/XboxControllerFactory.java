package purejavaxbox;

public interface XboxControllerFactory<T extends XboxController>
{
    T create(int ordinal);

    boolean isCorrectPlatform();
}
