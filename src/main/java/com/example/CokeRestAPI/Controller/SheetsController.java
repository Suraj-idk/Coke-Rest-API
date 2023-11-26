package com.example.CokeRestAPI.Controller;

import com.example.CokeRestAPI.Service.SheetsService;
import com.example.CokeRestAPI.Utils.SheetsServiceUtil;
import com.google.api.services.sheets.v4.Sheets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    private final String SpreadSheetId = "1V1XuftkU-sVDxptzICYyj7rMyKlpCCiBoUecARuQ0ag";

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

        String orderStatus = "Preparing for dispatch";

        String orderId = sheetsService.generateRandom4Digit();


        List<Object> rowData = Arrays.asList(
                orderId,
                name,
                phoneNumber,
                productName,
                units,
                quantity,
                price,
                dateOrdered,
                deliveryDate,
                orderStatus
        );

        sheetsService.writeToSheet(SpreadSheetId, "Data", rowData);
    }


    @GetMapping("/read")
    public List<Map<String, Object>> readData(
            @RequestParam String name,
            @RequestParam String phoneNumber) throws IOException {

        String range = "Data!A:J";

        List<List<Object>> allData = sheetsService.readFromSheet(SpreadSheetId, range);
        List<Map<String, Object>> result = new ArrayList<>();

        if (allData.isEmpty()) {
            return result;
        }

        List<String> columnOrder = Arrays.asList(
                "Order Id", "Name", "Phone Number", "Product Name", "Units", "Quantity", "Price", "Ordered Date", "Est. Delivery Date", "Order Status"
        );

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

            //filter based on name and phoneNumber
            String rowName = rowData.get("Name").toString();
            String rowPhoneNumber = rowData.get("Phone Number").toString();

            if (name.equals(rowName) && phoneNumber.equals(rowPhoneNumber) && !"Delivered".equals(rowData.get("Order Status"))) {
                result.add(rowData);
            }
        }
        return result;
    }


    @GetMapping("/readByOrderId")
    public Map<String, Object> readDataByOrderId(@RequestParam String orderId) throws IOException {
        String range = "Data!A:J";

        List<List<Object>> allData = sheetsService.readFromSheet(SpreadSheetId, range);
        Map<String, Object> result = new HashMap<>();

        if (allData.isEmpty()) {
            return result;
        }

        List<String> columnOrder = Arrays.asList(
                "Order Id", "Name", "Phone Number", "Product Name", "Units", "Quantity", "Price", "Ordered Date", "Est. Delivery Date", "Order Status"
        );

        result = sheetsService.findRowByOrderId(allData, orderId, columnOrder);

        // If orderId is not found, return an empty result
        return result;
    }
}