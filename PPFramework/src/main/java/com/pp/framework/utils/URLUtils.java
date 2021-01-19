package com.pp.framework.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils {

    public static String generateFullURL(String baseURLString, String referenceURL) throws MalformedURLException {
        URL baseUrl;
        try {
            baseUrl = new URL(referenceURL);
            return baseUrl.toString();
        } catch (MalformedURLException e) {
            baseUrl = new URL(baseURLString);
        }

        String cleanBaseURL = baseUrl.getProtocol() + "://" + baseUrl.getHost();
        if (referenceURL.charAt(0) != '/') {
            referenceURL = '/' + referenceURL;
        }
        URL fullURL = new URL(cleanBaseURL + referenceURL);
        return fullURL.toString();
    }


    public static boolean isValidUrl(String url) {
        return url.startsWith("http");
    }

    public static String escapeURLRegex(String url) {
        String escapedURL = url.replaceAll("\\(","\\\\(");
        escapedURL = escapedURL.replaceAll("\\)","\\\\)");
        return escapedURL;
    }
}
