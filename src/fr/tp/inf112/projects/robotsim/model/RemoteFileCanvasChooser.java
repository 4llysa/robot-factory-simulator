package fr.tp.inf112.projects.robotsim.model;

import java.io.IOException;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

public class RemoteFileCanvasChooser extends FileCanvasChooser {

    public RemoteFileCanvasChooser(String fileExtension, String documentTypeLabel) {
        super(fileExtension, documentTypeLabel);
    }

    @Override
    protected String browseCanvases(boolean open) throws IOException {
        if (open) {

        }
        return super.browseCanvases(open);
    }
}
