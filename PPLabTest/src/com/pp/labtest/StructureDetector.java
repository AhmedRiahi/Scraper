package com.pp.labtest;

import com.pp.structureDetector.algorithm.ClassificationEngine;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StructureDetector {


    public static void main(String[] args) throws IOException {
        Element element = Jsoup.parse(new File("C:\\scaper_test\\Search _ LinkedIn.html"),"UTF-8","");
        ClassificationEngine classificationEngine = new ClassificationEngine();
        classificationEngine.setDomTree(element);
        classificationEngine.generateStatTree();
        classificationEngine.detectStructures();
    }
}
