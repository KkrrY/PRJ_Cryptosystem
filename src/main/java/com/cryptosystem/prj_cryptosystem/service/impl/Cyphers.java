package com.cryptosystem.prj_cryptosystem.service.impl;

import com.cryptosystem.prj_cryptosystem.EncryptionAccessor;
import com.cryptosystem.prj_cryptosystem.service.Cypher;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import lombok.SneakyThrows;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.function.IntBinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Cyphers {
    public static class Caesar implements Cypher {
        EncryptionAccessor accessor;
        public Caesar(EncryptionAccessor accessor) {
            this.accessor = accessor;
        }
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }
        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {
            String cypherString = inputTextArea.getText();
            validate(cypherString);

            BigInteger key = new BigInteger(keyTextArea.getText());

            StringBuilder sb = new StringBuilder();

            for (char c: cypherString.toCharArray() ) { //this is equation of kind `k = B`, where B is a coefficient we move our letters
                sb.append((char) (c - key.mod(BigInteger.valueOf(Character.MAX_VALUE)).intValue())); //Character.MAX_VALUE concat all key values higher than 65535
            }
            return sb.toString();

        }

        @Override
        public String encrypt(TextArea inputTextArea, TextArea keyTextArea) {
            String cypherString = inputTextArea.getText();
            validate(cypherString);

            BigInteger key = new BigInteger(keyTextArea.getText());

            StringBuilder sb = new StringBuilder(); //empty String the encrypted text will be

            for (char c : cypherString.toCharArray()) {
                sb.append((char) (c + key.mod(BigInteger.valueOf(Character.MAX_VALUE)).intValue())); // cast to char to get Unicode
                //This code uses key.mod(BigInteger.valueOf(Character.MAX_VALUE)) to calculate the remainder when the key is divided by Character.MAX_VALUE (which is 65535), which ensures that the resulting value is always within the range of a char value.
            }

            return sb.toString();
        }

        @Override
        public void attack(TextArea inputTextArea) {
            TextArea textArea = new TextArea();
            String result = "";
            for (int key = 0 ; key <= Character.MAX_VALUE ; key++) {
                textArea.setText(String.valueOf(key));
                if (Cypher.isReadable(decrypt(inputTextArea, textArea) )  )
                    result += System.lineSeparator() + "key is: " + key +
                            System.lineSeparator() + decrypt(inputTextArea, textArea ) + System.lineSeparator();
            }
            inputTextArea.setText(result);
        }

    }

    public static class Trithemius implements Cypher {
        EncryptionAccessor accessor;
        public Trithemius(EncryptionAccessor accessor) {
            this.accessor = accessor;
        }
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }
        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {
            return doCypher(inputTextArea, keyTextArea, (x, y) -> x - y );
        }


        @Override
        public String encrypt(TextArea inputTextArea, TextArea keyTextArea) {
            return doCypher(inputTextArea, keyTextArea, Integer::sum);
        }

        public String doCypher(TextArea inputTextArea, TextArea keyTextArea, IntBinaryOperator operator) {
            String textAreaContainment = inputTextArea.getText();
            String textAreaKey = keyTextArea.getText();

            StringBuilder ciphertext = new StringBuilder();

            int length = textAreaContainment.length();

            if (textAreaKey.matches(".*[a-zA-Z].*")) { //any letter occurrence
                for (int i = 0; i < length; i++) {
                    char c = textAreaContainment.charAt(i);
                    // Apply the shift parameter to the character using the key and append it to the ciphertext
                    ciphertext.append( (char) (operator.applyAsInt(c, getKeyShift(textAreaKey, i) ) )); //applyAsInt represents arithmetic operation
                }
                return ciphertext.toString();
            }

            //encrypting using equations like `k = Ap + B` or `k = Ap^2 + Bp + C` or `k = Ap^3 + Bp^2 + Cp + D` , etc...
            String filteredTextAreaKey = textAreaKey.replaceAll("\\s+", " ").trim(); //trim removes any spaces at start and end
            String digitOccurrence = "\\b\\d+\\b"; //This regular expression uses the word boundary anchor (\b) to match each sequence of digits as a separate word.
            Pattern pattern = Pattern.compile(digitOccurrence);
            Matcher matcher = pattern.matcher(filteredTextAreaKey);
            int occurrenceCount = 0;
            while (matcher.find()) {
                occurrenceCount++;
            }

            String[] parts = filteredTextAreaKey.split(" ");
            int[] coefficients = new int[occurrenceCount];
            for (int i = 0; i< occurrenceCount; i++) {
                coefficients[i] = Integer.parseInt(parts[i]);
            }

            for (int i = 0; i < length; i++) {
                int equation = 0;
                int equationPart = 0; //new value because of applyAsInt method ( we can't append to equation applyOperation between our equation and part of it ).
                for (int j = 0; j < occurrenceCount; j++) {
                    //coefficients.length - j - 1 represents current position in the equation
                    equation += operator.applyAsInt(equationPart, (int) (Math.pow( i+1, coefficients.length - j - 1) * coefficients[j])); //i+1 because the first member would be 0
                }
                char c = textAreaContainment.charAt(i);
                ciphertext.append((char) (c + equation));
            }
            return ciphertext.toString();

        }

        @Override
        public void attack(TextArea inputTextArea) {

        }

        /**
         * Calculates the shift parameter for a given position in the plaintext/ciphertext using the key.
         * @param position the position of the character in the plaintext/ciphertext
         * @return the shift parameter to be applied to the character
         */
        private static int getKeyShift(String KEY, int position) {
            // Calculate the index of the key to use based on the position
            int keyIndex = position % KEY.length();
            // Get the Unicode value of the key character
            int keyShift = KEY.charAt(keyIndex);
            return keyShift;
        }




    }

    public static class XOR implements Cypher {
        EncryptionAccessor accessor;
        public XOR(EncryptionAccessor accessor) {
            this.accessor = accessor;
        }
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }
        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {
            return doCypher(inputTextArea, keyTextArea);
        }

        @Override
        public String encrypt(TextArea inputTextArea, TextArea keyTextArea) {
            return doCypher(inputTextArea, keyTextArea);
        }

        @Override
        public void attack(TextArea inputTextArea) {

        }

        private static String doCypher (TextArea inputTextArea, TextArea keyTextArea) {
            String input = inputTextArea.getText();
            String seed = keyTextArea.getText();

            int length = (input.length() > 100) ? 100 : input.length(); //concerning to the Geo-proton approach, key is impossible to crack when its length >= 100

            Random random = new Random(seed.hashCode());
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < length; i++) { //generating String using seed
                //+ 1 is added to include the maximum value in the range. Without this addition, the range would be from 0 to 1114110, excluding the highest possible value of 1114111.
                char c = (char) (random.nextInt(0x10FFFF + 1)); // 0x10FFFF value used in the nextInt() method represents the highest possible Unicode value. Using this value ensures that all possible Unicode characters are included in the generated String.
                sb.append(c);
            }

            String key = sb.toString();

            StringBuilder output = new StringBuilder();

            for (int i = 0; i < input.length(); i++) {
                output.append((char) (input.charAt(i) ^ key.charAt(i % (key.length() ) )));
            }

            return output.toString();
        }


    }

    public static class DisposableNotebooks implements Cypher {
        EncryptionAccessor accessor;
        public DisposableNotebooks(EncryptionAccessor accessor) {
            this.accessor = accessor;
        }
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }
        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {
            return decryptMessage(inputTextArea, keyTextArea);
        }

        @Override
        public String encrypt(TextArea inputTextArea, TextArea keyTextArea) {
            int[] message = encryptMessage(inputTextArea, keyTextArea);
            return Arrays.toString(message).replaceAll("[\\[|\\]]|\\s", "");
        }

        @Override
        public void attack(TextArea inputTextArea) {

        }

        /**
         * Key.length() returns the length of the key String.
         * key.length() - 1 subtracts 1 from the length of the key String. This is done to ensure that the index used to retrieve the character from the key stays within the bounds of the key String.
         * i % (key.length() - 1) calculates the remainder of i divided by key.length() - 1. This ensures that the index used to retrieve the character from the key String is always within the bounds of the key String, and also allows the key to be repeated cyclically if the input String is longer than the key.
         * key.charAt(i % (key.length() - 1)) retrieves the character from the key String at the index calculated in the previous step.
         * @param  inputTextArea Containing the input text
         * @param  keyTextArea Containing the key value
         * @return Encrypted integer array as a gamma.
         */
        private static int[] encryptMessage(TextArea inputTextArea, TextArea keyTextArea) {
            String str = inputTextArea.getText();
            String key = keyTextArea.getText();
            int[] output = new int[str.length()];
            for(int i = 0; i < str.length(); i++) {
                int o = ((int) str.charAt(i) ^ (int) key.charAt(i % (key.length() - 1))) + '0'; //str.charAt(i) retrieves the character at position i from the input String str. The (int) casts the character to its integer Unicode value. key.charAt(i % (key.length() - 1)) retrieves the character from the key at the position given by the modulus of i and the length of the key minus one. This ensures that the key is used repeatedly to encrypt the message. The XOR operator (^) is applied to the two integer values obtained from the previous steps. This operation will result in an integer value. The resulting integer value is then added to the ASCII value of the character '0' using the `+` operator. This ensures that the resulting integer value is a printable character within the ASCII range. The resulting integer value is assigned to the variable `o`.
                output[i] = o;
            }
            return output;
        }

        private static int[] parseToString(String str) {
            String[] sArr = str.split(",");
            int[] out = new int[sArr.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = Integer.parseInt(sArr[i]);
            }
            return out;
        }

        private static String decryptMessage(TextArea inputTextArea, TextArea keyTextArea) {

            if (inputTextArea.getText().matches(".*\\p{L}|\\p{S}.*")) return inputTextArea.getText();

            int[] input = parseToString(inputTextArea.getText());
            String key = keyTextArea.getText();
            String output = "";
            for(int i = 0; i < input.length; i++) {
                output += (char) ((input[i] - 48) ^ (int) key.charAt(i % (key.length() - 1))); // - 48 subtracts the integer value of the character '0' to get the original value of the encrypted character. This is because during the encryption process, the ASCII value of the character '0' was added to the result of the XOR operation to ensure that the resulting integer value was a printable character within the ASCII range.
            }
            return output;
        }


    }

    public static class Book implements Cypher {
        EncryptionAccessor accessor;
        public Book(EncryptionAccessor accessor) {
            this.accessor = accessor;
        }
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }

        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {
            char[][] key = initKey(keyTextArea.getText());
            StringBuilder result = new StringBuilder();
            String[] codes = inputTextArea.getText().split(",");
            for (String code : codes) {
                if (!code.isEmpty()) {
                    result.append(decodeSymbol(code, key));
                }
            }
            return result.toString();
        }

        @Override
        public String encrypt(TextArea inputTextArea, TextArea keyTextArea) {
            char[][] key = initKey(keyTextArea.getText());
            String message = inputTextArea.getText().toLowerCase();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < message.length(); i++) {
                char symbol = message.charAt(i);

                result.append(encodeSymbol(symbol, key));

            }
            return result.toString();
        }

        private char[][] initKey (String poem) {
            //Parse whole String to a bigger array and then truncate it to 10x10 array
            String[] lines = poem.split("\\r?\\n");
            int numRows = lines.length;
            int numCols = 0;
            for (String line : lines) {
                int tempVal = Math.max(numCols, line.length());
                if (tempVal > numCols) numCols = tempVal;
            }
            char[][] result = new char[numRows][numCols];
            for (int i = 0; i < numRows; i++) {
                String line = lines[i];
                for (int j = 0; j < line.length(); j++) {
                    //if (Character.isWhitespace(line.charAt(j))) continue;
                    result[i][j] = Character.toLowerCase(line.charAt(j));
                    //TODO: Remember letter state ?
                }
            }

            return result;
        }

        private String encodeSymbol(char symbol, char[][] key) {
            List<int[]> coordinates = new ArrayList<>();
            for (int i = 0; i < key.length; i++) {
                for (int j = 0; j < key[0].length; j++) { //width if array is not equal
                    if (key[i][j] == symbol) {
                        coordinates.add(new int[]{i, j}); //collecting occurrence
                    }
                }
            }
            if (coordinates.size() > 1) {
                Random random = new Random();
                int[] randomCord = coordinates.get(random.nextInt(coordinates.size()));
                return String.format("%02d/%02d,", randomCord[0], randomCord[1]);
            } else if (coordinates.size() == 1) {
                int[] cord = coordinates.get(0);
                return String.format("%02d/%02d,", cord[0], cord[1]);
            } else {
                return "";
            }
        }

        private char decodeSymbol(String code, char[][] key ) {
            int row = Integer.parseInt(code.substring(0, 2)); //returns the first two characters of the String `code`.
            int col = Integer.parseInt(code.substring(3, 5)); //returns characters from index 3 to 5th (2 is / so we skip).
            return key[row][col];
        }

        @Override
        public void attack(TextArea inputTextArea) {

        }
    }

    public static class DES implements Cypher {
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }
        protected RadioButton ECB;
        protected RadioButton CBC;
        protected RadioButton CFB;
        protected RadioButton OFB;
        protected RadioButton CTR;
        protected final Alert errorMessage ;
        protected String selectedEncryption = "ECB"; // default value
        EncryptionAccessor accessor;
        public DES(EncryptionAccessor accessor, RadioButton ECB, RadioButton CBC, RadioButton CFB, RadioButton OFB, RadioButton CTR, Alert alert) {
            this.accessor = accessor;

            this.ECB = ECB;
            this.CBC = CBC;
            this.CFB = CFB;
            this.OFB = OFB;
            this.CTR = CTR;

            errorMessage = alert;

            errorMessage.setTitle("Error");
            errorMessage.setHeaderText("Wrong key size");

            ECB.setSelected(true);
        }

        protected String getSelectedEncryption() {
            if (ECB.isSelected()) {
                return "ECB";
            }
            if (CBC.isSelected()) {
                return "CBC";
            }
            if (CFB.isSelected()) {
                return "CFB";
            }
            if (OFB.isSelected()) {
                return "OFB";
            }
            if (CTR.isSelected()) {
                return "CTR";
            }
            return null;
        }


        @SneakyThrows
        @Override
        public String encrypt(TextArea inputTextArea, TextArea keyTextArea) {
            selectedEncryption = getSelectedEncryption();
            if (keyTextArea.getText().length() != 8) {
                errorMessage.setContentText("This key is not appropriate for DES encryption.\nThe length must be 8");
                errorMessage.showAndWait();
            }

            if (selectedEncryption.equals("ECB")) {
                return provideEncryption("DES", "DES/ECB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CBC")) {
                return provideBlockEncryption("DES","DES/CBC/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CFB")) {
                return provideBlockEncryption("DES","DES/CFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("OFB")) {
                return provideBlockEncryption("DES","DES/OFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            return inputTextArea.getText();
        }

        @SneakyThrows
        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {
            selectedEncryption = getSelectedEncryption();
            if (keyTextArea.getText().length() != 8) {
                errorMessage.setContentText("This key is not appropriate for DES encryption.\nThe length must be 8");
                errorMessage.showAndWait();
            }

            if (selectedEncryption.equals("ECB")) {
                return provideDecryption("DES", "DES/ECB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CBC")) {
                return provideBlockDecryption("DES","DES/CBC/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CFB")) {
                return provideBlockDecryption("DES","DES/CFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("OFB")) {
                return provideBlockDecryption("DES","DES/OFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            return inputTextArea.getText();
        }

        //In UTF-8 Cyrillic letters take 2bytes instead of one, as English symbolic.
        protected String provideEncryption (String algorithm, String transformation, TextArea inputTextArea, TextArea keyTextArea) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
            //StandardCharsets.UTF could be passed
            byte[] keyBytes = keyTextArea.getText().getBytes("Windows-1251"); //Unicode can be represented in different ways, including UTF-8, UTF-16, and UTF-32. UTF-8 is a variable-length encoding scheme that uses 1 to 4 bytes to represent each Unicode character, while UTF-16 uses 2 or 4 bytes, and UTF-32 uses a fixed 4 bytes for each character.
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, algorithm); // "DES" => algorithm

            Cipher cipher = Cipher.getInstance(transformation); // "DES/ECB/PKCS5Padding" => transformation
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] plaintextBytes = inputTextArea.getText().getBytes("Windows-1251");
            int blockSize = cipher.getBlockSize();
            byte[] paddedPlaintext = new byte[(plaintextBytes.length / blockSize + 1) * blockSize];
            System.arraycopy(plaintextBytes, 0, paddedPlaintext, 0, plaintextBytes.length);

            byte[] ciphertextBytes = cipher.doFinal(paddedPlaintext);
            return Base64.getEncoder().encodeToString(ciphertextBytes);
        }

        protected String provideDecryption (String algorithm, String transformation, TextArea inputTextArea, TextArea keyTextArea) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
            byte[] keyBytes = keyTextArea.getText().getBytes("Windows-1251");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, algorithm);

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] ciphertextBytes = Base64.getDecoder().decode(inputTextArea.getText());
            byte[] paddedPlaintextBytes = cipher.doFinal(ciphertextBytes);

            // Remove the padding bytes from the plaintext
            int paddingLength = paddedPlaintextBytes[paddedPlaintextBytes.length - 1];
            byte[] plaintextBytes = new byte[paddedPlaintextBytes.length - paddingLength];
            System.arraycopy(paddedPlaintextBytes, 0, plaintextBytes, 0, plaintextBytes.length);

            return new String(plaintextBytes, "Windows-1251");
        }

        protected String provideBlockEncryption(String algorithm, String transformation, TextArea inputTextArea, TextArea keyTextArea) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
            byte[] keyBytes = keyTextArea.getText().getBytes("Windows-1251");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, algorithm);

            Cipher cipher = Cipher.getInstance(transformation);
            SecureRandom secureRandom = new SecureRandom();
            byte[] iv = new byte[cipher.getBlockSize()]; //In DES, the initialization vector (IV) is a fixed-size input to the encryption algorithm that is used to initialize the block cipher's state. The IV is typically the same size as the block size of the cipher, which for DES is 64 bits. The purpose of the IV is to ensure that the same plaintext does not always produce the same ciphertext, which can be vulnerable to certain types of attacks. When using DES in a block cipher mode like CBC or CTR, the IV is combined with the first plaintext block before encryption.
            //The IV should be unique and unpredictable for each encryption operation to provide the maximum security benefit. Reusing the same IV for multiple encryption operations can allow an attacker to gain information about the plaintext or the key, making the encryption less secure. One way to generate a unique and unpredictable IV is to use a secure random number generator to generate a new IV for each encryption operation.
            secureRandom.nextBytes(iv);

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] plaintextBytes = inputTextArea.getText().getBytes("Windows-1251");
            int blockSize = cipher.getBlockSize();
            byte[] paddedPlaintext = new byte[(plaintextBytes.length / blockSize + 1) * blockSize];
            System.arraycopy(plaintextBytes, 0, paddedPlaintext, 0, plaintextBytes.length);

            byte[] ciphertextBytes = cipher.doFinal(paddedPlaintext);
            byte[] ivAndCiphertextBytes = new byte[iv.length + ciphertextBytes.length];
            System.arraycopy(iv, 0, ivAndCiphertextBytes, 0, iv.length);
            System.arraycopy(ciphertextBytes, 0, ivAndCiphertextBytes, iv.length, ciphertextBytes.length);

            return Base64.getEncoder().encodeToString(ivAndCiphertextBytes);
        }

        protected String provideBlockDecryption(String algorithm, String transformation, TextArea inputTextArea, TextArea keyTextArea) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
            byte[] keyBytes = keyTextArea.getText().getBytes("Windows-1251");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, algorithm);

            Cipher cipher = Cipher.getInstance(transformation);
            byte[] ivAndCiphertextBytes = Base64.getDecoder().decode(inputTextArea.getText());
            byte[] iv = Arrays.copyOfRange(ivAndCiphertextBytes, 0, cipher.getBlockSize());
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            byte[] ciphertextBytes = Arrays.copyOfRange(ivAndCiphertextBytes, iv.length, ivAndCiphertextBytes.length);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] paddedPlaintextBytes = cipher.doFinal(ciphertextBytes);

            // Remove the padding bytes from the plaintext
            int paddingLength = paddedPlaintextBytes[paddedPlaintextBytes.length - 1];
            byte[] plaintextBytes = new byte[paddedPlaintextBytes.length - paddingLength];
            System.arraycopy(paddedPlaintextBytes, 0, plaintextBytes, 0, plaintextBytes.length);

            return new String(plaintextBytes, "Windows-1251");
        }

        @Override
        public void attack(TextArea inputTextArea) {

        }

        static {
            //list of available cyphers with their mods
            for (Provider provider : Security.getProviders()) {
                for (String algorithm : provider.stringPropertyNames()) {
                    if (algorithm.startsWith("Cipher.")) {
                        System.out.println(algorithm.substring(7)); //from 7 to truncate `Cipher.`
                    }
                }
            }
        }


    }

    public static class DES_EDE extends DES {
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }
        EncryptionAccessor accessor = EncryptionAccessor.Symmetric; //TODO: didn't set accessor properly through the constructor
        public DES_EDE(EncryptionAccessor accessor, RadioButton ECB, RadioButton CBC, RadioButton CFB, RadioButton OFB, RadioButton CTR, Alert alert) {
            super(accessor, ECB, CBC, CFB, OFB, CTR, alert);
            Security.addProvider(new BouncyCastleProvider());
        }

        @SneakyThrows
        @Override
        public String encrypt(TextArea inputTextArea, TextArea keyTextArea) {
            selectedEncryption = getSelectedEncryption();
            if (keyTextArea.getText().length() != 24) {
                errorMessage.setContentText("This key is not appropriate for 3DES encryption.\nThe length must be 24");
                errorMessage.showAndWait();
            }

            if (selectedEncryption.equals("ECB")) {
                return provideEncryption("DESede", "DESede/ECB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CBC")) {
                System.out.println("CBC");
                return provideBlockEncryption("DESede","DESede/CBC/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CFB")) {
                return provideBlockEncryption("DESede","DESede/CFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("OFB")) {
                return provideBlockEncryption("DESede","DESede/OFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CTR")) {
                return provideBlockEncryption("DESede","DESede/CTR/PKCS5Padding", inputTextArea, keyTextArea);
            }
            return null;
        }

        @SneakyThrows
        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {
            selectedEncryption = getSelectedEncryption();
            if (keyTextArea.getText().length() != 24) {
                errorMessage.setContentText("This key is not appropriate for 3DES encryption.\nThe length must be 24");
                errorMessage.showAndWait();
            }

            if (selectedEncryption.equals("ECB")) {
                return provideDecryption("DESede", "DESede/ECB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CBC")) {
                return provideBlockDecryption("DESede","DESede/CBC/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CFB")) {
                return provideBlockDecryption("DESede","DESede/CFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("OFB")) {
                return provideBlockDecryption("DESede","DESede/OFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CTR")) {
                return provideBlockDecryption("DESede","DESede/CTR/PKCS5Padding", inputTextArea, keyTextArea);
            }
            return null;
        }
    }

    public static class AES extends DES {
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }
        EncryptionAccessor accessor = EncryptionAccessor.Symmetric; //TODO: didn't set accessor properly through the constructor
        public AES(EncryptionAccessor accessor, RadioButton ECB, RadioButton CBC, RadioButton CFB, RadioButton OFB, RadioButton CTR, Alert alert) {
            super(accessor, ECB, CBC, CFB, OFB, CTR, alert);
        }

        @SneakyThrows
        @Override
        public String encrypt(TextArea inputTextArea, TextArea keyTextArea) {
            selectedEncryption = getSelectedEncryption();
            if (keyTextArea.getText().length() != 16 && keyTextArea.getText().length() != 24 && keyTextArea.getText().length() != 32) {
                errorMessage.setContentText("This key is not appropriate for AES encryption.\nThe length must be 16 or 24 or 32");
                errorMessage.showAndWait();
            }

            if (selectedEncryption.equals("ECB")) {
                return provideEncryption("AES", "AES/ECB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CBC")) {
                return provideBlockEncryption("AES","AES/CBC/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CFB")) {
                return provideBlockEncryption("AES","AES/CFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("OFB")) {
                return provideBlockEncryption("AES","AES/OFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CTR")) {
                return provideBlockEncryption("AES","AES/CTR/PKCS5Padding", inputTextArea, keyTextArea);
            }
            return null;
        }

        @SneakyThrows
        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {
            selectedEncryption = getSelectedEncryption();
            if (keyTextArea.getText().length() != 16 && keyTextArea.getText().length() != 24 && keyTextArea.getText().length() != 32) {
                errorMessage.setContentText("This key is not appropriate for AES encryption.\nThe length must be 16 or 24 or 32");
                errorMessage.showAndWait();
            }


            if (selectedEncryption.equals("ECB")) {
                return provideDecryption("AES", "AES/ECB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CBC")) {
                return provideBlockDecryption("AES","AES/CBC/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CFB")) {
                return provideBlockDecryption("AES","AES/CFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("OFB")) {
                return provideBlockDecryption("AES","AES/OFB/PKCS5Padding", inputTextArea, keyTextArea);
            }
            if (selectedEncryption.equals("CTR")) {
                return provideBlockDecryption("AES","AES/CTR/PKCS5Padding", inputTextArea, keyTextArea);
            }
            return null;
        }
    }


    public static class Knapsack implements Cypher {
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }
        private final Alert alert;
        EncryptionAccessor accessor;
        private final TextArea startingPoint;

        public Knapsack(EncryptionAccessor accessor, Alert alert, TextArea startingPoint) {
            this.accessor = accessor;
            this.alert = alert;
            this.startingPoint = startingPoint;

        }

        private void checkNextElement(BigInteger[] array) throws Exception { //SuperInc sequence check
            for (int i = 1; i < array.length; i++) {
                if (array[i].compareTo(array[i - 1]) <= 0) {
                    alert.setHeaderText("Is not super increasing");
                    alert.setContentText(array[i] + "at position" + i + "is not a member of super increasing sequence");
                    throw new Exception("Next element is not greater than all previous elements.");
                }
            }
        }
        @Override
        public void generateKeyPair(TextArea keyLengthArea, TextArea publicKeyArea, TextArea privateKeyArea) {
            String keyLength = keyLengthArea.getText();
            BigInteger[] privateKey = generateSuperIncreasingSequence(Objects.equals(startingPoint.getText(), "") ? BigInteger.ONE : BigInteger.valueOf(Long.parseLong(startingPoint.getText())) , Objects.equals(keyLength, "") ? 10 : Integer.parseInt(keyLength) );
            BigInteger m = calculateMValue(privateKey);
            BigInteger n = getNextCoPrime(m);
            BigInteger[] publicKey = generatePublicKey(privateKey, n, m);
            privateKeyArea.setText(Arrays.toString(privateKey).replaceAll("[\\[|\\]]|\\s", ""));
            publicKeyArea.setText(Arrays.toString(publicKey).replaceAll("[\\[|\\]]|\\s", ""));
        }

        private BigInteger[] generatePublicKey (BigInteger[] privateKey, BigInteger n, BigInteger m) {
            BigInteger[] publicKey = new BigInteger[privateKey.length];
            for (int i = 0; i < privateKey.length; i++) {
                publicKey[i] = (privateKey[i].multiply(n)).mod(m);
            }
            return publicKey;
        }

        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {
            BigInteger[] privateKey = parseStringToBigIntArray(keyTextArea.getText());
            BigInteger m = calculateMValue(privateKey);
            BigInteger n = getNextCoPrime(m);
            BigInteger[] encrypted = parseStringToBigIntArray(inputTextArea.getText());
            String decrypted = initDecryption(encrypted, privateKey, n, m );
            return binaryToText(decrypted);
        }

        @Override
        public String encrypt(TextArea inputTextArea, TextArea publicKeyTextArea) {
            String binaryText = textToBinary(inputTextArea.getText());
            BigInteger[] ciphertext = initEncryption(binaryText, parseStringToBigIntArray(publicKeyTextArea.getText().replaceAll("[\\[|\\]]|\\s", "")));
            return Arrays.toString(ciphertext).replaceAll("[\\[|\\]]|\\s", "");
        }

        @Override
        public void attack(TextArea inputTextArea) {

        }

        public static BigInteger[] parseStringToBigIntArray(String input) {
            String[] strArr = input.split(",");
            BigInteger[] bigIntArr = new BigInteger[strArr.length];
            for (int i = 0; i < strArr.length; i++) {
                bigIntArr[i] = new BigInteger(strArr[i]);
            }
            return bigIntArr;
        }

        public static BigInteger getNextCoPrime(BigInteger m) {
            BigInteger n = m.divide(BigInteger.valueOf(100)); //percentage representation (1% of m value)
            while (true) {
                if (n.gcd(m).equals(BigInteger.ONE)) {
                    return n;
                }
                n = n.add(BigInteger.ONE);
            }
        }

        public static BigInteger calculateMValue(BigInteger[] sequence) {
            BigInteger sum = BigInteger.ZERO;
            for (BigInteger bigInteger : sequence) {
                sum = sum.add(bigInteger);
            }
            sum = sum.add(BigInteger.ONE);
            return sum;
        }

        public static BigInteger[] generateSuperIncreasingSequence(BigInteger entryNumber, int length) {
            BigInteger[] sequence = new BigInteger[length];
            BigInteger sum = entryNumber.subtract(BigInteger.ONE);
            for (int i = 0; i < length; i++) {
                // Generate a random number between sum + 1 and 2 * sum
                BigInteger randomNumber = new BigInteger(sum.bitLength(), new Random()).add(sum).add(BigInteger.ONE);
                sequence[i] = randomNumber;
                sum = sum.add(randomNumber);
            }
            return sequence;
        }

        public static BigInteger[] initEncryption(String plaintext, BigInteger[] publicKey) {
            // Split the plaintext into groups of six bits
            String[] groups = plaintext.split("(?<=\\G.{" + publicKey.length + "})"); //6 is a key size

            // Convert each group of n bits into an integer value
            BigInteger[] values = new BigInteger[groups.length];
            for (int i = 0; i < groups.length; i++) {
                values[i] = new BigInteger(groups[i], 2);
            }

            // Multiply each value with the corresponding values of public key and take their sum
            BigInteger sum = BigInteger.ZERO;
            BigInteger[] ciphertext = new BigInteger[groups.length];
            for (int i = 0; i < groups.length; i++) {
                for (int j = 0; j < publicKey.length; j++) {
                    if (values[i].testBit(j) ) {
                        sum = sum.add(publicKey[j]);
                    }
                }
                ciphertext[i] = sum;
                sum = BigInteger.ZERO;
            }

            return ciphertext;
        }

        public static String initDecryption(BigInteger[] ciphertext, BigInteger[] privateKey, BigInteger n, BigInteger m) {
            // Calculate the multiplicative inverse of n mod m
            BigInteger inverse = n.modInverse(m);

            // Multiply each block of ciphertext with the multiplicative inverse and take modulo m
            BigInteger[] decryptedValues = new BigInteger[ciphertext.length];
            for (int i = 0; i < ciphertext.length; i++) {
                decryptedValues[i] = (ciphertext[i].multiply(inverse)).mod(m);
            }

            // Make the sum of each decrypted value from the values of private key
            StringBuilder decryptedText = new StringBuilder();
            for (int i = 0; i < decryptedValues.length; i++) {
                BigInteger sum = BigInteger.ZERO;
                for (int j = privateKey.length - 1; j >= 0; j--) {
                    if (sum.add(privateKey[j]).compareTo(decryptedValues[i]) <= 0) {
                        sum = sum.add(privateKey[j]);
                        decryptedText.append("1");
                    } else {
                        decryptedText.append("0");
                    }
                }
            }

            return decryptedText.toString();
        }

        // Method to convert binary string to normal text
        public static String binaryToText(String binaryText) {
            int charCount = binaryText.length() / 8;
            byte[] bytes = new byte[charCount];

            for (int i = 0; i < charCount; ++i) {
                int charCode = Integer.parseInt(binaryText.substring(8*i, 8*i + 8), 2);
                bytes[i] = (byte) charCode;
            }

            return new String(bytes, StandardCharsets.UTF_8);
        }

        // Method to convert normal text to binary string
        public static String textToBinary(String text) {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            StringBuilder binary = new StringBuilder();

            for (byte b : bytes) {
                int code = (int) b;
                for (int i = 7; i >= 0; --i) {
                    binary.append((code >> i) & 1);
                }
            }

            return binary.toString();
        }

    }

    public static class RSA implements Cypher {
        @Override
        public EncryptionAccessor getEncryptionAccessor() {
            return accessor;
        }
        private final Alert alert;
        EncryptionAccessor accessor;

        public RSA(EncryptionAccessor accessor, Alert alert) {
            this.accessor = accessor;
            this.alert = alert;

        }
        @SneakyThrows
        public void generateKeyPair (TextArea keyLengthArea, TextArea publicKeyArea, TextArea privateKeyArea) {
            if (keyLengthArea.getText() == "" || Integer.parseInt(keyLengthArea.getText()) < 512 || Integer.parseInt(keyLengthArea.getText()) > 4096) {
                alert.setHeaderText("Wrong key size");
                alert.setContentText("Key size should be in range between 512 and 4096");
                alert.showAndWait();
                return;
            }

            KeyPairGenerator pairGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            pairGen.initialize(Integer.parseInt(keyLengthArea.getText()), random);
            KeyPair keyPair = pairGen.generateKeyPair();
            Key publicKey = keyPair.getPublic();
            Key privateKey = keyPair.getPrivate();
            privateKeyArea.setText(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            publicKeyArea.setText(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        }
        @SneakyThrows
        @Override
        public String encrypt(TextArea inputTextArea, TextArea keyTextArea) {
            try {
                byte[] messageBytes = inputTextArea.getText().getBytes();
                byte[] publicKeyBytes = Base64.getDecoder().decode(keyTextArea.getText());
                X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey loadedPublicKey = keyFactory.generatePublic(spec);
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, loadedPublicKey);
                byte[] encryptedMessage = cipher.doFinal(messageBytes);
                return Base64.getEncoder().encodeToString(encryptedMessage);
            } catch (IllegalBlockSizeException e) {
                alert.setContentText("Maximal text length in 4096 key size is 501.\nFor 512 key size text length is 54\nTry to increase key size or shorten your text");
                alert.showAndWait();
            }
            return inputTextArea.getText();
        }

        @SneakyThrows
        @Override
        public String decrypt(TextArea inputTextArea, TextArea keyTextArea) {

            byte[] encryptedMessageBytes = Base64.getDecoder().decode(inputTextArea.getText());
            byte[] privateKeyBytes = Base64.getDecoder().decode(keyTextArea.getText());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey loadedPrivateKey = keyFactory.generatePrivate(spec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, loadedPrivateKey);
            byte[] decryptedMessage = cipher.doFinal(encryptedMessageBytes);
            return new String(decryptedMessage);
        }

        @Override
        public void attack(TextArea inputTextArea) {

        }
    }



}