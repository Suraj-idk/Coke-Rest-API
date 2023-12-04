package com.example.CokeRestAPI.Controller;

import com.example.CokeRestAPI.Entity.SochSheet;
import com.example.CokeRestAPI.Service.SheetsService;
import com.example.CokeRestAPI.Service.SochService;
import com.example.CokeRestAPI.Utils.SheetsServiceUtil;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
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

        if (sochService.isPhoneNumberOrEmailAlreadyExists(phoneNumber, email)) {
            // Return a JSON response with error message and response code 400
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("responseCode", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("data", "Either Phone Number or Email-Id is already present");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
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

    @GetMapping("/readOrderByOrderId")
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

    @GetMapping("/readOrderByCustomerId")
    public ResponseEntity<Object> readDataByCustomerId(@RequestParam String customerId) throws IOException {
        String range = "Soch_Products!A:H";

        List<List<Object>> allData = sochService.readFromProductSheet(SpreadSheetId, range);
        List<Map<String, Object>> result = new ArrayList<>();

        if (allData.isEmpty()) {
//            result.put("rowCount", 0);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> columnOrder = Arrays.asList(
                "customerId", "orderId", "productName", "price", "quantity", "orderStatus", "returnOrder", "cancelOrder"
        );

        List<Map<String, Object>> matchingRows = sochService.findRowsByCustomerIdForProductSheet(allData, customerId, columnOrder);


        result.addAll(matchingRows);
//        result.put("rowCount", matchingRows.size());
//        result.put("data", matchingRows); // Add the matching rows to the result

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/cancelOrder")
    public ResponseEntity<Object> cancelOrder(@RequestParam String orderId) throws IOException {
        String range = "Soch_Products!A:H";

        List<List<Object>> allData = sochService.readFromProductSheet(SpreadSheetId, range);
        Map<String, Object> result = new LinkedHashMap<>();

        if (allData.isEmpty()) {
            result.put("message", "No orders found.");
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }

        List<String> columnOrder = Arrays.asList(
                "customerId", "orderId", "productName", "price", "quantity", "orderStatus", "returnOrder", "cancelOrder"
        );

        // Find the row by orderId
        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            Object currentOrderId = row.get(1); // orderId is in the second column

            if (orderId.equalsIgnoreCase(currentOrderId.toString())
                    && !"delivered".equalsIgnoreCase(row.get(columnOrder.indexOf("orderStatus")).toString())
                    && !"True".equals(row.get(columnOrder.indexOf("cancelOrder")))
                    && !"True".equals(row.get(columnOrder.indexOf("returnOrder")))) {

                // Update cancelOrder status to true
                row.set(columnOrder.indexOf("cancelOrder"), "True");
                row.set(columnOrder.indexOf("orderStatus"), "Order Cancelled");

                // Update the spreadsheet with the modified data
                sochService.updateSheet(SpreadSheetId, range, allData);

//                // Prepare the response
//                for (int i = 0; i < columnOrder.size(); i++) {
//                    String columnName = columnOrder.get(i);
//                    Object columnValue = row.get(i);
//                    result.put(columnName, columnValue);
//                }

                result.put("customerId", row.get(columnOrder.indexOf("customerId")));
                result.put("orderId", row.get(columnOrder.indexOf("orderId")));
                result.put("productName", row.get(columnOrder.indexOf("productName")));
                result.put("price", row.get(columnOrder.indexOf("price")));
                result.put("quantity", row.get(columnOrder.indexOf("quantity")));
                result.put("orderStatus", row.get(columnOrder.indexOf("orderStatus")));
                result.put("returnOrder", row.get(columnOrder.indexOf("returnOrder")));
                result.put("cancelOrder", row.get(columnOrder.indexOf("cancelOrder")));
                result.put("message", "Order canceled successfully.");
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        }

        result.put("message", "Order not found or cannot be canceled.");
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/updateDetails")
    public ResponseEntity<Object> updateDetailsByCustomerId(
            @RequestParam String customerId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address) throws IOException {

        String range = "Soch_Sheet!A:E";

        List<List<Object>> allData = sochService.readFromSheet(SpreadSheetId, range);
        Map<String, Object> result = new LinkedHashMap<>();

        if (allData.isEmpty()) {
            result.put("message", "No customer details found.");
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }

        List<String> columnOrder = Arrays.asList(
                "Customer id", "User Name", "Contact Number", "Email Id", "Address"
        );

        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            Object currentCustomerId = row.get(0); // customerId is in the first column

            if (customerId.equalsIgnoreCase(currentCustomerId.toString()))
            {
                if (username != null) {
                    row.set(columnOrder.indexOf("User Name"), username);
                }
                if (contactNumber != null) {
                    row.set(columnOrder.indexOf("Contact Number"), contactNumber);
                }
                if (email != null) {
                    row.set(columnOrder.indexOf("Email Id"), email);
                }
                if (address != null) {
                    row.set(columnOrder.indexOf("Address"), address);
                }

                // Update the spreadsheet with the modified data
                sochService.updateSheet(SpreadSheetId, range, allData);

                // Prepare the response with the updated details
                result.put("customerId", row.get(columnOrder.indexOf("Customer id")));
                result.put("userName", row.get(columnOrder.indexOf("User Name")));
                result.put("contactNumber", row.get(columnOrder.indexOf("Contact Number")));
                result.put("email", row.get(columnOrder.indexOf("Email Id")));
                result.put("address", row.get(columnOrder.indexOf("Address")));
                result.put("message", "Customer details updated successfully.");

                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        }

        result.put("message", "Customer details not found or cannot be updated.");
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/updateReturnOrder")
    public ResponseEntity<Object> updateReturnOrder(
            @RequestParam String orderId) throws IOException {

        String range = "Soch_Products!A:H";

        List<List<Object>> allData = sochService.readFromProductSheet(SpreadSheetId, range);
        Map<String, Object> result = new LinkedHashMap<>(); // Use LinkedHashMap to maintain the insertion order

        if (allData.isEmpty()) {
            result.put("message", "No orders found.");
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }

        List<String> columnOrder = Arrays.asList(
                "customerId", "orderId", "productName", "price", "quantity", "orderStatus", "returnOrder", "cancelOrder"
        );

        // Find the row by orderId
        for (List<Object> row : allData.subList(1, allData.size())) { // Start from the second row
            Object currentOrderId = row.get(1); // orderId is in the second column

            if (orderId.equalsIgnoreCase(currentOrderId.toString())
                    && "delivered".equalsIgnoreCase(row.get(columnOrder.indexOf("orderStatus")).toString())
                    && !"True".equalsIgnoreCase(String.valueOf(row.get(columnOrder.indexOf("cancelOrder"))))
                    && !"True".equalsIgnoreCase(String.valueOf(row.get(columnOrder.indexOf("returnOrder"))))) {

                // Update returnOrder status to true
                row.set(columnOrder.indexOf("returnOrder"), "True");
                row.set(columnOrder.indexOf("orderStatus"), "Order Returned");

                // Update the spreadsheet with the modified data
                sochService.updateSheet(SpreadSheetId, range, allData);

                // Prepare the response with the updated details
                result.put("customerId", row.get(columnOrder.indexOf("customerId")));
                result.put("orderId", row.get(columnOrder.indexOf("orderId")));
                result.put("productName", row.get(columnOrder.indexOf("productName")));
                result.put("price", row.get(columnOrder.indexOf("price")));
                result.put("quantity", row.get(columnOrder.indexOf("quantity")));
                result.put("orderStatus", row.get(columnOrder.indexOf("orderStatus")));
                result.put("returnOrder", row.get(columnOrder.indexOf("returnOrder")));
                result.put("cancelOrder", row.get(columnOrder.indexOf("cancelOrder")));
                result.put("message", "Return order updated successfully.");

                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        }

        result.put("message", "Order not found, not delivered, or return order cannot be updated.");
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/readProductList")
    public ResponseEntity<Object> readProductList() throws IOException {
        String range = "Soch_Products_List!A:C";

        List<List<Object>> allData = sochService.readFromSheet(SpreadSheetId, range);
        Map<String, Object> result = new HashMap<>();

        if (allData.isEmpty()) {
            result.put("rowCount", 0);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> columnOrder = Arrays.asList(
                "ProductNumber", "ProductName", "Price"
        );

        List<Map<String, Object>> formattedData = new ArrayList<>();
        for (int rowIndex = 1; rowIndex < allData.size(); rowIndex++) {
            List<Object> row = allData.get(rowIndex);
            Map<String, Object> rowData = new HashMap<>();
            for (int i = 0; i < columnOrder.size(); i++) {
                rowData.put(columnOrder.get(i), row.get(i));
            }
            formattedData.add(rowData);
        }

        result.put("rowCount", formattedData.size());
        result.put("data", formattedData); // Add the formatted data to the result

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}