
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class PaintServer extends UnicastRemoteObject implements PaintServerInterface {

    /**
     * 
     */
    private static final long serialVersionUID = 3237989332058510161L;
    private List<List<Point>> draw = new ArrayList<List<Point>>();
    private ArrayList<PaintClientInterface> clients = new ArrayList<PaintClientInterface>();
    private static int MAX_CLIENTS = 3;
    private int CLIENT_RESPONSES = 0;
    private boolean RESET_DECISION = true;
    private ArrayList<PaintServerInterface> serversList = new ArrayList<PaintServerInterface>();
    private boolean isRunning = false;

    protected PaintServer() throws RemoteException {
        super();
        // TODO Auto-generated constructor stub
    }

    public synchronized void setDraw(List<List<Point>> draw) throws RemoteException {
        this.draw = draw;
        this.broadcast();
    }

    public void register(PaintClientInterface client) throws RemoteException {
        clients.add(client);

        if (clients.size() >= MAX_CLIENTS) {
            wakeUpClients();
        }
    }

    public void wakeUpClients() throws RemoteException {
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).wakeUp();
        }
    }

    public synchronized void broadcast() throws RemoteException {

        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).repaintFrame();
        }
    }

    public synchronized List<List<Point>> getDraw() throws RemoteException {
        return draw;
    }

    public synchronized void resetDraw() throws RemoteException {


        askClientsForReset();

        if (RESET_DECISION) {
            setDraw(new ArrayList<List<Point>>());
        }

        broadcast();
        RESET_DECISION = true;
        CLIENT_RESPONSES = 0;
    }

    public synchronized void askClientsForReset() throws RemoteException {
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).askReset();
        }
    }

    public void clientAcceptResetDraw() throws RemoteException {
        voteDecision(true);
    }

    public void clientDenyResetDraw() throws RemoteException {
        voteDecision(false);
    }

    public void voteDecision(boolean vote) {
        RESET_DECISION = (RESET_DECISION && vote);
        CLIENT_RESPONSES++;
    }

    public static void setNumberOfClients(int numberOfClients) {
        MAX_CLIENTS = numberOfClients;
    }

    public void addServer() throws RemoteException {

        serversList.add(this);

        if (serversList.size() > 1) {
            broadcastServersList();
        }
    }

    public void broadcastServersList() throws RemoteException {


        for (int i = 0; i < serversList.size(); i++) {
            PaintServerInterface server = serversList.get(i);
            server.setServerList(serversList);
        }
    }

    public void setServerList(ArrayList<PaintServerInterface> newServersList) throws RemoteException {
        serversList = newServersList;
    }

    public void setClients(ArrayList<PaintClientInterface> newClientsList) throws RemoteException {
        clients = newClientsList;
    }

    public void resetServer() throws RemoteException {
        clients = new ArrayList<PaintClientInterface>();
        resetDraw();
    }

    public void setRunningState(boolean status) throws RemoteException {
        isRunning = status;
    }

    public boolean isRunning() throws RemoteException {
        return isRunning;
    }

    public double getServerLoad() throws RemoteException {
        OperatingSystemMXBean myOsBean = ManagementFactory.getOperatingSystemMXBean();
        return myOsBean.getSystemLoadAverage();
    }

    public PaintServerInterface getNewHostingServer() throws RemoteException {

        double minLoad = getServerLoad();
        PaintServerInterface serverCandidate = null;
        PaintServerInterface newServer = serverCandidate;

        for (int i = 0; i < serversList.size(); i++) {
            serverCandidate = serversList.get(i);
            double serverCandidateLoad = serverCandidate.getServerLoad();
            if (!serverCandidate.isRunning() && serverCandidateLoad < minLoad) {
                minLoad = serverCandidateLoad;
                newServer = serverCandidate;
            }
        }

        return newServer;
    }

    public void initMigration() throws RemoteException {

        System.out.println("Iniciando Migracion...");


        // Searching for a new server
        PaintServerInterface newServer = getNewHostingServer();

        if (newServer == null) {
            System.out.println("No hay disponible otro servidor");
            return;
        }

        // Copy data to new server

        newServer.setClients(clients);
        newServer.setDraw(draw);

        // Change server in clients


        // Activate new Server
        newServer.setRunningState(true);


        // deactivate and reset server

        setRunningState(false);
        resetServer();



    }

    public static void main(String[] args) {

        String serverIP = null;
        PaintServerInterface activeServer = null;

        if (args != null && args.length > 0 && Integer.parseInt(args[0]) > 0) {
            setNumberOfClients(Integer.parseInt(args[0]));
            if (args.length > 1) {
                try {
                    serverIP = args[1];
                    activeServer = (PaintServerInterface) Naming.lookup("rmi://" + args[1] + "/paint");
                } catch (Exception e) {
                    activeServer = null;
                }
            }
        }


        try {
            PaintServerInterface server = new PaintServer();
            Naming.rebind("paint", server);
            Thread migrator = new MigrationHandler(server);
            migrator.start();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
