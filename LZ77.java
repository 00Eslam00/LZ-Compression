import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class LZ77 {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1- Compress File");
        System.out.println("2- Decompress File");
        int choice = scanner.nextInt();
        String fileName = readFileName();
        switch (choice) {
            case 1 -> {
                String userInput = readFromOriginalFile(fileName);
                System.out.println("Original:\n" + userInput);
                ArrayList<LZ77Token> compressed = compress(userInput);
                System.out.println("Compressed:\n" + compressed);
                String fileOut = fileName.replace(".", "Compressed.");
                writeOnCompressedFile(fileOut, compressed);
                System.out.println("Data compressed successfully and was added to file: " + fileOut);
            }
            case 2 -> {
                if (fileName.contains("Compressed")) {
                    ArrayList<LZ77Token> compressedTags = readFromCompressedFile(fileName);
                    System.out.println("Compressed: " + compressedTags);
                    String decompressed = decompress(compressedTags);
                    System.out.println("Decompressed: " + decompressed);
                } else
                    System.out.println("The Compressed file name should contain 'Compressed' word in it, please try again");
            }
            default -> System.out.println("Please choose 1 or 2 and try again");
        }

    }

    static class LZ77Token {
        int offset;
        int length;
        char nextChar;

        public LZ77Token(int offset, int length, char nextChar) {
            this.offset = offset;
            this.length = length;
            this.nextChar = nextChar;
        }

        @Override
        public String toString() {
            return "<" + offset + "," + length + "," + nextChar + ">";
        }
    }

    public static String readFileName() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Filename + extension OR Full path (if file isn't within this project files) : ");
        String file = scanner.nextLine();
        scanner.close();
        return file;
    }

    public static void writeOnCompressedFile(String fileName, ArrayList<LZ77Token> compressed) throws IOException {
        FileOutputStream fout = new FileOutputStream(fileName);
        for (LZ77Token lz77Token : compressed) {
            int offset = lz77Token.offset;
            int length = lz77Token.length;
            char nextChar = lz77Token.nextChar;
            fout.write(("<" + offset).getBytes());
            fout.write(("," + length).getBytes());
            fout.write(("," + nextChar + ">").getBytes());
        }
        fout.close();
    }

    public static String readFromOriginalFile(String fileName) throws IOException {
        FileInputStream fin = new FileInputStream(fileName);
        int data = fin.read();
        StringBuilder userInput = new StringBuilder();
        while (data != -1) {
            userInput.append((char) data);
            data = fin.read();
        }
        fin.close();
        return userInput.toString();
    }

    public static ArrayList<LZ77Token> readFromCompressedFile(String fileName) throws IOException {
        ArrayList<LZ77Token> compressed = new ArrayList<>();
        FileInputStream fin = new FileInputStream(fileName);
        int data;
        data = fin.read(); //<
        while (data != -1) {
            int temp = 0;
            StringBuilder of = new StringBuilder();
            data = fin.read(); // read first number
            while ((char) data != ',') { // loop till data = ','
                of.append((char) data);
                data = fin.read();
            }
            for (int j = of.length() - 1; j >= 0; j--) {
                temp += (of.charAt(j) - '0') * Math.pow(10, of.length() - 1 - j);
            }
            int offset = temp;
            data = fin.read(); // read first number
            of = new StringBuilder();
            while ((char) data != ',') {  // loop till data = ','
                of.append((char) data);
                data = fin.read();
            }
            temp = 0;
            for (int j = of.length() - 1; j >= 0; j--) {
                temp += (of.charAt(j) - '0') * Math.pow(10, of.length() - 1 - j);
            }
            int length = temp;
            data = fin.read(); // read the next char
            char nextChar = (char) data;
            data = fin.read();
            data = fin.read();
            compressed.add(new LZ77Token(offset, length, nextChar));
        }
        fin.close();
        return compressed;
    }

    public static ArrayList<LZ77Token> compress(String input) {
        ArrayList<LZ77Token> compressed = new ArrayList<>();
        int currentIndex = 0;

        while (currentIndex < input.length()) {
            int bestOffset = 0;
            int bestLength = 0;

            for (int offset = 1; offset <= currentIndex; offset++) {
                int length = 0;
                while (currentIndex + length < input.length() && input.charAt(currentIndex - offset + length) == input.charAt(currentIndex + length)) {
                    length++;
                }

                if (length > bestLength) {
                    bestLength = length;
                    bestOffset = offset;
                }
            }

            if (bestLength == 0) {
                compressed.add(new LZ77Token(0, 0, input.charAt(currentIndex)));
                currentIndex++;
            } else {
                if (currentIndex + bestLength < input.length())
                    compressed.add(new LZ77Token(bestOffset, bestLength, input.charAt(currentIndex + bestLength)));
                else
                    compressed.add(new LZ77Token(bestOffset, bestLength - 1, input.charAt(currentIndex + bestLength - 1)));
                currentIndex += bestLength + 1;
            }
        }

        return compressed;
    }

    public static String decompress(ArrayList<LZ77Token> compressed) {
        StringBuilder decompressed = new StringBuilder();

        for (LZ77Token token : compressed) {
            if (token.offset == 0) {
                decompressed.append(token.nextChar);
            } else {
                int startIndex = decompressed.length() - token.offset;
                for (int i = 0; i < token.length; i++) {
                    decompressed.append(decompressed.charAt(startIndex + i));
                }
                decompressed.append(token.nextChar);
            }
        }

        return decompressed.toString();
    }
}
