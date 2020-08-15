package com.company;

import java.awt.*;
import java.io.File;

public class PickAFile {

    public static File selectFile()
    {
        FileDialog dialog = new FileDialog((Frame)null, "Select File to Open");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);
        String file = dialog.getFile();
        System.out.println(ThreadColor.ANSI_BLUE + file + " chosen." );

        String path = dialog.getDirectory() + dialog.getFile();
        File filePath = new File(path);
        //System.out.println(filePath +" is this");
        return filePath;
    }

}