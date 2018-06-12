import java.util.Scanner;
import java.util.function.Consumer;

public class Util {
    private static final Scanner scanner = new Scanner(System.in);

    public static int fibonacci(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static String prompt(String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }

    public static void promptLoop(String prompt, Consumer<String> inputHandler) {
        String fullPrompt = prompt + " (or type quit): ";
        boolean quit = false;
        while (!quit) {
            String input = prompt(fullPrompt);
            if (input.equalsIgnoreCase("quit")) {
                quit = true;
            } else {
                inputHandler.accept(input);
            }
        }
    }
}
