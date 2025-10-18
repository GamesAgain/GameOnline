package com.gameonline.network.messages;

/**
 * Sent by a client once they press the play again button on the result screen.
 */
public final class PlayAgainRequestMessage implements Message {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "PlayAgainRequestMessage{}";
    }
}
