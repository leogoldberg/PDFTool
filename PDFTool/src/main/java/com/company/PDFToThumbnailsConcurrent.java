package com.company;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * This code is largely adapted from this example: https://stackoverflow.com/questions/23326562/convert-pdf-files-to-images-with-pdfbox
 * **/
public class PDFToThumbnailsConcurrent implements Runnable {
    private Thread t;
    private String threadName;
    private String pdfPath;
    private String outputDir;

    PDFToThumbnailsConcurrent(String name, String pdfPath, String outputDir) {
        threadName = name;
        this.outputDir = outputDir;
        this.pdfPath = pdfPath;

        System.out.println("Creating " + threadName + " with input pdf: " + pdfPath);
        t = new Thread(this, threadName);
    }

    public void run() {
        try {
            PDDocument document = PDDocument.load(new File(pdfPath));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 72, ImageType.RGB);

                // suffix in filename will be used as the file format
                ImageIOUtil.writeImage(bim, outputDir + "/" + pdfPath + "-thumbnail-" + (page + 1) + ".png", 300);
            }
            document.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        t.start();
    }
}
