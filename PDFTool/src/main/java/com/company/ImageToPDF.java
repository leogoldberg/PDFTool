package com.company;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This code is adapted from this example: https://stackoverflow.com/questions/22358478/java-create-pdf-pages-from-images-using-pdfbox-library
 * **/
public class ImageToPDF {
    private String imagePath;
    private String outputFile;

    public ImageToPDF(String imagePath, String outputFile) {
        this.imagePath = imagePath;
        this.outputFile = outputFile;
    }

    public void convert() {
        try (PDDocument document = new PDDocument())
        {
            InputStream in = new FileInputStream(imagePath);
            BufferedImage bimg = ImageIO.read(in);
            float width = bimg.getWidth();
            float height = bimg.getHeight();
            PDPage page = new PDPage(new PDRectangle(width, height));
            document.addPage(page);
            PDImageXObject img = PDImageXObject.createFromFile(imagePath, document);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page))
            {
                contentStream.drawImage(img, 0, 0);
                contentStream.close();
                in.close();
            }
            document.save(outputFile);
            return;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
