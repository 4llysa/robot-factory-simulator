package fr.tp.inf112.projects.robotsim.model;

import fr.tp.inf112.projects.canvas.controller.Observer;
import java.util.Collection;

public interface FactoryModelChangedNotifier {
    /**
     * Notifies all registered observers that the model has changed.
     */
    void notifyObservers();

    /**
     * Registers an observer to receive model change notifications.
     * @param observer The observer to add.
     */
    boolean addObserver(Observer observer);

    /**
     * Deregisters an observer.
     * @param observer The observer to remove.
     */
    boolean removeObserver(Observer observer);

    Collection<Observer> getObservers();
}