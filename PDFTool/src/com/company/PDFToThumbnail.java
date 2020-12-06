package com.company;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class PDFToThumbnail implements Runnable {
    private Thread t;
    private String threadName;
    private String pdfPath;
    private String outputDir;

    PDFToThumbnail(String name, String pdfPath, String outputDir) {
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
