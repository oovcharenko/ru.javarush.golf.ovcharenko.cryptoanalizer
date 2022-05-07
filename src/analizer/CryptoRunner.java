package analizer;

import java.util.Scanner;

public class CryptoRunner {

    public static void main(String[] args) {

        System.out.print(String.join("\n"
                , "1. Зашифровать текст"
                , "2. Дешифровать текст"
                , "3. Брут форс"
                , "4. Анализ"
                , ""
                , "Введите номер действия: "));

        Scanner sc = new Scanner(System.in);
        int fl = sc.nextInt();
        sc.nextLine();

        switch (fl) {
            case 1 -> Cryptographer.encriptFile(false);
            case 2 -> Cryptographer.encriptFile(true);
            case 3 -> Cryptographer.decryptUsingBruteForce();
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
}
