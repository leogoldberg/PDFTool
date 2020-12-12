package com.company;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;

public class PDFUtils {
    public static int getNumPages(String inputPath) {
        try {
            PDDocument doc = PDDocument.load(new File(inputPath));
            int count = doc.getNumberOfPages();
            return count;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void deletePage(String inputPath, int pageNumber) {
        try {
            File pdfFile = new File(inputPath);
            PDDocument doc = PDDocument.load(pdfFile);
            int count = doc.getNumberOfPages();
            // If last page, delete
            if(count == 1) {
                doc.close();
                pdfFile.delete();
            } else {
                doc.removePage(pageNumber);
                doc.save(inputPath);
                doc.close();
            }

            return;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return;
    }
}
