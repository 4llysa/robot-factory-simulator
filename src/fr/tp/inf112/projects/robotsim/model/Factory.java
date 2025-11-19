package fr.tp.inf112.projects.robotsim.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import fr.tp.inf112.projects.canvas.controller.Observable;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class Factory extends Component implements Canvas, Observable {

	private static final long serialVersionUID = 5156526483612458192L;

	private static final ComponentStyle DEFAULT = new ComponentStyle(5.0f);

	@JsonManagedReference("factory-components")
	private final List<Component> components;

	@JsonIgnore
	private transient List<Observer> observers;

	private transient boolean simulationStarted;

	public Factory(final int width,
				   final int height,
				   final String name ) {
		super(null, new RectangularShape(0, 0, width, height), name);

		components = new ArrayList<>();
		observers = null;
		simulationStarted = false;
	}

	public Factory() {
		this(-1, -1, null);
	}
	protected List<Observer> getObservers() {
		if (observers == null) {
			observers = new ArrayList<>();
		}

		return observers;
	}

	@Override
	public boolean addObserver(Observer observer) {
		return getObservers().add(observer);
	}

	@Override
	public boolean removeObserver(Observer observer) {
		return getObservers().remove(observer);
	}

	protected void notifyObservers() {
		for (final Observer observer : getObservers()) {
			observer.modelChanged();
		}
	}

	public boolean addComponent(final Component component) {
		if (components.add(component)) {
			notifyObservers();

			return true;
		}

		return false;
	}

	public boolean removeComponent(final Component component) {
		if (components.remove(component)) {
			notifyObservers();

			return true;
		}

		return false;
	}

	protected List<Component> getComponents() {
		return components;
	}
	@JsonIgnore
	protected transient List<Thread> threads = new ArrayList<>();

	@JsonIgnore
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<Figure> getFigures() {
		return (Collection) components;
	}

	@Override
	public String toString() {
		return super.toString() + " components=" + components + "]";
	}

	@JsonGetter("simulationStarted")
	public boolean isSimulationStarted() {
		return simulationStarted;
	}

	public void startSimulation() {
		if (!isSimulationStarted()) {
			LOGGER.info("Starting simulation of " + this.getName());
			this.simulationStarted = true;
			notifyObservers();
			behave();
		}
		else LOGGER.info("Simulation of " + this.getName() + " already started");
	}

	public void stopSimulation() {
		if (isSimulationStarted()) {
			this.simulationStarted = false;
			notifyObservers();
			LOGGER.info("Simulation of " + this.getName() + " ended");
		}
		else LOGGER.info("Simulation of " + this.getName() + " not started");
	}

	@Override
	public boolean behave() {
		boolean behaved = true; // not referenced in
		for (final Component component : getComponents()) {
			behaved = component.behave() || behaved;
			Thread t = new Thread(component, component.getName() + "-Thread");
			t.start();
//			threads.add(t);
			LOGGER.info("Started Thread for " + t.getName());
		}

		return true;
	}

	@JsonIgnore
	@Override
	public Style getStyle() {
		return DEFAULT;
	}

	public boolean hasObstacleAt(final PositionedShape shape) {
		for (final Component component : getComponents()) {
			if (component.overlays(shape) && !component.canBeOverlayed(shape)) {
				return true;
			}
		}

		return false;
	}
	public boolean hasObstacleAt(final Position position) {
		return hasObstacleAt(new RectangularShape(position.getxCoordinate(), position.getyCoordinate(), 2, 2));
	}

	public boolean hasMobileComponentAt(final PositionedShape shape,
										final Component movingComponent) {
		for (final Component component : getComponents()) {
			if (component != movingComponent && component.isMobile() && component.overlays(shape)) {
				return true;
			}
		}
		return false;
	}
	
	public Component getMobileComponentAt(	final Position position,
											final Component ignoredComponent) {
		if (position == null) {
			return null;
		}

		return getMobileComponentAt(new RectangularShape(position.getxCoordinate(), position.getyCoordinate(), 2, 2), ignoredComponent);
	}

	public Component getMobileComponentAt(	final PositionedShape shape,
											final Component ignoredComponent) {
		if (shape == null) {
			return null;
		}
		for (final Component component : getComponents()) {
			if (component != ignoredComponent && component.isMobile() && component.overlays(shape)) {
				return component;
			}
		}

		return null;
	}

	public synchronized int moveComponent(final Motion motion, final Component componentToMove) {
		if (hasObstacleAt(motion.getTargetPosition()) || getMobileComponentAt(motion.getTargetPosition(), componentToMove) != null) {
			System.err.println("can't move");
			return 0;
		}

		return motion.moveToTarget();
	}

	@Serial
	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		if (observers == null) observers = new ArrayList<>();
		if (threads == null) threads = new ArrayList<>();
		restoreTransientFields();
	}
}
