package utils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class InsecureHostnameVerifier implements HostnameVerifier {
    public InsecureHostnameVerifier(){

    }

    public boolean verify(String hostname, SSLSession session) {
        return true;
    }

}
