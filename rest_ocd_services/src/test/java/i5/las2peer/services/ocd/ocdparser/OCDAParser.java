package i5.las2peer.services.ocd.ocdparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.*;

import com.github.javaparser.JavaParser;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;




public class OCDAParser {


    // Name of the method that holds compatible graph types of OCDA
    private static final String COMPATIBLE_GRAPH_TYPE_METHOD_NAME = "compatibleGraphTypes";


    // Declare javaParser as a class variable
    private static JavaParser javaParser = JavaParserSingleton.getInstance();


    /**
     * @param ocdaFileName      Name of the OCDA class file name
     * @return                  String representation of the path to the OCDA class
     */
    public static String getOCDAPath(String ocdaFileName){
        // path starting with 'rest_ocd_services'
        String relativeFilePath = "rest_ocd_services/src/main/java/i5/las2peer/services/ocd/algorithms/" + ocdaFileName;

        // Get the current working directory (content root)
        String contentRoot = System.getProperty("user.dir");

        // Create the absolute file path
        String absoluteFilePath = contentRoot + File.separator + relativeFilePath;

        return absoluteFilePath;
    }


    /**
     * Use the singleton JavaParser to parse the Java file
     * @return Compulation unit of the parsed file
     */
    private static CompilationUnit parseJavaFile(File file) {
        try {
            return javaParser.parse(file).getResult().orElseThrow(() -> new RuntimeException("Parsing failed"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return class name from the CompilationUnit
      */
    private static String getClassName(CompilationUnit cu) {
        return cu.findFirst(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse("No class name found");
    }



    /**
     * Gets method calls and corresponding method call lines for a specified java class (file)
     * @param javaFile    Java class the method calls of which to list
     */
    public static void listMethodCalls(File javaFile) {
        try {
            JavaParser javaParser = JavaParserSingleton.getInstance();
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(MethodCallExpr n, Object arg) {
                    super.visit(n, arg);
                    System.out.println(" [L " + n.getBegin().get().line + "] " + n);
                }
            }.visit(javaParser.parse(javaFile).getResult().get(), null);
        } catch ( IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets method calls and corresponding method call lines for a specified method within a specified
     * Java class.
     * @param javaFile         Java class to parse
     * @param targetMethodName Method within the Java class whose methods should be listed
     */
    public static void listMethodCallsInMethod(File javaFile, String targetMethodName) {
        try {
            javaParser.parse(javaFile).getResult().ifPresent(cu -> {
                Optional<MethodDeclaration> method = cu.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals(targetMethodName));

                method.ifPresent(m -> {
                    new VoidVisitorAdapter<Object>() {
                        @Override
                        public void visit(MethodCallExpr n, Object arg) {
                            super.visit(n, arg);
                            System.out.println(" [L " + n.getBegin().get().line + "] " + n);
                        }
                    }.visit(m, null);
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Identifies compatible graph types for a given OCDA algorithm
     * @param javaFile        OCDA algorithm class file
     * @return                List of compatible graph types with the specified OCDA
     */
    public static List<String> extractCompatibilities(File javaFile) {
        List<String> compatibilities = new ArrayList<>();
        try {
            javaParser.parse(javaFile).getResult().ifPresent(cu -> {
                Optional<MethodDeclaration> method = cu.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals(COMPATIBLE_GRAPH_TYPE_METHOD_NAME));

                method.ifPresent(m -> {
                    m.findAll(MethodCallExpr.class).stream()
                            .filter(mc -> mc.getNameAsString().equals("add"))
                            .forEach(mc -> {
                                mc.getArguments().forEach(arg -> {
                                    compatibilities.add(arg.toString());
                                });
                            });
                });
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return compatibilities;
    }


    /**
     * Identifies methods defined within the OCDA algorithm
     * @param javaFile       OCDA algorithm class file
     * @return               List of methods defined within the specified OCDA
     */
    public static List<String> extractMethods(File javaFile)  {

        try {
            CompilationUnit cu = javaParser.parse(javaFile).getResult().get();
            List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
            return methods.stream()
                    .map(MethodDeclaration::getNameAsString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}