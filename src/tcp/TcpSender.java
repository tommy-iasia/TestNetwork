package tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class TcpSender {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        var host = args.length >= 1 ? args[0] : "10.112.125.186";
        var port = args.length >= 2 ? Integer.parseInt(args[1]) : 51000;
        var time = args.length >= 3 ? Integer.parseInt(args[2]) : 3000;
        var rate = args.length >= 4 ? Integer.parseInt(args[3]) : 21 * 1024 * 1024 / 8;
        run(host, port, time, rate);
    }

    public static void run(String host, int port, int time, long rate) throws IOException, ExecutionException, InterruptedException {
        System.out.println("start");

        try (var channel = AsynchronousSocketChannel.open()) {
            var address = new InetSocketAddress(host, port);
            var connect = channel.connect(address);
            connect.get();

            var payload = ByteBuffer.allocateDirect(32);
            for (var i = 0; i < payload.limit(); i++) {
                payload.put((byte) i);
            }
            payload.flip();

            System.out.println("send");

            var startTime = System.nanoTime();
            var endTime = startTime + time * 1_000_000L;

            var written = 0L;
            var count = 0;
            var lost = 0L;

            while (true) {
                var currentTime = System.nanoTime();

                var allow = rate * (currentTime - startTime) / 1_000_000_000;
                if (written >= allow) {
                    continue;
                }

                payload.putInt(count);
                payload.rewind();

                var write = channel.write(payload);
                var writing = write.get();

                if (writing > 0) {
                    written += writing;
                    count++;

                    if (writing < payload.limit()) {
                        lost += payload.limit() - writing;
                    }

                    payload.rewind();
                }

                if (currentTime >= endTime) {
                    break;
                }
            }

            channel.close();

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

            var writeRate = written * 1_000_000_000 / usedTime;
            System.out.println("bandwidth: "
                    + 8 * writeRate / 1024 / 1024 + "Mbps "
                    + "/ " + 8 * writeRate / 1024 + "Kbps");
        }

        System.out.println("end");
    }
}
