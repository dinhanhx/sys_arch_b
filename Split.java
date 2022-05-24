import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

class SplitWorker implements Runnable {
    int nodeID;
    int chunkSize;
    int startLine;
    Path filePath;

    public SplitWorker(int nodeID, int chunkSize, Path filePath) {
        this.nodeID = nodeID;
        this.chunkSize = chunkSize;
        this.startLine = nodeID * chunkSize;
        this.filePath = filePath;
    }

    public void run() {   
        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            ArrayList<String> al = new ArrayList<String>();
            lines.skip(startLine).limit(chunkSize).forEach(line -> {al.add(line);});

            
            Socket socket = new Socket("localhost", 8001+nodeID);
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectInputStream = new ObjectOutputStream(outputStream);
            objectInputStream.writeObject(al);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start () {
        Thread t = new Thread(this, Integer.toString(nodeID));
        t.start();
    }
}

public class Split {
    public static void main(String[] args) {
        Path filePath = Paths.get(args[0]);
        int numNode = Integer.parseInt(args[1]);
        int numLine = 0;
        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            numLine = (int) lines.count();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int chunkSize = numLine / numNode; // Assume remainder 0

        ArrayList<SplitWorker> workForce = new ArrayList<SplitWorker>();
        for(int i = 0; i < numNode; i++) {
            workForce.add(new SplitWorker(i, chunkSize, filePath));
            workForce.get(i).start();
        }
    }
}
