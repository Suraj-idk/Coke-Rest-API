package com.example.CokeRestAPI.Service;

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
public class SochService {

    @Value("${spreadsheet.id}")
    private String SpreadSheetId;

    private final Sheets sheets;

    public SochService() throws IOException, GeneralSecurityException {
        this.sheets = SheetsServiceUtil.getSheetsService();
    }

    public List<List<Object>> readFromSheet(String spreadsheetId, String range) throws IOException {
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    public List<List<Object>> readFromProductSheet(String spreadsheetId, String range) throws IOException {
        ValueRange response = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }


    public void writeToSheet(String spreadsheetId, String sheetName, List<Object> rowData) throws IOException {
        // Get the current values in the sheet
        String range=sheetName+"!A:E";
        ValueRange existingData = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        // Calculate the next available row number
        int nextRow = 1;
        if (existingData.getValues() != null) {
            nextRow = existingData.getValues().size() + 1;
        }
        // Set the new range to append data to the next row
        range =sheetName+ "!A" + nextRow + ":E" + nextRow;
        // Prepare the body for the update
        ValueRange body = new ValueRange().setValues(Arrays.asList(rowData));
        // Update the spreadsheet
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        System.out.println("Data written to sheet: " + result);
    }

    public void writeInProductSheet(String spreadsheetId, String sheetName, List<Object> rowData) throws IOException {
        // Get the current values in the sheet
        String range=sheetName+"!A:H";
        ValueRange existingData = sheets.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        // Calculate the next available row number
        int nextRow = 1;
        if (existingData.getValues() != null) {
            nextRow = existingData.getValues().size() + 1;
        }
        // Set the new range to append data to the next row
        range =sheetName+ "!A" + nextRow + ":H" + nextRow;
        // Prepare the body for the update
        ValueRange body = new ValueRange().setValues(Arrays.asList(rowData));
        // Update the spreadsheet
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        System.out.println("Data written to Product sheet: " + result);
    }

    public List<Map<String, Object>> findRowsByContact(
            List<List<Object>> allData, String phoneNumber, List<String> columnOrder) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            String rowPhoneNumber = row.get(2).toString(); // PhoneNumber is in the third column

            // Check if the current row matches the provided name and phoneNumber
            if (phoneNumber.equalsIgnoreCase(rowPhoneNumber)) {
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
//    public Map<String, Object> findRowByOrderId(List<List<Object>> allData, String orderId, List<String> columnOrder) {
//        Map<String, Object> result = new LinkedHashMap<>();
//
//        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
//            Object firstColumnValue = row.get(0); // OrderId is in the first column
//
//            // Check if the current row matches the provided orderId
//            if (orderId.equalsIgnoreCase(firstColumnValue.toString())) {
//                for (int i = 0; i < columnOrder.size(); i++) {
//                    String columnName = columnOrder.get(i);
//                    Object columnValue = row.get(i);
//                    result.put(columnName, columnValue);
//                }
//                return result;
//            }
//        }
//
//        return result;
//    }
    public Map<String, Object> createJsonResponseForWrite(int responseCode, List<Object> rowData) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("OrderId", rowData.get(0));
        dataMap.put("Name", rowData.get(1));
        dataMap.put("PhoneNumber", rowData.get(2));
        dataMap.put("email", rowData.get(3));
        dataMap.put("address", rowData.get(4));

        Map<String, Object> jsonResponse = new LinkedHashMap<>();
        jsonResponse.put("responseCode", responseCode);
        jsonResponse.put("data", dataMap);

        return jsonResponse;
    }

    public String generateRandom4Digit() {
        Random random = new Random();
        int random4DigitNumber = 1000 + random.nextInt(9000);
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String formattedDate = dateFormat.format(currentDate);
        return random4DigitNumber + formattedDate;
    }

    public String generateRandom4DigitForOrderId() {
        Random random = new Random();
        int random4DigitNumber = 1000 + random.nextInt(9000);
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
        String formattedDate = dateFormat.format(currentDate);
        return random4DigitNumber + formattedDate;
    }

    public Map<String, Object> createJsonResponseForWriteInProductSheet(int responseCode, List<Object> rowData) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("customerId", rowData.get(0));
        dataMap.put("orderId", rowData.get(1));
        dataMap.put("productName", rowData.get(2));
        dataMap.put("price", rowData.get(3));
        dataMap.put("quantity", rowData.get(4));
        dataMap.put("orderStatus", rowData.get(5));
        dataMap.put("returnOrder", rowData.get(6));
        dataMap.put("cancelOrder", rowData.get(7));

        Map<String, Object> jsonResponse = new LinkedHashMap<>();
        jsonResponse.put("responseCode", responseCode);
        jsonResponse.put("data", dataMap);

        return jsonResponse;
    }

    public Map<String, Object> findRowByOrderIdForProductSheet(List<List<Object>> allData, String orderId, List<String> columnOrder) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            Object firstColumnValue = row.get(1); // OrderId is in the second column

            if (orderId.equalsIgnoreCase(firstColumnValue.toString())
                    && !"delivered".equalsIgnoreCase(row.get(columnOrder.indexOf("orderStatus")).toString())
                    && !"True".equals(row.get(columnOrder.indexOf("cancelOrder")))
                    && !"True".equals(row.get(columnOrder.indexOf("returnOrder")))) {

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

    public List<Map<String, Object>> findRowsByCustomerIdForProductSheet(List<List<Object>> allData, String customerId, List<String> columnOrder) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            Object firstColumnValue = row.get(0); // customerId is in the first column

            if (customerId.equalsIgnoreCase(firstColumnValue.toString())
                    && !"delivered".equalsIgnoreCase(row.get(columnOrder.indexOf("orderStatus")).toString())
                    && !"True".equals(row.get(columnOrder.indexOf("cancelOrder")))
                    && !"True".equals(row.get(columnOrder.indexOf("returnOrder")))) {

                Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < columnOrder.size(); i++) {
                    String columnName = columnOrder.get(i);
                    Object columnValue = row.get(i);
                    rowMap.put(columnName, columnValue);
                }
                result.add(rowMap);
            }
        }

        return result;
    }

    public void setCancelOrderTrue(List<List<Object>> allData, String orderId, List<String> columnOrder) {
        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            Object orderIdValue = row.get(columnOrder.indexOf("orderId"));
            Object orderStatusValue = row.get(columnOrder.indexOf("orderStatus"));
            Object returnOrderValue = row.get(columnOrder.indexOf("returnOrder"));

            if (orderId.equalsIgnoreCase(orderIdValue.toString())
                    && ("preparing for dispatch".equalsIgnoreCase(orderStatusValue.toString())
                    || "dispatched".equalsIgnoreCase(orderStatusValue.toString()))
                    && !Boolean.TRUE.equals(returnOrderValue)) {

                int cancelOrderIndex = columnOrder.indexOf("cancelOrder");
                row.set(cancelOrderIndex, true);
            }
        }
    }



}