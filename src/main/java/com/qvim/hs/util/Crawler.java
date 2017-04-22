package com.qvim.hs.util;

import com.qvim.hs.util.logger.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by RINES on 21.04.17.
 */
public class Crawler {

    public static String executeGet(String urlGet) {
        try {
            URL url = new URL(urlGet);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null)
                    sb.append(line);
                return sb.toString();
            }
        } catch (Exception ex) {
            Logger.warn("Could not execute get-query!", ex);
            return null;
        }
    }

    public static InputStream execute(String urlGet) {
        try {
            String[] spl = urlGet.split("\\?");
            String urlClean = spl[0];
            URL url = new URL(urlClean);
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            if(spl.length > 1) {
                String params = spl[1];
                http.setDoOutput(true);
                byte[] out = params.getBytes("UTF-8");
                int length = out.length;
                http.setFixedLengthStreamingMode(length);
                http.connect();
                try (OutputStream os = http.getOutputStream()) {
                    os.write(out);
                }
            }
            return http.getInputStream();
        } catch (Exception ex) {
            Logger.warn("Could not execute post-query '" + urlGet + "'!", ex);
            return null;
        }
    }

    public static String executeAndGet(String urlGet) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(execute(urlGet), "UTF-8"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while((line = reader.readLine()) != null)
                sb.append(line);
            return sb.toString();
        } catch (Exception ex) {
            Logger.warn("Could not execute and get post-query!", ex);
            return null;
        }
    }

    public static String encode(String toEncode, Object... params) {
        return encode(String.format(toEncode, params));
    }

    public static String encode(String toEncode) {
        try {
            return URLEncoder.encode(toEncode, "UTF-8");
        } catch (Exception ex) {
            Logger.warn("Could not encode given text!");
            return toEncode;
        }
    }

}
