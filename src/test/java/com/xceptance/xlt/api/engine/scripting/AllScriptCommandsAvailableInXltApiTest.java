package com.xceptance.xlt.api.engine.scripting;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.engine.scripting.htmlunit.HtmlUnitScriptCommands;
import com.xceptance.xlt.engine.scripting.webdriver.WebDriverScriptCommands;

/**
 * Test class to check, if all methods of the class {@link ScriptCommands} also are implemented in the class
 * {@link AbstractHtmlUnitCommandsModule} and in the class {@link AbstractHtmlUnitScriptAction}.
 * 
 * @author sebastianloob
 */
public class AllScriptCommandsAvailableInXltApiTest
{
    /**
     * Check the methods of class {@link AbstractHtmlUnitCommandsModule}.
     */
    @Test
    public void testAbstractHtmlUnitCommandsModule()
    {
        // implemented methods
        final Method[] expectedMethods = ScriptCommands.class.getDeclaredMethods();
        final Method[] actualMethods = AbstractHtmlUnitCommandsModule.class.getDeclaredMethods();

        // check the methods
        checkAllMethods(expectedMethods, actualMethods, AllScriptCommandsAvailableInXltApiTest::isImplemented_HtmlUnit);
    }

    /**
     * Check the methods of class {@link AbstractHtmlUnitScriptAction}.
     */
    @Test
    public void testAbstractHtmlUnitScriptAction()
    {
        // implemented methods
        final Method[] expectedMethods = ScriptCommands.class.getDeclaredMethods();
        final Method[] actualMethods = AbstractHtmlUnitScriptAction.class.getDeclaredMethods();

        // check the methods
        checkAllMethods(expectedMethods, actualMethods, AllScriptCommandsAvailableInXltApiTest::isImplemented_HtmlUnit);
    }

    /**
     * Check the methods of class {@link HtmlUnitScriptCommands}.
     */
    @Test
    public void testHtmlUnitScriptCommands()
    {
        // implemented methods
        final Method[] expectedMethods = ScriptCommands.class.getDeclaredMethods();
        final Method[] actualMethods = HtmlUnitScriptCommands.class.getMethods();

        // check the methods
        checkAllMethods(expectedMethods, actualMethods, AllScriptCommandsAvailableInXltApiTest::isImplemented_HtmlUnit);
    }

    /**
     * Check the methods of class {@link WebDriverScriptCommands}.
     */
    @Test
    public void testWebDriverScriptCommands()
    {
        // implemented methods
        final Method[] expectedMethods = ScriptCommands.class.getDeclaredMethods();
        final Method[] actualMethods = WebDriverScriptCommands.class.getMethods();

        // check the methods
        checkAllMethods(expectedMethods, actualMethods, AllScriptCommandsAvailableInXltApiTest::isImplemented_WebDriver);
    }

    /**
     * Check the methods of class {@link StaticScriptCommands}.
     */
    @Test
    public void testStaticScriptCommands()
    {
        // implemented methods
        final Method[] expectedMethods = ScriptCommands.class.getDeclaredMethods();
        final Method[] actualMethods = StaticScriptCommands.class.getDeclaredMethods();

        // check the methods
        checkAllMethods(expectedMethods, actualMethods, AllScriptCommandsAvailableInXltApiTest::isImplemented_WebDriverStatic);
    }

    /**
     * Assure that {@link AbstractWebDriverScriptTestCase} implements {@link WebDriverScriptCommands}.
     */
    @Test
    public void testAbstractWebDriverScriptTestCase()
    {
        final Method[] expected = WebDriverScriptCommands.class.getMethods();
        final Method[] actual = AbstractWebDriverScriptTestCase.class.getDeclaredMethods();

        checkAllMethods(expected, actual, AllScriptCommandsAvailableInXltApiTest::isImplemented_WebDriver2);
    }

    /**
     * Assure that {@link AbstractWebDriverModule} implements {@link WebDriverScriptCommands}.
     */
    @Test
    public void testAbstractWebDriverModule()
    {
        final Method[] expected = WebDriverScriptCommands.class.getMethods();
        final Method[] actual = AbstractWebDriverModule.class.getDeclaredMethods();

        checkAllMethods(expected, actual, AllScriptCommandsAvailableInXltApiTest::isImplemented_WebDriver2);
    }

    /**
     * Assure that {@link StaticScriptCommands} implements {@link WebDriverScriptCommands}.
     */
    @Test
    public void testStaticScriptCommands2()
    {
        final Method[] expected = WebDriverScriptCommands.class.getMethods();
        final Method[] actual = StaticScriptCommands.class.getDeclaredMethods();

        checkAllMethods(expected, actual, AllScriptCommandsAvailableInXltApiTest::isImplemented_WebDriver2);
    }

    private void checkAllMethods(final Method[] expectedMethods, final Method[] actualMethods,
                                 final BiFunction<Method, Method, Boolean> checkFunction)
    {
        String missingMethods = "";
        // check all expected methods
        for (final Method expectedMethod : expectedMethods)
        {
            boolean isImplemented = false;
            // check, if the expected method is implemented in the actual methods
            for (final Method actualMethod : actualMethods)
            {
                if (checkFunction.apply(expectedMethod, actualMethod))
                {
                    isImplemented = true;
                    break;
                }
            }
            if (!isImplemented)
            {
                missingMethods += "\n" + expectedMethod.getName() + arrayToString(expectedMethod.getParameterTypes());
            }
        }
        Assert.assertEquals("", missingMethods);
    }

    // check, if the two given methods are equal
    private static boolean isImplemented_HtmlUnit(final Method expectedMethod, final Method actualMethod)
    {
        // special methods, which don't need to be implemented
        if (expectedMethod.getName().equals("startAction") || expectedMethod.getName().equals("open"))
        {
            return true;
        }

        return isImplemented(expectedMethod, actualMethod);
    }

    // check, if the two given methods are equal
    private static boolean isImplemented_WebDriver(final Method expectedMethod, final Method actualMethod)
    {
        // special methods, which don't need to be implemented
        if (expectedMethod.getName().equals("startAction"))
        {
            return true;
        }

        return isImplemented(expectedMethod, actualMethod);
    }

    private static boolean isImplemented_WebDriver2(final Method expectedMethod, final Method actualMethod)
    {
        /*
         * Some methods are implemented by adapter only -> filter them out
         */
        final String methodName = expectedMethod.getName();
        if (methodName.equals("getUnderlyingWebDriver"))
        {
            return true;
        }
        if (methodName.equals("open"))
        {
            final Class<?>[] paramTypes = expectedMethod.getParameterTypes();
            if (paramTypes.length > 0 && paramTypes[0].equals(URL.class))
            {
                return true;
            }
        }

        return isImplemented_WebDriver(expectedMethod, actualMethod);
    }

    // check, if the two given methods are equal
    private static boolean isImplemented_WebDriverStatic(final Method expectedMethod, final Method actualMethod)
    {
        // ensure the actual method is public and static
        if ((actualMethod.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) != (Modifier.STATIC | Modifier.PUBLIC))
        {
            return false;
        }

        return isImplemented(expectedMethod, actualMethod);
    }

    // check, if the two given methods are equal
    private static boolean isImplemented(final Method expectedMethod, final Method actualMethod)
    {
        // have the methods the same name
        if (actualMethod.getName().equals(expectedMethod.getName()))
        {
            // have the methods the same count of parameters
            if (actualMethod.getParameterTypes().length == expectedMethod.getParameterTypes().length)
            {
                for (int i = 0; i < actualMethod.getParameterTypes().length; i++)
                {
                    // have the methods the same parameter types
                    if (!actualMethod.getParameterTypes()[i].equals(expectedMethod.getParameterTypes()[i]))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private String arrayToString(final Object[] array)
    {
        String result = "(";
        for (int i = 0; i < array.length; i++)
        {
            result += (i + 1 == array.length) ? array[i] : array[i] + ", ";
        }
        result += ")";
        return result;
    }
}
