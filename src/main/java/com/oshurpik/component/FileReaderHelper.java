package com.oshurpik.component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileReaderHelper {
    private static final String FILE_NAME = "urls.txt";
    private static BufferedReader br;
    
    static {
        try {
            br = new BufferedReader(new FileReader(FILE_NAME));
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String read() {
        try {
            String sCurrentLine;
            sCurrentLine = br.readLine();
            if (sCurrentLine == null) {
                br = new BufferedReader(new FileReader(FILE_NAME));
                sCurrentLine = br.readLine();
            }
            return sCurrentLine;                            
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
