package no.ntnu.sanderol;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Main {
    public static final String BASE_URL = "http://datakomm.work";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String ARGUMENTS = "arguments";
    public static final String DKREST_SOLVE = "/dkrest/solve";
    public static final String GET_TASK = "/dkrest/gettask/";
    public static final String SESSION_ID_TAG = "?sessionId=";
    public static final String EMAIL = "stefhola@stud.ntnu.no";
    public static final String PHONE_NUM = "93865541";
    public static final String DKREST_RESULTS = "/dkrest/results/";
    public static final String DKREST_RESULTS_TOP = "/dkrest/results/top";

    public JSONObject sendPostRequest(String path, JSONObject object) {
        System.out.println(object);
        String url = BASE_URL + path;
        System.out.println(url);
        JSONObject jsonObject;

        URL urlObj = null;
        try {
            urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod(POST);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(object.toString().getBytes());
            os.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Server connected with OK");
                // Response was OK, read the body (data)
                InputStream stream = connection.getInputStream();
                String responseBody = convertStreamToString(stream);
                stream.close();
                System.out.println("Response from the server:");
                System.out.println(responseBody);

                jsonObject = new JSONObject(responseBody);
                return jsonObject;
            } else {
                String responseDescription = connection.getResponseMessage();
                System.out.println("Request failed, response code: " + responseCode + " (" + responseDescription + ")");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        jsonObject = null;
        return jsonObject;
    }

    public JSONObject sendGetRequest(String path) {
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
                return new JSONObject(responseBody);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }

    /**
     * Read the whole content from an InputStream, return it as a string
     *
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
        main.sendGetRequest("/dkrest/test/get");

        int a = main.randomWithRange(1, 50);
        int b = main.randomWithRange(1, 50);

        JSONObject object = new JSONObject();
        object.put("a", a);
        object.put("b", b);

        main.sendPostRequest("/dkrest/test/post", object);

        final String AUTH = "/dkrest/auth";
        JSONObject token = new JSONObject();
        token.put("email", EMAIL);
        token.put("phone", PHONE_NUM);

        JSONObject me = main.sendPostRequest(AUTH, token);
        JSONObject response = new JSONObject();
        int sessionId = Integer.parseInt(me.get("sessionId").toString());
        System.out.println("session ID " + sessionId);

        int tasknum = 1;

        main.sendGetRequest(GET_TASK + tasknum + SESSION_ID_TAG + sessionId);

        me.put("msg", "Hello");
        main.sendPostRequest(DKREST_SOLVE, me);

        tasknum = tasknum + 1;
        response = main.sendGetRequest(GET_TASK + tasknum + SESSION_ID_TAG + sessionId);
        String msg = response.getJSONArray(ARGUMENTS).get(0).toString();

        me.put("msg", msg);
        main.sendPostRequest(DKREST_SOLVE, me);

        tasknum = tasknum + 1;
        response = main.sendGetRequest(GET_TASK + tasknum + SESSION_ID_TAG + sessionId);
        JSONArray jsonArray = response.getJSONArray(ARGUMENTS);

        int multi = 1;
        for (int i = 0; i < jsonArray.length(); i++) {
            multi = multi * Integer.parseInt(jsonArray.get(i).toString());
            System.out.println(multi);
        }

        me.put("result", multi);
        main.sendPostRequest(DKREST_SOLVE, me);

        tasknum = tasknum + 1;
        response = main.sendGetRequest(GET_TASK + tasknum + SESSION_ID_TAG + sessionId);
        String md5 = response.getJSONArray(ARGUMENTS).get(0).toString();

        boolean found = false;
        int i = 0;
        while (!found && i < 9999) {
            if (getMd5(String.valueOf(i)).equals(md5)) {
                found = true;
            } else {
                i++;
            }
        }
        me.put("pin", i);
        main.sendPostRequest(DKREST_SOLVE, me);

        response = main.sendGetRequest(GET_TASK + 2016 + SESSION_ID_TAG + sessionId);
        String ipAddr = response.getJSONArray(ARGUMENTS).get(0).toString();
        String subNet = response.getJSONArray(ARGUMENTS).get(1).toString();
        IPv4 iPv4 = new IPv4(ipAddr, subNet);
        List<String> availableIpS =iPv4.getAvailableIPs(3);
        String availableIp = availableIpS.get(0);

        me.put("ip", availableIp);
        main.sendPostRequest(DKREST_SOLVE,me);

        main.sendGetRequest(DKREST_RESULTS +sessionId);
        main.sendGetRequest(DKREST_RESULTS_TOP);

    }

    public static String getMd5(String input)
    {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
