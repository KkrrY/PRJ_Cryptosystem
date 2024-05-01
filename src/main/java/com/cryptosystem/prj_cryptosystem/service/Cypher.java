package com.cryptosystem.prj_cryptosystem.service;

import com.cryptosystem.prj_cryptosystem.EncryptionAccessor;
import javafx.scene.control.TextArea;

import java.util.InputMismatchException;
import java.util.logging.Logger;
import static java.util.logging.Level.SEVERE;

public interface Cypher {
    String decrypt(TextArea controller, TextArea keyTextArea) ;

    String encrypt(TextArea inputTextArea, TextArea keyTextArea) ;
    EncryptionAccessor getEncryptionAccessor ();
    default void generateKeyPair(TextArea keyLengthArea, TextArea publicKeyArea, TextArea privateKeyArea) {}

    default void validate (String cypherString) throws NullPointerException {
        if (cypherString == "") {
            Logger.getLogger(getClass().getName()).log(SEVERE, (String) null);
            throw new InputMismatchException("Text area shouldn't be empty");
        }
    }

    /**
     * Checks if a String appears to be readable text.
     * @param s the String to check
     * @return true if the String appears to be readable text, false otherwise
     */
    static boolean isReadable(String s) {
        // Check if the String contains only printable ASCII characters and spaces
        return s.matches("[\\x20-\\x7E]+");
    }

    void attack(TextArea inputTextArea) ;
}
