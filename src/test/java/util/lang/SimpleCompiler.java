package util.lang;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import com.xceptance.xlt.api.util.XltLogger;

/**
 * Convenience class to compile classes which are given by their source code at runtime.
 * 
 * @author Sebastian Oerding
 */
public class SimpleCompiler
{
    private static final String CLASSES_DIRECTORY = "target/classes";

    /**
     * Compiles the argument source code and returns the compilation result as a {@link ClassAsByteArray} instance.
     * Currently this method only returns one single class for the argument source, so it can not deal with nested
     * classes appropriately (the class will compile but you will get a {@link ClassNotFoundException} when trying to
     * load such a class).
     * 
     * @param className
     *            the full qualified class name without the file suffix &quot;.java&quot;
     * @param sourceCode
     *            the source code of the class, while pretty printing is not required is gives a helpful representation
     *            in case of compilation errors, including line numbers
     * @throws AssertionError
     *             in case of an compiler error or compilation resulted in 0 or more than 1 classes
     * @throws IllegalArgumentException
     *             if the argument class name is <code>null</code> or empty or ends with &quot; .java&quot; or the
     *             source code is <code>null</code> or empty
     * @throws IllegalArgumentException
     *             if the className is an invalid URI
     */
    public static ClassAsByteArray compile(final String className, final String sourceCode)
    {
        /* checking / dealing with arguments */
        checkArguments(className, sourceCode);
        final String modifiedClassName = classNameAsRelativePath(className);
        final int indexOfLastSlash = modifiedClassName.lastIndexOf('/');
        final String fileName = modifiedClassName.substring(indexOfLastSlash + 1) + ".java";

        /* collecting compiler arguments */
        final List<String> opts = Arrays.asList(new String[]
            {
                "-d", CLASSES_DIRECTORY
            });
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);
        final List<? extends JavaFileObject> fileObjects = getCompilationUnits(fileName, sourceCode);

        /* call compiler and log result */
        final CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector, opts, null, fileObjects);
        logMessages(className, sourceCode, task.call(), diagnosticsCollector.getDiagnostics());

        return extractResult(CLASSES_DIRECTORY, modifiedClassName, fileManager);
    }

    /**
     * Replaces the dots in the class name with slashes.
     * 
     * @param className
     * @return the argument class name as file system path (UNIX-like) with each dot replaced by a slash.
     */
    private static String classNameAsRelativePath(final String className)
    {
        return className.replace('.', '/');
    }

    /**
     * Wraps the arguments into a list of {@link JavaFileObject}s containing a single element.
     * 
     * @param fileName
     * @param sourceCode
     * @return a list containing a single element constructed from the method arguments
     * @throws URISyntaxException
     */
    private static List<? extends JavaFileObject> getCompilationUnits(final String fileName, final String sourceCode)
    {
        JavaFileObject javaObjectFromString;
        try
        {
            javaObjectFromString = new JavaObjectFromString(fileName, sourceCode);
        }
        catch (final URISyntaxException e)
        {
            throw new IllegalArgumentException("Invalid class name, can not be treated as URI!", e);
        }
        final List<? extends JavaFileObject> fileObjects = Arrays.asList(javaObjectFromString);
        return fileObjects;
    }

    /**
     * Checks the arguments and throws an error if one of them or both are <code>
     * null</code> or empty or the class name does not end with &quot;.java&quot;
     * 
     * @param className
     * @param sourceCode
     * @throws IllegalArgumentException
     */
    private static void checkArguments(final String className, final String sourceCode)
    {
        if (className == null || className.isEmpty() || className.endsWith(".java"))
        {
            throw new IllegalArgumentException("Class name may neither be \"null\" nor empty and must not end with \".java\"!");
        }

        if (sourceCode == null || sourceCode.isEmpty())
        {
            throw new IllegalArgumentException("Source code may neither be \"null\" nor empty!");
        }
    }

    /**
     * Extracts the single class from the result.
     * 
     * @param classesDirectory
     *            the directory in which the classes are contained, required to get the class from the argument
     *            fileManager
     * @param className
     *            the name of the class to return
     * @param fileManager
     *            the {@link StandardJavaFileManager} which contains the compilation result
     * @return a new {@link ClassAsByteArray} from the argument strings and the compilation result in the argument
     *         fileManager
     * @throws IOException
     *             if there is no compilation result for the argument class name in the argument fileManager
     */
    private static ClassAsByteArray extractResult(final String classesDirectory, final String className,
                                                  final StandardJavaFileManager fileManager)
    {
        // relative path to class
        final String pathToClass = classesDirectory + "/" + className + ".class";
        final Iterable<? extends JavaFileObject> outFileObjects = fileManager.getJavaFileObjects(pathToClass);

        final List<ClassAsByteArray> tempReturnList = new ArrayList<ClassAsByteArray>();
        for (final JavaFileObject jfo : outFileObjects)
        {
            try
            {
                final byte[] classBytes = IOUtils.toByteArray(jfo.openInputStream());
                tempReturnList.add(new ClassAsByteArray(className, classBytes));
            }
            catch (final IOException e)
            {
                throw new IllegalStateException("Failed to obtain an input stream contained the compilation result!" + "Looked for \"" +
                                                pathToClass + "\".");
            }
        }
        Assert.assertTrue("Expected 1 compilation result but obtained " + tempReturnList.size(), tempReturnList.size() == 1);
        return tempReturnList.get(0);
    }

    /**
     * Logs the messages collected during compilation, also logs an additional error in case that compilation has been
     * failed.
     * 
     * @param className
     * @param sourceCode
     * @param result
     * @param diagnostics
     * @throws AssertionError
     *             in case that compilation failed
     */
    private static void logMessages(final String className, final String sourceCode, final Boolean result,
                                    final List<Diagnostic<? extends JavaFileObject>> diagnostics)
    {
        for (final Diagnostic<? extends JavaFileObject> d : diagnostics)
        {
            XltLogger.runTimeLogger.warn(d.getMessage(Locale.getDefault()));
        }
        if (!result)
        {
            final StringBuilder sb = new StringBuilder(className.length() + (sourceCode.length() * 2) + 80);
            sb.append("Compilation of class with name \"");
            sb.append(className);
            sb.append("\" with source code \"\n");
            final String[] lines = sourceCode.split("\n");
            int lineIndex = 0;
            for (lineIndex = 0; lineIndex < lines.length;)
            {
                sb.append((lineIndex + 1));
                sb.append("\t");
                sb.append(lines[lineIndex++]);
                sb.append("\n");
            }
            sb.append("\" failed!");
            XltLogger.runTimeLogger.error(sb.toString());
            Assert.fail("Compilation of class \"" + className + "\"failed");
        }
    }

    /**
     * Compiles the argument classes and returns an array which contains for each {@link ClassAsSourceCode} one
     * {@link ClassAsByteArray} which wraps the byte code and class name of the corresponding ClassAsSourceCode.
     * 
     * @param classes
     *            the classes to compile given by their source code
     * @return an array which contains the resulting byte code for the argument classes
     */
    public static ClassAsByteArray[] compile(final Collection<ClassAsSourceCode> classes)
    {
        if (classes == null || classes.isEmpty())
        {
            throw new IllegalArgumentException("You may neither give \"null\" nor an empty collection as argument!");
        }
        final ClassAsByteArray[] returnValue = new ClassAsByteArray[classes.size()];

        int index = 0;
        for (final ClassAsSourceCode casc : classes)
        {
            // intentional side effect
            returnValue[index++] = compile(casc.getClassName(), casc.getClassAsString());
        }
        return returnValue;
    }
}
