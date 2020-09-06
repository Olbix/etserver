package pl.wlodarski.server;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class EyetrackerMsg implements Serializable {
    private static final long serialVersionUID = 950824;
    double x;
    double y;
}
