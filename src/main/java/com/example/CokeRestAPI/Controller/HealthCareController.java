package com.example.CokeRestAPI.Controller;

import com.example.CokeRestAPI.Service.HealthCareService;
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
@RequestMapping("/api/healthcare")
public class HealthCareController {

    @Autowired
    private HealthCareService healthcareService;

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

  /*  public HealthCareController(HealthCareService healthcareService, String spreadSheetId) {
        this.healthcareService = healthcareService;
        SpreadSheetId = spreadSheetId;
    }
*/
    @PostMapping("/write")
    public ResponseEntity<Object> writeData(
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String name,
            @RequestParam int age,
            @RequestParam String gender,
            @RequestParam String DOB) throws IOException {

        String orderStatus = "Preparing for dispatch";
        String visitId = healthcareService.generateRandom4Digit();

        List<Object> rowData = createRowData(visitId,email,phone,name,age,gender,DOB);
        healthcareService.writeToSheet(SpreadSheetId, "HealthCare_Sheet", rowData);

        // Create and return the JSON response
        Map<String, Object> jsonResponse = healthcareService.createJsonResponseForWrite(HttpStatus.OK.value(), rowData);
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    private List<Object> createRowData(String visitId,String email,String phone,String name,int age,String gender,String DOB) {
        return Arrays.asList(visitId, email, phone, name, age, gender,DOB);
    }

    @GetMapping("/read")
    public ResponseEntity<Object> readDataByEmailAndNumber(
            @RequestParam String email, @RequestParam String phone, @RequestParam String name) throws IOException {
        String range = "HealthCare_Sheet!A:G";

        List<List<Object>> allData = healthcareService.readFromSheet(SpreadSheetId, range);
        List<Map<String, Object>> result = new ArrayList<>();

        if (allData.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> columnOrder = Arrays.asList(
                "VisitID","Email","Phone","Name","Age","Gender","DOB"
        );

        result = healthcareService.findRowsByEmailAndPhone(allData, email, phone, name, columnOrder);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/readByVisitId")
    public ResponseEntity<Object> readDataByVisitId(@RequestParam String visitId) throws IOException {
        String range = "HealthCare_Sheet!A:G";

        List<List<Object>> allData = healthcareService.readFromSheet(SpreadSheetId, range);
        Map<String, Object> result = new HashMap<>();

        if (allData.isEmpty()) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> columnOrder = Arrays.asList(
                "VisitID","Email","Phone","Name","Age","Gender","DOB"
        );

        result = healthcareService.findRowByVisitId(allData, visitId, columnOrder);

        // If orderId is not found, return an empty result
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PostMapping("/writeDoctorAppointment")
    public ResponseEntity<Object>writeDoctorAppointment(
            @RequestParam String visitId,
            @RequestParam String doctorName,
            @RequestParam String doctorType,
            @RequestParam String visitDate) throws IOException {

            List<Object> rowData = createRowData(visitId,doctorName,doctorType,visitDate);
            healthcareService.writeToDoctorAppointmentSheet(SpreadSheetId, "HealthCare_Dr_Appointmnet", rowData);

            // Create and return the JSON response
            Map<String, Object> jsonResponse = healthcareService.createJsonResponseForDrAppointment(HttpStatus.OK.value(), rowData);
            return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
        }
    private List<Object> createRowData(String visitId,String doctorName,String doctorType,String visitDate) {
        return Arrays.asList(visitId, doctorName, doctorType, visitDate);
    }

    @PostMapping("/writePrescription")
    public ResponseEntity<Object> writePrescriptionUrl(
            @RequestParam String visitId,
            @RequestParam String prescriptionUrl) throws IOException {

        // Ensure that visitId is provided
        if (visitId == null || visitId.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "VisitId cannot be null or empty");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Check if the visitId exists in the sheet
        String range = "HealthCare_Sheet!A:H"; // Adjusted to cover columns A to H
        List<List<Object>> allData = healthcareService.readFromSheet(SpreadSheetId, range);
        boolean visitIdExists = healthcareService.visitIdExists(allData, visitId);

        if (!visitIdExists) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "VisitId not found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // Write the prescriptionUrl to the sheet
        List<Object> rowData = Collections.singletonList(prescriptionUrl);
        healthcareService.writePrescriptionUrl(SpreadSheetId, "HealthCare_Sheet", visitId, rowData);

        // Create and return the JSON response
        Map<String, Object> jsonResponse = healthcareService.createJsonResponseForWritePrescription(HttpStatus.OK.value(), rowData);
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }
}