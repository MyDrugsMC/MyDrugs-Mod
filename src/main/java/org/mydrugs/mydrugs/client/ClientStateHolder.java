package org.mydrugs.mydrugs.client;

import org.mydrugs.mydrugs.core.client.ClientState;

/**
 * Client-side singleton for shared ClientState. Lives in a client-only package
 * so the dedicated server never touches it; replaces the previous
 * {@code MyDrugs.CLIENT_STATE} static field which polluted the common mod class.
 *
 * Only call this from code that is guaranteed to run on the physical client
 * (renderers, screens, client-tick subscribers, etc.).
 */
public final class ClientStateHolder {
    private static final ClientState INSTANCE = new ClientState();

    private ClientStateHolder() {
    }

    public static ClientState get() {
        return INSTANCE;
    }
}
