package com.gameonline.network.messages;

/**
 * Lightweight ping message that allows clients to discover servers on the LAN
 * without committing to a full join handshake.
 */
public final class ServerInfoRequestMessage implements Message {
    private static final long serialVersionUID = 1L;
}
