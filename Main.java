package flashcards;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    static final String ADD_ACTION = "add";
    static final String REMOVE_ACTION = "remove";
    static final String IMPORT_ACTION = "import";
    static final String EXPORT_ACTION = "export";
    static final String ASK_ACTION = "ask";
    static final String LOG_ACTION = "log";
    static final String HARDEST_CARD_ACTION = "hardest card";
    static final String RESET_STATS_ACTION = "reset stats";
    static final String EXIT_ACTION = "exit";

    static String importFileArg = "";
    static String exportFileArg = "";

    public static void main(String[] args) {

        parseCommandLineArgs(args);

        InputOutputLogScannerWrapper scannerWrapper = new InputOutputLogScannerWrapper();

        Deck deck = new Deck(scannerWrapper);

        if (! importFileArg.isEmpty()) {
            deck.importCardsFromFile(importFileArg);
        }

        String action = "";
        do {
            scannerWrapper.println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            action = scannerWrapper.nextLine();

            switch(action) {
                case ADD_ACTION:
                    deck.addCardFromConsole();
                    break;
                case REMOVE_ACTION:
                    deck.removeCard();
                    break;
                case IMPORT_ACTION:
                    deck.importCardsFromFile();
                    break;
                case EXPORT_ACTION:
                    deck.exportCardsToFile();
                    break;
                case ASK_ACTION:
                    deck.evaluateCards();
                    break;
                case LOG_ACTION:
                    deck.log();
                    break;
                case HARDEST_CARD_ACTION:
                    deck.hardestCard();
                    break;
                case RESET_STATS_ACTION:
                    deck.resetStats();
                    break;
                case EXIT_ACTION:
                    exit(deck, scannerWrapper);
                    break;
                default:
                    break;
            }
        } while(! action.equals(EXIT_ACTION));
    }

    static void parseCommandLineArgs(String[] args) {
        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("-import")) {
                importFileArg = args[i + 1];
            } else if (args[i].equals("-export")) {
                exportFileArg = args[i + 1];
            }
        }
    }

    static void exit(Deck deck, InputOutputLogScannerWrapper scannerWrapper) {
        scannerWrapper.println("Bye bye!");
        if (! exportFileArg.isEmpty()) {
            deck.exportCardsToFile(exportFileArg);
        }
    }
}

class InputOutputLogScannerWrapper {

    List<String> logs = new ArrayList<>();

    void println(String text) {
        logs.add(text);
        System.out.println(text);
    }

    String nextLine() {
        String text;
        try(Scanner scanner = new Scanner(System.in)) {
            text = scanner.nextLine();
        }
        logs.add(text);
        return text;
    }

    int nextInt() {
        int number;
        try(Scanner scanner = new Scanner(System.in)) {
            number = scanner.nextInt();
        }
        logs.add(String.valueOf(number));
        return number;
    }

    void saveLogsToFile(String fileName) {
        try (PrintWriter writer = new PrintWriter(fileName);) {
            logs.forEach(logLine -> writer.println(logLine));
            println("The log has been saved.");
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }
}

class Deck {

    Map<String, Card> cards = new LinkedHashMap<>();
    InputOutputLogScannerWrapper scannerWrapper;

    Deck(InputOutputLogScannerWrapper scannerWrapper) {
        this.scannerWrapper = scannerWrapper;
    }

    void addCard(Card card) {
        cards.put(card.getTerm(), card);
    }

    void addCardFromConsole() {
        try {
            Card cardFromConsole = new Card(this, scannerWrapper);

            cards.put(cardFromConsole.getTerm(), cardFromConsole);
            scannerWrapper.println("The pair (\"" + cardFromConsole.getTerm() + "\":" + "\"" + cardFromConsole.getDefinition() + "\") has been added.");
        } catch(RuntimeException e) {

        }
    }

    void removeCard() {
        scannerWrapper.println("The card:");
        String termToRemove = scannerWrapper.nextLine();

        if(cards.containsKey(termToRemove)) {
            cards.remove(termToRemove);
            scannerWrapper.println("The card has been removed.");
        } else {
            scannerWrapper.println("Can't remove \"" + termToRemove + "\": there is no such card.");
        }
    }

    void checkIfTermIsValid(String term) {
        if (cards.containsKey(term)) {
            scannerWrapper.println("The card \"" + term + "\" already exists.");
            String cardAlreadyExistsExceptionMessage = "The card already exists.";
            scannerWrapper.println(cardAlreadyExistsExceptionMessage);
            throw new RuntimeException(cardAlreadyExistsExceptionMessage);
        }
    }

    Card getCardMatchingDefinition(String answer) {
        return cards.values().stream().filter(cardValue -> answer.equals(cardValue.getDefinition()))
                .findFirst().get();
    }

    void checkIfDefinitionIsValid(String finalDefinition) {
        if (isDefinitionAlreadyUsedInPreviousCards(finalDefinition)) {
            scannerWrapper.println("The definition \"" + finalDefinition + "\" already exists.");
            String definitionAlreadyExistsExceptionMessage = "The definition already exists.";
            scannerWrapper.println(definitionAlreadyExistsExceptionMessage);
            throw new RuntimeException(definitionAlreadyExistsExceptionMessage);
        }
    }

    boolean isDefinitionAlreadyUsedInPreviousCards(String finalDefinition) {
        return cards.values().stream().anyMatch(cardValue -> finalDefinition.equals(cardValue.getDefinition()));
    }

    String getFileNameFromConsole() {
        scannerWrapper.println("File Name:");
        return scannerWrapper.nextLine();
    }

    public void exportCardsToFile() {
        String outputFileName = getFileNameFromConsole();
        exportCardsToFile(outputFileName);
    }

    public void exportCardsToFile(String outputFileName) {
        try (PrintWriter writer = new PrintWriter(outputFileName);) {
            cards.values().forEach( card -> writer.println(card.getTerm() + ":" + card.getDefinition() + ":" + card.getMistakes()));
            scannerWrapper.println(cards.size() + " cards have been saved.");
        } catch (FileNotFoundException e) {
            scannerWrapper.println("File not found.");
        }
    }

    public void importCardsFromFile() {
        String inputFileName = getFileNameFromConsole();
        importCardsFromFile(inputFileName);
    }

    public void importCardsFromFile(String inputFileName) {
        try(Scanner scanner = new Scanner(Paths.get(inputFileName))) {
            int count = 0;
            while(scanner.hasNextLine()) {
                String cardLine = scanner.nextLine();
                String[] cardSplit = cardLine.split(":");
                addCard(new Card(cardSplit[0], cardSplit[1], Integer.valueOf(cardSplit[2]), scannerWrapper));
                count++;
            }
            scannerWrapper.println(count + " cards have been loaded.");
        } catch (FileNotFoundException e) {
            scannerWrapper.println("File not found.");
        } catch (IOException e) {
            scannerWrapper.println("File not found.");
        }
    }

    void evaluateCards() {
        scannerWrapper.println("How many times to ask?");

        int timesToAsk = Integer.valueOf(scannerWrapper.nextLine());

        for (int i = 0; i < timesToAsk; i++) {
            Card card = getRandomCardFromDeck();

            scannerWrapper.println("Print the definition of \"" + (card.getTerm()) + "\":");
            String answer = scannerWrapper.nextLine();

            if (answer.equals(card.definition)) {
                scannerWrapper.println("Correct answer.");
            } else {
                card.wrongAnswer();
                String correctAnswerOfAnotherCard = ".";
                if (isDefinitionAlreadyUsedInPreviousCards(answer)) {
                    Card cardMatchingDefinition = getCardMatchingDefinition(answer);
                    correctAnswerOfAnotherCard = ", you've just written the definition of \"" + cardMatchingDefinition.getTerm() + "\".";
                }
                scannerWrapper.println("Wrong answer. The correct one is \"" + card.getDefinition() + "\"" + correctAnswerOfAnotherCard);
            }
        }
    }

    private Card getRandomCardFromDeck() {
        Random random = new Random();
        int cardRandomNumber = random.nextInt(cards.size());
        String[] terms = cards.keySet().toArray(new String[cards.size()]);
        return cards.get(terms[cardRandomNumber]);
    }

    void log() {
        String fileName = getFileNameFromConsole();
        scannerWrapper.saveLogsToFile(fileName);
    }

    void resetStats() {
        cards.values().forEach(card -> card.clearMistakes());
        scannerWrapper.println("Card statistics has been reset.");
    }

    void hardestCard() {
        List<Card> hardestCards = new ArrayList<>();
        int highestNumberOfMistakes = 0;
        for(Card card : cards.values()) {
            int actualCardMistakes = card.getMistakes();
            if (actualCardMistakes == 0) {
                continue;
            }
            if (actualCardMistakes > highestNumberOfMistakes) {
                highestNumberOfMistakes = card.getMistakes();
                hardestCards.clear();
                hardestCards.add(card);
            } else if (actualCardMistakes == highestNumberOfMistakes) {
                hardestCards.add(card);
            }
        }

        // The same but using streams
//        highestNumberOfMistakes = cards.values().stream().max(Comparator.comparing(card -> card.getMistakes())).get().getMistakes();
//        int finalHighestNumberOfMistakes = highestNumberOfMistakes;
//        hardestCards = cards.values().stream().filter(card -> card.getMistakes() == finalHighestNumberOfMistakes)
//                .collect(Collectors.toList());

        String cardsWithErrorsString = getCardsWithMostMistakesString(hardestCards, highestNumberOfMistakes);
        scannerWrapper.println(cardsWithErrorsString);
    }

    String getCardsWithMostMistakesString(List<Card> cardsWithErrors, int highestNumberOfMistakes) {
        String cardsWithMostMistakes;
        if (cardsWithErrors.isEmpty()) {
            cardsWithMostMistakes ="There are no cards with errors.";
        } else {
            if (cardsWithErrors.size() == 1) {
                cardsWithMostMistakes = "The hardest card is \"" + cardsWithErrors.get(0).getTerm() + "\". ";
                cardsWithMostMistakes = cardsWithMostMistakes + "You have " + highestNumberOfMistakes + " errors answering it.";
            } else {
                List<String> termsWithErrors = cardsWithErrors.stream().map(card -> "\"" + card.getTerm() + "\"")
                                        .collect(Collectors.toList());
                String termsWithErrorsCommaSeparated = String.join(", ",termsWithErrors);
                cardsWithMostMistakes = "The hardest cards are " + termsWithErrorsCommaSeparated + ". ";
                cardsWithMostMistakes = cardsWithMostMistakes + "You have " + highestNumberOfMistakes + " errors answering them.";
            }
        }

        return cardsWithMostMistakes;
    }
}

class Card {
    String term;
    String definition;
    int mistakes = 0;

    InputOutputLogScannerWrapper scannerWrapper;

    public int getMistakes() {
        return mistakes;
    }

    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
    }

    Card(String term, String definition, InputOutputLogScannerWrapper scannerWrapper) {
        this.term = term;
        this.definition = definition;
        this.scannerWrapper = scannerWrapper;
    }

    Card(String term, String definition, int mistakes, InputOutputLogScannerWrapper scannerWrapper) {
        this.term = term;
        this.definition = definition;
        this.mistakes = mistakes;
        this.scannerWrapper = scannerWrapper;
    }

    Card(Deck deck, InputOutputLogScannerWrapper scannerWrapper) {
        this.scannerWrapper = scannerWrapper;
        this.term = addTermFromConsole(deck);
        this.definition = addDefinitionFromConsole(deck);
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return "Card{" +
                "term='" + term + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }

    private String addTermFromConsole(Deck deck) {
        String term;
        boolean termIsValid = false;

        scannerWrapper.println("The card:");
        term = scannerWrapper.nextLine();
        deck.checkIfTermIsValid(term);
        return term;
    }

    private String addDefinitionFromConsole(Deck deck) {
        String definition;
        boolean definitionIsValid = false;

        scannerWrapper.println("The definition of the card:");

        definition = scannerWrapper.nextLine();
        deck.checkIfDefinitionIsValid(definition);
        return definition;
    }

    void wrongAnswer() {
        mistakes++;
    }

    void clearMistakes() {
        mistakes = 0;
    }
}


