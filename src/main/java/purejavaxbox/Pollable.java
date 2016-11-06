package purejavaxbox;

/**
 * Internal interface for marking objects that can be polled by the {@link Polling} service.
 */
interface Pollable
{
    /**
     * Requests this pollable to poll its resource.
     */
    void poll();
}
