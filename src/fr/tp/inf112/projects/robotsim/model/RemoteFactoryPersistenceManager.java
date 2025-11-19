package fr.tp.inf112.projects.robotsim.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import com.sun.tools.javac.Main;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

public class RemoteFactoryPersistenceManager extends AbstractCanvasPersistenceManager {
    private Socket socket;
    private InetAddress inetAddress;
    private int port;

    protected static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public RemoteFactoryPersistenceManager(CanvasChooser cc, InetAddress inetAddress, int port) {
        super(cc);
        this.inetAddress = inetAddress;
        this.port = port;
        LOGGER.info("Remote Factory Persistence Manager instantiated");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist(Canvas canvasModel) throws IOException{
        try (Socket socket = new Socket(inetAddress, port);
             OutputStream outputStream = socket.getOutputStream();
             ObjectOutputStream objectOutStream = new ObjectOutputStream(outputStream);
        ) {
            objectOutStream.writeObject(canvasModel);
            LOGGER.info(canvasModel.getId() + " has been persisted by Remote Persistence Manager");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Canvas read(final String canvasId)
            throws IOException {
        try (Socket socket = new Socket(inetAddress, port);
             PrintWriter ignored = new PrintWriter(socket.getOutputStream(), true);
             OutputStream outputStream = socket.getOutputStream();
             ObjectOutputStream objectOutStream = new ObjectOutputStream(outputStream);
             InputStream inpStr = socket.getInputStream();
             ObjectInputStream objInStream = new ObjectInputStream(inpStr);
        ) {
            objectOutStream.writeObject(canvasId);
            Canvas c = (Canvas) objInStream.readObject();
            LOGGER.info(c.getId() + " has been retrieved by Persistence Manager");
            return c;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(final Canvas canvasModel)
            throws IOException {
        final File canvasFile = new File(canvasModel.getId());
        return canvasFile.delete();
    }
}