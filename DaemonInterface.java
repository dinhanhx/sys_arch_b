import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DaemonInterface extends Remote {
    public void call(MapReduceInterface mapReduce, String blockIn, String blockOut, CallBackInterface callBack) 
    throws RemoteException;
}