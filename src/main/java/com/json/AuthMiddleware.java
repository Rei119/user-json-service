package com.json;

import org.springframework.stereotype.Component;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Scanner;

@Component
public class AuthMiddleware {

    public boolean validate(String token) {
        try {
            String soapRequest =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:soap=\"http://soap.com/\">" +
                "<soapenv:Header/>" +
                "<soapenv:Body>" +
                "<soap:validateToken>" +
                "<arg0>" + token + "</arg0>" +
                "</soap:validateToken>" +
                "</soapenv:Body>" +
                "</soapenv:Envelope>";

            URL url = new URL("http://localhost:9002/auth");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setRequestProperty("SOAPAction", "");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(soapRequest.getBytes("UTF-8"));
            os.flush();

            InputStream is = conn.getInputStream();
            String response = new Scanner(is).useDelimiter("\\A").next();

            System.out.println("SOAP Response: " + response);

            return response.contains("true");
        } catch (Exception e) {
            System.out.println("Auth error: " + e.getMessage());
            return false;
        }
    }
}