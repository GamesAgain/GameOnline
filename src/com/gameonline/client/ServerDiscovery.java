package com.gameonline.client;

import com.gameonline.network.messages.Message.Discovery.ServerInfoRequestMessage;
import com.gameonline.network.messages.Message.Discovery.ServerInfoResponseMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Performs lightweight LAN discovery by probing likely IP addresses for active servers.
 */
public final class ServerDiscovery {
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 350;
    private static final int DEFAULT_READ_TIMEOUT_MS = 350;

    private ServerDiscovery() {
    }

    public static List<DiscoveredServer> discover(int port) {
        List<InetAddress> candidates = collectCandidates();
        if (candidates.isEmpty()) {
            return List.of();
        }
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(32, candidates.size()));
        List<DiscoveredServer> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(candidates.size());
        for (InetAddress candidate : candidates) {
            executor.submit(() -> {
                try {
                    DiscoveredServer server = probe(candidate, port);
                    if (server != null) {
                        results.add(server);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdownNow();
        }
        results.sort(Comparator.comparing(DiscoveredServer::getServerName)
                .thenComparing(server -> server.getAddress().getHostAddress()));
        return List.copyOf(results);
    }

    private static DiscoveredServer probe(InetAddress address, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, port), DEFAULT_CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(DEFAULT_READ_TIMEOUT_MS);
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            output.writeObject(new ServerInfoRequestMessage());
            output.flush();
            Object response = input.readObject();
            if (response instanceof ServerInfoResponseMessage info) {
                return new DiscoveredServer(address, port, info.getServerName(), info.getCurrentPlayers(),
                        info.getRequiredPlayers(), info.isGameInProgress(), info.getPlayers());
            }
        } catch (IOException | ClassNotFoundException ignored) {
            // Not a compatible server or not reachable.
        }
        return null;
    }

    private static List<InetAddress> collectCandidates() {
        Set<String> visited = new LinkedHashSet<>();
        List<InetAddress> result = new ArrayList<>();
        visited.add("127.0.0.1");
        result.add(InetAddress.getLoopbackAddress());
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isVirtual()) {
                    continue;
                }
                for (InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                    if (!(address instanceof java.net.Inet4Address inet4)) {
                        continue;
                    }
                    addCandidate(result, visited, inet4);
                    addSubnetCandidates(result, visited, inet4);
                }
            }
        } catch (SocketException e) {
            // Ignore enumeration issues; we simply return the best effort list.
        }
        return result;
    }

    private static void addCandidate(List<InetAddress> result, Set<String> visited, InetAddress address) {
        String key = address.getHostAddress();
        if (visited.add(key)) {
            result.add(address);
        }
    }

    private static void addSubnetCandidates(List<InetAddress> result, Set<String> visited,
                                            java.net.Inet4Address address) {
        byte[] raw = address.getAddress();
        int addressInt = ByteBuffer.wrap(raw).getInt();
        int network = addressInt & 0xFFFFFF00;
        for (int host = 1; host < 255; host++) {
            int candidateInt = (network & 0xFFFFFF00) | host;
            if (candidateInt == addressInt) {
                continue;
            }
            byte[] bytes = ByteBuffer.allocate(4).putInt(candidateInt).array();
            try {
                InetAddress candidate = InetAddress.getByAddress(bytes);
                addCandidate(result, visited, candidate);
            } catch (UnknownHostException ignored) {
            }
        }
    }
}
