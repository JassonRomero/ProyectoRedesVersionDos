package Client;

import Server.Conectar;
import Server.Server;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.jdom.JDOMException;

public class Client extends JFrame implements ActionListener {

    private JLabel labelIp;
    private JLabel labelId;
    private JLabel labelPassword;

    private JTextField textIp;
    private JTextField textId;
    private JPasswordField textPassword;

    private JButton botonIniciar;

    private HiloClient cliente;

    public Client() throws JDOMException, IOException {
        super("Client");

        this.setLayout(null);
        this.setSize(250, 250);

        init();

    }

    private void init() throws JDOMException, IOException {
        /* Crea la carpeta del usuario */
        File f = new File("Respaldo");
        if (f.mkdirs()) {
            System.out.println("[*] Directorio creado correctamente.");
        } else {
            System.out.println("[*] El directorio ya existe.");
        }
        
        
        this.labelIp = new JLabel("Ip:");
        this.labelIp.setBounds(10, 10, 75, 30);
        this.add(this.labelIp);

        this.labelId = new JLabel("Nombre:");
        this.labelId.setBounds(10, 60, 75, 30);
        this.add(this.labelId);

        this.labelPassword = new JLabel("Contraseña");
        this.labelPassword.setBounds(10, 110, 100, 30);
        this.add(this.labelPassword);

        this.textIp = new JTextField();
        this.textIp.setBounds(100, 10, 100, 30);
        this.add(this.textIp);

        this.textId = new JTextField();
        this.textId.setBounds(100, 60, 100, 30);
        this.add(this.textId);

        this.textPassword = new JPasswordField();
        this.textPassword.setBounds(100, 110, 100, 30);
        this.add(this.textPassword);

        this.botonIniciar = new JButton("Iniciar");
        this.botonIniciar.setBounds(50, 150, 100, 30);
        this.botonIniciar.addActionListener(this);
        this.add(this.botonIniciar);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == this.botonIniciar) {

            try {
                Conectar conect = new Conectar();
                Connection conectar = conect.conexion();
                Statement pst = conectar.createStatement();
                ResultSet rs = pst.executeQuery("call get_Usuario('" + this.textId.getText() + "','"+this.textPassword.getText()+"')");
                String i = "";
                while (rs.next()) {
                    i = rs.getString("nombre");
                    System.out.println("nombre = " + i);
                }

                if (i.equals(this.textId.getText())) {
                    this.dispose();
                    this.cliente = new HiloClient(this.textIp.getText().trim(),this.textId.getText().trim());     
                    this.cliente.start();
                } else {
                    JOptionPane.showMessageDialog(null, "Datos ingresados incorrecto");
                }
                conectar.close();
            } catch (SQLException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
