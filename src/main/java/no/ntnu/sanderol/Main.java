package no.ntnu.sanderol;

import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Main {
    public static final String BASE_URL = "http://datakomm.work";
    public static final String GET = "GET";

    public void sendGetRequest() {
        String path = "/dkrest/test/get";
        String url = BASE_URL + path;

        URL urlObj = null;
        try {
            urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod(GET);

            int resonseCode = connection.getResponseCode();
            if (resonseCode == 200) {
                System.out.println("Server connected with OK");
                // Response was OK, read the body (data)
                InputStream stream = connection.getInputStream();
                String responseBody = convertStreamToString(stream);
                stream.close();
                System.out.println("Response from the server:");
                System.out.println(responseBody);

                JSONObject object = new JSONObject(responseBody);
                String success = object.get("success").toString();

                System.out.println(success);
                System.out.println(object);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Read the whole content from an InputStream, return it as a string
     * @param is Inputstream to read the body from
     * @return The whole body as a string
     */
    private String convertStreamToString(InputStream is) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
                response.append('\n');
            }
        } catch (IOException ex) {
            System.out.println("Could not read the data from HTTP response: " + ex.getMessage());
        }
        return response.toString();
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.sendGetRequest();
    }
}
