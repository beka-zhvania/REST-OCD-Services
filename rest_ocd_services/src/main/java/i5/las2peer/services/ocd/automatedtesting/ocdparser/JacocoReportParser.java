package i5.las2peer.services.ocd.automatedtesting.ocdparser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import i5.las2peer.services.ocd.automatedtesting.helpers.CoverageData;
import i5.las2peer.services.ocd.automatedtesting.helpers.FileHelpers;
import i5.las2peer.services.ocd.automatedtesting.helpers.PathResolver;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.io.File;
import java.util.HashMap;

/**
 * This class is used to parse code coverage reports generated by JaCoCo.
 */
public class JacocoReportParser {

    private static final String JACOCO_REPORT_LOCATION = "rest_ocd_services/build/jacoco/test/jacocoTestReport.xml";

    /**
     * Parses coverage data from the JaCoCo report for a specified class
     * @param targetClassName       Class for which the coverage data should be parsed
     */
    public static HashMap<String, CoverageData> parseJacocoXmlReportForClass(String targetClassName) {

        // Map of aggregate coverage data where key is the type (e.g., LINE, BRANCH),
        // and value is a pair of missed and covered counts
        HashMap<String, CoverageData> aggregateCoverageData = new HashMap<>();


        try {
            File xmlFile = new File(PathResolver.addProjectRootPathIfSet(FileHelpers.cleanDuplicateDirectories(JACOCO_REPORT_LOCATION)));
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            // without this line, parser will try to load report.dtd which causes file not found exception
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList classes = doc.getElementsByTagName("class");

            for (int i = 0; i < classes.getLength(); i++) {
                Element clazz = (Element) classes.item(i);
                String className = clazz.getAttribute("name").replace('/', '.');
                String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
                if (simpleClassName.equals(targetClassName)) {
                    NodeList counters = clazz.getElementsByTagName("counter");
                    for (int j = 0; j < counters.getLength(); j++) {
                        Element counter = (Element) counters.item(j);
                        String type = counter.getAttribute("type");
                        int missed = Integer.parseInt(counter.getAttribute("missed"));
                        int covered = Integer.parseInt(counter.getAttribute("covered"));

                        aggregateCoverageData.put(type, new CoverageData(missed, covered));
                    }
                    break; // Break after finding the target class
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return aggregateCoverageData;
    }

    public static void main(String[] args) {
        HashMap<String, CoverageData> coverageData
                = parseJacocoXmlReportForClass("SpeakerListenerLabelPropagationAlgorithm");
        System.out.println(coverageData);
    }
}
