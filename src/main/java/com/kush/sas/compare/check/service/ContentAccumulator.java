package com.kush.sas.compare.check.service;

import static com.kush.sas.compare.check.service.Constants.END_FOR_NO_OBS;
import static com.kush.sas.compare.check.service.Constants.END_NORMAL;
import static com.kush.sas.compare.check.service.Constants.START;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContentAccumulator {
    private static ContentAccumulator contentAccumulator;

    private final List<FileContent> compareOutputsRaw = new ArrayList<>();
    private final List<FileNoCheck> noCheck = new ArrayList<>();
    private String pathToResultFile;
    private String nameOption;
    private long dateOption = -1;

    private ContentAccumulator() {
    }

    public static ContentAccumulator getContentAccumulator() {
        if (contentAccumulator == null) {
            synchronized (ContentAccumulator.class) {
                if (contentAccumulator == null) {
                    contentAccumulator = new ContentAccumulator();
                }
            }
        }

        return contentAccumulator;
    }

    public void setNameOption(String nameOption) {
        this.nameOption = nameOption;
    }

    public void setDateOption(long dateOption) {
        this.dateOption = dateOption;
    }

    public List<FileContent> getCompareOutputsRaw() {
        return compareOutputsRaw;
    }

    public List<FileNoCheck> getNoCheck() {
        return noCheck;
    }

    String getPathToResultFile() {
        return pathToResultFile;
    }

    public void parseTheseFilesOrPath(File[] files) throws IOException {
        compareOutputsRaw.clear();
        noCheck.clear();

        for (File file : files) {
            if (file.isDirectory()) {
                for (File innerFile : file.listFiles()) {
                    if (innerFile.isFile()) {
                        parseFile(innerFile);
                    }
                }
            } else {
                parseFile(file);
            }
        }

    }

    private void parseFile(File file) throws IOException {
        String simpleFileName = file.getName();
        String[] fileNameParts = simpleFileName.split("\\.");
        pathToResultFile = file.isDirectory() ? file.getAbsolutePath() + File.separator : file.getAbsolutePath().substring(0, file
                .getAbsolutePath().length() - simpleFileName.length());

        if (!fileNameParts[fileNameParts.length - 1].equalsIgnoreCase("lis")) {
            return;
        }

        if (nameOption != null && !"".equals(nameOption) && !simpleFileName.substring(0, nameOption.length())
                                                                           .equalsIgnoreCase(nameOption)) {
            return;
        }

        long timeOfCreationInMillis = file.lastModified();

        if (dateOption != -1 && timeOfCreationInMillis < dateOption) {
            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String value;
        int start = -1, end = -1, startCounter = 0, counterFewComparesStart = 0, counterFewComparesEnd = 0;

        for (int lineCounter = 0; (value = reader.readLine()) != null; lineCounter++) {
            value = value.toUpperCase().trim();

            switch (value) {
                case START: {
                    if (start == -1) {
                        start = lineCounter;
                        ++startCounter;
                    }

                    counterFewComparesStart++;
                    break;
                }
                case END_NORMAL:
                case END_FOR_NO_OBS: {
                    end = lineCounter;
                    counterFewComparesEnd++;
                    break;
                }
            }

            stringBuilder.append(value).append(System.lineSeparator());
        }

        if (startCounter == 0) {
            return;
        }

        if (counterFewComparesStart > 1 && (counterFewComparesStart == counterFewComparesEnd)) {
            noCheck.add(new FileNoCheck(simpleFileName, timeOfCreationInMillis));
            return;
        }

        compareOutputsRaw.add(new FileContent(stringBuilder, start, end, simpleFileName, timeOfCreationInMillis));
    }
}