package com.company;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;

/**
 * This code is largely adapted from this example: https://stackoverflow.com/questions/22358478/java-create-pdf-pages-from-images-using-pdfbox-library
 * **/
public class PDFTool {

//    public void createThumbnailsFromPdf( String pdfPath)
//            throws IOException
//    {
//        try (PDDocument document = new PDDocument())
//        {
//            InputStream in = new FileInputStream(imagePath);
//            BufferedImage bimg = ImageIO.read(in);
//            float width = bimg.getWidth();
//            float height = bimg.getHeight();
//            PDPage page = new PDPage(new PDRectangle(width, height));
//            document.addPage(page);
//            PDImageXObject img = PDImageXObject.createFromFile(imagePath, document);
//
//            try (PDPageContentStream contentStream = new PDPageContentStream(document, page))
//            {
//                contentStream.drawImage(img, 0, 0);
//                contentStream.close();
//                in.close();
//            }
//            document.save(outputFile);
//        }
//    }

    public static void main(String[] args) throws IOException
    {
        if(args[0].equals("image2pdf")) {
            ImageToPDF t1 = new ImageToPDF("Image Conversion Thread", args[1], args[2]);
            t1.start();
        } else if(args[0].equals("pdf2thumbnails"))
        {
            PDFToThumbnail t1 = new PDFToThumbnail("PDF Thumbnails Thread", args[1], args[2]);
            t1.start();
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        System.err.println( "usage: " + this.getClass().getName() + "<action> <input-path> <output-path>" );
    }
}
