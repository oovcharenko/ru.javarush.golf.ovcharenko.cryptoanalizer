package analizer;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Cryptographer {
    protected static Map<Character, Character> alphabet = new HashMap<>();
    protected static final char[] ALPHABET_ARRAY = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя.,\":-!? ".toCharArray();
    public static final int MAX_KEY_VALUE = ALPHABET_ARRAY.length;
    private static final HashMap<Character, Double> DICTIONARY = getDictionary();

    private Cryptographer() {
    }

    /**
     * Creates the resulting file. If the file already exists, it deletes it and creates a new one
     * @param path - path to the source file
     * @param cryptedString - encoded text
     * @throws IOException - exception
     * */
    public static void createDestFile(Path path, String cryptedString) throws IOException {
         Path destPath = getDestFilePath(path);
         Path destFilePath;
         try {
             destFilePath = Files.createFile(destPath);
         } catch (FileAlreadyExistsException e) {
             Files.delete(destPath);
             destFilePath = Files.createFile(destPath);
         }
         Files.writeString(destFilePath, cryptedString);
         System.out.println("Создан файл: " + destFilePath);
     }

    public static void decryptUsingBruteForce()  {

        String sourcePath = setSourcePath();
        Path filePath = getFilePath(sourcePath);
        if (filePath == null) return;

        String sourceText = "";
        try {
            sourceText = Files.readString(filePath);
        } catch (IOException e) {
            System.out.println("Не удалось прочитать файл");
        }

        int bestKey = 0;
        double bestRatio = Double.MAX_VALUE;
        String bestText = "";
        int length = sourceText.length();

        // Перебрать ключи из возможного диапазона
        for (int i = -1; i > -Cryptographer.MAX_KEY_VALUE; i--) {
            Cryptographer.getAlphabetMap(i);
            String cryptedString = Cryptographer.getCryptedString(sourceText);

            // Получить карту с частотой букв в тексте, сравнить со словарем
            HashMap<Character, Double> statistics = getEmptyDictionary();
            for (int j = 0; j < length; j++) {
                char currChar = cryptedString.charAt(j);
                double count = statistics.get(currChar) == null ? -1.0 : statistics.get(currChar);
                if (count == -1) continue;
                statistics.put(currChar, ++count);
            }

            // Вычислить наиболее приближенный к словарю коэффициент
            // Т.о. с наибольшей вероятностью получим расшифрованный текст
            double ratio = 0;
            double dictionaryValue;
            for (var kv : statistics.entrySet()) {
                statistics.put(kv.getKey(), kv.getValue() / length * 100);
                dictionaryValue = DICTIONARY.get(kv.getKey());
                ratio += Math.abs(dictionaryValue - kv.getValue());
            }
            if (ratio < bestRatio) {
                bestKey = i;
                bestRatio = ratio;
                bestText = cryptedString;
            }
        }

        System.out.println("Лучший ключ: " + bestKey);
        System.out.println("Лучший коэффициент: " + bestRatio);
        System.out.println("Лучший текст: " + bestText);

        try {
            createDestFile(filePath, bestText);
        } catch (IOException e) {
            System.out.println("Не удалось создать результирующий файл");
        }
    }

    public static void encriptFile(boolean decript) {

        String sourcePath = setSourcePath();

        Path filePath = getFilePath(sourcePath);
        if (filePath == null) return;

        System.out.print("Введите ключ: ");
        Scanner sc = new Scanner(System.in);
        int key = sc.nextInt();
        key = decript ? -key : key;

        encryptDecryptFile(filePath, key);
    }

    /**
     * Encrypts/decrypts the file
     * @param path - path to the file
     * @param key - the key. If negative - shifts the alphabet to the beginning (decrypts)
     * */
    public static void encryptDecryptFile(Path path, int key) {

        key = normalizeKey(key);

        // Прочитать файл
        Path filePath = Path.of(String.valueOf(path));
        if (!Files.isRegularFile(path)) {
            System.out.println(path + " не найден. Обработка завершена");
            return;
        }

        String sourceText = "";
        try {
            sourceText = Files.readString(filePath);
        } catch (IOException e) {
            System.out.println("Не удалось прочитать файл");
        }
        getAlphabetMap(key);

        // Получить массив символов из sourceText
        String cryptedString = getCryptedString(sourceText);

        // Выведем результат, после отладки удалить вывод
        System.out.print("Зашифрованный результат: ");
        System.out.println(cryptedString);

        // Создать файл. Если создан - пересоздать
        try {
            createDestFile(path, cryptedString);
        } catch (IOException e) {
            System.out.println("Не удалось записать результат в файл");
        }
    }

    /**
     * Fills in the alphabet HashMap according to the passed key
     * The key is the letter of the original alphabet, the value is the encryption alphabet
     * @param key - key
     * */
     public static void getAlphabetMap(int key) {
         // По ключу заполнить значения в alphabet
         alphabet.clear();
         int lastIndex = ALPHABET_ARRAY.length - 1;
         for (int i = 0; i < ALPHABET_ARRAY.length; i++) {
             int index = i + key;
             if (index > lastIndex) {
                 index = index - lastIndex - 1;
             }
             if (index < 0) {
                 index = index + lastIndex + 1;
             }
             alphabet.put(ALPHABET_ARRAY[i], ALPHABET_ARRAY[index]);
         }
     }

    /**
     * Receives encrypted text
     * @param sourceText - the source text for encryption
     * @return - encrypted string
     * */
    public static String getCryptedString(String sourceText) {
        var arrayOfText = sourceText.toCharArray();

        // Преобразовать его, и вернуть строку
        for (int i = 0; i < arrayOfText.length; i++) {
            var charOfText = arrayOfText[i];
            char currChar;
            try {
                currChar = alphabet.get(charOfText);
                arrayOfText[i] = currChar;
            } catch (Exception e) {
                // Оставить символ без изменений
            }
        }
        return new String(arrayOfText);
    }

    /**
     * Gets the path for the resulting file - adds "_parsed" to the name of the source
     * @param path - path to the source file
     * @return - path
     * */
     public static Path getDestFilePath(Path path) {

         // Получить каталог файла и имя
         String strPath = path.toString();
         String fileName = path.getFileName().toString();

         // Получить имя файла и расширение
         int pointPosition = fileName.lastIndexOf(".");
         String name = fileName.substring(0, pointPosition);
         String extension = fileName.substring(pointPosition);
         String destFileName = name + "_parsed" + extension;

         destFileName = strPath.replaceFirst(fileName, destFileName);

         // Дополнить имя, склеить с каталогом, получить путь
         return Path.of(destFileName);
     }

    /**
     * * Normalizes the transmitted key if it goes beyond the acceptable limits for the key
     * @param key key
     * @return normalized key
     * */
    public static int normalizeKey(int key) {
        if (Math.abs(key) > MAX_KEY_VALUE) {
            return key % MAX_KEY_VALUE;
        }
        return key;
    }

    /**
     * Returns map with usage coefficients in Russian
     * the most popular letters (as a percentage)
     * */
    private static HashMap<Character, Double> getDictionary() {
        HashMap<Character, Double> map = new HashMap<>();
        map.put('о', 10.983);
        map.put('е', 8.483);
        map.put('а', 7.998);
        map.put('и', 7.367);
        map.put('н', 6.7);
        map.put('т', 6.318);
        map.put('с', 5.473);
        map.put('р', 4.746);
        map.put('в', 4.533);
        map.put('л', 4.343);
        map.put('к', 3.486);
        return map;
    }

    private static HashMap<Character, Double> getEmptyDictionary() {
        HashMap<Character, Double> map = new HashMap<>();
        for (var key : DICTIONARY.keySet()) {
            map.put(key, 0.0);
        }
        return map;
    }

    private static Path getFilePath(String sourcePath) {
        // Прочитать файл
        Path filePath = Path.of(sourcePath);
        if (!Files.isRegularFile(filePath)) {
            System.out.println(filePath + " не найден. Обработка завершена");
            return null;
        }
        return filePath;
    }

    /**
     * Requests the path to the source file for processing. By default, if nothing is entered,
     * used SourceFile.txt from src
     *
     * @return - string-file path
     * */
    public static String setSourcePath() {

        System.out.print("Введите путь к файлу для обработки (по умолчанию SourceFile.txt): ");
        String sourcePath = new Scanner(System.in).nextLine();
        if (sourcePath.isEmpty()) {
            sourcePath = "SourceFile.txt";
        }
        return sourcePath;
    }

}
