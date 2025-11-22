package fr.tp.inf112.projects.robotsim.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.sun.tools.javac.Main;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;
import org.apache.commons.math3.analysis.function.Log;

public class FactoryPersistenceManager extends AbstractCanvasPersistenceManager {

	protected static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	public FactoryPersistenceManager(final CanvasChooser canvasChooser) {
		super(canvasChooser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Canvas read(final String canvasId)
	throws IOException {
		try (
			final InputStream fileInputStream = new FileInputStream(canvasId);
			final InputStream bufInputStream = new BufferedInputStream(fileInputStream);
			final ObjectInputStream objectInputStream = new ObjectInputStream(bufInputStream);
		) {
			Canvas c = (Canvas) objectInputStream.readObject();
			LOGGER.info(c.getName() + c.getId() + " successfully read");
			return c;
		}
		catch (ClassNotFoundException | IOException ex) {
			throw new IOException(ex);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void persist(Canvas canvasModel)
	throws IOException {
		try (
			final OutputStream fileOutStream = new FileOutputStream(canvasModel.getId());
			final OutputStream bufOutStream = new BufferedOutputStream(fileOutStream);
			final ObjectOutputStream objOutStream = new ObjectOutputStream(bufOutStream);
		) {
			objOutStream.writeObject(canvasModel);
			LOGGER.info(canvasModel.getName() + canvasModel.getId() + " successfully persisted");
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
