package utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads notification emails from Gmail.
 */
public class GmailReader {

    private static final JsonFactory JSON = GsonFactory.getDefaultInstance();
    private static Gmail gmail;


    // ========== WAIT FOR EMAILS ==========

    /**
     * Returns the subjects of the emails sent for this Reference ID after the form was submitted.
     *
     * 1. Checks Gmail every few seconds until the expected number of emails has arrived.
     * 2. Then waits a little longer and checks once more, to catch extra emails
     *    that should NOT have been sent.
     */
    public static List<String> waitForEmails(String referenceId, int expectedCount, long submitTime) {
        long searchFrom = submitTime - seconds("gmail.clock.buffer");
        long endTime = System.currentTimeMillis() + seconds("gmail.max.wait");

        // 1. Wait for the expected emails
        List<String> subjects = new ArrayList<>();
        int check = 0;

        while (subjects.size() < expectedCount && System.currentTimeMillis() < endTime) {
            if (check > 0) {
                pause(seconds("gmail.poll.interval"));
            }
            check++;
            subjects = checkGmail(referenceId, searchFrom, check);
        }

        if (subjects.size() < expectedCount) {
            Log.detail("Stopped waiting: time limit reached");
            return subjects;
        }

        // 2. Wait a little longer for any extra emails
        long extraWait = (expectedCount == 0) ? seconds("gmail.zero.email.quiet.period") : seconds("gmail.quiet.period");
        Log.detail("Waiting " + extraWait / 1000 + "s more for any extra emails...");
        pause(extraWait);

        check++;
        return checkGmail(referenceId, searchFrom, check);
    }

    // True if any subject starts with the given tag, e.g. "[ALWAYS]"
    public static boolean hasSubject(List<String> subjects, String tag) {
        // An empty tag in Excel means "no subject to look for"
        if (tag == null || tag.isBlank()) {
            return false;
        }
        for (String subject : subjects) {
            if (subject.trim().toUpperCase().startsWith(tag.toUpperCase())) {
                return true;
            }
        }
        return false;
    }


    // ========== READ GMAIL ==========

    // One Gmail check. Tries again if the network fails.
    private static List<String> checkGmail(String referenceId, long searchFrom, int check) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                List<String> subjects = getEmailSubjects(referenceId, searchFrom);
                Log.detail("Check " + check + " -> " + Log.emails(subjects.size()));
                return subjects;
            } catch (IOException e) {
                Log.detail("Check " + check + " -> Gmail not reachable, trying again");
                pause(seconds("gmail.poll.interval"));
            }
        }
        throw new RuntimeException("Could not reach Gmail after 3 attempts");
    }

    private static List<String> getEmailSubjects(String referenceId, long searchFrom) throws IOException {
        // in:anywhere also finds emails that went to Spam
        // after: limits the search to recent emails (Gmail uses seconds)
        String query = "in:anywhere \"" + referenceId + "\" after:" + (searchFrom / 1000 - 60);

        ListMessagesResponse response = getGmail().users().messages()
                .list("me").setQ(query).setMaxResults(100L).execute();

        List<String> subjects = new ArrayList<>();
        if (response.getMessages() == null) {
            return subjects;
        }

        for (Message item : response.getMessages()) {
            Message email = getGmail().users().messages()
                    .get("me", item.getId())
                    .setFormat("metadata")
                    .setMetadataHeaders(List.of("Subject"))
                    .execute();

            // Only count emails received after the form was submitted
            if (email.getInternalDate() != null && email.getInternalDate() >= searchFrom) {
                subjects.add(getSubject(email));
            }
        }
        return subjects;
    }

    private static String getSubject(Message email) {
        if (email.getPayload() == null || email.getPayload().getHeaders() == null) {
            return "";
        }
        for (MessagePartHeader header : email.getPayload().getHeaders()) {
            if (header.getName().equalsIgnoreCase("Subject")) {
                return header.getValue();
            }
        }
        return "";
    }


    // ========== GMAIL LOGIN ==========

    // Connects once and reuses the connection for the whole run
    private static Gmail getGmail() {
        if (gmail == null) {
            try {
                NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
                gmail = new Gmail.Builder(transport, JSON, login(transport))
                        .setApplicationName("Notification Automation")
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Could not connect to Gmail. Check credentials.json in src/test/resources", e);
            }
        }
        return gmail;
    }

    private static Credential login(NetHttpTransport transport) throws IOException {
        InputStream file = GmailReader.class.getResourceAsStream("/credentials.json");
        if (file == null) {
            throw new IOException("credentials.json not found in src/test/resources");
        }

        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON, new InputStreamReader(file));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, JSON, secrets, List.of(GmailScopes.GMAIL_READONLY))
                .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    // Reads a number of seconds from config.properties and returns milliseconds
    private static long seconds(String key) {
        return ConfigReader.getInt(key) * 1000L;
    }

    private static void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
