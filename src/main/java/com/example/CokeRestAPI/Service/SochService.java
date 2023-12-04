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

    public void updateSheet(String spreadsheetId, String range, List<List<Object>> updatedData) throws IOException {
        ValueRange body = new ValueRange().setValues(updatedData);
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        System.out.println("Data updated in Product sheet: " + result);
    }

    @Scheduled(cron = "0 0/15 * * * *")
    public void updateOrderStatusForSoch() {
        System.out.println("Cron Start");
        try {
            // Fetch data directly from Google Sheets
            ValueRange response = sheets.spreadsheets().values().get(SpreadSheetId, "Soch_Products!A2:H").execute();
            List<List<Object>> values = response.getValues();

            // Skip if values is null or empty
            if (values == null || values.isEmpty()) {
                return;
            }

            // Update order status for each row
            for (List<Object> row : values) {
                // Extract order data
                String orderId = (String) row.get(0);
                String orderStatus = (String) row.get(5); //Order Status is in the 6th column (column F)

                // Update order status based on logic
                String newOrderStatus = getNextOrderStatus(orderStatus);

                // Update the sheet with the new order status
                List<List<Object>> updateValues = List.of(List.of(newOrderStatus));
                ValueRange body = new ValueRange().setValues(updateValues);

                sheets.spreadsheets().values()
                        .update(SpreadSheetId, "Soch_Products!F" + (values.indexOf(row) + 2), body)
                        .setValueInputOption("RAW")
                        .execute();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Cron End");
    }

    private String getNextOrderStatus(String currentOrderStatus) {
        if ("Preparing for Dispatch".equalsIgnoreCase(currentOrderStatus)) {
            return "Dispatched";
        } else if ("Dispatched".equalsIgnoreCase(currentOrderStatus)) {
            return "In Transit";
        } else if ("In Transit".equalsIgnoreCase(currentOrderStatus)) {
            return "Shipped";
        } else if ("Shipped".equalsIgnoreCase(currentOrderStatus)) {
            return "Out for Delivery";
        } else if ("Out for Delivery".equalsIgnoreCase(currentOrderStatus)) {
            return "Delivered";
        } else {
            return currentOrderStatus; // No change if unknown status
        }
    }

//-------------------------------------------------------------------------------------Yet to complete---------------------------------------------------------------------------------------


    private final Map<String, Integer> customerPointsMap = new HashMap<>();

    //    @Scheduled(cron = "0 */2 * * * *") // Run every 15 minutes
    public void updatePointsInSheetB() {
        try {
            // Read data from Soch_Products
            List<List<Object>> sheetAData = readFromSheet(SpreadSheetId, "Soch_Products!A:H");

            // Process data and calculate points
            for (int i = 1; i < sheetAData.size(); i++) {
                List<Object> row = sheetAData.get(i);
                String customerId = (String) row.get(0);
                String orderStatus = (String) row.get(5);
                boolean isReturned = (Boolean) row.get(6); // to be changed to true.equal
                boolean isCancelled = (Boolean) row.get(7); // to be changed to true.equal
                int price = (int) row.get(3);

                if ("Delivered".equalsIgnoreCase(orderStatus) && !isReturned && !isCancelled) {
                    int pointsEarned = calculatePoints(price);
                    updateCustomerPoints(customerId, pointsEarned);
                }
            }

            // Update pointsSheet with the calculated points
            updatePointsSheet();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception as needed
        }
    }

    private int calculatePoints(int price) {
        // Customize this method based on your point calculation logic
        if (price == 499) return 5;
        else if (price == 599) return 6;
        else if (price == 999) return 10;
        else if (price == 1549) return 15;
        else return 0;  // No points for other prices
    }

    private void updateCustomerPoints(String customerId, int pointsEarned) {
        // Check if the customerId already exists in the map
        if (customerPointsMap.containsKey(customerId)) {
            // If yes, add the new points to the existing points
            int currentPoints = customerPointsMap.get(customerId);
            customerPointsMap.put(customerId, currentPoints + pointsEarned);
        } else {
            // If no, add the customerId to the map with the new points
            customerPointsMap.put(customerId, pointsEarned);
        }
    }

    private void updatePointsSheet() throws IOException {
        String rangeForPointSheet = "Soch_Points!A:B";  // Update with your sheet B range

        // Create the data to be updated in sheet B
        List<List<Object>> updateDataInPointSheet = newPointSheetData();

        // Update pointsheet using your sochService
        updateSheet(SpreadSheetId, rangeForPointSheet, updateDataInPointSheet);
    }

    private List<List<Object>> newPointSheetData() {
        List<List<Object>> dataToUpdateInPointSheet = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : customerPointsMap.entrySet()) {
            List<Object> row = new ArrayList<>();
            row.add(entry.getKey());   // CustomerId
            row.add(entry.getValue()); // Total Points
            dataToUpdateInPointSheet.add(row);
        }
        return dataToUpdateInPointSheet;
    }





}