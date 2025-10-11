package fr.tp.inf112.projects.robotsim.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

public class SimulatorPersistenceServer {
    static final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
    static final FactoryPersistenceManager factoryPersistenceManager = new FactoryPersistenceManager(canvasChooser);
    public static void main(String[] args) {
        // this is known as try with resources. socket will be closed in the end (like finally)
        try (ServerSocket serverSocket = new ServerSocket(13);
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

    public RequestProcessor(Socket socket, FactoryPersistenceManager factoryPersistenceManager) {
        this.socket = socket;
        this.factoryPersistenceManager = factoryPersistenceManager;
    }

    @Override
    public void run() {
        try (InputStream inpStr = socket.getInputStream();
             OutputStream outStr = socket.getOutputStream();
             ObjectInputStream objInStream = new ObjectInputStream(inpStr);
//             Reader strReader = new InputStreamReader(inpStr);
//             BufferedReader buffReader = new BufferedReader(strReader);
             PrintWriter writer = new PrintWriter(outStr, true);
        ){
            Object o = objInStream.readObject();
            if (o instanceof String) {
                // read with if o
                factoryPersistenceManager.read((String) o);
                writer.println("read " + o + " successful");
            } else if (o instanceof Factory) {
                // persist factory
                factoryPersistenceManager.persist((Factory) o);
                writer.println("persist " + ((Factory) o).getName() + " successful");
            }
//            String resp = buffReader.readLine();

//            LOGGER.info("Message received: " + resp);

        } catch (Exception e) {}
    }
//    @Override
    public void run2() {
        try (InputStream inpStr = socket.getInputStream();
             OutputStream outStr = socket.getOutputStream();
             Reader strReader = new InputStreamReader(inpStr);
             BufferedReader buffReader = new BufferedReader(strReader);
             PrintWriter writer = new PrintWriter(outStr, true);
        ){
            while (true) {
                String resp = buffReader.readLine();
                writer.println("Message received: " + resp);
                System.out.println(resp);
            }

        } catch (Exception e) {}
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}