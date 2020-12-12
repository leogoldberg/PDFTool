package com.company;
import java.io.IOException;

public class PDFTool {

    public static void main(String[] args) throws IOException
    {
        if(args[0].equals("img2pdf")) {
            ImageToPDFConcurrent t1 = new ImageToPDFConcurrent("Image Conversion Thread", args[1], args[2]);
            t1.start();
        } else if(args[0].equals("pdf2thumbnails"))
        {
            PDFToThumbnailsConcurrent t1 = new PDFToThumbnailsConcurrent("PDF Thumbnails Thread", args[1], args[2]);
            t1.start();
        } else {
            usage();
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println( "usage: <action> <input-path> <output-path>" );
    }
}
