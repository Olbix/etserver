package pl.wlodarski.server.io.thread;

import lombok.extern.slf4j.Slf4j;
import pl.wlodarski.server.io.ClientRegistry;

import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
public class ClientOutputRunnable implements Runnable {
    private final ObjectOutputStream out;
    private final String clientId;
    private final ClientRegistry clientRegistry = ClientRegistry.getInstance();
    private volatile boolean isThreadWorking = true;


    public ClientOutputRunnable(ObjectOutputStream out, String clientId) {
        this.out = out;
        this.clientId = clientId;
    }


    @Override
    public void run() {
        while (isThreadWorking) {
            try {
                clientRegistry.lockSpreadingDataToClient(clientId);
                out.writeObject(clientRegistry.getClientMsgs());
                out.reset();
                log.info("Server has sent data to: " + clientId);
            } catch (IOException | InterruptedException e) {
                clientRegistry.dataSweep(clientId);
                log.info("Output thread has stopped working for client: " + clientId);
                isThreadWorking = false;
            }
        }
    }
}