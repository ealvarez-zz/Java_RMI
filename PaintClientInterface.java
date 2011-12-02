import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PaintClientInterface extends Remote{

	public void repaintFrame() throws RemoteException;
        public void sleep() throws RemoteException;
        public void wakeUp() throws RemoteException;
        public void askReset() throws RemoteException;
        public void setServer(PaintServerInterface server) throws RemoteException;
}