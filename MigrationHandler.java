
import java.rmi.RemoteException;

public class MigrationHandler extends Thread {

    PaintServerInterface server;

    public MigrationHandler(PaintServerInterface serverInterface) {
        server = serverInterface;
    }

    @Override
    public void run() {

        try {
            while (true) {
                Thread.sleep(1000);
                if (server.isRunning() && !server.isMigrating()) {
                    double load = server.getServerLoad();
                    System.out.println("Current System load: " + load);
                    if (load > 0.7) {
                        server.initMigration();
                    }
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }
}
