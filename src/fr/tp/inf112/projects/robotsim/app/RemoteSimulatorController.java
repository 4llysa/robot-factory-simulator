package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;

public class RemoteSimulatorController extends SimulatorController {
    HttpClient httpClient = HttpClient.newHttpClient();

    public RemoteSimulatorController(Factory factoryModel, CanvasPersistenceManager persistenceManager) {
        super(factoryModel, persistenceManager);
    }

    @Override
    public void startAnimation() {
        final URI uri;
        try {
            uri = new URI("http", null, "localhost", 8080, "/simulation/test.factory",
                    null, null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper objectMapper = new ObjectMapper();
            Factory factory = objectMapper.readValue(response.body(), Factory.class);
        } catch (InterruptedException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void stopAnimation() {
        final URI uri;
        try {
            uri = new URI("http", null, "localhost", 8080, "/simulation/test.factory",
                    null, null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
