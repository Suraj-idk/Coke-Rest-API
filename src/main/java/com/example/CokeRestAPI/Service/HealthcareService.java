package com.example.CokeRestAPI.Service;

import com.example.CokeRestAPI.Entity.HealthCareData;
import com.example.CokeRestAPI.Utils.SheetsServiceUtil;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class HealthcareService {

    @Value("${spreadsheet.id}")
    private String SpreadSheetId;

    private Sheets sheets;

    public void SheetsService() throws IOException, GeneralSecurityException {
        this.sheets = SheetsServiceUtil.getSheetsService();
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
        range = "A" + nextRow + ":G" + nextRow;

        // Prepare the body for the update
        ValueRange body = new ValueRange().setValues(Arrays.asList(rowData));

        // Update the spreadsheet
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("Data written to sheet: " + result);
    }

    //completed
    public String generateRandom4Digit() {
        Random random = new Random();
        int random4DigitNumber = 1000 + random.nextInt(9999);
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
        String formattedDate = dateFormat.format(currentDate);
        return random4DigitNumber + formattedDate;
    }

    //completed
    public static List<Map<String, Object>> findRowsByNameAndPhone(
            List<List<Object>> allData, String name, String phone, List<String> columnOrder) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            String rowName = row.get(3).toString(); //Name is in the fourth column
            String rowPhone = row.get(2).toString(); // PhoneNumber is in the third column

            // Check if the current row matches the provided name and phoneNumber
            if (name.equalsIgnoreCase(rowName) && phone.equalsIgnoreCase(rowPhone)) {
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int i = 0; i < columnOrder.size(); i++) {
                    String columnName = columnOrder.get(i);
                    Object columnValue = row.get(i);
                    rowData.put(columnName, columnValue);
                }
                result.add(rowData);
            }
        }

        return result;
    }

    //completed
    public Map<String, Object> readDataByVisitId(List<List<Object>> allData, String visitId, List<String> columnOrder) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            Object firstColumnValue = row.get(0); // OrderId is in the first column

            // Check if the current row matches the provided orderId
            if (visitId.equalsIgnoreCase(firstColumnValue.toString())) {
                for (int i = 0; i < columnOrder.size(); i++) {
                    String columnName = columnOrder.get(i);
                    Object columnValue = row.get(i);
                    result.put(columnName, columnValue);
                }
                return result;
            }
        }

        return result;
    }

    public void writeData(String spreadsheetId, String range, HealthCareData healthCareData) throws IOException {
        List<Object> rowData = Arrays.asList(
                healthCareData.getVisitId(),
                healthCareData.getEmail(),
                healthCareData.getName(),
                healthCareData.getPhone(),
                healthCareData.getAge(),
                healthCareData.getGender(),
                healthCareData.getDOB()
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

    public List<List<Object>> readData(String spreadsheetId, String range, String name, String phone) throws IOException {
        List<List<Object>> allData = readFromSheet(spreadsheetId, range);

        List<List<Object>> result = new java.util.ArrayList<>();

        for (List<Object> row : allData) {
            String rowName = row.get(3).toString();
            String rowPhone = row.get(2).toString();

            if (rowName.equals(name) && Objects.equals(rowPhone, phone)) {
                result.add(row);
            }
        }

        return result;
    }

    public Map<String, Object> createJsonResponseForWrite(int responseCode, List<Object> rowData) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("VisitID", rowData.get(0));
        dataMap.put("Email", rowData.get(1));
        dataMap.put("Phone", rowData.get(2));
        dataMap.put("Name", rowData.get(3));
        dataMap.put("Age", rowData.get(4));
        dataMap.put("Gender", rowData.get(5));
        dataMap.put("DOB", rowData.get(6));

        Map<String, Object> jsonResponse = new LinkedHashMap<>();
        jsonResponse.put("responseCode", responseCode);
        jsonResponse.put("data", dataMap);

        return jsonResponse;
    }
}