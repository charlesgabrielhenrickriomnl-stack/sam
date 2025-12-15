package auth.proj.sam.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async; // <-- ADDED IMPORT
import java.util.Map;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}") // Commented out
    private String brevoApiKey;

    // @Value("${EMAIL_SENDER_ADDRESS}") // Commented out
    private String senderEmailAddress;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Async // <-- ADDED ANNOTATION: Sends email in a separate thread
    public void sendEmail(String to, Long templateId, Map<String, String> params) {
        String apiUrl = "https://api.brevo.com/v3/smtp/email";

        // Build the "params" part of the JSON
        StringBuilder paramsJson = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (paramsJson.length() > 0) {
                paramsJson.append(",");
            }
            paramsJson.append(String.format("\"%s\":\"%s\"", entry.getKey(), entry.getValue()));
        }

        // Construct the final JSON payload with templateId and params
        String jsonPayload = String.format(
            "{\"to\":[{\"email\":\"%s\"}],\"templateId\":%d,\"params\":{%s}}",
            to,
            templateId,
            paramsJson.toString()
        );

        RequestBody requestBody = RequestBody.create(
            jsonPayload,
            MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .addHeader("accept", "application/json")
            .addHeader("api-key", brevoApiKey)
            .addHeader("content-type", "application/json")
            .build();

        try {
            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.println("✅ Template email sent successfully to " + to + " via Brevo API.");
            } else {
                String responseBody = response.body() != null ? response.body().string() : "No response body.";
                System.err.println("❌ Failed to send template email via Brevo API. Response: " + responseBody);
                // Do not re-throw RuntimeException in an @Async method.
            }
        } catch (Exception e) {
            System.err.println("❌ Exception while sending template email via Brevo API: " + e.getMessage());
            // Do not re-throw RuntimeException in an @Async method.
        }
    }
}