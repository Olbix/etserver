package pl.wlodarski.server;

import jdk.nashorn.internal.runtime.logging.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.wlodarski.server.io.EyetrackerIOServer;

@SpringBootApplication
@Logger
public class EtserverApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(EtserverApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String host = null;
        Integer port = null;
        if (args.length > 0) {
            host = args[0];
            if (args.length > 1) {
                port = Integer.parseInt(args[1]);
            }
        }
        if (host == null) {
            host = "127.0.0.1";
        }
        if (port == null) {
            port = 9095;
        }
        ServerConfig serverConfig = ServerConfig.builder().host(host).port(port).build();
        EyetrackerServer eyetrackerServer = EyetrackerIOServer.getInstance();
        eyetrackerServer.runServer(serverConfig, true);
    }
}
