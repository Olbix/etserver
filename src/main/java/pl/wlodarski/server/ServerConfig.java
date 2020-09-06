package pl.wlodarski.server;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ServerConfig {
    String host;
    Integer port;
}
