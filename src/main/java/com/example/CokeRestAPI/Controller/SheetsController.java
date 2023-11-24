package com.example.CokeRestAPI.Controller;

import com.example.CokeRestAPI.Entity.SheetsData;
import com.example.CokeRestAPI.Service.SheetsService;
import com.example.CokeRestAPI.Utils.SheetsServiceUtil;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Access;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequestMapping("/api/sheets")
public class SheetsController {

    @Autowired
    private SheetsService sheetsService;

    private static Sheets sheets;

    static {
        try {
            sheets = SheetsServiceUtil.getSheetsService();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            //TO-DO Handle initialization error
        }
    }

//    @PostMapping("/write")
//    public void writeData(@RequestBody SheetsData sheetData) throws IOException {
//        List<Object> rowData = Arrays.asList(
//                sheetData.getName(),
//                sheetData.getPhoneNumber(),
//                sheetData.getProductName(),
//                sheetData.getUnits(),
//                sheetData.getQuantity(),
//                sheetData.getPrice(),
//                sheetData.getDateOrdered(),
//                sheetData.getDeliveryDate()
//        );
//
//        sheetsService.writeToSheet("1V1XuftkU-sVDxptzICYyj7rMyKlpCCiBoUecARuQ0ag", "Data", rowData);
//    }

    @PostMapping("/write")
    public void writeData(
            @RequestParam String name,
            @RequestParam String phoneNumber,
            @RequestParam String productName,
            @RequestParam String units,
            @RequestParam int quantity,
            @RequestParam int price,
            @RequestParam String dateOrdered,
            @RequestParam String deliveryDate) throws IOException {

        List<Object> rowData = Arrays.asList(
                name,
                phoneNumber,
                productName,
                units,
                quantity,
                price,
                dateOrdered,
                deliveryDate
        );

        sheetsService.writeToSheet("1V1XuftkU-sVDxptzICYyj7rMyKlpCCiBoUecARuQ0ag", "Data", rowData);
    }

    @GetMapping("/read")
    public List<Map<String, Object>> readData(
            @RequestParam String name,
            @RequestParam String phoneNumber) throws IOException {

        String spreadsheetId = "1V1XuftkU-sVDxptzICYyj7rMyKlpCCiBoUecARuQ0ag";

        // Data is in the "Data" sheet between A to H columns
        String range = "Data!A:H";

        List<List<Object>> allData = sheetsService.readFromSheet(spreadsheetId, range);

        List<Map<String, Object>> result = new ArrayList<>();

        // Ensure there is at least one row in the spreadsheet
        if (allData.isEmpty()) {
            return result;
        }

        // Define the order of columns based on your spreadsheet
        List<String> columnOrder = Arrays.asList(
                "Name", "Phone Number", "Product Name", "Units", "Quantity", "Price", "Ordered Date", "Est. Delivery Date"
        );

        // Extract header row for column names
        List<Object> headerRow = allData.get(0);

        for (List<Object> row : allData.subList(1, allData.size())) {
            Map<String, Object> rowData = new LinkedHashMap<>();

            for (String columnName : columnOrder) {
                int columnIndex = headerRow.indexOf(columnName);
                if (columnIndex >= 0 && columnIndex < row.size()) {
                    Object columnValue = row.get(columnIndex);
                    rowData.put(columnName, columnValue);
                }
            }

            result.add(rowData);
        }

        return result;
    }




//    private List<List<Object>> readFromSheet(String spreadsheetId, String range) throws IOException {
//        ValueRange response = sheets.spreadsheets().values()
//                .get(spreadsheetId, range)
//                .execute();
//        return response.getValues();
//    }



//    private void writeToSheet(String spreadsheetId, String range, List<Object> rowData) throws IOException {
//        ValueRange body = new ValueRange().setValues(Arrays.asList(rowData));
//        UpdateValuesResponse result = sheetsService.spreadsheets().values()
//                .update(spreadsheetId, range, body)
//                .setValueInputOption("RAW")
//                .execute();
//        System.out.println("Data written to sheet: " + result);
//    }
}


//https://docs.google.com/spreadsheets/d/1V1XuftkU-sVDxptzICYyj7rMyKlpCCiBoUecARuQ0ag/edit?usp=sharing