package pl.softwaremill.asamal.example.service.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringHasher {

    /**
     * Returns a MD5 hash of a passed string
     */
    public String encode(String toHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            return new String(md.digest(toHash.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            throw new RuntimeException(e);
        }
    }
}
