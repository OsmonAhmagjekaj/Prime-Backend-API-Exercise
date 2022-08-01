package io.exercise.api.utils;

import org.mindrot.jbcrypt.BCrypt;
/**
 * Password utility class. This handles password encryption and validation.
 * User: yesnault Date: 25/01/12
 */
public class Hash {
    /**
     * Create an encrypted password from a clear string.
     *
     * @param encryptedString the encrypted string
     * @return an encrypted password of the clear string
     */
    public static String createPassword(String encryptedString) throws Exception {
        return BCrypt.hashpw(encryptedString, BCrypt.gensalt());
    }
    /**
     *  Check the password
     *
     * @param encryptedCandidate the  encryptedText
     * @param encryptedPassword the encrypted password string to check.
     * @return true if the candidate matches, false otherwise.
     */
    public static boolean checkPassword(String encryptedCandidate, String encryptedPassword) throws Exception {
        if (encryptedCandidate == null) {
            return false;
        }
        if (encryptedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(encryptedCandidate, encryptedPassword);
    }
}
