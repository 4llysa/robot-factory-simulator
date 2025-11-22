package fr.tp.inf112.projects.robotsim;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.sun.tools.javac.Main;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

public class SimulatorPersistenceServer {
    static final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
    static final FactoryPersistenceManager factoryPersistenceManager = new FactoryPersistenceManager(canvasChooser);
    public static void main(String[] args) {
        // this is known as try with resources. socket will be closed in the end (like finally)
        try (ServerSocket serverSocket = new ServerSocket(666);
        ){
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable reqProcessor = new RequestProcessor(socket, factoryPersistenceManager);
                new Thread(reqProcessor).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
class RequestProcessor implements Runnable {
    private Socket socket;
    private FactoryPersistenceManager factoryPersistenceManager;
    protected transient final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public RequestProcessor(Socket socket, FactoryPersistenceManager factoryPersistenceManager) {
        this.socket = socket;
        this.factoryPersistenceManager = factoryPersistenceManager;
    }

    @Override
    public void run() {
        try (Socket socket = this.socket;
             InputStream inpStr = socket.getInputStream();
             OutputStream outStr = socket.getOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStr);
             ObjectInputStream objInStream = new ObjectInputStream(inpStr);
        ){
            Object o = objInStream.readObject();
            if (o instanceof String) {
                // read with if o
                LOGGER.info("Request to read " + o);
                Canvas canvas = factoryPersistenceManager.read((String) o);
                objectOutputStream.writeObject(canvas);
            } else if (o instanceof Factory) {
                // persist factory
                LOGGER.info("Request to persist " + ((Factory) o).getName() + " " + ((Factory) o).getId());
                factoryPersistenceManager.persist((Factory) o);
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }
}