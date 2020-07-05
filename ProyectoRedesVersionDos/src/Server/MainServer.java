package Server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdom.JDOMException;

public class MainServer {

    public static void main(String[] args) {
        try {
            Server servidor = new Server();
            servidor.setVisible(true);
            servidor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            servidor.setLocationRelativeTo(null);
            servidor.setResizable(false);
        } catch (JDOMException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
