package pl.wlodarski.server.io;

import lombok.Builder;
import lombok.Data;
import pl.wlodarski.server.io.thread.ClientInputRunnable;
import pl.wlodarski.server.io.thread.ClientOutputRunnable;

import java.net.Socket;

@Data
@Builder
public class ClientData {
    String id;
    ClientInputRunnable clientInputRunnable;
    ClientOutputRunnable clientOutputRunnable;
    @Builder.Default
    volatile Byte status = 0;
    Socket socket;
}
