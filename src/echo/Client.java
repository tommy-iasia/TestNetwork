package echo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

    public static void run(String host, int port) throws IOException {
        System.out.println("start");

        try (var channel = SocketChannel.open()) {
            System.out.println("connecting...");

            var address = new InetSocketAddress(host, port);
            channel.connect(address);

            var buffer = ByteBuffer.allocate(3);
            buffer.put((byte) 1);
            buffer.put((byte) 2);
            buffer.put((byte) 3);
            buffer.flip();

            System.out.print("writing... ");
            var write = channel.write(buffer);
            System.out.println(write);

            buffer.clear();

            System.out.print("reading... ");
            var read = channel.read(buffer);
            System.out.println(read);

            buffer.flip();
            for (var i = 0; i < read; i++) {
                var value = buffer.get();
                System.out.print(value + " ");
            }
            System.out.println();
        }

        System.out.println("stop");
    }

    public static void main(String[] args) throws IOException {
        var host = args.length >= 1 ? args[0] : "10.112.125.186";
        var port = args.length >= 2 ? Integer.valueOf(args[1]) : 51011;
        run(host, port);
    }
}
