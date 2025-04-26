package com.kush.sas.compare.check.service;

import static com.kush.sas.compare.check.service.Constants.REGEXP_AMOUNT_OF_VARS_AND_OBS;
import static com.kush.sas.compare.check.service.Constants.REGEXP_AMOUNT_OF_VARS_AND_OBS_AND_LABEL;
import static com.kush.sas.compare.check.service.Constants.createListFromString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class FileContent {
    private final StringBuilder fileContentRaw;
    private final String simpleFileName;
    private final Date dateOfCreation;

    private int startLine;
    private int endLine;
    private String fileContentData;
    private boolean isGood;
    private boolean areAmountOfObsZero;
    private boolean isLabelColumnExists;


    FileContent(StringBuilder fileContentRaw, int startLine, int endLine, String simpleFileName, long timeInMillis) throws IOException {
        this.fileContentRaw = fileContentRaw;
        this.simpleFileName = simpleFileName;
        dateOfCreation = new Date(timeInMillis);

        setInnerParameters(fileContentRaw.toString());

        if (endLine == -1 && !areAmountOfObsZero) {
            return;
        }

        this.startLine = startLine;
        this.endLine = endLine;
        isGood = true;

        setFileContentValue();
    }

    private void setInnerParameters(String value) {
        List<String> values = createListFromString(value, System.lineSeparator());

        for (int i = 0; i < values.size(); i++) {
            String lineFromFile = values.get(i);

            if (lineFromFile.matches(REGEXP_AMOUNT_OF_VARS_AND_OBS_AND_LABEL) || lineFromFile.matches(REGEXP_AMOUNT_OF_VARS_AND_OBS)) {
                if (lineFromFile.contains("LABEL")) {
                    isLabelColumnExists = true;
                }

                if (areAmountOfObsZero(values.get(i + 1), values.get(i + 2))) {
                    areAmountOfObsZero = true;
                }

                return;
            }
        }
    }

    private boolean areAmountOfObsZero(String firstLine, String secondLine) {
        if (firstLine.length() > secondLine.length()) {
            firstLine = firstLine.substring(0, secondLine.length());
        } else if (secondLine.length() > firstLine.length()) {
            secondLine = secondLine.substring(0, firstLine.length());
        }

        List<String> firstLineContent = Arrays.asList(firstLine.split(" +"));
        List<String> secondLineContent = Arrays.asList(secondLine.split(" +"));

        if (firstLineContent.size() > secondLineContent.size()) {
            firstLineContent.remove(firstLineContent.size() - 1);
        } else if (firstLineContent.size() < secondLineContent.size()) {
            secondLineContent.remove(secondLineContent.size() - 1);
        }

        int firstLineAmountOfObs = Integer.parseInt(firstLineContent.get(firstLineContent.size() - 1));
        int secondLineAmountOfObs = Integer.parseInt(secondLineContent.get(secondLineContent.size() - 1));

        return firstLineAmountOfObs == 0 && secondLineAmountOfObs == 0;
    }

    private void setFileContentValue() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(fileContentRaw.toString()));
        StringBuilder stringBuilder = new StringBuilder();
        String value;

        for (int lineCounter = 0; (value = bufferedReader.readLine()) != null; lineCounter++) {
            if (lineCounter >= startLine && lineCounter <= endLine) {
                stringBuilder.append(value).append(System.lineSeparator());
            }
        }

        fileContentData = stringBuilder.substring(0, stringBuilder.length() - System.lineSeparator().length());
    }

    @Override
    public String toString() {
        return String.format("Start line: %d%nEnd line: %d%nDate of creation: %s%nFile name: %s%n%nContents:%n%n%s",
                             startLine, endLine, new SimpleDateFormat("dd MMM yyyy HH:mm").format(dateOfCreation), simpleFileName, fileContentData);
    }

    String getFileContentData() {
        return fileContentData;
    }

    String getSimpleFileName() {
        return simpleFileName;
    }

    Date getDateOfCreation() {
        return dateOfCreation;
    }

    boolean areAmountOfObsZero() {
        return areAmountOfObsZero;
    }

    boolean isGood() {
        return isGood;
    }

    boolean isLabelColumnExists() {
        return isLabelColumnExists;
    }
}
