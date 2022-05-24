import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;

class DaemonWorker implements Runnable {
    MapReduceInterface mr;
    String bi;
    String bo;
    CallBackInterface cb;
    int nodeID;
    public DaemonWorker(MapReduceInterface mapReduce, String blockIn, String blockOut, CallBackInterface callback, int nodeID) {
        this.mr = mapReduce;
        this.bi = blockIn;
        this.bo = blockOut;
        this.cb = callback;
        this.nodeID = nodeID;
    }

    public void run() {
        mr.executeMap(bi, bo);
        try {
            cb.completed(nodeID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }
}

public class Daemon extends UnicastRemoteObject implements DaemonInterface {
    int nodeID;
    Daemon(int nodeID) throws RemoteException {this.nodeID = nodeID;}
    
    public void call(MapReduceInterface mapReduce, String blockIn, String blockOut, CallBackInterface callback) throws RemoteException {
        if (Objects.isNull(mapReduce) && Objects.isNull(blockIn) && Objects.isNull(blockOut) && Objects.isNull(callback)) {
            System.out.println("Invoked");   
        }

        DaemonWorker daemonWorker = new DaemonWorker(mapReduce, blockIn, blockOut, callback, nodeID);
        daemonWorker.start();

        try {
            // Initialize server at port corresponding to node ID
            ServerSocket serverSocket = new ServerSocket(nodeID+8001);
            Socket socket = serverSocket.accept();

            // Upload file
            try {
                List<String> lines = Files.readAllLines(Paths.get("result"+nodeID+".txt"), StandardCharsets.UTF_8);
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectInputStream = new ObjectOutputStream(outputStream);
                objectInputStream.writeObject(lines);
            } catch (Exception e) {
                e.printStackTrace();
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Initialize server at port corresponding to node ID
            int nodeID = Integer.parseInt(args[0]);
            ServerSocket serverSocket = new ServerSocket(nodeID + 8001);
            System.out.println(serverSocket.getLocalPort());
            Socket socket = serverSocket.accept();

            // Get output from Split
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            ArrayList<String> al = (ArrayList<String>) objectInputStream.readObject();

            // Save it
            for (String line : al) {
                Files.writeString(Paths.get("block"+String.valueOf(nodeID)+".txt"), line+"\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            serverSocket.close();

            // RMI thingy
            LocateRegistry.createRegistry(7999-nodeID);
            DaemonInterface stub = new Daemon(nodeID);
            Naming.rebind("rmi://localhost:"+String.valueOf(7999-nodeID)+"/sth", (Remote) stub);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
