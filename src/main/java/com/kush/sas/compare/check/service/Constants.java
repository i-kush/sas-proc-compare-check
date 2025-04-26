package com.kush.sas.compare.check.service;

import java.util.ArrayList;
import java.util.List;

final class Constants {
    static final String PASSED = "Passed";
    static final String FAILED = "Failed";
    static final String WAS_NOT_CHECKED = "No check";

    static final String START = "THE COMPARE PROCEDURE";
    static final String END_NORMAL = "NOTE: NO UNEQUAL VALUES WERE FOUND. ALL VALUES COMPARED ARE EXACTLY EQUAL.";
    static final String END_FOR_NO_OBS = "NUMBER OF OBSERVATIONS WITH ALL COMPARED VARIABLES EQUAL: 0.";

    static final String REGEXP_AMOUNT_OF_VARS_AND_OBS = "DATASET +CREATED +MODIFIED +NVAR +NOBS";
    static final String REGEXP_AMOUNT_OF_VARS_AND_OBS_AND_LABEL = REGEXP_AMOUNT_OF_VARS_AND_OBS + " +LABEL";
    static final String NUMBER_OF_OBS_NOT_EQUAL = "NUMBER OF OBSERVATIONS WITH SOME COMPARED VARIABLES UNEQUAL:";
    static final String DIFF_ATTRIBUTES = "LISTING OF COMMON VARIABLES WITH DIFFERING ATTRIBUTES";
    static final String WARNING_DUPLICATES = "WARNING: THE DATA SET WORK.DATA1 CONTAINS A DUPLICATE OBSERVATION AT OBSERVATION";

    private Constants() {
        throw new UnsupportedOperationException();
    }

    static List<String> createListFromString(String content, String splitChar) {
        List<String> values = new ArrayList<>();
        for (String string : content.split(splitChar)) {
            if (!string.isEmpty()) {
                values.add(string);
            }
        }

        return values;
    }
}