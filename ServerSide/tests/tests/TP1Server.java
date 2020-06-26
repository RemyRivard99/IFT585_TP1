package tests;

import app.Server;
import org.junit.Test;

import java.io.IOException;

public class TP1Server {

    @Test
    public void startServerAndThreads() throws IOException {
        //init
        Server server = new Server(99);
        server.run();
    }

}