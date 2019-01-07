package org.fastpay.common;

import spark.utils.IOUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

// Helper methods for unit test
public abstract class TestBase {

    protected String loadFileFromResource(String fileName) {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    protected String submitRequest(String method, String path) {
        return submitRequest(method, path, null, null);
    }

    protected String submitRequest(String method, String path, String body) {
        return submitRequest(method, path, null, body);
    }

    protected String submitRequest(String method, String path, Map<String, String> queryParameters) {
        return submitRequest(method, path, queryParameters, null);
    }

    protected String submitRequest(String method, String path, Map<String, String> queryParameters, String jsonBody) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("http://localhost:4567");
            sb.append(path);
            if (queryParameters != null) {
                String params = ParameterStringBuilder.getParamsString(queryParameters);
                sb.append("?");
                sb.append(params);
            }

            URL url = new URL(sb.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            if (jsonBody != null) {
                connection.setRequestProperty("Content-length", jsonBody.getBytes().length + "");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                //send the json as body of the request
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonBody.getBytes("UTF-8"));
                outputStream.close();
            }

            connection.connect();
            if (connection.getResponseCode() < 400) {
                return IOUtils.toString(connection.getInputStream());
            } else {
                return IOUtils.toString(connection.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
