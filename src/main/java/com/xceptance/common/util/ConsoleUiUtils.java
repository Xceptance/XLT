/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.lang.ThreadUtils;

/**
 * The {@link ConsoleUiUtils} class provides utility methods to ease building console-based UIs.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class ConsoleUiUtils
{
    /**
     * An {@link EofHandler} is called if EOF was detected while reading input from stdin.
     */
    public interface EofHandler
    {
        /**
         * Called if EOF was detected.
         */
        public void onEof();
    }

    /**
     * The list of registered {@link EofHandler} instances.
     */
    private static final Set<EofHandler> EOF_HANDLERS = new LinkedHashSet<EofHandler>();

    /**
     * A reader on top of {@link System#in}.
     */
    private static final BufferedReader STDIN_READER = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Adds the passed {@link EofHandler} instance to the internal list of handlers.
     * 
     * @param eofHandler
     *            the handler
     */
    public static void addEofHandler(final EofHandler eofHandler)
    {
        ParameterCheckUtils.isNotNull(eofHandler, "eofHandler");
        EOF_HANDLERS.add(eofHandler);
    }

    /**
     * Asks the user for confirmation.
     * 
     * @param message
     *            the message text presented to the user
     * @return <code>true</code> if the user confirmed, <code>false</code> otherwise
     */
    public static boolean confirm(String message)
    {
        message = message + " [y/n]";

        while (true)
        {
            final String s = readLine(message);

            if ("n".equalsIgnoreCase(s))
            {
                return false;
            }
            else if ("y".equalsIgnoreCase(s))
            {
                return true;
            }
        }
    }

    /**
     * Presents the user a list of items from which the user might select one or more. The selected items will be
     * returned. If so specified, an "All" item is added to the list, which - when chosen - selects all items at once.
     * 
     * @param <T>
     * @param header
     *            the header text
     * @param displayNames
     *            the display names of the items
     * @param items
     *            the corresponding items
     * @param allInsteadOfNone
     *            whether an "all" item is to be shown instead of the default "none" item
     * @return the selected items (never <code>null</code>)
     */
    public static <T> List<T> multiSelectItems(final String header, final List<String> displayNames, final List<T> items,
                                               final boolean allInsteadOfNone)
    {
        return multiSelectItems(header, displayNames, items, allInsteadOfNone, null);
    }

    /**
     * Presents the user a list of items from which the user might select one or more. The selected items will be
     * returned. If so specified, an "All" item is added to the list, which - when chosen - selects all items at once.
     * 
     * @param <T>
     * @param header
     *            the header text
     * @param displayNames
     *            the display names of the items
     * @param items
     *            the corresponding items
     * @param allInsteadOfNone
     *            whether an "all" item is to be shown instead of the default "none" item
     * @param allInsteadOfNothingLabel
     *            all/nothing label text. If blank there will be default labels.
     * @return the selected items (never <code>null</code>)
     */
    public static <T> List<T> multiSelectItems(final String header, List<String> displayNames, final List<T> items,
                                               final boolean allInsteadOfNone, final String allInsteadOfNothingLabel)
    {
        // copy the list of display names as we will extend the list
        displayNames = new ArrayList<>(displayNames);
        
        // add the "all"/"none" item as the first item
        if (StringUtils.isNotBlank(allInsteadOfNothingLabel))
        {
            displayNames.add(0, allInsteadOfNothingLabel);
        }
        else
        {
            displayNames.add(0, allInsteadOfNone ? "<all>" : "<none>");
        }

        // determine longest key
        int maxKeyLength = String.valueOf(displayNames.size()).length();

        // print the menu
        System.out.println(header);
        for (int i = 0; i < displayNames.size(); i++)
        {
            System.out.printf(" %s %s\n", StringUtils.leftPad("(" + i + ")", maxKeyLength + 2), displayNames.get(i));
        }

        // get the user's selection
        while (true)
        {
            final String s = readLine();
            if (StringUtils.isNotBlank(s))
            {
                try
                {
                    final String[] parts = StringUtils.split(s, " ,;");
                    final Set<T> result = new LinkedHashSet<T>();

                    for (int i = 0; i < parts.length; i++)
                    {
                        final int n = Integer.parseInt(parts[i]);

                        if (n == 0)
                        {
                            // all/none item
                            if (allInsteadOfNone)
                            {
                                result.addAll(items);
                                break;
                            }
                        }
                        else
                        {
                            result.add(items.get(n - 1));
                        }
                    }

                    return new ArrayList<T>(result);
                }
                catch (final IndexOutOfBoundsException e)
                {
                    // simply retry
                }
                catch (final NumberFormatException e)
                {
                    // simply retry
                }
            }
        }
    }

    /**
     * Asks the user to enter an integer value. The number must be greater than 0.
     * 
     * @param message
     *            the message text
     * @return the value entered
     */
    public static int readInt(final String message)
    {
        while (true)
        {
            final String s = readLine(message);

            try
            {
                final int i = Integer.parseInt(s);

                if (i >= 1)
                {
                    return i;
                }
            }
            catch (final NumberFormatException e)
            {
                // simply retry
            }
        }
    }

    /**
     * Reads a line of text from the console.
     * 
     * @return the line just read (never <code>null</code>)
     */
    public static String readLine()
    {
        return readLine(null);
    }

    /**
     * Reads a line of text from the console.
     * 
     * @param message
     *            the message text to show
     * @return the line just read (never <code>null</code>)
     */
    public static String readLine(final String message)
    {
        final StringBuilder prompt = new StringBuilder();

        if (!StringUtils.isBlank(message))
        {
            prompt.append(message).append(' ');
        }

        prompt.append("=> ");

        System.out.print(prompt);

        try
        {
            final String s = STDIN_READER.readLine();

            // EOF handling
            if (s == null)
            {
                // give some visual feedback
                System.out.println();

                // execute any registered EOF handlers
                runEofHandlers();

                // block this thread until regular shutdown is complete
                ThreadUtils.sleep();
            }

            return s;
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Failed to read input from stdin", e);
        }
    }

    /**
     * Removes the passed {@link EofHandler} instance from the internal list of handlers.
     * 
     * @param eofHandler
     *            the handler
     */
    public static void removeEofHandler(final EofHandler eofHandler)
    {
        ParameterCheckUtils.isNotNull(eofHandler, "eofHandler");
        EOF_HANDLERS.remove(eofHandler);
    }

    /**
     * Asks the user to select an item from a list of items.
     * 
     * @param <T>
     * @param header
     *            the header line
     * @param keys
     *            the keys used to select the respective items (maybe <code>null</code>, in which case numbers will be
     *            used)
     * @param displayNames
     *            the display names for the items
     * @param items
     *            the list of items
     * @return the selected item (never <code>null</code>)
     */
    public static <T> T selectItem(final String header, List<String> keys, final List<String> displayNames, final List<T> items)
    {
        // build the list of keys if none was specified
        if (keys == null)
        {
            keys = new ArrayList<String>();
            for (int i = 0; i < displayNames.size(); i++)
            {
                keys.add(String.valueOf(i + 1));
            }
        }

        // parameter check
        if (keys.size() != displayNames.size())
        {
            throw new IllegalArgumentException("The count of display names and keys has to be equal!");
        }
        if (keys.size() != items.size())
        {
            throw new IllegalArgumentException("The count of items and keys has to be equal!");
        }

        // determine longest key
        int maxKeyLength = 0;
        for (String key : keys)
        {
            maxKeyLength = Math.max(maxKeyLength, key.length());
        }

        // print the menu
        System.out.println(header);
        for (int i = 0; i < displayNames.size(); i++)
        {
            System.out.printf(" %s %s\n", StringUtils.leftPad("(" + keys.get(i) + ")", maxKeyLength + 2), displayNames.get(i));
        }

        // let the user choose an item
        while (true)
        {
            // read the key
            final String key = readLine();

            // search the key in the list of keys and return the corresponding item
            for (int i = 0; i < keys.size(); i++)
            {
                if (keys.get(i).equals(key))
                {
                    return items.get(i);
                }
            }
        }
    }

    /**
     * Asks the user to select an item from a list of items.
     * 
     * @param <T>
     * @param header
     *            the header line
     * @param displayNames
     *            the display names for the items
     * @param items
     *            the list of items
     * @return the selected item (never <code>null</code>)
     */
    public static <T> T selectItem(final String header, final List<String> displayNames, final List<T> items)
    {
        // parameter check
        if (displayNames.size() != items.size())
        {
            throw new IllegalArgumentException("The count of display names and items has to be equal!");
        }
        return selectItem(header, null, displayNames, items);
    }

    /**
     * Checks whether the ENTER key was pressed.
     * 
     * @return <code>true</code> if the ENTER key was pressed, <code>false</code> otherwise
     */
    public static boolean wasEnterKeyPressed()
    {
        try
        {
            if (STDIN_READER.ready())
            {
                // consume a line of input
                STDIN_READER.readLine();

                return true;
            }
            else
            {
                // no input available
                return false;
            }
        }
        catch (final IOException e)
        {
            // should never happen
            throw new RuntimeException("Failed to read input from stdin", e);
        }
    }

    /**
     * Runs the registered {@link EofHandler} instances in the order they were added.
     */
    private static void runEofHandlers()
    {
        for (final EofHandler eofHandler : EOF_HANDLERS)
        {
            eofHandler.onEof();
        }
    }

    /**
     * Constructor. Declared private to avoid instantiation.
     */
    private ConsoleUiUtils()
    {
    }
}
