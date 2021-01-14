package echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;

public class EchoServer {

    public static void run(int port) throws IOException {
        System.out.println("start");

        try (var server = ServerSocketChannel.open()) {
            var address = new InetSocketAddress(port);
            server.bind(address);

            System.out.println("accept");

            try (var socket = server.accept()) {
                var buffer = ByteBuffer.allocate(100);
                var read = socket.read(buffer);
                System.out.println("read: " + read + "B");

                buffer.flip();
                var write = socket.write(buffer);

                System.out.println("write: " + write + "B");
            }
        }

        System.out.println("stop");
    }

    public static void main(String[] args) throws IOException {
        var port = args.length >= 1 ? Integer.valueOf(args[0]) : 51000;
        run(port);
    }
}
