package com.filbertkm.importer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import com.filbertkm.importer.WikiPageHandler;

public class Importer {

    public static void main(String[] args) {
    	Importer importer = new Importer();
    	importer.processDump();

    	System.out.println("done");
    }

    public void processDump() {
        File file = new File("/Users/katie/Downloads/dumps/wikidatawiki-20140804-pages-meta-current.xml");

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);

            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                Reader reader = new InputStreamReader(dis, "UTF-8");

                InputSource is = new InputSource(reader);
                is.setEncoding("UTF-8");

                WikiPageHandler handler = new WikiPageHandler();

                saxParser.parse(is, handler);

                int pageId = handler.getPageId();
                System.out.println(pageId);
            } catch (Exception e) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("done");
    }
}
