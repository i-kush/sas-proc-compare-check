package com.kush.sas.compare.check.view;

import static com.kush.sas.compare.check.view.Components.ADDITIONAL_OPTIONS_BUTTON;
import static com.kush.sas.compare.check.view.Components.CLEAR_BUTTON;
import static com.kush.sas.compare.check.view.Components.CURRENT_FILE_LABEL;
import static com.kush.sas.compare.check.view.Components.DATE_PATTERN;
import static com.kush.sas.compare.check.view.Components.DATE_PICKER;
import static com.kush.sas.compare.check.view.Components.DATE_PICKER_LABEL;
import static com.kush.sas.compare.check.view.Components.FILE_CHOOSER;
import static com.kush.sas.compare.check.view.Components.FILE_OR_DIRECTORY;
import static com.kush.sas.compare.check.view.Components.FIND_ISSUES_BUTTON;
import static com.kush.sas.compare.check.view.Components.FRAME;
import static com.kush.sas.compare.check.view.Components.LABEL_MATTERS_CHECKBOX;
import static com.kush.sas.compare.check.view.Components.OPEN_FILE_WITH_ISSUES_BUTTON;
import static com.kush.sas.compare.check.view.Components.PREFIX_FOR_FILE_NAME;
import static com.kush.sas.compare.check.view.Components.PREFIX_FOR_FILE_NAME_LABEL;
import static com.kush.sas.compare.check.view.Components.SELECT_FILE_OR_DIRECTORY_BUTTON;

import com.kush.sas.compare.check.service.ContentAccumulator;
import com.kush.sas.compare.check.service.ContentAnalyser;
import com.kush.sas.compare.check.service.ExcelWriter;

import javax.activation.UnsupportedDataTypeException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class Actions {
    private static final String ERROR = "Error";
    private static final String WARNING = "Warning";
    private static final String INFO = "Info";
    private static final String MESSAGE_WRONG_FILES = "There are no files to check." + System.lineSeparator() + "Please, select file or directory with appropriate options.";
    private static final String MESSAGE_ALL_CLEAR = "Great, all files are clean.";
    private static final String NOT_ALL_FILES_WERE_CHECKED = "Not all files were checked.";
    private static final String MESSAGE_PLEASE_CLOSE_FILE = "Please, close result file before finding issues.";
    private static final String MESSAGE_UNKNOWN_ERROR = "Sorry, unknown error.";

    private static boolean isHidden = true;

    private final String currentChooseType = (String) FILE_OR_DIRECTORY[1];
    private final ContentAccumulator contentAccumulator = ContentAccumulator.getContentAccumulator();
    private final ContentAnalyser contentAnalyser = ContentAnalyser.getContentAnalyser();
    private final ExcelWriter excelWriter = ExcelWriter.getExcelWriter();

    private File[] files;

    void setActions() {
        SELECT_FILE_OR_DIRECTORY_BUTTON.addActionListener(event -> selectFileOrDirectoryButtonAction());
        FIND_ISSUES_BUTTON.addActionListener(event -> findIssuesButtonAction());
        OPEN_FILE_WITH_ISSUES_BUTTON.addActionListener(event -> openFileWithIssuesButton());
        CLEAR_BUTTON.addActionListener(event -> clearButton());
        ADDITIONAL_OPTIONS_BUTTON.addActionListener(event -> additionalOptionsButton());

        DATE_PICKER.addActionListener(event -> datePickerAction());
        PREFIX_FOR_FILE_NAME.addActionListener(event -> prefixForFileNameAction());
    }

    private void findIssuesButtonAction() {
        try {
            contentAccumulator.setNameOption(PREFIX_FOR_FILE_NAME.getText());
            contentAccumulator.setDateOption(getValueFromDatePicker());
            contentAccumulator.parseTheseFilesOrPath(files);

            contentAnalyser.setLabelMatters(LABEL_MATTERS_CHECKBOX.isSelected());
            contentAnalyser.analyzeContents(contentAccumulator.getCompareOutputsRaw());

            excelWriter.setContentAccumulator(contentAccumulator);
            excelWriter.setContentAnalyser(contentAnalyser);
            excelWriter.saveResult();

            int issues = contentAnalyser.getFileWithIssuesCounter();

            boolean areAllFilesChecked = (contentAccumulator.getNoCheck().isEmpty());
            if (issues != 0) {
                JOptionPane.showMessageDialog(FRAME, issues + " files with issues.", WARNING, JOptionPane.WARNING_MESSAGE);
            } else {
                if (areAllFilesChecked) {
                    JOptionPane.showMessageDialog(FRAME, MESSAGE_ALL_CLEAR, INFO, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(FRAME, NOT_ALL_FILES_WERE_CHECKED, WARNING, JOptionPane.WARNING_MESSAGE);
                }
            }

            OPEN_FILE_WITH_ISSUES_BUTTON.setEnabled(true);
            openFileWithIssuesButton();
        } catch (UnsupportedDataTypeException e1) {
            JOptionPane.showMessageDialog(FRAME, MESSAGE_WRONG_FILES, ERROR, JOptionPane.ERROR_MESSAGE);
        } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(FRAME, MESSAGE_PLEASE_CLOSE_FILE, ERROR, JOptionPane.ERROR_MESSAGE);
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(FRAME, e1.getMessage(), MESSAGE_UNKNOWN_ERROR, JOptionPane.ERROR_MESSAGE);
        }

        refreshFrame();
    }

    private void selectFileOrDirectoryButtonAction() {
        int resultOfAction = FILE_CHOOSER.showDialog(FRAME, "Choose " + currentChooseType);

        if (resultOfAction == JFileChooser.APPROVE_OPTION) {
            files = FILE_CHOOSER.getSelectedFiles();
            FIND_ISSUES_BUTTON.setEnabled(true);

            CURRENT_FILE_LABEL.setText(getPath(files[0]));
            OPEN_FILE_WITH_ISSUES_BUTTON.setEnabled(false);
        }

        refreshFrame();
    }

    private String getPath(File file) {
        return file.isDirectory() ? file.getAbsolutePath() + "\\" : file.getAbsolutePath().substring(0, file.getAbsolutePath()
                                                                                                            .length() - file.getName()
                                                                                                                            .length());
    }

    private void openFileWithIssuesButton() {
        try {
            Desktop.getDesktop().open(excelWriter.getResultFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void clearButton() {
        PREFIX_FOR_FILE_NAME.setText("");
        DATE_PICKER.getJFormattedTextField().setText("");
        LABEL_MATTERS_CHECKBOX.setSelected(false);
        OPEN_FILE_WITH_ISSUES_BUTTON.setEnabled(false);
        refreshFrame();
    }

    private void additionalOptionsButton() {
        if (isHidden) {
            ADDITIONAL_OPTIONS_BUTTON.setText("Options <<");
            FRAME.add(PREFIX_FOR_FILE_NAME, View.getGridBagConstraint(0, 4, 1));
            FRAME.add(DATE_PICKER, View.getGridBagConstraint(1, 4, 1));
            FRAME.add(LABEL_MATTERS_CHECKBOX, View.getGridBagConstraint(2, 4, 1));
            FRAME.add(CLEAR_BUTTON, View.getGridBagConstraint(0, 5, 1));

            PREFIX_FOR_FILE_NAME_LABEL.setText("File name prefix:");
            DATE_PICKER_LABEL.setText("Starting date:");

            isHidden = false;
        } else {
            ADDITIONAL_OPTIONS_BUTTON.setText("Options >>");
            FRAME.remove(PREFIX_FOR_FILE_NAME);
            FRAME.remove(DATE_PICKER);
            FRAME.remove(LABEL_MATTERS_CHECKBOX);
            FRAME.remove(CLEAR_BUTTON);

            PREFIX_FOR_FILE_NAME_LABEL.setText("");
            DATE_PICKER_LABEL.setText("");

            isHidden = true;
        }

        refreshFrame();
    }

    private long getValueFromDatePicker() {
        String valueFromDatePicker = DATE_PICKER.getJFormattedTextField().getText();

        if ("".equals(valueFromDatePicker) || valueFromDatePicker == null) {
            return -1;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
        try {
            Date date = simpleDateFormat.parse(DATE_PICKER.getJFormattedTextField().getText());
            return date.getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    private void datePickerAction() {
        OPEN_FILE_WITH_ISSUES_BUTTON.setEnabled(false);
    }

    private void prefixForFileNameAction() {
        OPEN_FILE_WITH_ISSUES_BUTTON.setEnabled(false);
    }

    private void refreshFrame() {
        FRAME.revalidate();
        FRAME.repaint();
    }
}
