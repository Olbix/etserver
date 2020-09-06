package pl.wlodarski.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pl.wlodarski.server.io.EyetrackerIOServer;

@SpringBootTest
class EtserverApplicationTests {


    private void runServer() {
        String localhost = "localhost";
        Integer port = 5009;
        ServerConfig serverConfig = ServerConfig.builder().host(localhost).port(port).build();
        EyetrackerServer eyetrackerServer = EyetrackerIOServer.getInstance();
        eyetrackerServer.runServer(serverConfig, true);
    }


    @Test
    void contextLoads() {
        runServer();



    }

}
