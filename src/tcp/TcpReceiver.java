package tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutionException;

public class TcpReceiver {

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

                var buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);
                var length = 0;

                System.out.println("read");

                while (System.currentTimeMillis() < endTime) {
                    var read = socket.read(buffer);
                    var count = read.get();
                    if (count < 0) {
                        length += buffer.position();

                        buffer.clear();
                    } else if (count < 0) {
                        break;
                    }
                }

                System.out.println("payload: "
                        + length / 1024 / 1024 + "MB "
                        + "/ " + length / 1024 + "KB"
                        + "/ " + length + "B");
            }
        }

        System.out.println("end");
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        var port = args.length >= 1 ? Integer.valueOf(args[0]) : 51000;
        var time = args.length >= 2 ? Integer.valueOf(args[1]) : 7000;
        run(port, time);
    }
}
