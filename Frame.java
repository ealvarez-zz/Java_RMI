
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.WindowConstants;

import java.rmi.RemoteException;

public class Frame extends JFrame {

    public static final int WIDTH = 480, HEIGHT = 640;
    private static final String TITLE = "CC5303 - Sistemas Distribuidos";
    private JPanel panelMain = new PanelMain();
    private JLabel labelInfo = new JLabel();
    private static PaintServerInterface server;
    private boolean enabled = false;
    private boolean resetRequested = false;

    public Frame(PaintServerInterface newServer) {
        super(TITLE);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.setSize(WIDTH, HEIGHT);

        JPanel southLayout = new JPanel();
        southLayout.setLayout(new FlowLayout());
        JButton deleteButton = new JButton("Borrar");

        deleteButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                try {
                    if (enabled) {
                        showResetDialog();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        this.add(panelMain, BorderLayout.CENTER);
        this.add(labelInfo, BorderLayout.NORTH);
        southLayout.add(deleteButton);


        MouseController mouseController = new MouseController();
        panelMain.addMouseListener(mouseController);
        panelMain.addMouseMotionListener(mouseController);
        // this.buttonFinish.addActionListener(new FinishActionListener());


        this.add(southLayout, BorderLayout.SOUTH);
        this.server = newServer;
        setLabel("Esperando usuarios");

    }

    public void requestReset() throws RemoteException {
        server.resetDraw();
    }

    public void showResetDialog() throws RemoteException {

        this.setLabel("Esperando respuesta del resto de usuarios");

        Object[] options = {"Borrar", "Cancelar"};
//        int n = JOptionPane.showOptionDialog(this,
//                "Borrar dibujo",
//                "¿Estas seguro que reiniciar el dibujo?",
//                JOptionPane.YES_NO_OPTION,
//                JOptionPane.QUESTION_MESSAGE,
//                null,
//                options,
//                options[0]);


        JOptionPane optionPane = new JOptionPane("¿Estas seguro que reiniciar el dibujo?",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]);

        JDialog myDialog = optionPane.createDialog(this, "Borrar Dibujo");
        myDialog.setModal(false);
        myDialog.setVisible(true);

//        if (n == 0) {
//            resetRequested = true;
//            requestReset();
//        } else {
//            enableDraw();
//        }
 
           
        
    }

    public void acceptReset() throws RemoteException {
        server.clientAcceptResetDraw();
    }

    public void denyReset() throws RemoteException {
        server.clientDenyResetDraw();
    }

    public void showServerResetDialog() throws RemoteException {


        if (!resetRequested) {
            Object[] options = {"Borrar", "Cancelar"};
            int n = JOptionPane.showOptionDialog(this,
                    "Un usuario desea borrar el dibujo, reiniciar?",
                    "¿Deseas Reiniciar el dibujo?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            setLabel("Esperando respuesta del resto de usuarios");

            if (n == 0) {
                acceptReset();
            } else {
                denyReset();
            }
        } else {
            acceptReset();
            resetRequested = false;
        }
    }

    private class PanelMain extends JPanel {

        public void paint(Graphics graphics) {
            graphics.setColor(Color.BLUE);
            try {
                List<List<Point>> localDraw = server.getDraw();
                if (enabled) {
                    setLabel(countPoints() + " points");
                }
                for (List<Point> lst : localDraw) {
                    for (int i = 0; i < lst.size() - 1; i++) {
                        Point start = lst.get(i), finish = lst.get(i + 1);
                        graphics.drawLine((int) start.x, (int) start.y,
                                (int) finish.x, (int) finish.y);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int countPoints() throws RemoteException {
        int count = 0;
        for (List<Point> lst : server.getDraw()) {
            count += lst.size();
        }
        return count;
    }

    public void setLabel(String text) {
        if (labelInfo != null) {
            labelInfo.setText(text);
        }
    }

    public void enableDraw() throws RemoteException {
        enabled = true;
        setLabel(countPoints() + " points");
        repaint();
    }

    public void disableDraw() throws RemoteException {
        enabled = false;
    }

    class MouseController extends MouseAdapter {

        public void mousePressed(MouseEvent event) {

            if (enabled) {
                try {
                    List<List<Point>> localDraw = server.getDraw();
                    localDraw.add(new ArrayList<Point>());
                    server.setDraw(localDraw);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void mouseReleased(MouseEvent event) {
        }

        public void mouseDragged(MouseEvent event) {
            if (enabled) {
                try {
                    List<List<Point>> localDraw = server.getDraw();
                    int size = localDraw.size();
                    List<Point> lst = localDraw.get(size - 1);
                    lst.add(event.getPoint());
                    localDraw.add(size - 1, lst);
                    server.setDraw(localDraw);
                    repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
