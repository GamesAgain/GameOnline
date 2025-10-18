import com.gameonline.server.GameServer;

/**
 * Entry point for hosting a multiplayer rhythm game session.
 * Usage: java Server [port] [requiredPlayers]
 */
public final class Server {
    private Server() {
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        int requiredPlayers = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        GameServer server = new GameServer(port, requiredPlayers);
        server.start();
    }
}
