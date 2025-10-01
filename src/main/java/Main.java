import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final String USERS_FILE = "users.json";
    private static final String QUIZ_FILE = "quiz.json";
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        initializeUsers();


        User currentUser = login();
        if (currentUser == null) {
            System.out.println("Login failed!");
            return;
        }

        if (currentUser.getRole().equals("admin")) {
            adminFlow();
        } else {
            studentFlow(currentUser.getUsername());
        }
    }

    private static void initializeUsers() {
        File file = getResourceFile(USERS_FILE);
        if (file == null || !file.exists()) {
            JSONArray users = new JSONArray();

            JSONObject admin = new JSONObject();
            admin.put("username", "admin");
            admin.put("password", "1234");
            admin.put("role", "admin");
            users.put(admin);

            JSONObject student = new JSONObject();
            student.put("username", "nafis");
            student.put("password", "1234");
            student.put("role", "student");
            users.put(student);

            saveJsonToResourceFile(USERS_FILE, users.toString(2));
        }
    }

    private static User login() {
        System.out.print("System:> Enter your username\nUser:> ");
        String username = scanner.nextLine();

        System.out.print("System:> Enter password\nUser:> ");
        String password = scanner.nextLine();

        try {
            String content = readResourceFile(USERS_FILE);
            if (content == null) {
                System.out.println("Users file not found!");
                return null;
            }

            JSONArray users = new JSONArray(content);

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (user.getString("username").equals(username) &&
                        user.getString("password").equals(password)) {

                    String role = user.getString("role");
                    if (role.equals("admin")) {
                        System.out.println("System:> Welcome admin! Please create new questions in the question bank.");
                    } else {
                        System.out.println("System:> Welcome " + username + " to the quiz! We will throw you 10 questions. Each MCQ mark is 1 and no negative marking. Are you ready? Press 's' to start.");
                    }
                    return new User(username, password, role);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("System:> Invalid username or password!");
        return null;
    }

    private static void adminFlow() {
        JSONArray questions = loadQuestions();

        while (true) {
            System.out.print("System:> Input your question\nAdmin:> ");
            String questionText = scanner.nextLine();

            System.out.print("System:> Input option 1:\nAdmin:> ");
            String option1 = scanner.nextLine();

            System.out.print("System:> Input option 2:\nAdmin:> ");
            String option2 = scanner.nextLine();

            System.out.print("System:> Input option 3:\nAdmin:> ");
            String option3 = scanner.nextLine();

            System.out.print("System:> Input option 4:\nAdmin:> ");
            String option4 = scanner.nextLine();

            System.out.print("System:> What is the answer key?\nAdmin:> ");
            int answerKey = Integer.parseInt(scanner.nextLine());

            JSONObject newQuestion = new JSONObject();
            newQuestion.put("question", questionText);
            newQuestion.put("option 1", option1);
            newQuestion.put("option 2", option2);
            newQuestion.put("option 3", option3);
            newQuestion.put("option 4", option4);
            newQuestion.put("answerkey", answerKey);

            questions.put(newQuestion);


            boolean saved = saveJsonToResourceFile(QUIZ_FILE, questions.toString(2));

            if (saved) {
                System.out.println("System:> Saved successfully!");
                System.out.println("System:> Total questions in bank: " + questions.length());
            } else {
                System.out.println("System:> Error saving question!");
            }

            System.out.print("System:> Do you want to add more questions? (press 's' to start, 'q' to quit)\nAdmin:> ");
            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("q")) {
                break;
            }
        }
    }

    private static void studentFlow(String username) {
        String input = scanner.nextLine();
        if (!input.equalsIgnoreCase("s")) {
            return;
        }

        JSONArray allQuestions = loadQuestions();
        if (allQuestions.length() == 0) {
            System.out.println("System:> No questions available!");
            return;
        }

        int score = takeQuiz(allQuestions);


        displayResult(score);

        System.out.print("System:> Would you like to start again? Press 's' for start or 'q' for quit\nStudent:> ");
        String choice = scanner.nextLine();
        if (choice.equalsIgnoreCase("s")) {
            System.out.println("System:> Welcome " + username + " to the quiz! We will throw you 10 questions. Each MCQ mark is 1 and no negative marking. Are you ready? Press 's' to start.");
            studentFlow(username);
        }
    }

    private static int takeQuiz(JSONArray allQuestions) {
        int questionsToAsk = Math.min(10, allQuestions.length());
        List<Integer> selectedIndices = getRandomIndices(allQuestions.length(), questionsToAsk);
        int score = 0;

        for (int i = 0; i < selectedIndices.size(); i++) {
            JSONObject q = allQuestions.getJSONObject(selectedIndices.get(i));

            System.out.println("\nSystem:> [Question " + (i + 1) + "] " + q.getString("question"));
            System.out.println("1. " + q.getString("option 1"));
            System.out.println("2. " + q.getString("option 2"));
            System.out.println("3. " + q.getString("option 3"));
            System.out.println("4. " + q.getString("option 4"));
            System.out.print("Student:> ");

            try {
                int answer = Integer.parseInt(scanner.nextLine());
                if (answer == q.getInt("answerkey")) {
                    score++;
                }
            } catch (NumberFormatException e) {

            }
        }
        return score;
    }

    private static List<Integer> getRandomIndices(int max, int count) {
        List<Integer> indices = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            indices.add(random.nextInt(max));
        }

        return indices;
    }

    private static void displayResult(int score) {
        System.out.println("\n=== Quiz Result ===");

        if (score >= 8 && score <= 10) {
            System.out.println("Excellent! You have got " + score + " out of 10");
        } else if (score >= 5 && score <= 7) {
            System.out.println("Good. You have got " + score + " out of 10");
        } else if (score >= 3 && score <= 4) {
            System.out.println("Very poor! You have got " + score + " out of 10");
        } else {
            System.out.println("Very sorry you are failed. You have got " + score + " out of 10");
        }
    }

    private static JSONArray loadQuestions() {
        try {
            String content = readResourceFile(QUIZ_FILE);
            if (content == null || content.trim().isEmpty()) {
                return new JSONArray();
            }
            return new JSONArray(content);
        } catch (Exception e) {
            System.out.println("Error loading questions: " + e.getMessage());
            return new JSONArray();
        }
    }


    private static String readResourceFile(String filename) {
        try {

            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(filename);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                return content.toString();
            }


            File file = new File("src/main/resources/" + filename);
            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath()));
            }

            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static File getResourceFile(String filename) {
        try {
            URL resource = Main.class.getClassLoader().getResource(filename);
            if (resource != null) {
                return new File(resource.toURI());
            }

            File file = new File("src/main/resources/" + filename);
            if (file.exists()) {
                return file;
            }

            return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static boolean saveJsonToResourceFile(String filename, String jsonContent) {
        try {

            File resourcesDir = new File("src/main/resources");
            if (!resourcesDir.exists()) {
                resourcesDir.mkdirs();
            }

            File file = new File(resourcesDir, filename);
            FileWriter writer = new FileWriter(file);
            writer.write(jsonContent);
            writer.flush();
            writer.close();

            System.out.println("File saved to: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}