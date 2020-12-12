package com.company;

import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class PDFMerge {
    private String inputDir;
    private String outputDir;
    private String outputFileName;
    public PDFMerge(String inputDir, String outputDir, String outputFileName) {
        this.inputDir = inputDir;
        this.outputDir = outputDir;
        this.outputFileName = outputFileName;
    }

    public void mergeFiles() {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationFileName(outputDir + "/" + outputFileName);
        System.out.println("Destination File name: " + pdfMerger.getDestinationFileName());
        System.out.println("Output file name: " + outputFileName);
        File dir = new File(inputDir);
        File[] directoryListing = dir.listFiles();
        // sort by date modified in ascending order
        Arrays.sort(directoryListing, Comparator.comparingLong(File::lastModified));
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(child.isFile()) {
                    try {
                        // Do something with child
                        pdfMerger.addSource(child);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        try {
            pdfMerger.mergeDocuments(null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
