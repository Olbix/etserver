package pl.wlodarski.server.io;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import pl.wlodarski.server.EyetrackerMsg;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@Data
@Slf4j
public class ClientRegistry {
    private static ClientRegistry clientRegistry = new ClientRegistry();

    private Map<String, ClientData> clientThreads = new ConcurrentHashMap<>();
    private Map<String, EyetrackerMsg> clientMsgs = new ConcurrentHashMap<>();
    private Map<String, Semaphore> sendSignal = new ConcurrentHashMap<>();

    private ClientRegistry() {
    }

    public static ClientRegistry getInstance() {
        return clientRegistry;
    }

    public void spreadDataToClients() {
        sendSignal.values().forEach(s ->
        {
            if (s.availablePermits() == 0) {
                s.release();
            }
        });
    }

    public void lockSpreadingDataToClient(String clientId) throws InterruptedException {
        Semaphore signal = sendSignal.get(clientId);
        signal.acquire();
    }

    public byte getClientStatus(String id) {
        return clientThreads.get(id).getStatus();
    }

    public synchronized void dataSweep(String clientId) {
        if (!clientMsgs.containsKey(clientId))
            return;

        clientMsgs.remove(clientId);
        terminateSocket(clientId);
        deleteMaps(clientId);

    }

    private synchronized void deleteMaps(String clientId) {
        clientThreads.remove(clientId);
        clientMsgs.remove(clientId);
    }

    private synchronized void terminateSocket(String clientId) {
        ClientData clientData = clientThreads.get(clientId);
        Socket dataSocket = clientData.getSocket();
        try {
            dataSocket.close();
        } catch (IOException e) {
            log.error("Could not terminate socket ", e);
        }
    }

    public void setClientStatus(String id, Byte status) {
        clientThreads.get(id).setStatus(status);
    }

    public EyetrackerMsg getClientMsg(String id) {
        return clientMsgs.get(id);
    }

    private ClientData getClientData(String id) {
        return clientThreads.get(id);
    }
}
