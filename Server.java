import com.gameonline.server.GameServer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

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
        printJoinInstructions(port);
        server.start();
    }

    private static void printJoinInstructions(int port) {
        Set<String> addresses = new LinkedHashSet<>();
        addresses.add("127.0.0.1");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                    if (address instanceof Inet4Address inet4) {
                        addresses.add(inet4.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("[Server] Unable to enumerate network interfaces: " + e.getMessage());
        }
        System.out.println("[Server] Players can join using the following addresses:");
        for (String address : addresses) {
            System.out.println("[Server]  - " + address + ":" + port);
        }
    }
}
