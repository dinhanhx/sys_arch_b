import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallBackInterface extends Remote {
    public void completed() throws RemoteException;
    public void completed(int nodeID) throws RemoteException;
}