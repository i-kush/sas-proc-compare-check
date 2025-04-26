package com.kush.sas.compare.check.service;

import java.util.Date;

class FileNoCheck {

    private String simpleFileName;
    private Date dateOfCreation;

    FileNoCheck(String simpleFileName, long dateOfCreation) {
        this.simpleFileName = simpleFileName;
        this.dateOfCreation = new Date(dateOfCreation);
    }

    String getSimpleFileName() {
        return simpleFileName;
    }

    Date getDateOfCreation() {
        return dateOfCreation;
    }
}
