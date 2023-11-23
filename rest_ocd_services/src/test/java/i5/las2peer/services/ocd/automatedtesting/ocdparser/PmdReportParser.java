package i5.las2peer.services.ocd.automatedtesting.ocdparser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import i5.las2peer.services.ocd.automatedtesting.ocdparser.helpers.CodeSmellData;
import i5.las2peer.services.ocd.automatedtesting.ocdparser.helpers.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used to parse code quality reports generated by PMD.
 * These contribute to calculating the code quality metric value.
 */
public class PmdReportParser {

    private static final String PMD_REPORT_LOCATION = "rest_ocd_services/build/reports/pmd/pmd.xml";


    /**
     * Parses violations from the PMD report for a specified class
     * @param targetClassName       Class for which the violations should be parsed
     */
    public static HashMap<String, List<CodeSmellData>> parsePmdXmlReportForViolationsForClass(String targetClassName) {

        // map of rule violations where value is a pair of line number where violation occurred and its description
        HashMap<String, List<CodeSmellData>> ruleViolations = new HashMap<>();

        try {
            File xmlFile = new File(PMD_REPORT_LOCATION);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList violations = doc.getElementsByTagName("violation");

            for (int i = 0; i < violations.getLength(); i++) {
                Element violation = (Element) violations.item(i);
                String className = violation.getAttribute("class");
                if (className.equals(targetClassName)) {
                    String violatedRule = violation.getAttribute("rule");
                    if(!violatedRule.equals("DataflowAnomalyAnalysis")) { // ignore data flow anomalies

                        String beginLine = violation.getAttribute("beginline");
                        String endLine = violation.getAttribute("endline");
                        String ruleSet = violation.getAttribute("ruleset");
                        String description = violation.getTextContent().trim();

                        CodeSmellData codeSmellData = new CodeSmellData(beginLine, endLine, violatedRule, ruleSet, className, description);


                        // add violation to the list of violations
                        ruleViolations.computeIfAbsent(violatedRule, k -> new ArrayList<>()).add(codeSmellData);

                    }
                }
            }

            System.out.println("codeSmells are: " + ruleViolations);//TODO:DELETE
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ruleViolations;
    }


    public static void main(String[] args) {

        HashMap<String, List<CodeSmellData>> ruleViolations
                = parsePmdXmlReportForViolationsForClass("SskAlgorithmTest");
        System.out.println(ruleViolations);
    }
}