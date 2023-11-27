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
public class SheetsService {

    @Value("${spreadsheet.id}")
    private String SpreadSheetId;

    private final Sheets sheets;

    public SheetsService() throws IOException, GeneralSecurityException {
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
        range = "A" + nextRow + ":J" + nextRow;

        // Prepare the body for the update
        ValueRange body = new ValueRange().setValues(Arrays.asList(rowData));

        // Update the spreadsheet
        UpdateValuesResponse result = sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        System.out.println("Data written to sheet: " + result);
    }

    public void addOrderStatus(Map<String, Object> rowData) {
        String orderDateStr = (String) rowData.get("Ordered Date");
        String deliveryDateStr = (String) rowData.get("Est. Delivery Date");

        if (orderDateStr != null && deliveryDateStr != null) {
            try {
                // Parse date strings into Date objects
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date orderDate = dateFormat.parse(orderDateStr);
                Date deliveryDate = dateFormat.parse(deliveryDateStr);

                // Get today's date
                Date today = new Date();

                // Determine the order status based on the logic
                String orderStatus = "";

                if (isSameDay(today, orderDate)) {
                    orderStatus = "Preparing for Dispatch";
                } else if (today.before(addDays(orderDate, 1))) {
                    orderStatus = "Dispatched";
                } else if (today.before(addDays(orderDate, 2))) {
                    orderStatus = "In Transit";
                } else if (today.before(addDays(orderDate, 3))) {
                    orderStatus = "In Transit";
                } else if (today.before(addDays(orderDate, 4))) {
                    orderStatus = "Shipped";
                } else if (today.equals(deliveryDate)) {
                    orderStatus = "Out for Delivery";
                } else {
                    orderStatus = "Delivered";
                }

                rowData.put("Order Status", orderStatus);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    // Helper method to add days to a Date
    public Date addDays(Date date, int days) {
        long timeInMillis = date.getTime();
        timeInMillis += days * 24 * 60 * 60 * 1000; // Convert days to milliseconds
        return new Date(timeInMillis);
    }
    public String generateRandom4Digit() {
        Random random = new Random();
        int random4DigitNumber = 1000 + random.nextInt(9000);
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        String formattedDate = dateFormat.format(currentDate);
        return random4DigitNumber + formattedDate;
    }


    @Scheduled(cron = "0 0 10 * * *") // Every day at 10AM
    public void updateOrderStatus() {
        System.out.println("Cron Start");
        try {
            // Fetch data directly from Google Sheets
            ValueRange response = sheets.spreadsheets().values().get(SpreadSheetId, "Data!A2:J").execute();
            List<List<Object>> values = response.getValues();

            // Skip if values is null or empty
            if (values == null || values.isEmpty()) {
                return;
            }

            // Update order status for each row
            for (List<Object> row : values) {
                String orderId = (String) row.get(0); // Assuming OrderId is in the first column (column A)

                if (orderId != null && !orderId.isEmpty()) {
                    String orderDateStr = (String) row.get(7);
                    String deliveryDateStr = (String) row.get(8);

                    try {
                        Date orderDate = new SimpleDateFormat("dd-MM-yyyy").parse(orderDateStr);
                        Date deliveryDate = new SimpleDateFormat("dd-MM-yyyy").parse(deliveryDateStr);

                        String orderStatus = calculateOrderStatus(orderDate, deliveryDate);

                        // Order Status column is in the last column (column J)
                        List<List<Object>> updateValues = List.of(List.of(orderStatus));
                        ValueRange body = new ValueRange().setValues(updateValues);

                        sheets.spreadsheets().values()
                                .update(SpreadSheetId, "Data!J" + (values.indexOf(row) + 2), body)
                                .setValueInputOption("RAW")
                                .execute();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Cron end");
    }

    private String calculateOrderStatus(Date orderDate, Date deliveryDate) {
        Date today = new Date();

        if (isSameDay(today, orderDate)) {
            return "Preparing for Dispatch";
        } else if (today.before(addDays(orderDate, 1))) {
            return "Dispatched";
        } else if (today.before(addDays(orderDate, 2)) || today.before(addDays(orderDate, 3))) {
            return "In Transit";
        } else if (today.before(addDays(orderDate, 4))) {
            return "Shipped";
        } else if (today.equals(deliveryDate)) {
            return "Out for Delivery";
        } else {
            return "Delivered";
        }
    }

    public List<Map<String, Object>> findRowsByNameAndNumber(
            List<List<Object>> allData, String name, String phoneNumber, List<String> columnOrder) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            String rowName = row.get(1).toString(); //Name is in the second column
            String rowPhoneNumber = row.get(2).toString(); // PhoneNumber is in the third column

            // Check if the current row matches the provided name and phoneNumber
            if (name.equalsIgnoreCase(rowName) && phoneNumber.equalsIgnoreCase(rowPhoneNumber)) {
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

    public Map<String, Object> findRowByOrderId(List<List<Object>> allData, String orderId, List<String> columnOrder) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            Object firstColumnValue = row.get(0); // OrderId is in the first column

            // Check if the current row matches the provided orderId
            if (orderId.equalsIgnoreCase(firstColumnValue.toString())) {
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

    public Map<String, Object> createJsonResponseForWrite(int responseCode, List<Object> rowData) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("OrderId", rowData.get(0));
        dataMap.put("Name", rowData.get(1));
        dataMap.put("PhoneNumber", rowData.get(2));
        dataMap.put("ProductName", rowData.get(3));
        dataMap.put("Units", rowData.get(4));
        dataMap.put("Quantity", rowData.get(5));
        dataMap.put("Price", rowData.get(6));
        dataMap.put("OrderedDate", rowData.get(7));
        dataMap.put("EstDeliveryDate", rowData.get(8));
        dataMap.put("OrderStatus", rowData.get(9));

        Map<String, Object> jsonResponse = new LinkedHashMap<>();
        jsonResponse.put("responseCode", responseCode);
        jsonResponse.put("data", dataMap);

        return jsonResponse;
    }
}