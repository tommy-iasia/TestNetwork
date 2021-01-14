package udp.multicast;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpMulticastReceiver {

    public static void run(String localHost, String groupHost, int port, int time) throws IOException {
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

            System.out.println("receive");

            var startTime = System.currentTimeMillis();
            var endTime = startTime + time;

            var count = 0;
            var length = 0;

            var firstTime = 0L;
            var lastTime = 0L;

            var buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);

            while (true) {
                var currentTime = System.currentTimeMillis();

                if (channel.receive(buffer) != null) {
                    length += buffer.position();
                    count++;

                    if (firstTime <= 0) {
                        firstTime = currentTime;
                    }

                    lastTime = currentTime;

                    buffer.clear();
                }

                if (currentTime >= endTime) {
                    break;
                }
            }

            var receiveTime = lastTime - firstTime;
            System.out.println("time: "
                    + receiveTime / 1000 + "s "
                    + "/ " + receiveTime + "ms");

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
        var time = args.length >= 4 ? Integer.valueOf(args[3]) : 7000;
        run(localHost, groupHost, port, time);
    }
}
