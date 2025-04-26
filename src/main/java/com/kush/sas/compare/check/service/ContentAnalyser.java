package com.kush.sas.compare.check.service;

import static com.kush.sas.compare.check.service.Constants.DIFF_ATTRIBUTES;
import static com.kush.sas.compare.check.service.Constants.NUMBER_OF_OBS_NOT_EQUAL;
import static com.kush.sas.compare.check.service.Constants.REGEXP_AMOUNT_OF_VARS_AND_OBS;
import static com.kush.sas.compare.check.service.Constants.REGEXP_AMOUNT_OF_VARS_AND_OBS_AND_LABEL;
import static com.kush.sas.compare.check.service.Constants.WARNING_DUPLICATES;
import static com.kush.sas.compare.check.service.Constants.createListFromString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContentAnalyser {
    private static ContentAnalyser contentAnalyser;

    private final List<FileContent> clear = new ArrayList<>();
    private final List<FileContent> dirty = new ArrayList<>();

    private int fileCounter;
    private int fileWithIssuesCounter;

    private boolean isLabelMatters;

    private ContentAnalyser() {
    }

    public static ContentAnalyser getContentAnalyser() {
        if (contentAnalyser == null) {
            synchronized (ContentAnalyser.class) {
                if (contentAnalyser == null) {
                    contentAnalyser = new ContentAnalyser();
                }
            }
        }

        return contentAnalyser;
    }

    List<FileContent> getClear() {
        return clear;
    }

    List<FileContent> getDirty() {
        return dirty;
    }

    int getFileCounter() {
        return fileCounter;
    }

    public int getFileWithIssuesCounter() {
        return fileWithIssuesCounter;
    }

    public void setLabelMatters(boolean labelMatters) {
        isLabelMatters = labelMatters;
    }

    public void analyzeContents(List<FileContent> fileContents) {
        resetResources();
        fileCounter = fileContents.size();

        for (FileContent fileContent : fileContents) {
            if (fileContent.isGood() && isCompareClear(fileContent)) {
                clear.add(fileContent);
            } else {
                dirty.add(fileContent);
            }
        }

        fileWithIssuesCounter = dirty.size();
    }

    private void resetResources() {
        fileCounter = 0;
        fileWithIssuesCounter = 0;
        dirty.clear();
        clear.clear();
    }

    private boolean isCompareClear(FileContent fileContent) {
        List<String> values = createListFromString(fileContent.getFileContentData(), System.lineSeparator());

        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            boolean rowWithCommonTitles = value.matches(REGEXP_AMOUNT_OF_VARS_AND_OBS) || value.matches(REGEXP_AMOUNT_OF_VARS_AND_OBS_AND_LABEL);

            if (rowWithCommonTitles && !criterionOnePassed(values.get(i + 1), values.get(i + 2), fileContent.isLabelColumnExists())) {
                return false;
            }

            if (value.contains(NUMBER_OF_OBS_NOT_EQUAL) && !criterionTwoPassed(value)) {
                return false;
            }

            if (value.contains(DIFF_ATTRIBUTES) || value.contains(WARNING_DUPLICATES)) {
                return false;
            }
        }

        return true;
    }

    private boolean criterionOnePassed(String firstLine, String secondLine, boolean isLabelColumnExists) {
        if (isLabelMatters && isLabelColumnExists) {
            return false;
        }

        List<String> firstLineContent = Arrays.asList(firstLine.split(" +"));
        List<String> secondLineContent = Arrays.asList(secondLine.split(" +"));

        int firstLineAmountOfVars = Integer.parseInt(firstLineContent.get(3));
        int secondLineAmountOfVars = Integer.parseInt(secondLineContent.get(3));

        int firstLineAmountOfObs = Integer.parseInt(firstLineContent.get(4));
        int secondLineAmountOfObs = Integer.parseInt(secondLineContent.get(4));

        return firstLineAmountOfVars == secondLineAmountOfVars && firstLineAmountOfObs == secondLineAmountOfObs;
    }

    private boolean criterionTwoPassed(String value) {
        String[] values = value.split(" ");
        String rawNumber = values[values.length - 1];

        return Integer.parseInt(rawNumber.substring(0, rawNumber.length() - 1)) == 0;
    }
}
