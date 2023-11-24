package com.example.CokeRestAPI.Utils;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class SheetsServiceUtil {

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        GoogleCredential credentials = GoogleCredential.fromStream(
                        new ByteArrayInputStream(getJsonCredentials().getBytes()))
                .createScoped(Arrays.asList(SheetsScopes.SPREADSHEETS));

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                credentials)
                .setApplicationName("Coke-API")
                .build();
    }

    private static String getJsonCredentials() {

        return "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"coke-api-406107\",\n" +
                "  \"private_key_id\": \"017cafc69ecc23b90f1857e47125abdcc3fc56cb\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC0Yp+zlptkDmQJ\\nxDRL3mrTtwW0/rtWtTEms0Vle4Cd3Jgr4AYP25yQliFefqtoPXTlKjukRjEoIyO8\\n/MkZ/nNvd+QmrudmBOic0zASCOpwC6lXRaQ4eopEojX899twvK+vovoDf8VpMUGp\\n1oZ2XdZjAs0J5OSUTOMebOULFPVmV0QzdOd9T6HTxulgzyjP5SmgwGM+aKgvQIsC\\n/onZxylzvrMCxVCXYFVUrQZgyK0uLN4obgAyTsxUbOrAnOlDNEaSBeSlv8ZWxt/k\\nTqwbgMtzFMLGkzAVpMc5/NkYunzr5ZIXLHYe6x6CZHTusVc64odRyxF/wZ+DLLWH\\n6NYUT4ohAgMBAAECggEAAtffHoyMLHqRlxnRnTSHSIkE/QJa35YBjVOcD2pv740P\\nlPl1/v/+KsVDB2NFvmHhms8cm41DrxdGs+7kGJXLKgGgxf9RxGjlZbtqcY5Ua6mz\\nZJ3DZeSAk25fHXqmqdM/jawq9q0n5m2RVGT+P5VhELR1mOdTtwazOcKLpa4R5CgL\\nJkkNKGKCaesz9kpu9bDpj4pNDWrqnHHuh6YNr1znlDgVGwhfk7dqao83zIQBwTGi\\nVibQFfDs2/Q/VP0r5sFuP2HMjqkQn+nANGTQPiQ5CUN27P9FZMqncClAGblFIhb5\\nuBY5tT8TJg6f4+uneQql9o+6z9vFxJ9YwhNVQ8QWwQKBgQDzklJj+8euiF0+sayt\\noQ7QFs61aQN5fwa0dUCwCKjHHZStUpVU45SctkZwlJXu823KrrBKE9A5RAy4ct++\\nSpfRNYJhG+98quFRXLjQJ8L4WwhpxxPlj+yTf3QXIGgZjAuSZfyNC3RFcs6iBnyr\\nvO2IVvVJiZLdzJ+OJSrdNzbvOQKBgQC9luygZJ5N5CqAbppdadnBwntSNY7PL/et\\nBgnZd0LUDmE0vvAkUqbDSABky4C9b68Vgt+aq/99x/xA3di0COKXXAGrklVMJ7bv\\n40ZmAARo59jA+AOH93U7tGp1ZkNOd90FIPf0HHCN8CsGkRXXrgEjW4/NhAor7khp\\nsoSkFYMKKQKBgQDff5xqclZAKJCnPpGcoPTerI7wl3PAwHZ+kDjTTp7QVqxefjDY\\nvBtZ0UfBdhY+NNDB8pmA371pTq/9cCsuiXPVXKxOPzv5wTMPXW2YU2PsUFvf8/Sl\\n6PLERWdY8TuUdZUIuNyM7725HCfPNPShAbNUL7bTy5EgROsDm8dBRbD6wQKBgQCE\\ny0HS1qdLR5W9f7r/0zSDUwWt/3WYCbEhsCkV+UMF5UyQ9WyfBIVqIvSjpSb3iVLJ\\nbnHQlMjQAVPe24T+FXshKYDB/C1iui6YuEiHCVwNXtej0hQnGF01BJnMwWRDghF9\\nlioXAhSUVrTMVLNH7sk9StTezq2RN8XgwvnFNIwkgQKBgBBp5gq+IRv258JLgeE1\\ncr4mKavtUahfVS1IYDoS4vIsG8ASqQ+6nrGrGl2NMivIDBf2ocwNXOFZ79igY1nk\\nd0/KeRJFQqeVLIPsl+pC7o98eVbV9VHI3oxkpJyHp1p4Qm30cuVwFwSzKQjc3erC\\nqkl/joUm7Voof0jJ5lHGMEOw\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"coke-api@coke-api-406107.iam.gserviceaccount.com\",\n" +
                "  \"client_id\": \"105478965048090542617\",\n" +
                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/coke-api%40coke-api-406107.iam.gserviceaccount.com\",\n" +
                "  \"universe_domain\": \"googleapis.com\"\n" +
                "}\n";
    }
}
