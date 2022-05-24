import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

public class Launch {
    public static boolean checkFilesExist(int numNode) {
        boolean filesExist = true;
        for(int i = 0; i < numNode; i++) {
            Path path = Paths.get("download"+String.valueOf(i)+".txt");
            if (Files.notExists(path)) filesExist = false;
        }

        return filesExist;
    }

    public static void main(String[] args) {
        int numNode = Integer.parseInt(args[0]);
        for (int i=0; i < numNode; i++) {
            try {
                DaemonInterface daemon = (DaemonInterface) Naming.lookup("rmi://localhost:"+String.valueOf(7999-i)+"/sth");
                WordCount wc = new WordCount();
                DoneCallBack dcb = new DoneCallBack();
                daemon.call(wc, "block"+i+".txt", "result"+i+".txt", dcb);
            } catch (MalformedURLException | RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }

        // A very primitive way to receive callback
        while(true) {
            if (checkFilesExist(numNode)) break;
            else continue;
        }

        WordCount wc = new WordCount();
        Collection<String> blocks = new ArrayList<String>();
        for (int i = 0; i < numNode; i++) {
            blocks.add("download"+String.valueOf(i)+".txt");
        }
        wc.executeReduce(blocks, "finalresult.txt");
        System.exit(0);
    }
}
