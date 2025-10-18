
import com.gameonline.client.GameClientLauncher;



/**
 * Entry point for the rhythm game client. Usage:
 * java Main [host] [port] [playerName]
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : null;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;
        String playerName = args.length > 2 ? args[2] : "Player" + (int) (Math.random() * 1000);
        GameClientLauncher.launch(host, port, playerName);
    }
}
