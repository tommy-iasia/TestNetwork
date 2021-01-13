package udp;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Client {

    public static void run(String localHost, String groupHost, int port) throws IOException {
        System.out.println("start");

        try (var channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

            var portAddress = new InetSocketAddress(port);
            channel.bind(portAddress);

            var localAddress = InetAddress.getByName(localHost);
            var network = NetworkInterface.getByInetAddress(localAddress);

            var groupAddress = InetAddress.getByName(groupHost);
            channel.join(groupAddress, network);

            System.out.println("run");

            var startTime = System.currentTimeMillis();
            var endTime = startTime + 5_000;

            var buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);

            var count = 0;
            var length = 0;

            while (System.currentTimeMillis() < endTime) {
                if (channel.receive(buffer) != null) {
                    length += buffer.position();
                    count++;

                    buffer.clear();
                }
            }

            System.out.println("payload: "
                    + length / 1024 / 1024 + "MB "
                    + "/ " + length / 1024 + "KB"
                    + "/ " + length + "B");

            System.out.println("packet: "
                    + count / 1000 + "kp "
                    + "/ " + count + "p");
        }

        System.out.println("stop");
    }

    public static void main(String[] args) throws IOException {
        var localHost = args.length >= 1 ? args[0] : "10.112.125.146";
        var groupHost = args.length >= 2 ? args[1] : "239.1.1.1";
        var port = args.length >= 3 ? Integer.valueOf(args[2]) : 51000;
        run(localHost, groupHost, port);
    }
}
