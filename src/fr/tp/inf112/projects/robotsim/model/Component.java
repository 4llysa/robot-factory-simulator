package fr.tp.inf112.projects.robotsim.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.tools.javac.Main;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Shape;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import static java.lang.Thread.sleep;
import org.apache.commons.math3.analysis.function.Log;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Component implements Figure, Serializable, Runnable {

	private static final long serialVersionUID = -5960950869184030220L;

	private String id;

	@JsonBackReference("factory-components")
	private Factory factory;

	private PositionedShape positionedShape;

	private String name;

	@JsonIgnore
	protected transient Logger LOGGER = Logger.getLogger(Main.class.getName());

	protected Component(final Factory factory,
						final PositionedShape shape,
						final String name) {
		this.factory = factory;
		this.positionedShape = shape;
		this.name = name;

		if (factory != null) {
			factory.addComponent(this);
		}
	}
	public Component() {
		this.factory = null;
		this.positionedShape = null;
		this.name = null;
	}
	@Override
	public void run() {
		while (factory.isSimulationStarted()) {
			behave();
			try {
				sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PositionedShape getPositionedShape() {
//		if (positionedShape == null) LOGGER.severe("Component " + this.name + " has no positioned shape");
		return positionedShape;
	}

	@JsonIgnore
	public Position getPosition() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? null : shape.getPosition();
	}

	protected Factory getFactory() {
		return factory;
	}

	@JsonIgnore
	@Override
	public int getxCoordinate() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getxCoordinate();
	}

	protected boolean setxCoordinate(int xCoordinate) {
		final PositionedShape shape = getPositionedShape();
		if (shape == null) return false;
		if ( shape.setxCoordinate( xCoordinate ) ) {
			notifyObservers();

			return true;
		}

		return false;
	}

	@JsonIgnore
	@Override
	public int getyCoordinate() {
		final PositionedShape shape = getPositionedShape();
		return shape == null ? -1 : shape.getyCoordinate();
	}

	protected boolean setyCoordinate(final int yCoordinate) {
		final PositionedShape shape = getPositionedShape();
		if (shape == null) return false;
		if (shape.setyCoordinate(yCoordinate) ) {
			notifyObservers();

			return true;
		}

		return false;
	}

	protected void notifyObservers() {
		final Factory factory = getFactory();
		if (factory != null) factory.notifyObservers();
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [name=" + name + " xCoordinate=" + getxCoordinate() + ", yCoordinate=" + getyCoordinate()
				+ ", shape=" + getPositionedShape();
	}

	@JsonIgnore
	public int getWidth() {
		final PositionedShape shape = getPositionedShape();
		if (shape == null) return -1;
		return shape.getWidth();
	}

	@JsonIgnore
	public int getHeight() {
		final PositionedShape shape = getPositionedShape();
		if (shape == null) return -1;
		return shape.getHeight();
	}

	public boolean behave() {
		return false;
	}

	@JsonIgnore
	public boolean isMobile() {
		return false;
	}

	public boolean overlays(final Component component) {
		return overlays(component.getPositionedShape());
	}

	public boolean overlays(final PositionedShape shape) {
		return getPositionedShape().overlays(shape);
	}

	public boolean canBeOverlayed(final PositionedShape shape) {
		return false;
	}

	@JsonIgnore
	@Override
	public Style getStyle() {
		return ComponentStyle.DEFAULT;
	}

	@JsonIgnore
	@Override
	public Shape getShape() {
		return getPositionedShape();
	}

//	public boolean isSimulationStarted() {
//		final Factory factory = getFactory();
//		if (factory == null) return false;
//		return factory.isSimulationStarted();
//	}
	@Serial
	protected void restoreTransientFields() {
		LOGGER = Logger.getLogger(Main.class.getName());
	}
}
