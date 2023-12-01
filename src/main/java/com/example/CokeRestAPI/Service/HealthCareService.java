package com.example.CokeRestAPI.Service;

import com.example.CokeRestAPI.Entity.HealthCareData;
import com.example.CokeRestAPI.Entity.SheetsData;
import com.example.CokeRestAPI.Utils.SheetsServiceUtil;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class HealthCareService {

    @Value("${spreadsheet.id}")
    private String SpreadSheetId;

    private final Sheets sheets;

    public HealthCareService() throws IOException, GeneralSecurityException {
        this.sheets = SheetsServiceUtil.getSheetsService();
    }

    public List<List<Object>> readFromSheet(String spreadsheetId, String range) throws IOException {
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    public void writeToSheet(String spreadsheetId, String sheetName, List<Object> rowData) throws IOException {
        // Get the current values in the sheet
        String range=sheetName+"!A:G";
        ValueRange existingData = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        // Calculate the next available row number
        int nextRow = 1;
        if (existingData.getValues() != null) {
            nextRow = existingData.getValues().size() + 1;
        }

        // Set the new range to append data to the next row
        range =sheetName+ "!A" + nextRow + ":G" + nextRow;

        // Prepare the body for the update
        ValueRange body = new ValueRange().setValues(Arrays.asList(rowData));

        // Update the spreadsheet
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("Data written to sheet: " + result);
    }

    public String generateRandom4Digit() {
        Random random = new Random();
        int random4DigitNumber = 1000 + random.nextInt(9999);

        char randomChar1 = generateRandomUppercaseLetter();
        char randomChar2 = generateRandomUppercaseLetter();

        return String.valueOf(random4DigitNumber) + randomChar1 + randomChar2;
    }

    private static char generateRandomUppercaseLetter() {
        Random random = new Random();
        char randomChar = (char) (random.nextInt(26) + 'A'); // Uppercase letters
        return randomChar;
    }

    public List<Map<String, Object>> findRowsByEmailAndPhone(
            List<List<Object>> allData, String email, String phone, List<String> columnOrder) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            String rowEmail = row.get(1).toString(); //Name is in the fourth column
            String rowPhone = row.get(2).toString(); // PhoneNumber is in the third column

            // Check if the current row matches the provided name and phoneNumber
            if (email.equalsIgnoreCase(rowEmail) && phone.equalsIgnoreCase(rowPhone)) {
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

    public Map<String, Object> findRowByVisitId(List<List<Object>> allData, String visitId, List<String> columnOrder) {
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

    public List<List<Object>> readData(String spreadsheetId, String range, String name, int number) throws IOException {
        List<List<Object>> allData = readFromSheet(spreadsheetId, range);

        List<List<Object>> result = new java.util.ArrayList<>();

        for (List<Object> row : allData) {
            String rowName = row.get(3).toString();
            int rowNumber = Integer.parseInt(row.get(2).toString());

            if (rowName.equals(name) && rowNumber == number) {
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
    public void writeToDoctorAppointmentSheet(String spreadsheetId, String sheetName, List<Object> rowData) throws IOException {
        // Get the current values in the sheet
        String range=sheetName+"!A:D";
        ValueRange existingData = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        // Calculate the next available row number
        int nextRow = 1;
        if (existingData.getValues() != null) {
            nextRow = existingData.getValues().size() + 1;
        }

        // Set the new range to append data to the next row
        range =sheetName+ "!A" + nextRow + ":D" + nextRow;

        // Prepare the body for the update
        ValueRange body = new ValueRange().setValues(Arrays.asList(rowData));

        // Update the spreadsheet
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("Data written to sheet: " + result);
    }
    public Map<String, Object> createJsonResponseForDrAppointment(int responseCode, List<Object> rowData) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("VisitID", rowData.get(0));
        dataMap.put("doctorName", rowData.get(1));
        dataMap.put("doctorType", rowData.get(2));
        dataMap.put("visitDate", rowData.get(3));

        Map<String, Object> jsonResponse = new LinkedHashMap<>();
        jsonResponse.put("responseCode", responseCode);
        jsonResponse.put("data", dataMap);

        return jsonResponse;
    }

    public void updateRowData(String spreadsheetId, String range, int rowIndex, List<Object> rowData) throws IOException {
        ValueRange body = new ValueRange().setValues(Collections.singletonList(rowData));
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range + rowIndex, body)
                .setValueInputOption("RAW")
                .execute();
    }
    public void writePrescriptionUrl(String spreadsheetId, String sheetName, String visitId, List<Object> rowData) throws IOException {
        String range = sheetName + "!H:H"; //PrescriptionUrl is in the 8th column (H)
        int rowIndex = findRowIndexByVisitId(spreadsheetId, sheetName, visitId);

        if (rowIndex != -1) {
            updateRowData(spreadsheetId, range, rowIndex, rowData);
        } else {
            throw new RuntimeException("VisitId not found");
        }
    }

    public int findRowIndexByVisitId(String spreadsheetId, String sheetName, String visitId) throws IOException {
        String range = sheetName + "!A:A"; // Assuming VisitID is in the 1st column (A)
        List<List<Object>> allData = readFromSheet(spreadsheetId, range);

        for (int i = 0; i < allData.size(); i++) {
            List<Object> row = allData.get(i);
            if (row.size() > 0 && row.get(0).toString().equals(visitId)) {
                return i + 1; // Adding 1 because sheet row index starts from 1
            }
        }

        return -1; // VisitId not found
    }

    public boolean visitIdExists(List<List<Object>> allData, String visitId) {
        for (List<Object> row : allData) {
            if (row.size() > 0 && row.get(0).toString().equals(visitId)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> createJsonResponseForWritePrescription(int statusCode, List<Object> rowData) {
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("status", statusCode);
        jsonResponse.put("message", "PrescriptionUrl successfully added");
        jsonResponse.put("data", rowData);
        return jsonResponse;
    }
}