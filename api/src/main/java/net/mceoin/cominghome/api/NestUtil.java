/*
 * Copyright 2014 Randy McEoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mceoin.cominghome.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Utilities for calling Nest.
 *
 * <ul>
 * <li>{@link #getNestAwayStatus(String, String)}</li>
 * <li>{@link #tellNestAwayStatus(String, String, String)}</li>
 * </ul>
 */
public class NestUtil {

    private static final Logger log = Logger.getLogger(NestUtil.class.getName());

    /**
     * A wrapper for the actual call {@link #tellNestAwayStatusCall(String, String, String)}.
     * It tries the actual call.  If that returns an error, it
     * will sleep for a random period then try one more time.
     *
     * @param access_token Token to be used to allow access to Nest
     * @param structure_id Nest id for the structure where the thermostat is located
     * @param away_status  Either "home" or "away"
     * @return Equal to "Success" if successful, otherwise it contains a hint on the error.
     */
    public static String tellNestAwayStatus(String access_token, String structure_id, String away_status) {

        String result = tellNestAwayStatusCall(access_token, structure_id, away_status);

        if ((result.contains("Error:")) && (!result.contains("Unauthorized"))) {
            // Try again if it was an Error but not an Unauthorized
            try {
                Random randomGenerator = new Random();
                int seconds = 5 + randomGenerator.nextInt(10);
                log.info("retry in " + seconds + " seconds");
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = tellNestAwayStatusCall(access_token, structure_id, away_status);
        }

        return result;
    }

    /**
     * Make HTTP/JSON call to Nest and set away status.
     *
     * @param access_token OAuth token to allow access to Nest
     * @param structure_id ID of structure with thermostat
     * @param away_status  Either "home" or "away"
     * @return Equal to "Success" if successful, otherwise it contains a hint on the error.
     */
    public static String tellNestAwayStatusCall(String access_token, String structure_id, String away_status) {

        String urlString = "https://developer-api.nest.com/structures/" + structure_id + "/away?auth=" + access_token;
        //log.info("url=" + urlString);

        StringBuilder builder = new StringBuilder();
        boolean error = false;
        String errorResult = "";

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "ComingHomeBackend/1.0");
            urlConnection.setRequestMethod("PUT");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setChunkedStreamingMode(0);

            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf8");

            String payload = "\"" + away_status + "\"";

            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(payload);
            wr.flush();
            log.info(payload);

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = urlConnection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == 307    // Temporary redirect
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

//            System.out.println("Response Code ... " + status);

            if (redirect) {

                // get redirect url from "location" header field
                String newUrl = urlConnection.getHeaderField("Location");

                // open the new connnection again
                urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setChunkedStreamingMode(0);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf8");
                urlConnection.setRequestProperty("Accept", "application/json");

//                System.out.println("Redirect to URL : " + newUrl);

                wr = new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write(payload);
                wr.flush();

            }

            int statusCode = urlConnection.getResponseCode();

            log.info("statusCode=" + statusCode);
            if ((statusCode == HttpURLConnection.HTTP_OK)) {
                error = false;
            } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // bad auth
                error = true;
                errorResult = "Unauthorized";
            } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                error = true;
                InputStream response;
                response = urlConnection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                log.info("response=" + builder.toString());
                JSONObject object = new JSONObject(builder.toString());

                Iterator keys = object.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (key.equals("error")) {
                        // error = Internal Error on bad structure_id
                        errorResult = object.getString("error");
                        log.info("errorResult=" + errorResult);
                    }
                }
            } else {
                error = true;
                errorResult = Integer.toString(statusCode);
            }

        } catch (IOException e) {
            error = true;
            errorResult = e.getLocalizedMessage();
            log.warning("IOException: " + errorResult);
        } catch (Exception e) {
            error = true;
            errorResult = e.getLocalizedMessage();
            log.warning("Exception: " + errorResult);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        if (error) {
            return "Error: " + errorResult;
        } else {
            return "Success";
        }
    }

    /**
     * Get the status of the specified structure.
     *
     * @param access_token Token that allows access
     * @param structure_id ID of desired structure
     * @return Status.  For example 'home' or 'away'.
     */
    public static String getNestAwayStatus(String access_token, String structure_id) {
        String result = getNestAwayStatusCall(access_token, structure_id);

        if ((result.contains("Error:")) && (!result.contains("Unauthorized"))) {
            // Try again if it was an Error but not an Unauthorized
            try {
                Random randomGenerator = new Random();
                int seconds = 5 + randomGenerator.nextInt(10);
                log.info("retry in " + seconds + " seconds");
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = getNestAwayStatusCall(access_token, structure_id);
        }

        return result;
    }

    private static String getNestAwayStatusCall(String access_token, String structure_id) {

        String away_status = "";

        String urlString = "https://developer-api.nest.com/structures?auth=" + access_token;
        //log.info("url=" + urlString);

        StringBuilder builder = new StringBuilder();
        boolean error = false;
        String errorResult = "";

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "ComingHomeBackend/1.0");
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
//            urlConnection.setChunkedStreamingMode(0);

//            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf8");

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = urlConnection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == 307    // Temporary redirect
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

//            System.out.println("Response Code ... " + status);

            if (redirect) {

                // get redirect url from "location" header field
                String newUrl = urlConnection.getHeaderField("Location");

                // open the new connnection again
                urlConnection = (HttpURLConnection) new URL(newUrl).openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
//                urlConnection.setChunkedStreamingMode(0);

//                System.out.println("Redirect to URL : " + newUrl);

            }

            int statusCode = urlConnection.getResponseCode();

            log.info("statusCode=" + statusCode);
            if ((statusCode == HttpURLConnection.HTTP_OK)) {
                error = false;

                InputStream response;
                response = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                log.info("response=" + builder.toString());
                JSONObject object = new JSONObject(builder.toString());

                // The response can contain multiple structures.
                //
                // { "n3i0tZr6F8YZZk8wZOikSWI7VuRJiC7h3TETUo0s_-CiUIImsMlg":
                //  {
                //      "name":"test home",
                //      "country_code":"US",
                //      "time_zone":"America/Los_Angeles",
                //      "away":"home",
                //      "thermostats":["PGq6O4cTqO7tNpDjWzT1lVoZTWQG0s2y","PGq6O4cTqO5QIcACON2TPVoZTWQG0s2y"],
                //      "structure_id":"n3i0tZr6F8YZZk8wZOikSWI7VuRJiC7h3TETUo0s_-CiUIImsMlg"},
                //  "cLLE1o_C0kqB3sXC2jwzrWI7VuRJiC7h3TETUo0s_-CiUIImsMlg":
                //  {
                //      "name":"Cabin Retreat",
                //      "country_code":"US",
                //      "time_zone":"America/Los_Angeles",
                //      "away":"away",
                //      "thermostats":["PGq6O4cTqO7Z17srVrCMU1oZTWQG0s2y"],
                //      "structure_id":"cLLE1o_C0kqB3sXC2jwzrWI7VuRJiC7h3TETUo0s_-CiUIImsMlg"}
                // }
                Iterator keys = object.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    JSONObject structure = object.getJSONObject(key);

                    if (structure.has("structure_id")) {
                        if (structure.getString("structure_id").equals(structure_id)) {
                            if (structure.has("away")) {
                                away_status = structure.getString("away");
                            } else {
                                log.info("missing away");
                            }
                        } else {
                            // after initial debugging, should pull this
                            log.info("skipping structure_id=" + structure.getString("structure_id"));
                        }
                    } else {
                        log.info("missing structure_id");
                    }
                }

            } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // bad auth
                error = true;
                errorResult = "Unauthorized";
                log.warning("Unauthorized");
            } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                error = true;
                InputStream response;
                response = urlConnection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                log.info("response=" + builder.toString());
                JSONObject object = new JSONObject(builder.toString());

                Iterator keys = object.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (key.equals("error")) {
                        // error = Internal Error on bad structure_id
                        errorResult = object.getString("error");
                        log.info("errorResult=" + errorResult);
                    }
                }
            } else {
                error = true;
                errorResult = Integer.toString(statusCode);
            }

        } catch (IOException e) {
            error = true;
            errorResult = e.getLocalizedMessage();
            log.warning("IOException: " + errorResult);
        } catch (Exception e) {
            error = true;
            errorResult = e.getLocalizedMessage();
            log.warning("Exception: " + errorResult);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        if (error) away_status = "Error: " + errorResult;
        return away_status;
    }

}
