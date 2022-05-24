import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class DoneCallBack extends UnicastRemoteObject implements CallBackInterface {
    protected DoneCallBack() throws RemoteException {
        super();
    }

    public void completed() throws RemoteException {
        System.out.println("completed");
    }

    @Override
    public void completed(int nodeID) throws RemoteException {
        try {
            Socket socket = new Socket("localhost", 8001+nodeID);
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            List<String> al = (List<String>) objectInputStream.readObject();

            // Save it
            for (String line : al) {
                Files.writeString(Paths.get("download"+String.valueOf(nodeID)+".txt"), line+"\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

            socket.close();
            notify();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }  
    }
}
