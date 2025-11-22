package fr.tp.inf112.projects.robotsim.app;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;

import fr.tp.inf112.projects.canvas.model.CanvasChooser;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;

public class MyFileCanvasChooser extends FileCanvasChooser {

    public MyFileCanvasChooser(Component viewer, String extension, String label) {
        super(viewer, extension, label);
    }

    public MyFileCanvasChooser(String extension, String label) {
        this(null, extension, label);
    }

    @Override
    protected String browseCanvases(boolean open) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return showNativeDialog(open);
        } else {
            return super.browseCanvases(open);
        }
    }

    private String showNativeDialog(final boolean open) {
        Component viewer = getViewer();
        Frame parent = (viewer instanceof Frame) ? (Frame) viewer : null;

        FileDialog dialog = new FileDialog(parent,
                open ? "Open Canvas" : "Save Canvas",
                open ? FileDialog.LOAD : FileDialog.SAVE);
        dialog.setVisible(true);

        String file = dialog.getFile();
        String dir = dialog.getDirectory();

        if (file != null && dir != null) {
            // enforce extension
            if (!file.endsWith("." + getFileExtension())) {
                file += "." + getFileExtension();
            }
            return new File(dir, file).getAbsolutePath();
        }

        return null;
    }

    private String getFileExtension() {
        return "factory";
    }
}

//
//public class MyFileCanvasChooser implements CanvasChooser {
//
//    private final CanvasChooser delegate; // original FileCanvasChooser
//    private Component viewer;
//    private final String extension;
//    private final String label;
//
//    public MyFileCanvasChooser(Component viewer, String extension, String label) {
//        this.viewer = viewer;
//        this.extension = extension;
//        this.label = label;
//        this.delegate = new FileCanvasChooser(viewer, extension, label); // delegate for non-macOS
//    }
//
//    public MyFileCanvasChooser(final String fileExtension,
//                                final String documentTypeLabel) {
//        this(null, fileExtension, documentTypeLabel);
//    }
//
//    @Override
//    public String choseCanvas() throws IOException {
//        String os = System.getProperty("os.name").toLowerCase();
//        if (os.contains("mac")) {
//            return nativeDialog(true);
//        } else {
//            return delegate.choseCanvas();
//        }
//    }
//
//    @Override
//    public String newCanvasId() throws IOException {
//        String os = System.getProperty("os.name").toLowerCase();
//        if (os.contains("mac")) {
//            return nativeDialog(false);
//        } else {
//            return delegate.newCanvasId();
//        }
//    }
//
//    private String nativeDialog(boolean open) throws IOException {
//        Frame parent = (viewer instanceof Frame) ? (Frame) viewer : null;
//        FileDialog dialog = new FileDialog(parent, open ? "Open Canvas" : "Save Canvas", open ? FileDialog.LOAD : FileDialog.SAVE);
//        dialog.setVisible(true);
//
//        String file = dialog.getFile();
//        String dir = dialog.getDirectory();
//        if (file != null && dir != null) {
//            // enforce extension
//            if (!file.endsWith("." + extension)) file += "." + extension;
//            return new File(dir, file).getAbsolutePath();
//        }
//        return null;
//    }
//
//    public Component getViewer() {
//        return this.viewer;
//    }
//
//    public void setViewer(Component viewer) {
//        this.viewer = viewer;
//    }
//}
