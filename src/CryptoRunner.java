import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

public class CryptoRunner {

    private static final Scanner sc = new Scanner(System.in);
    private static final HashMap<Character, Double> dictionary = getDictionary();

    public static void main(String[] args) throws IOException {

        System.out.print(String.join("\n"
                , "1. Зашифровать текст"
                , "2. Дешифровать текст"
                , "3. Брут форс"
                , "4. Анализ"
                , ""
                , "Введите номер действия: "));

        int fl = sc.nextInt();
        sc.nextLine();

        switch (fl) {
            case 1 -> encriptFile(false);
            case 2 -> encriptFile(true);
            case 3 -> decryptUsingBruteForce();
            case 4 -> decryptUsingAnalyzer();
            default -> System.out.println("Отказ пользователя");
        }
    }

    /**
     * Решение будет очень похожим на поиск ключа с помощью brute-force.
     * Отличие лишь в создании словаря на основании аналогичного текста автора.
     * Есть большое подозрение, что словарь лишь в мелочах будет отличаться от getDictionary()
     */
    public static void decryptUsingAnalyzer() {
        System.out.println("Будет скоро реализовано...");
    }

    /**
     * Подбирает ключ и расшифровывает файл
     */
    public static void decryptUsingBruteForce() throws IOException {

        String sourcePath = setSourcePath();
        Path filePath = getFilePath(sourcePath);
        if (filePath == null) return;

        String sourceText = Files.readString(filePath);

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
                dictionaryValue = dictionary.get(kv.getKey());
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

        Cryptographer.createDestFile(filePath, bestText);
    }

    public static void encriptFile(boolean decript) throws IOException {

        String sourcePath = setSourcePath();

        Path filePath = getFilePath(sourcePath);
        if (filePath == null) return;

        System.out.print("Введите ключ: ");
        int key = sc.nextInt();
        key = decript ? -key : key;

        Cryptographer.encryptDecryptFile(filePath, key);
    }

    /**
     * Возвращает мапу с коэффициентами использования в русском языке
     * самых популярных букв (в процентах)
     */
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
        for (var key : dictionary.keySet()) {
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
     * Запрашивает путь к исходному файлу для обработки. По умолчанию, если ничего не введено,
     * используется SourceFile.txt из src
     *
     * @return - строка-путь к файлу
     */
    public static String setSourcePath() {

        System.out.print("Введите путь к файлу для обработки (по умолчанию SourceFile.txt): ");
        String sourcePath = sc.nextLine();
        if (sourcePath.isEmpty()) {
            sourcePath = "src/SourceFile.txt";
        }
        return sourcePath;
    }
}
