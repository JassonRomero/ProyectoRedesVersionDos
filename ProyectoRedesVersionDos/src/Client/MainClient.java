package Client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdom.JDOMException;

public class MainClient {

    public static void main(String args[]) {
        try {
            Client c = new Client();
            c.setVisible(true);
            c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            c.setLocationRelativeTo(null);
            c.setResizable(false);
        } catch (IOException e) {

        } catch (JDOMException ex) {
            Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
