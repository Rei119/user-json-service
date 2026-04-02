package com.json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private AuthMiddleware authMiddleware;

    @Autowired
    private UserRepository userRepository; 

    private boolean isAuthorized(String token) {
        if (token == null) return false;
        return authMiddleware.validate(token);
    }

    // ─── AUTH PROXY ───────────────────────────────────────────

    @GetMapping("/auth/register")
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String password) {
        try {
            String soapRequest =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://soap.com/\">" +
                "<soapenv:Header/><soapenv:Body>" +
                "<soap:registerUser><arg0>" + username + "</arg0><arg1>" + password + "</arg1></soap:registerUser>" +
                "</soapenv:Body></soapenv:Envelope>";

            String response = callSoap(soapRequest);
            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("<return>(.*?)</return>")
                .matcher(response);
            String message = m.find() ? m.group(1) : response;
            return ResponseEntity.ok("{\"message\":\"" + message + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/auth/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password) {
        try {
            String soapRequest =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://soap.com/\">" +
                "<soapenv:Header/><soapenv:Body>" +
                "<soap:loginUser><arg0>" + username + "</arg0><arg1>" + password + "</arg1></soap:loginUser>" +
                "</soapenv:Body></soapenv:Envelope>";

            String response = callSoap(soapRequest);
            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("<return>(.*?)</return>")
                .matcher(response);
            String token = m.find() ? m.group(1) : "";

            if (token.startsWith("ERROR") || token.isEmpty())
                return ResponseEntity.status(401).body("{\"error\":\"Invalid credentials\"}");

            return ResponseEntity.ok("{\"token\":\"" + token + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // ─── USER PROFILE CRUD ────────────────────────────────────

    @PostMapping("/users")
    public ResponseEntity<?> createProfile(
            @RequestHeader("Authorization") String token,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String phone) {

        if (!isAuthorized(token))
            return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");

        UserProfile p = new UserProfile();
        p.setId(UUID.randomUUID().toString());
        p.setName(name);
        p.setEmail(email);
        p.setBio(bio);
        p.setPhone(phone);

        userRepository.save(p); // Save to MongoDB
        return ResponseEntity.ok(p);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getProfile(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {

        if (!isAuthorized(token))
            return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");

        return userRepository.findById(id)
            .map(p -> ResponseEntity.ok((Object) p))
            .orElse(ResponseEntity.status(404).body("{\"error\":\"Not found\"}"));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllProfiles(
            @RequestHeader("Authorization") String token) {

        if (!isAuthorized(token))
            return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");

        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String phone) {

        if (!isAuthorized(token))
            return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");

        return userRepository.findById(id).map(p -> {
            if (name != null) p.setName(name);
            if (bio != null) p.setBio(bio);
            if (phone != null) p.setPhone(phone);
            userRepository.save(p); // Save updated to MongoDB
            return ResponseEntity.ok((Object) p);
        }).orElse(ResponseEntity.status(404).body("{\"error\":\"Not found\"}"));
    }


    @PatchMapping("/users/{id}/image")
    public ResponseEntity<?> updateProfileImage(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestParam String imageUrl) {

        if (!isAuthorized(token))
            return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");

        return userRepository.findById(id).map(p -> {
            p.setProfileImage(imageUrl);
            userRepository.save(p);
            return ResponseEntity.ok((Object) p);
        }).orElse(ResponseEntity.status(404).body("{\"error\":\"Not found\"}"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteProfile(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {

        if (!isAuthorized(token))
            return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");

        userRepository.deleteById(id);
        return ResponseEntity.ok("{\"message\":\"Deleted\"}");
    }


    private String callSoap(String soapRequest) throws Exception {
    	java.net.URL url = new java.net.URL("https://user-soap-service.onrender.com/auth");
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conn.setRequestProperty("SOAPAction", "");
        conn.setDoOutput(true);
        conn.getOutputStream().write(soapRequest.getBytes("UTF-8"));
        conn.getOutputStream().flush();
        return new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A").next();
    }
}