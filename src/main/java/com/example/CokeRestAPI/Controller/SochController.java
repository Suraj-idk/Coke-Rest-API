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

        String orderStatus = "Preparing for Dispatch";
        String orderId = sochService.generateRandom4Digit();

        List<Object> rowData = createRowData(orderId, name, phoneNumber, email, address);
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
                "OrderId", "Name", "PhoneNumber", "email", "address");

        result = sochService.findRowsByContact(allData, phoneNumber, columnOrder);
            return new ResponseEntity<>(result, HttpStatus.OK);
    }

//    @GetMapping("/readByOrderId")
//    public ResponseEntity<Object> readDataByOrderId(@RequestParam String orderId) throws IOException {
//        String range = "Data!A:J";
//
//        List<List<Object>> allData = sochService.readFromSheet(SpreadSheetId, range);
//        Map<String, Object> result = new HashMap<>();
//
//        if (allData.isEmpty()) {
//            return new ResponseEntity<>(result, HttpStatus.OK);
//        }
//
//        List<String> columnOrder = Arrays.asList(
//                "OrderId", "Name", "PhoneNumber", "ProductName", "Units", "Quantity", "Price", "OrderedDate", "EstDeliveryDate", "OrderStatus"
//        );
//
//        result = sochService.findRowByOrderId(allData, orderId, columnOrder);
//
//        // If orderId is not found, return an empty result
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }
}