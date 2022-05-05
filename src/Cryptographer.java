import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Утильный класс
 */
public class Cryptographer {
    protected static Map<Character, Character> alphabet = new HashMap<>();
    protected static final char[] alphabetArray = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя.,\":-!? ".toCharArray();
    public static final int MAX_KEY_VALUE = alphabetArray.length;

    private Cryptographer() {
    }

    /**
     * Создает результирующий файл. Если файл уже существует - удаляет, и создает новый
     * @param path - путь к исходному файлу
     * @param cryptedString - кодированный текст
     * @throws IOException - исключение
     */
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

    /**
     * Шифрует/дешифрует файл
     * @param path - путь к файлу
     * @param key - ключ. Если отрицательный - сдвигает алфавит к началу (дешифрует)
     * @throws IOException - исключение
     */
    public static void encryptDecryptFile(Path path, int key) throws IOException {

        key = normalizeKey(key);

        // Прочитать файл
        Path filePath = Path.of(String.valueOf(path));
        if (!Files.isRegularFile(path)) {
            System.out.println(path + " не найден. Обработка завершена");
            return;
        }

        String sourceText = Files.readString(filePath);
        getAlphabetMap(key);

        // Получить массив символов из sourceText
        String cryptedString = getCryptedString(sourceText);

        // Выведем результат, после отладки удалить вывод
        System.out.print("Зашифрованный результат: ");
        System.out.println(cryptedString);

        // Создать файл. Если создан - пересоздать
        createDestFile(path, cryptedString);
    }

    /**
     * Заполняет HashMap alphabet в соответствии с переданным ключом
     * Ключ - буква исходного алфавита, значение - алфавита шифрования
     * @param key - ключ
     */
     public static void getAlphabetMap(int key) {
         // По ключу заполнить значения в alphabet
         alphabet.clear();
         int lastIndex = alphabetArray.length - 1;
         for (int i = 0; i < alphabetArray.length; i++) {
             int index = i + key;
             if (index > lastIndex) {
                 index = index - lastIndex - 1;
             }
             if (index < 0) {
                 index = index + lastIndex + 1;
             }
             alphabet.put(alphabetArray[i], alphabetArray[index]);
         }
     }

    /**
     * Получает шифрованный текст
     * @param sourceText - исходный текст для шифрования
     * @return - шифрованная строка
     */
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
     * Получает путь для результирующего файла - добавляет к имени исходного "_parsed"
     * @param path - путь к исходному файлу
     * @return - путь
     */
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
     * Нормализует переданный ключ, если он выходит за допустимые переделы для ключа
     * @param key ключ
     * @return нормализованный ключ
     */
    public static int normalizeKey(int key) {
        if (Math.abs(key) > MAX_KEY_VALUE) {
            return key % MAX_KEY_VALUE;
        }
        return key;
    }
}
