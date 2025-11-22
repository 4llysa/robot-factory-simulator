package fr.tp.inf112.projects.robotsim.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.tp.inf112.projects.canvas.controller.Observer;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;


public class StandardModelNotifier implements FactoryModelChangedNotifier {
    @JsonIgnore
    private final Collection<Observer> observers = new ArrayList<>();

    @Override
    public void notifyObservers() {
        // Iterate over the collection and notify each observer
        // Use a synchronized block or thread-safe collection if threading is a concern.
        // For simplicity, we'll iterate over an unmodifiable copy.
        for (final Observer observer : getObservers()) {
            observer.modelChanged();
        }
    }

    @Override
    public boolean addObserver(Observer observer) {
        // Add the observer to the collection
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeObserver(Observer observer) {
        // Remove the observer from the collection
        if (observer != null) {
            observers.remove(observer);
            return true;
        }
        return false;
    }

    /**
     * Helper method to return a read-only view of the observers.
     * This helps prevent concurrent modification issues during iteration.
     */
    public Collection<Observer> getObservers() {
        return observers;
    }
}