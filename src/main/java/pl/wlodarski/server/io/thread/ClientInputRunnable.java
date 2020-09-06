package pl.wlodarski.server.io.thread;

import lombok.extern.slf4j.Slf4j;
import pl.wlodarski.server.EyetrackerMsg;
import pl.wlodarski.server.io.ClientRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;

@Slf4j
public class ClientInputRunnable implements Runnable {
    private final ObjectInputStream in;
    private final String clientId;
    private final ClientRegistry clientRegistry = ClientRegistry.getInstance();
    private volatile boolean isThreadWorking = true;

    public ClientInputRunnable(ObjectInputStream in, String clientId) {
        this.in = in;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        while (isThreadWorking) {
            try {
                final EyetrackerMsg eyetrackerMsg = (EyetrackerMsg) in.readObject();
                log.info("Received from client " + clientId + " :" + eyetrackerMsg);
                clientRegistry.getClientMsgs().put(clientId, eyetrackerMsg);
                clientRegistry.spreadDataToClients();
            } catch (IOException | ClassNotFoundException e) {
                clientRegistry.dataSweep(clientId);
                log.info("Input thread has stopped working for client: " + clientId);
                isThreadWorking = false;
            }
        }
    }


}