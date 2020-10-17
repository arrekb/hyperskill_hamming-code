package correcter;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    final private static String INPUT_FILE = "send.txt";
    final private static String ENCODED_FILE = "encoded.txt";
    final private static String RECEIVED_FILE = "received.txt";
    final private static String DECODED_FILE = "decoded.txt";

    public static void main(String[] args) {
        // Hamming code: https://www.youtube.com/watch?v=373FUw-2U2k

        Scanner scanner = new Scanner(System.in);
        System.out.print("Write a mode: ");
        switch (scanner.next()) {
            case "encode":
                encodeUsingHammingCode();
                break;
            case "send":
                sendAndReceive();
                break;
            case "decode":
                decodeUsingHammingCode();
                break;
            default:
                System.out.println("Correct modes are encode, send and decode. Try again.");
        }

    }

    private static void encodeUsingHammingCode() {
        try {
            InputStream inputStream = new FileInputStream(INPUT_FILE);
            byte[] inputContent = inputStream.readAllBytes();
            inputStream.close();

            byte[] outputContentBuffer = new byte[inputContent.length * 2];
            for (int ii = 0; ii < inputContent.length; ii++) {

                byte[] newByte = encodeByteUsingHammingCode(inputContent[ii]);
                outputContentBuffer[ii * 2] = newByte[0];
                outputContentBuffer[ii * 2 + 1] = newByte[1];
            }

            OutputStream outputStream = new FileOutputStream(ENCODED_FILE);
            outputStream.write(outputContentBuffer);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] encodeByteUsingHammingCode(byte inputByte) {
        byte[] outputBytes = new byte[2];

        // set original value
        if (getBit(inputByte, 0) == 1) outputBytes[0] = setBit(outputBytes[0], 2);
        if (getBit(inputByte, 1) == 1) outputBytes[0] = setBit(outputBytes[0], 4);
        if (getBit(inputByte, 2) == 1) outputBytes[0] = setBit(outputBytes[0], 5);
        if (getBit(inputByte, 3) == 1) outputBytes[0] = setBit(outputBytes[0], 6);
        if (getBit(inputByte, 4) == 1) outputBytes[1] = setBit(outputBytes[1], 2);
        if (getBit(inputByte, 5) == 1) outputBytes[1] = setBit(outputBytes[1], 4);
        if (getBit(inputByte, 6) == 1) outputBytes[1] = setBit(outputBytes[1], 5);
        if (getBit(inputByte, 7) == 1) outputBytes[1] = setBit(outputBytes[1], 6);

        // set parity bits
        for (int ii = 0; ii < outputBytes.length; ii++) {
            if ((getBit(outputBytes[ii], 2) + getBit(outputBytes[ii], 4) + getBit(outputBytes[ii], 6)) % 2 > 0) {
                outputBytes[ii] = setBit(outputBytes[ii], 0);
            }
            if ((getBit(outputBytes[ii], 2) + getBit(outputBytes[ii], 5) + getBit(outputBytes[ii], 6)) % 2 > 0) {
                outputBytes[ii] = setBit(outputBytes[ii], 1);
            }
            if ((getBit(outputBytes[ii], 4) + getBit(outputBytes[ii], 5) + getBit(outputBytes[ii], 6)) % 2 > 0) {
                outputBytes[ii] = setBit(outputBytes[ii], 3);
            }
        }
        return outputBytes;
    }

    private static int getBit(byte b, int offset) {
        offset = 7 - offset;
        return (b & (1 << offset)) > 0 ? 1 : 0;
    }

    private static byte setBit(byte b, int offset) {
        offset = 7 - offset;
        return (byte) (b | (1 << offset));
    }

    private static byte toggleBit(byte b, int offset) {
        offset = 7 - offset;
        return (byte) (b ^ (1 << offset));
    }

    private static void sendAndReceive() {
        try {
            InputStream inputStream = new FileInputStream(ENCODED_FILE);
            byte[] inputContent = inputStream.readAllBytes();
            inputStream.close();

            byte[] outputContent = new byte[inputContent.length];
            for (int ii = 0; ii < inputContent.length; ii++) {
                outputContent[ii] = simulateError(inputContent[ii]);
            }
            OutputStream outputStream = new FileOutputStream(RECEIVED_FILE);
            outputStream.write(outputContent);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte simulateError(byte inputByte) {
        Random random = new Random();
        int bitPosition = random.nextInt(8);
        return (byte) (inputByte ^ (1 << bitPosition));
    }

    private static void decodeUsingHammingCode() {
        try {
            InputStream inputStream = new FileInputStream(RECEIVED_FILE);
            byte[] inputContent = inputStream.readAllBytes();
            inputStream.close();

            byte[] outputContent = new byte[inputContent.length / 2];
            for (int ii = 0; ii < outputContent.length; ii++) {
                byte[] buf = new byte[2];
                buf[0] = inputContent[ii * 2];
                buf[1] = inputContent[ii * 2 + 1];

                outputContent[ii] = decode2Bytes(buf);
            }

            OutputStream outputStream = new FileOutputStream(DECODED_FILE);
            outputStream.write(outputContent);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte decode2Bytes(byte[] inputBytes) {
        //correct every byte
        for (int ii = 0; ii < inputBytes.length; ii++) {
            inputBytes[ii] = correctByteUsingHammingCode(inputBytes[ii]);
        }

        // gather values from two bytes
        byte outputByte = 0;
        if (getBit(inputBytes[0], 2) == 1) outputByte = setBit(outputByte, 0);
        if (getBit(inputBytes[0], 4) == 1) outputByte = setBit(outputByte, 1);
        if (getBit(inputBytes[0], 5) == 1) outputByte = setBit(outputByte, 2);
        if (getBit(inputBytes[0], 6) == 1) outputByte = setBit(outputByte, 3);
        if (getBit(inputBytes[1], 2) == 1) outputByte = setBit(outputByte, 4);
        if (getBit(inputBytes[1], 4) == 1) outputByte = setBit(outputByte, 5);
        if (getBit(inputBytes[1], 5) == 1) outputByte = setBit(outputByte, 6);
        if (getBit(inputBytes[1], 6) == 1) outputByte = setBit(outputByte, 7);

        return outputByte;
    }

    private static byte correctByteUsingHammingCode(byte b) {
        // set parity bits
        int incorrectBitPosition = 0;

        if ((getBit(b, 2) + getBit(b, 4) + getBit(b, 6)) % 2 != getBit(b, 0)) {
            incorrectBitPosition += 1;
        }
        if ((getBit(b, 2) + getBit(b, 5) + getBit(b, 6)) % 2 != getBit(b, 1)) {
            incorrectBitPosition += 2;
        }
        if ((getBit(b, 4) + getBit(b, 5) + getBit(b, 6)) % 2 != getBit(b, 3)) {
            incorrectBitPosition += 4;
        }
        if (incorrectBitPosition > 0) {
            b = toggleBit(b, incorrectBitPosition - 1);
        }
        return b;
    }
}
