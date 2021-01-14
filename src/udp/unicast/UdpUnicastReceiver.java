package udp.unicast;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class UdpUnicastReceiver {

    public static void run(int port, int time) throws IOException {
        System.out.println("start");

        try (var channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

            var portAddress = new InetSocketAddress(port);
            channel.bind(portAddress);

            System.out.println("receive");

            var startTime = System.currentTimeMillis();
            var endTime = startTime + time;

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
                        firstTime = currentTime;
                    }

                    lastTime = currentTime;

                    buffer.clear();
                }

                if (currentTime >= endTime) {
                    break;
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

    public static void main(String[] args) throws IOException {
        var port = args.length >= 1 ? Integer.valueOf(args[0]) : 51000;
        var time = args.length >= 2 ? Integer.valueOf(args[1]) : 7000;
        run(port, time);
    }
}
