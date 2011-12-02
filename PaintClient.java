
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

import java.rmi.server.UnicastRemoteObject;

public class PaintClient extends UnicastRemoteObject implements PaintClientInterface {

    private Frame clientDraw;

    public static void main(String[] args) {

        if (args != null && args.length > 0) {

            try {
                PaintServerInterface server = (PaintServerInterface) Naming.lookup("rmi://" + args[0] + "/paint");
                new PaintClient(server);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Se debe ingresar la direccion del servidor.");
        }
    }

    public void sleep() throws RemoteException {
        if(clientDraw != null) {
            clientDraw.disableDraw();
        }
    }

    public void wakeUp() throws RemoteException {
        if (clientDraw != null) {
            clientDraw.enableDraw();
            repaintFrame();
        }
    }

    public void setServer(PaintServerInterface server) throws RemoteException{
         clientDraw.setServer(server);
    }

    public void serverIsMigrating() throws RemoteException{
        sleep();
    }

    public void serverIsDoneMigrating() throws RemoteException {
        wakeUp();
    }

    public void askReset() throws RemoteException {
        if (clientDraw != null) {
            clientDraw.showServerResetDialog();
        }
    }

    protected PaintClient(PaintServerInterface server) throws RemoteException {
        super();
        clientDraw = new Frame(server);
        server.register(this);
    }

    public void repaintFrame() throws RemoteException {
        clientDraw.repaint();
    }
}
