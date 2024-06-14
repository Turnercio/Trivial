package io.keepcoding.keeptrivial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class MainTrivial {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialize questions
        ArrayList<Question> questions = getQuestions();
        System.out.println("Questions loaded: " + questions.size());

        // Initialize teams
        System.out.print("Enter the number of teams: ");
        int numTeams = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < numTeams; i++) {
            System.out.print("Enter the name of team " + (i + 1) + ": ");
            String teamName = scanner.nextLine();
            teams.add(new Team(teamName));
        }

        // Main game loop
        boolean exit = false;
        int currentTeamIndex = 0;
        Set<String> allCategories = getAllCategories(questions); // Obtener todas las categorías
        while (!exit) {
            Team currentTeam = teams.get(currentTeamIndex);
            System.out.println("It's " + currentTeam.getName() + "'s turn!");

            // Select a random question from a category the team doesn't have a quesito in
            Question question = getRandomQuestion(questions, currentTeam, allCategories);
            if (question == null) {
                System.out.println("No more questions available for the team.");
                exit = true;
                continue;
            }
            question.displayQuestion();

            // Get the team's answer
            System.out.print("Enter the answer (1-4) or 'exit' to quit: ");
            String answer = scanner.nextLine();

            if (answer.equalsIgnoreCase("exit")) {
                exit = true;
            } else if (esTransformableAEntero(answer)) {
                int answerInt = Integer.parseInt(answer);
                if (answerInt == question.getCorrectAnswer()) {
                    System.out.println("Correct!");
                    currentTeam.addScore(1);
                    currentTeam.addQuesito(question.getTopic().getName());

                    if (currentTeam.hasAllQuesitos(allCategories)) {
                        System.out.println("Congratulations! " + currentTeam.getName() + " has won all the quesitos and the game!");
                        exit = true;
                    }
                } else {
                    System.out.println("Incorrect. The correct answer was " + question.getCorrectAnswer());
                }
            } else {
                System.out.println("Invalid input. Please enter a number between 1 and 4.");
            }

            // Display current scores
            System.out.println("Current Scores:");
            for (Team team : teams) {
                System.out.println(team);
            }

            // Move to the next team
            currentTeamIndex = (currentTeamIndex + 1) % teams.size();
        }

        // Display final scores and winner
        Team winner = teams.get(0);
        for (Team team : teams) {
            if (team.getScore() > winner.getScore()) {
                winner = team;
            }
        }

        System.out.println();
        title("Ha ganado: " + winner.getName());

        scanner.close();
    }

    public static void title(String text) {
        int length = text.length();
        printHashtagLine(length + 4); // Bordes

        System.out.println("# " + text + " #");

        printHashtagLine(length + 4);
    }

    public static void printHashtagLine(int length) {
        for (int i = 0; i < length; i++) {
            System.out.print("#");
        }
        System.out.println();
    }

    public static boolean esTransformableAEntero(String cadena) {
        try {
            Integer.parseInt(cadena);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int getRandomInt(int max) {
        return new Random().nextInt(max);
    }

    private static ArrayList<Question> getQuestions() {
        ArrayList<Question> list = new ArrayList<>();

        File folder = new File("questions");
        if (!folder.exists() || !folder.isDirectory()) {
            title("Error: La carpeta 'questions' no existe o no es un directorio.");
        } else {
            File[] filesList = folder.listFiles();

            for (File file : filesList) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    String topicName = file.getName().substring(0, file.getName().length() - 4);
                    Topic topic = new Topic(topicName);

                    // Read the questions
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        List<String> block = new ArrayList<>();

                        while ((line = br.readLine()) != null) {
                            block.add(line);

                            if (block.size() == 6) { // número de lineas que componen una pregunta
                                String questionText = block.get(0);
                                String answer1 = block.get(1);
                                String answer2 = block.get(2);
                                String answer3 = block.get(3);
                                String answer4 = block.get(4);
                                int rightOption = Integer.parseInt(block.get(5));

                                Question question = new Question(questionText, answer1, answer2, answer3, answer4, rightOption, topic);
                                list.add(question);
                                block.clear();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return list;
    }

    private static Set<String> getAllCategories(ArrayList<Question> questions) {
        Set<String> categories = new HashSet<>();
        for (Question question : questions) {
            categories.add(question.getTopic().getName());
        }
        return categories;
    }

    private static Question getRandomQuestion(ArrayList<Question> questions, Team team, Set<String> allCategories) {
        List<Question> filteredQuestions = new ArrayList<>();
        Set<String> teamQuesitos = team.getQuesitos();
        for (Question question : questions) {
            if (!teamQuesitos.contains(question.getTopic().getName())) {
                filteredQuestions.add(question);
            }
        }
        if (filteredQuestions.isEmpty()) {
            return null;
        }
        return filteredQuestions.get(getRandomInt(filteredQuestions.size()));
    }
}

class Question {
    private String question;
    private String answer1;
    private String answer2;
    private String answer3;
    private String answer4;
    private int correctAnswer;
    private Topic topic;

    public Question(String question, String answer1, String answer2, String answer3, String answer4, int correctAnswer, Topic topic) {
        this.question = question;
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.answer3 = answer3;
        this.answer4 = answer4;
        this.correctAnswer = correctAnswer;
        this.topic = topic;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public Topic getTopic() {
        return topic;
    }

    public void displayQuestion() {
        System.out.println("Topic: " + topic.getName());
        System.out.println(question);
        System.out.println("1. " + answer1);
        System.out.println("2. " + answer2);
        System.out.println("3. " + answer3);
        System.out.println("4. " + answer4);
    }

    @Override
    public String toString() {
        return "Question{" +
                "question='" + question + '\'' +
                ", answer1='" + answer1 + '\'' +
                ", answer2='" + answer2 + '\'' +
                ", answer3='" + answer3 + '\'' +
                ", answer4='" + answer4 + '\'' +
                ", correctAnswer=" + correctAnswer +
                ", topic=" + topic +
                '}';
    }
}

class Topic {
    private String name;

    public Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "name='" + name + '\'' +
                '}';
    }
}

class Team {
    private String name;
    private int score;
    private Set<String> quesitos;

    public Team(String name) {
        this.name = name;
        this.score = 0;
        this.quesitos = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public void addQuesito(String category) {
        quesitos.add(category);
    }

    public Set<String> getQuesitos() {
        return quesitos;
    }

    public boolean hasAllQuesitos(Set<String> allCategories) {
        return quesitos.containsAll(allCategories);
    }

    @Override
    public String toString() {
        String quesitosString = quesitos.isEmpty() ? "ningún quesito conseguido aún" : quesitos.toString();
        return name + " (Score: " + score + ", Quesitos: " + quesitosString + ")";
    }
}
