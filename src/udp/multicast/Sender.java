package udp.multicast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Sender {

    public static void run(String host, int port, int time, long bandwidth) throws IOException {
        System.out.println("start");

        try (var channel = DatagramChannel.open()) {
            var address = new InetSocketAddress(host, port);
            channel.connect(address);

            var payload = ByteBuffer.allocateDirect(32);
            for (var i = 0; i < payload.limit(); i++) {
                payload.put((byte) i);
            }
            payload.flip();

            System.out.println("send");

            var startTime = System.nanoTime();
            var endTime = startTime + time * 1_000_000L;

            var sent = 0L;
            var written = 0L;
            var count = 0;
            var lost = 0L;

            while (true) {
                var currentTime = System.nanoTime();

                var allow = bandwidth * (currentTime - startTime) / 1_000_000_000;
                if (sent >= allow) {
                    continue;
                }

                payload.putInt(count);
                payload.rewind();

                var write = channel.write(payload);
                if (write > 0) {
                    written += write;

                    sent += 42 + write;
                    count++;

                    payload.rewind();
                }

                if (write < payload.limit()) {
                    lost += payload.limit() - write;
                }

                if (currentTime >= endTime) {
                    break;
                }
            }

            var usedTime = System.nanoTime() - startTime;
            System.out.println("time: "
                    + usedTime / 1_000_000_000 + "s "
                    + "/ " + usedTime / 1_000_000 + "ms");

            System.out.println("payload: "
                    + written / 1024 / 1024 + "MB "
                    + "/ " + written / 1024 + "KB "
                    + "/ " + written + "B ");

            System.out.println("packets: "
                    + count / 1000 + "kp "
                    + "/ " + count + "p");

            if (lost > 0) {
                System.out.println("lost: "
                        + lost / 1024 / 1024 + "MB "
                        + "/" + lost / 1024 + "KB "
                        + "/" + lost + "B");
            }

            var sentRate = sent * 1_000_000_000 / usedTime;
            System.out.println("bandwidth: "
                    + 8 * sentRate / 1024 / 1024 + "Mbps "
                    + "/ " + 8 * sentRate / 1024 + "Kbps");
        }

        System.out.println("end");
    }

    public static void main(String[] args) throws IOException {
        var host = args.length >= 1 ? args[0] : "239.1.1.1";
        var port = args.length >= 2 ? Integer.parseInt(args[1]) : 51000;
        var time = args.length >= 3 ? Integer.parseInt(args[2]) : 3000;
        var bandwidth = args.length >= 4 ? Integer.parseInt(args[3]) : 21 * 1024 * 1024 / 8;
        run(host, port, time, bandwidth);
    }
}
