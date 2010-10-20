package eu.delving.core.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

/**
 * This class handles the creation and checking from the key which allows people
 * to access the services REST API.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ServiceAccessToken {

    @Value("#{launchProperties['services.password']}")
    private String servicesPassword;

    public ServiceAccessToken() {
    }

    ServiceAccessToken(String servicesPassword) {
        this.servicesPassword = servicesPassword;
    }

    public boolean checkKey(String key) {
        return servicesPassword != null && checkKey(key, servicesPassword);
    }

    public String createKey(String prefix) {
        return createKey(prefix, servicesPassword);
    }

    static boolean checkKey(String key, String servicesPassword) {
        if (key.length() <= 22) {
            return false;
        }
        key = key.toUpperCase();
        int dash = key.indexOf('-');
        if (dash < 0) {
            return false;
        }
        String userToken = key.substring(0, dash);
        String hash = key.substring(dash + 1);
        String calculatedHash = hash(userToken, servicesPassword);
        return calculatedHash.equals(hash);
    }

    static String hash(String userToken, String servicesPassword) {
        ShaPasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder();
        shaPasswordEncoder.setEncodeHashAsBase64(false);
        String hash = shaPasswordEncoder.encodePassword(userToken.toUpperCase() + " hashed against " + servicesPassword, null);
        return hash.substring(0,20).toUpperCase();
    }

    static String createKey(String userToken, String servicesPassword) {
        return userToken.toUpperCase()+"-"+hash(userToken,servicesPassword);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: <user-token> <services-password>");
            System.exit(0);
        }
        String userToken = args[0];
        String servicesPassword = args[1];
        System.out.println("Key is "+createKey(userToken, servicesPassword));
    }

}
