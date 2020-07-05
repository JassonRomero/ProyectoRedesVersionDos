package Client;

import Utility.Utility;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocketFactory;

public class HiloClient extends Thread {

    private int porEsperar;
    private boolean execute;

    private String[] listaDeArchivosServidor;
    private String nombrelog;
    private String nombreCarpeta;
    private String token;

    private Socket socket;
    private DataOutputStream send;
    private DataInputStream receive;
    private InetAddress address;

    public HiloClient(String ip, String nombre) throws IOException {
        this.porEsperar = 5000;
        this.execute = true;
        this.listaDeArchivosServidor = null;
        this.nombrelog = nombre;

        String sistema = System.getProperty("os.name");
        if (sistema.equalsIgnoreCase("Linux")) {
            this.token = "/";
        } else {
            this.token = "\\";
        }

        this.nombreCarpeta = "Respaldo" + this.token;

        this.address = InetAddress.getByName(ip);

        configurar();

        this.send = new DataOutputStream(this.socket.getOutputStream());
        this.receive = new DataInputStream(this.socket.getInputStream());
    }

    private void configurar() throws IOException {
        System.setProperty("javax.net.ssl.keyStore", "certs" + this.token + "serverKey.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "112358");
        System.setProperty("javax.net.ssl.trustStore", "certs" + this.token + "clientTrustedCerts.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "112358");

        SSLSocketFactory clientFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        this.socket = clientFactory.createSocket(this.address, Utility.SOCKETNUMBER);
    }

    @Override
    public void run() {
        try {
            while (this.execute) {
                this.sleep(this.porEsperar);
                /* 
                * Cada treinta segundos descarga la lista de archivos
                * del servidor para compararlos con los del cliente.
                 */
                descargarListaArchivos();
                compararListas();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HiloClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void compararListas() throws IOException, NullPointerException {
        File carpetaUsuario = new File(this.nombreCarpeta);
        String listaDeArchivosCliente[] = carpetaUsuario.list();

        if (this.listaDeArchivosServidor == null) {
            System.out.println("this.listaDeArchivosServidor == null");
            if (listaDeArchivosCliente.length > 0) {
                ArrayList<String> t = new ArrayList<>();
                for (String s : listaDeArchivosCliente) {
                    t.add(s);
                }
                System.out.println("Línea 96");
                cargar(t);
            }
            return;
        }

        /* Si en el cliente hay más archivos, éstos son cargados al servidor */
        if (listaDeArchivosCliente.length > this.listaDeArchivosServidor.length) {
            System.out.println("listaDeArchivosCliente.length > this.listaDeArchivosServidor.length");
            tareaComparar(listaDeArchivosCliente, this.listaDeArchivosServidor, 1);
        } else if (listaDeArchivosCliente.length < this.listaDeArchivosServidor.length) {
            System.out.println("listaDeArchivosCliente.length < this.listaDeArchivosServidor.length");
            tareaComparar(this.listaDeArchivosServidor, listaDeArchivosCliente, 2);
        }
    }

    private void tareaComparar(String[] lista1, String[] lista2, int op) throws IOException {
        int i;
        int j;
        boolean existe = false;
        ArrayList<String> listaDeDiferencias = new ArrayList();
        for (i = 0; i < lista1.length; i++) {
            for (j = 0; j < lista2.length; j++) {
                if (lista1[i].equalsIgnoreCase(lista2[j])) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                listaDeDiferencias.add(lista1[i]);
            }
            existe = false;
        }
        if (!listaDeDiferencias.isEmpty()) {
            switch (op) {
                case 1:
                    cargar(listaDeDiferencias);
                    break;
                case 2:
                    descargar(listaDeDiferencias);
                    break;
            }
        }
    }

    private void cargar(ArrayList<String> lista) throws IOException {
        identificarse();
        for (String archivoIterador : lista) {
            /* Avisa al servidor que se le enviara un archivo */
            this.send.writeUTF(Utility.AVISOENVIO);

            /* Se envia el nombre del archivo */
            this.send.writeUTF(archivoIterador);

            byte byteArray[] = null;

            byteArray = Files.readAllBytes(Paths.get(this.nombreCarpeta + archivoIterador));

            this.send.write(byteArray);
            this.send.flush();

            this.send.close();

            configurar();
            
            this.send = new DataOutputStream(this.socket.getOutputStream());
            this.receive = new DataInputStream(this.socket.getInputStream());
            identificarse();
            System.out.println("[*] Envío satisfactorio");
        }
    }

    private void descargar(ArrayList<String> lista) throws IOException {
        identificarse();
        for (String archivoIterador : lista) {
            this.send.writeUTF(Utility.AVISODESCARGA);
            this.send.writeUTF(archivoIterador);
            String mensaje = this.receive.readUTF();
            if (mensaje.equalsIgnoreCase(Utility.CONFIRMADO)) {
                byte readbytes[] = new byte[4096];
                InputStream in = this.socket.getInputStream();
                try (OutputStream file = Files.newOutputStream(Paths.get(this.nombreCarpeta + archivoIterador))) {
                    for (int read = -1; (read = in.read(readbytes)) > 0;) {
                        file.write(readbytes, 0, read);
                        if (read < 4096) {
                            break;
                        }
                    }
                    file.flush();
                    file.close();
                }
                this.receive.close();
                
                configurar();
                
                this.receive = new DataInputStream(this.socket.getInputStream());
                this.send = new DataOutputStream(this.socket.getOutputStream());
                in.close();
                identificarse();
            }
        }
    }

    public void descargarListaArchivos() throws IOException {
        identificarse();
        this.send.writeUTF(Utility.AVISOLISTAR);
        /* Manda la cantidad de archivos */
        String mensaje = this.receive.readUTF();

        if (mensaje.equalsIgnoreCase(Utility.DENEGADO)) {
            this.listaDeArchivosServidor = null;
            System.err.println("No se encontraron archivos para listar");
            return;
        }

        try {
            int cantidadArchivos = Integer.parseInt(mensaje);
            this.listaDeArchivosServidor = new String[cantidadArchivos];

            for (int i = 0; i < cantidadArchivos; i++) {
                this.listaDeArchivosServidor[i] = this.receive.readUTF();
            }
        } catch (NumberFormatException e) {
            System.err.println(e);
        }
    }

    public void identificarse() throws IOException {
        this.send.writeUTF(Utility.IDENTIFICAR);
        this.send.writeUTF(this.nombrelog);
    }
}
