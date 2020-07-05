package Server;

import Utility.Utility;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.jdom.JDOMException;

public class HiloServer extends Thread {

    private boolean execute;
    private String accion;
    private String filename;
    private String rutaCarpeta;
    private String archivoADescargar;
    private Socket socket;
    private DataOutputStream send;
    private DataInputStream receive;
    private String token;

    public HiloServer(Socket socket) throws JDOMException, IOException {
        this.execute = true;
        this.accion = "";
        this.filename = "";
        this.rutaCarpeta = "";
        this.archivoADescargar = "";
        this.socket = socket;
        this.send = new DataOutputStream(this.socket.getOutputStream());
        this.receive = new DataInputStream(this.socket.getInputStream());
        String sistema = System.getProperty("os.name");
        if (sistema.equalsIgnoreCase("Linux")) {
            this.token = "/";
        } else {
            this.token = "\\";
        }
    }

    public HiloServer() throws IOException {
        this.execute = true;
        this.accion = "";
        this.filename = "";
        this.rutaCarpeta = "";
        this.archivoADescargar = "";
        this.socket = new Socket();
        this.send = new DataOutputStream(this.socket.getOutputStream());
        this.receive = new DataInputStream(this.socket.getInputStream());
    }

    @Override
    public void run() {

        try {
            do {
                this.accion = this.receive.readUTF();
                System.out.println(this.accion);
                if (this.accion.equalsIgnoreCase(Utility.IDENTIFICAR)) {
                    this.rutaCarpeta = this.receive.readUTF();
                    System.out.println("Nombre es: " + this.rutaCarpeta);
                    String ruta ="Usuarios" + this.token + this.rutaCarpeta;
                    System.out.println("La ruta es: "+ruta);
                    File directorio = new File(ruta);
                    if (!directorio.exists()) {
                        if (directorio.mkdirs()) {
                            System.out.println("Directorio creado");
                        } else {
                            System.out.println("Error al crear directorio");
                        }
                    }
                    this.accion = "";
                } else if (this.accion.equalsIgnoreCase(Utility.AVISOLISTAR)) {
                    listarArchivos();
                } else if (this.accion.equalsIgnoreCase(Utility.AVISODESCARGA)) {
                    enviarArchivo();
                } else if (this.accion.equalsIgnoreCase(Utility.AVISOENVIO)) {
                    recibirArchivo();
                }
            } while (this.execute);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void enviarArchivo() throws FileNotFoundException, IOException {
        this.filename = this.receive.readUTF();
        File archivo = new File("Usuarios" + this.token + this.rutaCarpeta + this.token + this.filename);
        if (archivo.exists()) {
            this.send.writeUTF(Utility.CONFIRMADO);

            byte byteArray[] = null;
            byteArray = Files.readAllBytes(Paths.get("Usuarios" + this.token + this.rutaCarpeta + this.token + this.filename));
            this.send.write(byteArray);
            this.send.flush();

            this.accion = "";
            this.filename = "";
        } else {
            this.send.writeUTF(Utility.DENEGADO);
        }
    }

    public void recibirArchivo() throws IOException {
        this.filename = this.receive.readUTF();

        byte readbytes[] = new byte[1024];
        InputStream in = this.socket.getInputStream();

        try (OutputStream file = Files.newOutputStream(Paths.get("Usuarios" + this.token + this.rutaCarpeta + this.token + this.filename))) {
            for (int read = -1; (read = in.read(readbytes)) >= 0;) {
                file.write(readbytes, 0, read);
                if (read < 1024) {
                    break;
                }
            }
            file.flush();
        }

        this.receive = new DataInputStream(this.socket.getInputStream());
        in.close();
        this.accion = "";
        this.filename = "";
        System.out.println("Acaba de recibir");
    }

    public void listarArchivos() throws IOException {
        File carpeta = new File("Usuarios" + this.token + this.rutaCarpeta);
        String[] listado = carpeta.list();
        if (listado == null || listado.length == 0) {
            System.out.println("No hay elementos dentro de la carpeta actual");
            this.send.writeUTF(Utility.DENEGADO);
        } else {
            this.send.writeUTF("" + listado.length);
            for (int i = 0; i < listado.length; i++) {
                this.send.writeUTF(listado[i]);
            }
        }

    }
}
