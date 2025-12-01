package com.healthrx.hiringtest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookTaskRunner implements CommandLineRunner {

    // ---------------------------------------------------------
    // CHANGE THESE DETAILS TO YOUR OWN
    // ---------------------------------------------------------
    private static final String MY_NAME = "B S Bharath Raj"; 
    private static final String MY_REG_NO = "22BAI1279"; 
    private static final String MY_EMAIL = "jsbharath444@gmail.com"; 
    // ---------------------------------------------------------

    private static final String GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    // Fallback URL from your problem statement instructions
    private static final String FALLBACK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==========================================");
        System.out.println("STARTED: HealthRx Hiring Task");
        System.out.println("==========================================");

        RestTemplate restTemplate = new RestTemplate();

        // STEP 1: Generate Webhook
        // -------------------------------------------------------
        System.out.println("Step 1: Requesting Webhook URL...");
        
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", MY_NAME);
        requestBody.put("regNo", MY_REG_NO);
        requestBody.put("email", MY_EMAIL);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    GENERATE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            
            // DEBUGGING: Print the entire response to see the correct key names
            System.out.println("DEBUG - FULL SERVER RESPONSE: " + responseBody);

            if (responseBody == null) {
                System.err.println("Error: Received null response from generateWebhook API.");
                return;
            }

            // Try to fetch URL with different possible keys
            String webhookUrl = (String) responseBody.get("webhookUrl");
            if (webhookUrl == null) webhookUrl = (String) responseBody.get("url");
            if (webhookUrl == null) webhookUrl = (String) responseBody.get("webhook");
            
            // If still null, use the hardcoded one from the instructions
            if (webhookUrl == null) {
                System.out.println("WARNING: Could not find webhook URL in response. Using Fallback URL.");
                webhookUrl = FALLBACK_URL;
            }

            String accessToken = (String) responseBody.get("accessToken");

            System.out.println("Target Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken);

            if (accessToken == null) {
                System.err.println("CRITICAL ERROR: Access Token is null. Cannot proceed.");
                return;
            }

            // STEP 2: Solve the SQL Problem
            // -------------------------------------------------------
            System.out.println("Step 2: Preparing SQL Solution...");
            String sqlSolution = getSqlSolution();

            // STEP 3: Submit Solution
            // -------------------------------------------------------
            System.out.println("Step 3: Submitting Solution...");
            
            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            submitHeaders.set("Authorization", accessToken); // JWT Token

            Map<String, String> submitBody = new HashMap<>();
            submitBody.put("finalQuery", sqlSolution);

            HttpEntity<Map<String, String>> submitEntity = new HttpEntity<>(submitBody, submitHeaders);

            ResponseEntity<String> submitResponse = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    submitEntity,
                    String.class
            );

            System.out.println("==========================================");
            System.out.println("SUBMISSION COMPLETE");
            System.out.println("Response Status: " + submitResponse.getStatusCode());
            System.out.println("Response Body: " + submitResponse.getBody());
            System.out.println("==========================================");

        } catch (Exception e) {
            System.err.println("AN ERROR OCCURRED:");
            e.printStackTrace();
        }
    }

    private String getSqlSolution() {
        return "SELECT " +
               "    d.DEPARTMENT_NAME, " +
               "    ranked.total_salary AS SALARY, " +
               "    ranked.EMPLOYEE_NAME, " +
               "    ranked.AGE " +
               "FROM ( " +
               "    SELECT " +
               "        e.DEPARTMENT, " +
               "        SUM(p.AMOUNT) AS total_salary, " +
               "        CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, " +
               "        TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
               "        RANK() OVER (PARTITION BY e.DEPARTMENT ORDER BY SUM(p.AMOUNT) DESC) as rnk " +
               "    FROM PAYMENTS p " +
               "    JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
               "    WHERE DAY(p.PAYMENT_TIME) <> 1 " +
               "    GROUP BY e.DEPARTMENT, e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB " +
               ") ranked " +
               "JOIN DEPARTMENT d ON ranked.DEPARTMENT = d.DEPARTMENT_ID " +
               "WHERE ranked.rnk = 1;";
    }
}