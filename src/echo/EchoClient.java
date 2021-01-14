package echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        var host = args.length >= 1 ? args[0] : "127.0.0.1";
        var port = args.length >= 2 ? Integer.valueOf(args[1]) : 51000;
        run(host, port);
    }

    public static void run(String host, int port) throws IOException {
        System.out.println("start");

        try (var channel = SocketChannel.open()) {
            System.out.println("connect");

            var address = new InetSocketAddress(host, port);
            channel.connect(address);

            var buffer = ByteBuffer.allocate(3);
            buffer.put((byte) 1);
            buffer.put((byte) 2);
            buffer.put((byte) 3);
            buffer.flip();

            System.out.print("write: ");
            var write = channel.write(buffer);
            System.out.println(write + "B");

            buffer.clear();

            System.out.print("read: ");
            var read = channel.read(buffer);
            System.out.println(read + "B");

            System.out.print("bytes: ");
            buffer.flip();
            for (var i = 0; i < read; i++) {
                var value = buffer.get();
                System.out.print(value + " ");
            }
            System.out.println();
        }

        System.out.println("stop");
    }
}
