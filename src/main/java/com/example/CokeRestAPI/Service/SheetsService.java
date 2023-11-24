package com.example.CokeRestAPI.Service;

import com.example.CokeRestAPI.Entity.SheetsData;
import com.example.CokeRestAPI.Utils.SheetsServiceUtil;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import com.google.api.services.sheets.v4.model.ValueRange; ;
import java.util.Arrays;
import java.util.List;

@Service
public class SheetsService {

    private final Sheets sheets;

    public SheetsService() throws IOException, GeneralSecurityException {
        this.sheets = SheetsServiceUtil.getSheetsService();
    }

    public void writeData(String spreadsheetId, String range, SheetsData sheetData) throws IOException {
        List<Object> rowData = Arrays.asList(
                sheetData.getName(),
                sheetData.getPhoneNumber(),
                sheetData.getProductName(),
                sheetData.getUnits(),
                sheetData.getQuantity(),
                sheetData.getPrice(),
                sheetData.getDateOrdered(),
                sheetData.getDeliveryDate()
        );

        writeToSheetNPrint(spreadsheetId, range, rowData);
    }

    private void writeToSheetNPrint(String spreadsheetId, String range, List<Object> rowData) throws IOException {
        ValueRange body = new ValueRange().setValues(Arrays.asList(rowData));
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        System.out.println("Data written to sheet: " + result);
    }

    public List<List<Object>> readData(String spreadsheetId, String range, String name, int number) throws IOException {
        List<List<Object>> allData = readFromSheet(spreadsheetId, range);

        List<List<Object>> result = new java.util.ArrayList<>();

        for (List<Object> row : allData) {
            String rowName = row.get(0).toString();
            int rowNumber = Integer.parseInt(row.get(1).toString());

            if (rowName.equals(name) && rowNumber == number) {
                result.add(row);
            }
        }

        return result;
    }

    public List<List<Object>> readFromSheet(String spreadsheetId, String range) throws IOException {
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }


    public void writeToSheet(String spreadsheetId, String range, List<Object> rowData) throws IOException {
        // Get the current values in the sheet
        ValueRange existingData = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        // Calculate the next available row number
        int nextRow = 1;
        if (existingData.getValues() != null) {
            nextRow = existingData.getValues().size() + 1;
        }

        // Set the new range to append data to the next row
        range = "A" + nextRow + ":H" + nextRow;

        // Prepare the body for the update
        ValueRange body = new ValueRange().setValues(Arrays.asList(rowData));

        // Update the spreadsheet
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("Data written to sheet: " + result);
    }


}
