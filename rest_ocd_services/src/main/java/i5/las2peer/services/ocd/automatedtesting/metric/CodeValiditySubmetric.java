package i5.las2peer.services.ocd.automatedtesting.metric;

import i5.las2peer.services.ocd.automatedtesting.ocdparser.OCDAParser;
import i5.las2peer.services.ocd.automatedtesting.ocdaexecutor.GradleTaskExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static i5.las2peer.services.ocd.automatedtesting.OCDATestAutomationConstants.AUTO_GENERATED_TEST_CLASS_PACKAGE;
import static i5.las2peer.services.ocd.automatedtesting.helpers.FileHelpers.getAutoGeneratedTestPath;

public class CodeValiditySubmetric {

    /**
     * True if there is no parsing error detected in the parsed code.
     */
    public static boolean noParsingErrorFound = false; //TODO: make private

    /**
     * True if the parsed code is executable
     */
    private static boolean noRuntimeErrorFound = false;

    /**
     * Weight of metric that evaluates parsing errors found. The weight is used to determine
     * how much does this value contribute to the submetric calculation.
     */
    private static double noParsingErrorFoundWeight = 1.0;

    /**
     * Weight of metric that evaluates runtime errors found. The weight is used to determine
     * how much does this value contribute to the submetric calculation.
     */
    private static double noRuntimeErrorFoundWeight = 1.0;

    /**
     * List that holds instructions that will be used to generate the prompt for improving ChatGPT code
     */
    private static ArrayList<String> promptImprovementRemarks = new ArrayList<>();


    /**
     * Evaluates whether the code in a given Java file is parsable without syntax errors.
     * This method uses the OCDAParser to retrieve any parsing errors from the specified file.
     * If no errors are found, the code is considered parsable. If parsing errors are present,
     * they are added to a list of remarks for prompt improvement, to improve subsequent prompts.
     *
     * @param gptOutput The Java file to be evaluated for parsability.
     * @return          {@code true} if the code is parsable (error-free); {@code false} otherwise.
     *                  The method also updates a global flag 'isCodeParsingErrorFree' based on the result.
     *                  Additionally, it populates 'promptImprovementRemarks' with suggestions for fixing
     *                  each identified parsing error, if any are found.
     */
    public static boolean evaluateIsCodeParsable(File gptOutput){

        // Identify and get parsing error list found in the (autogenerated) class file
        List<String> parsingErrors = OCDAParser.getParsingErrors(gptOutput);

        if (parsingErrors.isEmpty()) {
            noParsingErrorFound = true;
        } else {
            noParsingErrorFound = false;
            for (String parsingError : parsingErrors) {
                promptImprovementRemarks.add(parsingError);
            }
        }

        return noParsingErrorFound;
    }


    /**
     * Evaluates if the test code in a test file corresponding to the specified OCD algorithm is
     * runnable by executing the tests.
     *
     * This method uses OCDTestRunner to run the JUnit 5 tests contained in a test file of the
     * specified OCD algorithm and determines if the code is runnable based on the presence or
     * absence of exceptions during test execution. It collects any exceptions thrown and uses
     * them to assess the executability of the code. If no exceptions are thrown,  the code is
     * considered runnable. Otherwise, it's deemed not runnable, and the exceptions are
     * logged for further analysis or for prompt improvement to be used in subsequent GPT prompts.
     *
     * @param testFileName The name of the algorithm test class to be evaluated.
     * @return A boolean value indicating whether the code in the file is runnable (true) or not (false).
     */
    public static boolean evaluateIsCodeRunnable(String testFileName) {
        try {



            String fullTestClassPath = AUTO_GENERATED_TEST_CLASS_PACKAGE + "." + testFileName;

            // Identify and get exceptions thrown when (autogenerated test) code is executed
            List<String> exceptionsThrown = GradleTaskExecutor.runGradleTask("runCustomTests", fullTestClassPath);

            if (exceptionsThrown.isEmpty()) {
                noRuntimeErrorFound = true;
            } else {
                noRuntimeErrorFound = false;
                for (String thrownException : exceptionsThrown) {
                    promptImprovementRemarks.add(thrownException);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return noRuntimeErrorFound;
    }

    /**
     * Resets variables of this submetric to be reused
     */
    public void resetCodeValiditySubmetricVariables(){
        promptImprovementRemarks = new ArrayList<>();
        noParsingErrorFound = true;
        noRuntimeErrorFound = true;
    }

    public static void main(String[] args) {

        String ocdaName = "SskAlgorithm";

        //File ocdaCode = new File(OCDAParser.getOCDAPath("SskAlgorithm.java"));

        File gptOutput = new File("GeneratedSskAlgorithmTest.java");

        File parsedGptOutput = new File(getAutoGeneratedTestPath("GeneratedSskAlgorithmTest.java"));

        evaluateIsCodeParsable(gptOutput); // input should be code outputted by GPT to see if it can be parsed
        evaluateIsCodeRunnable(ocdaName); // input should be GPT output that was successfully parsable to see if it is executable


        System.out.println("isNoParsingErrorFound = " + noParsingErrorFound + " | isNoRuntimeErrorFound = " + noRuntimeErrorFound);
        for (String remark : promptImprovementRemarks) {
            System.out.println(remark);
        }

    }

    public static boolean isNoParsingErrorFound() {
        return noParsingErrorFound;
    }

    public static boolean isNoRuntimeErrorFound() {
        return noRuntimeErrorFound;
    }

    public static double getNoParsingErrorFoundWeight() {
        return noParsingErrorFoundWeight;
    }

    public static double getNoRuntimeErrorFoundWeight() {
        return noRuntimeErrorFoundWeight;
    }

    public static ArrayList<String> getPromptImprovementRemarks() {
        return promptImprovementRemarks;
    }

    public static void setNoParsingErrorFoundWeight(double noParsingErrorFoundWeight) {
        CodeValiditySubmetric.noParsingErrorFoundWeight = noParsingErrorFoundWeight;
    }

    public static void setNoRuntimeErrorFoundWeight(double noRuntimeErrorFoundWeight) {
        CodeValiditySubmetric.noRuntimeErrorFoundWeight = noRuntimeErrorFoundWeight;
    }

    /**
     * Resets variables of code validity submetric to be reused
     */
    public static void resetSubmetricVariables(){
        promptImprovementRemarks = new ArrayList<>();
        noParsingErrorFound = false;
        noRuntimeErrorFound = false;

    }
}
