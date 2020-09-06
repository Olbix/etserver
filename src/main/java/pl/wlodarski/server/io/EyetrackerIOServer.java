package pl.wlodarski.server.io;

import lombok.extern.slf4j.Slf4j;
import pl.wlodarski.server.EyetrackerServer;
import pl.wlodarski.server.ServerConfig;
import pl.wlodarski.server.io.thread.ClientInputRunnable;
import pl.wlodarski.server.io.thread.ClientOutputRunnable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;


@Slf4j
public class EyetrackerIOServer implements EyetrackerServer {

    private static final EyetrackerIOServer eyetrackerIOServer = new EyetrackerIOServer();
    private static ServerStatus serverStatus = ServerStatus.STOPPED;
    private final ClientRegistry clientRegistry = ClientRegistry.getInstance();

    private EyetrackerIOServer() {
    }

    public static EyetrackerServer getInstance() {
        return eyetrackerIOServer;
    }

    @Override
    public synchronized void runServer(ServerConfig serverConfig, boolean async) {
        if (async) {
            new Thread(() -> EyetrackerIOServer.this.runServer(serverConfig)).start();
        } else {
            runServer(serverConfig);
        }
    }

    private synchronized void runServer(ServerConfig serverConfig) {
        if (serverStatus == ServerStatus.RUNNING) {
            log.warn("Server is currently running");
            return;
        }

        try (final ServerSocket serverSocket = new ServerSocket(serverConfig.getPort())) {
            log.info("Server has started: " + serverSocket.getLocalSocketAddress());
            log.info("Server is waiting for a new client");
            Socket socket;
            serverStatus = ServerStatus.RUNNING;
            do {
                socket = serverSocket.accept();
                log.info("A new client is connected: " + socket);
                handleIncomingClient(socket);
            } while (serverStatus == ServerStatus.RUNNING);
        } catch (IOException e) {
            log.error("Server error !", e);
            serverStatus = ServerStatus.STOPPED;
        }
    }

    String clientShakeHand(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        log.info("Performing sanity check for client");
        String id = (String) inputStream.readObject();
        log.info("Sanity check performed successfully for client : " + id);
        return id;
    }

    private void handleIncomingClient(Socket socket) throws IOException {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            String clientId = clientShakeHand(objectInputStream);


            ClientInputRunnable clientInputRunnable = runInputThread(objectInputStream, clientId);
            ClientOutputRunnable clientOutputRunnable = runOutputThread(objectOutputStream, clientId);
            ClientData clientData = ClientData
                    .builder()
                    .id(clientId)
                    .clientInputRunnable(clientInputRunnable)
                    .clientOutputRunnable(clientOutputRunnable)
                    .socket(socket)
                    .build();
            clientRegistry.getClientThreads().put(clientId, clientData);
            clientRegistry.getSendSignal().put(clientId, new Semaphore(0));
            Thread clientOutputThread = new Thread(clientOutputRunnable, clientId + "-OUTPUT");
            clientOutputThread.start();

            Thread clientInputThread = new Thread(clientInputRunnable, clientId + "-INPUT");
            clientInputThread.start();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Failed to initialize client :" + socket, e);
            socket.close();
        }
    }

    private ClientOutputRunnable runOutputThread(ObjectOutputStream outputStream, String clientId) {
        ClientOutputRunnable clientOutputRunnable = new ClientOutputRunnable(outputStream, clientId);
        return clientOutputRunnable;
    }


    private ClientInputRunnable runInputThread(ObjectInputStream inputStream, String clientId) {
        ClientInputRunnable clientInputRunnable = new ClientInputRunnable(inputStream, clientId);

        return clientInputRunnable;
    }


    public enum ServerStatus {
        RUNNING, STOPPED
    }


}
