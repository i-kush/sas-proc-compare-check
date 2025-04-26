package com.kush.sas.compare.check.service;

import static com.kush.sas.compare.check.service.Constants.FAILED;
import static com.kush.sas.compare.check.service.Constants.PASSED;
import static com.kush.sas.compare.check.view.Components.DATE_PATTERN;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.activation.UnsupportedDataTypeException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcelWriter {
    private static final String FILE_WITH_ISSUES_NAME = "Compare procedure output check.xls";

    private static ExcelWriter excelWriter;
    private ContentAnalyser contentAnalyser;
    private ContentAccumulator contentAccumulator;

    private int rowNumber;
    private int columnsAmount;

    private File resultFile;

    private Workbook excelWorkbook;
    private Sheet excelSheet;

    private ExcelWriter() {
    }

    public static ExcelWriter getExcelWriter() {
        if (excelWriter == null) {
            synchronized (ExcelWriter.class) {
                if (excelWriter == null) {
                    excelWriter = new ExcelWriter();
                }
            }
        }

        return excelWriter;
    }

    public File getResultFile() {
        return resultFile;
    }

    public void setContentAnalyser(ContentAnalyser contentAnalyser) {
        this.contentAnalyser = contentAnalyser;
    }

    public void setContentAccumulator(ContentAccumulator contentAccumulator) {
        this.contentAccumulator = contentAccumulator;
    }

    public void saveResult() throws IOException {
        if (contentAnalyser.getFileCounter() + contentAccumulator.getNoCheck().size() == 0) {
            throw new UnsupportedDataTypeException();
        }

        excelWorkbook = new HSSFWorkbook();
        excelSheet = excelWorkbook.createSheet("Result");

        setMetaInfoAndTitles();

        writeListWithChecksInFile(contentAnalyser.getClear(), PASSED);
        writeListWithChecksInFile(contentAnalyser.getDirty(), FAILED);
        writeListWithNoCheckInFile(contentAccumulator.getNoCheck());

        for (int i = 0; i < columnsAmount - 2; i++) {
            excelSheet.autoSizeColumn(i);
        }

        resultFile = new File(contentAccumulator.getPathToResultFile() + FILE_WITH_ISSUES_NAME);

        try (FileOutputStream fileOutputStream = new FileOutputStream(resultFile)) {
            excelWorkbook.write(fileOutputStream);
        }

        excelWorkbook.close();
    }

    private void setMetaInfoAndTitles() {
        String[] metaInfo = {"Date of the check: " + getFormattedDate(System.currentTimeMillis()),
                "Path: " + contentAccumulator.getPathToResultFile(),
                "All files: " + (contentAnalyser.getFileCounter() + contentAccumulator.getNoCheck().size()),
                "Checked files: " + contentAnalyser.getFileCounter(),
                "Clear files: " + (contentAnalyser.getFileCounter() - contentAnalyser.getFileWithIssuesCounter()),
                "Files with issues: " + contentAnalyser.getFileWithIssuesCounter()};

        rowNumber = metaInfo.length;

        for (int i = 0; i < metaInfo.length; i++) {
            excelSheet.createRow(i).createCell(0).setCellValue(metaInfo[i]);
            excelSheet.addMergedRegion(new CellRangeAddress(i, i, 0, 5));
        }

        Row row = excelSheet.createRow(++rowNumber);
        String[] titles = {"Program name", "Date/Time of run", "Status", "No obs?"};
        columnsAmount = titles.length;

        excelSheet.setAutoFilter(new CellRangeAddress(rowNumber, rowNumber, 0, columnsAmount - 1));

        Font font = excelWorkbook.createFont();
        font.setBold(true);
        CellStyle cellStyle = excelWorkbook.createCellStyle();
        cellStyle.setFont(font);

        Cell cell;
        for (int i = 0; i < columnsAmount; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(titles[i]);

            if (i == columnsAmount - 2) {
                excelSheet.setColumnWidth(i, 2_400);
            } else if (i == columnsAmount - 1) {
                excelSheet.setColumnWidth(i, 2_700);
            }
        }
    }

    private String getFormattedDate(long dateInMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN + ", HH:mm:ss", Locale.ENGLISH);
        return simpleDateFormat.format(new Date(dateInMillis));
    }

    private void writeListWithChecksInFile(List<FileContent> outputs, String status) {
        DataFormat format = excelWorkbook.createDataFormat();
        CellStyle dateStyle = excelWorkbook.createCellStyle();
        dateStyle.setAlignment(HorizontalAlignment.LEFT);
        dateStyle.setDataFormat(format.getFormat(DATE_PATTERN + ", HH:mm:ss"));

        for (FileContent fileContent : outputs) {
            Row row = excelSheet.createRow(++rowNumber);

            row.createCell(0).setCellValue(fileContent.getSimpleFileName());
            createDateCell(row, fileContent.getDateOfCreation(), dateStyle);
            row.createCell(2).setCellValue(status);
            if (PASSED.equals(status) && fileContent.areAmountOfObsZero()) {
                row.createCell(3).setCellValue("Yes");
            }
        }
    }

    private void createDateCell(Row row, Date date, CellStyle dateStyle) {
        Cell dateCell = row.createCell(1);
        dateCell.setCellStyle(dateStyle);
        dateCell.setCellValue(date);
    }

    private void writeListWithNoCheckInFile(List<FileNoCheck> outputs) {
        DataFormat format = excelWorkbook.createDataFormat();
        CellStyle dateStyle = excelWorkbook.createCellStyle();
        dateStyle.setAlignment(HorizontalAlignment.LEFT);
        dateStyle.setDataFormat(format.getFormat(DATE_PATTERN + ", HH:mm:ss"));

        for (FileNoCheck fileNoCheck : outputs) {
            Row row = excelSheet.createRow(++rowNumber);

            row.createCell(0).setCellValue(fileNoCheck.getSimpleFileName());
            createDateCell(row, fileNoCheck.getDateOfCreation(), dateStyle);
            row.createCell(2).setCellValue(Constants.WAS_NOT_CHECKED);
        }
    }
}
