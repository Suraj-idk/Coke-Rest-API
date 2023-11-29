package com.example.CokeRestAPI.Controller;

import com.example.CokeRestAPI.Entity.SochSheet;
import com.example.CokeRestAPI.Service.SheetsService;
import com.example.CokeRestAPI.Service.SochService;
import com.example.CokeRestAPI.Utils.SheetsServiceUtil;
import com.google.api.services.sheets.v4.Sheets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequestMapping("/api/soch")
public class SochController {

    @Autowired
    private SochService sochService;

    private static Sheets sheets;

    static {
        try {
            sheets = SheetsServiceUtil.getSheetsService();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            //TO-DO Handle initialization error
        }
    }

    @Value("${spreadsheet.id}")
    private String SpreadSheetId;

    @PostMapping("/write")
    public ResponseEntity<Object> writeData(
            @RequestParam String name,
            @RequestParam String phoneNumber,
            @RequestParam String email,
            @RequestParam String address) throws IOException {

        String customerId = sochService.generateRandom4Digit();

        List<Object> rowData = createRowData(customerId, name, phoneNumber, email, address);
        sochService.writeToSheet(SpreadSheetId, "Soch_Sheet", rowData);

        // Create and return the JSON response
        Map<String, Object> jsonResponse = sochService.createJsonResponseForWrite(HttpStatus.OK.value(), rowData);
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    private List<Object> createRowData(String orderId, String name, String phoneNumber, String email, String address) {
        return Arrays.asList(orderId, name, phoneNumber, email, address);
    }

    @GetMapping("/readBy")
    public ResponseEntity<Object> readDataByContact(
            @RequestParam String phoneNumber) throws IOException {
        String range = "Soch_Sheet!A:E";

        List<List<Object>> allData = sochService.readFromSheet(SpreadSheetId, range);
        List<Map<String, Object>> result = new ArrayList<>();

        if (allData.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> columnOrder = Arrays.asList(
                "CustomerId", "Name", "PhoneNumber", "email", "address");

        result = sochService.findRowsByContact(allData, phoneNumber, columnOrder);
            return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/writeOrderDetails")
    public ResponseEntity<Object> writeDataToProductSheet(
            @RequestParam String customerId ,
            @RequestParam String productName,
            @RequestParam String price,
            @RequestParam String quantity) throws IOException {

        String orderStatus = "Preparing for Dispatch";
        String orderId = sochService.generateRandom4DigitForOrderId();
        String cancelOrder = "False";
        String returnOrder = "False";

        List<Object> rowData = createRowDataForProductSheet(customerId, orderId, productName, price, quantity,orderStatus, returnOrder, cancelOrder);
        sochService.writeInProductSheet(SpreadSheetId, "Soch_Products", rowData);

        Map<String, Object> jsonResponse = sochService.createJsonResponseForWriteInProductSheet(HttpStatus.OK.value(), rowData);
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    private List<Object> createRowDataForProductSheet(String customerId, String orderId, String productName, String price, String quantity, String orderStatus, String returnOrder, String cancelOrder ) {
        return Arrays.asList(customerId, orderId, productName, price, quantity,orderStatus, returnOrder, cancelOrder);
    }

    @GetMapping("/readOrderId")
    public ResponseEntity<Object> readDataByOrderId(@RequestParam String orderId) throws IOException {
        String range = "Soch_Products!A:H";

        List<List<Object>> allData = sochService.readFromProductSheet(SpreadSheetId, range);
        Map<String, Object> result = new HashMap<>();

        if (allData.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> columnOrder = Arrays.asList(
                "customerId", "orderId", "productName", "price", "quantity","orderStatus", "returnOrder", "cancelOrder"
        );

        result = sochService.findRowByOrderIdForProductSheet(allData, orderId, columnOrder);

        // If orderId is not found, return an empty result
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/cancelOrder")
    public ResponseEntity<String> cancelOrder(@RequestParam String orderId) {
        String range = "Soch_Products";
        try {
            List<List<Object>> allData = sochService.readFromProductSheet(SpreadSheetId, range);
            List<String> columnOrder = Arrays.asList(
                    "customerId", "orderId", "productName", "price", "quantity", "orderStatus", "returnOrder", "cancelOrder"
            );

            sochService.setCancelOrderTrue(allData, orderId, columnOrder);

            // Update the spreadsheet with the modified data
            for (List<Object> rowData : allData) {
                sochService.writeInProductSheet(SpreadSheetId, range, rowData);
            }

            return new ResponseEntity<>("Order canceled successfully.", HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to cancel order."+ e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/readCustomerId")
    public ResponseEntity<Object> readDataByCustomerId(@RequestParam String customerId) throws IOException {
        String range = "Soch_Products!A:H";

        List<List<Object>> allData = sochService.readFromProductSheet(SpreadSheetId, range);
        Map<String, Object> result = new HashMap<>();

        if (allData.isEmpty()) {
            result.put("rowCount", 0);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> columnOrder = Arrays.asList(
                "customerId", "orderId", "productName", "price", "quantity", "orderStatus", "returnOrder", "cancelOrder"
        );

        List<Map<String, Object>> matchingRows = sochService.findRowsByCustomerIdForProductSheet(allData, customerId, columnOrder);

        result.put("rowCount", matchingRows.size());
        result.put("data", matchingRows); // Add the matching rows to the result

        return new ResponseEntity<>(result, HttpStatus.OK);
    }






}