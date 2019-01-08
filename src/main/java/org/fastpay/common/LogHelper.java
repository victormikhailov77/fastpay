package org.fastpay.common;

import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@Slf4j
public class LogHelper {

    private LogHelper() {}

    public static String requestToString(Request request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request: ");
        sb.append(request.requestMethod());
        sb.append(" " + request.url());
        sb.append(" " + request.body());
        return sb.toString();
    }

    public static String responseToString(Response response) {
        StringBuilder sb = new StringBuilder();
        HttpServletResponse raw = response.raw();
        if (response.body() != null ) {
            sb.append(" Reponse: " + response.body());
            sb.append(" " + raw.getHeader("content-type"));
            try {
                sb.append(" body size in bytes: " + response.body().getBytes(raw.getCharacterEncoding()).length);
            } catch (UnsupportedEncodingException e) {
                log.error("Exception during parsing response: ", e);
            }
        }
        return sb.toString();
    }
}
