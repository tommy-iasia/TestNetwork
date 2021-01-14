package udp.unicast;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpUnicastReceiver {
    public static void main(String[] args) throws IOException {
        var port = args.length >= 1 ? Integer.parseInt(args[0]) : 51000;
        var time = args.length >= 2 ? Integer.parseInt(args[1]) : 3500;
        run(port, time);
    }

    public static void run(int port, int time) throws IOException {
        System.out.println("start");

        try (var channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

            var portAddress = new InetSocketAddress(port);
            channel.bind(portAddress);

            System.out.println("receive");

            var length = 0;
            var count = 0;

            var firstTime = 0L;
            var lastTime = 0L;

            var buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);

            while (true) {
                var currentTime = System.currentTimeMillis();

                if (channel.receive(buffer) != null) {
                    length += buffer.position();
                    count++;

                    if (firstTime <= 0) {
                        System.out.println("first");
                        firstTime = currentTime;
                    }

                    lastTime = currentTime;

                    buffer.clear();
                }

                if (firstTime > 0) {
                    if (currentTime - firstTime >= time) {
                        break;
                    }
                }
            }

            var usedTime = lastTime - firstTime;
            System.out.println("time: "
                    + usedTime / 1000 + "s "
                    + "/ " + usedTime + "ms");

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
}
