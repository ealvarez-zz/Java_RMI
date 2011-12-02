import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PaintServerInterface extends Remote {

	public void setDraw(List<List<Point>> draw) throws RemoteException;
	public List<List<Point>> getDraw() throws RemoteException;
        public void register(PaintClientInterface client) throws RemoteException;
        public void broadcast() throws RemoteException;
        public void resetDraw() throws RemoteException;
        public void clientAcceptResetDraw() throws RemoteException;
        public void clientDenyResetDraw() throws RemoteException;
        public void setServerList(ArrayList<PaintServerInterface> newServersList) throws RemoteException;
        public boolean isRunning() throws RemoteException;
        public double getServerLoad() throws RemoteException;
        public void initMigration() throws RemoteException;
        public void setClients(ArrayList<PaintClientInterface> newClientsList) throws RemoteException;
        public void setRunningState(boolean status) throws RemoteException;
}
