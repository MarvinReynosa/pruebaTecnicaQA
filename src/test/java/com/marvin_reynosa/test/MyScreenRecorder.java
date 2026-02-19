package com.marvin_reynosa.test;
import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
public class MyScreenRecorder extends ScreenRecorder{
    public MyScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat, Format screenFormat,
                            Format mouseFormat, Format audioFormat, File movieFolder)
            throws IOException, AWTException {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
    }

    @Override
    protected File createMovieFile(Format fileFormat) throws IOException {
        if (!movieFolder.exists()) {
            movieFolder.mkdirs();
        } else if (!movieFolder.isDirectory()) {
            throw new IOException("\"" + movieFolder + "\" no es un directorio.");
        }
        return new File(movieFolder, "VideoRecording_" + System.currentTimeMillis() + "." +
                Registry.getInstance().getExtension(fileFormat));
    }
}
