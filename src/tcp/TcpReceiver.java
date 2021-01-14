package tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutionException;

public class TcpReceiver {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        var port = args.length >= 1 ? Integer.parseInt(args[0]) : 51000;
        var time = args.length >= 2 ? Integer.parseInt(args[1]) : 7000;
        run(port, time);
    }

    public static void run(int port, int time) throws IOException, ExecutionException, InterruptedException {
        System.out.println("start");

        try (var server = AsynchronousServerSocketChannel.open()) {
            var address = new InetSocketAddress(port);
            server.bind(address);

            System.out.println("accept");

            var accept = server.accept();
            try (var socket = accept.get()) {
                var startTime = System.currentTimeMillis();
                var endTime = startTime + time;

                var length = 0;

                var firstTime = 0L;
                var lastTime = 0L;

                var buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);

                System.out.println("read");

                while (true) {
                    var currentTime = System.currentTimeMillis();

                    var read = socket.read(buffer);
                    var count = read.get();
                    if (count < 0) {
                        length += buffer.position();

                        if (firstTime <= 0) {
                            firstTime = currentTime;
                        }

                        lastTime = currentTime;

                        buffer.clear();
                    } else if (count < 0) {
                        break;
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
            }
        }

        System.out.println("end");
    }
}
