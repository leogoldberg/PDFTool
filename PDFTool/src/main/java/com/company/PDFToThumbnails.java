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
public class PDFToThumbnails {
    private String pdfPath;
    private String outputDir;

    public PDFToThumbnails(String pdfPath, String outputDir) {
        this.outputDir = outputDir;
        this.pdfPath = pdfPath;
    }

    public void createThumbnails() {
        try {
            PDDocument document = PDDocument.load(new File(pdfPath));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 72, ImageType.RGB);

                // suffix in filename will be used as the file format
                ImageIOUtil.writeImage(bim, outputDir + "/" + (page + 1) + ".jpg", 72, 0.7f);
            }
            document.close();
            return;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return;
    }
}
