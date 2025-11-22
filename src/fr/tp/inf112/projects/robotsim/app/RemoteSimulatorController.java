package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.sun.tools.javac.Main;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.Vertex;
import fr.tp.inf112.projects.graph.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.RemoteFactoryPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import org.apache.commons.math3.analysis.function.Log;

public class RemoteSimulatorController extends SimulatorController {
    HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private Thread updateThread;
    private boolean isAnimationRunning = false;
    protected transient Logger LOGGER = Logger.getLogger(Main.class.getName());
    private transient FactorySimulationEventConsumer eventConsumer;

    public RemoteSimulatorController(Factory factoryModel, CanvasPersistenceManager persistenceManager) {
        super(factoryModel, persistenceManager);
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(PositionedShape.class.getPackageName())
                .allowIfSubType(Component.class.getPackageName())
                .allowIfSubType(BasicVertex.class.getPackageName())
                .allowIfSubType(Vertex.class.getPackageName())
                .allowIfSubType(ArrayList.class.getName())
                .allowIfSubType(LinkedHashSet.class.getName())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Override
    public void startAnimation() {
        final URI uri;
        if (getCanvas().getId() == null) {
            getCanvas().setId(LocalDateTime.now() + ".factory");
            LOGGER.warning(getCanvas().getName() + " not saved, automatically saved as " + getCanvas().getId());
            try {
                LOGGER.info(this.getPersistenceManager().getClass().getName());
                this.getPersistenceManager().persist(getCanvas());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Starting animation of " + this.getCanvas().getId());
        try {
            uri = new URI("http", null, "localhost", 8080, "/start",
                    "id=" + getCanvas().getId(), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("STATUS CODE: " + response.statusCode());
            if (response.statusCode() != 200) throw new RuntimeException("Error starting animation");
            LOGGER.info(response.body());
//            updateThread = new Thread(this::updateViewer);
//            LOGGER.info("Starting update thread for " + getCanvas().getId());
//            updateThread.start();
        } catch (InterruptedException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        isAnimationRunning = true;
        eventConsumer = new FactorySimulationEventConsumer(this);
        updateThread = new Thread(eventConsumer::consumeMessages);
        updateThread.start();

    }
    @Override
    public void stopAnimation() {
        final URI uri;
        try {
            uri = new URI("http", null, "localhost", 8080, "/end",
                    "id=" + getCanvas().getId(), null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw new RuntimeException("Error stopping animation");
            String jsonResponse = response.body();
            Factory finalFactory = this.objectMapper.readValue(jsonResponse, Factory.class);
            if (finalFactory != null) setCanvas(finalFactory);
            if (finalFactory == null) throw new RuntimeException("why null?? ");
        } catch (InterruptedException | URISyntaxException | IOException e) {
            LOGGER.warning("Error stopping animation: " + e);
            throw new RuntimeException(e);
        }

        isAnimationRunning = false;
        if (updateThread != null) {
            eventConsumer.terminate();
            try {
                updateThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        else LOGGER.warning("updateThread should not be null");

    }
    @Override
    public boolean isAnimationRunning() {
        return isAnimationRunning;
    }

    private Factory getFactory() throws  InterruptedException, URISyntaxException, IOException {
        final URI uri;
        uri = new URI("http", null, "localhost", 8080, "/get",
                "id=" + getCanvas().getId(), null);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonResponse = response.body();
        return this.objectMapper.readValue(jsonResponse, Factory.class);
    }

    @Override
    public void setCanvas(final Canvas canvasModel) {
        final List<Observer> observers = ((Factory) getCanvas()).getObservers();
        super.setCanvas(canvasModel);
        for (final Observer observer : observers) {
            ((Factory) getCanvas()).addObserver(observer);
        }
        ((Factory) getCanvas()).notifyObservers();
    }

//    private void updateViewer() {
//        LOGGER.warning("THREAD STARTED: " + Thread.currentThread().getName());
//        LOGGER.info("Update viewer thread started");
//        try {
//            Factory factory = getFactory();
//            while (!updateThread.isInterrupted() && factory.isSimulationStarted()) {
//                factory = getFactory();
//                setCanvas(factory);
//                Thread.sleep(100);
//            }
//        } catch (InterruptedException | URISyntaxException | IOException e) {
//            Thread.currentThread().interrupt();
//            LOGGER.info("Update viewer stopped");
//        }
//    }

    public void setCanvas(String jsonString) {
        try {
            this.setCanvas(this.objectMapper.readValue(jsonString, Factory.class));
        } catch (JsonProcessingException e) {
            LOGGER.severe("JSONString does not represent Factory" + jsonString);
        }
    }
}
