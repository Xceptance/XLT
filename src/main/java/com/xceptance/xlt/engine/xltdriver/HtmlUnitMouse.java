// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//
// Copyright (c) 2005-2022 Xceptance Software Technologies GmbH

package com.xceptance.xlt.engine.xltdriver;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Coordinates;

import org.htmlunit.ScriptException;
import org.htmlunit.html.DisabledElement;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.javascript.host.event.MouseEvent;

import com.xceptance.xlt.engine.scripting.htmlunit.HtmlUnitElementUtils;

/**
 * Implements mouse operations using the HtmlUnit WebDriver.
 *
 * @author Simon Stewart
 * @author Alexei Barantsev
 * @author Ahmed Ashour
 * @author Ronald Brill
 * @author Martin Bartoš
 */
public class HtmlUnitMouse {
    private final HtmlUnitDriver parent_;
    private final HtmlUnitKeyboard keyboard_;
    private DomElement currentActiveElement_;

    private Point currentMousePosition; // HA #2124

    public HtmlUnitMouse(final HtmlUnitDriver parent, final HtmlUnitKeyboard keyboard) {
        this.parent_ = parent;
        this.keyboard_ = keyboard;
    }

    private DomElement getElementForOperation(final Coordinates potentialCoordinates) {
        if (potentialCoordinates != null) {
            return (DomElement) potentialCoordinates.getAuxiliary();
        }

        if (currentActiveElement_ == null) {
            throw new NoSuchElementException(
                    "About to perform an interaction that relies" + " on the active element, but there isn't one.");
        }

        return currentActiveElement_;
    }

    public void click(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.click(element, false);
    }

    /**
     * @param directClick {@code true} for {@link WebElement#click()} or
     *                    {@code false} for {@link Actions#click()}
     */
    void click(final DomElement element, final boolean directClick) {
        if (!element.isDisplayed()) {
            throw new ElementNotInteractableException("You may only interact with visible elements");
        }

        moveOutIfNeeded(element);

        try {
            /*
            element.mouseOver();
            element.mouseMove();
            */

            element.click(keyboard_.isShiftPressed(),
                    keyboard_.isCtrlPressed() || (directClick && element instanceof HtmlOption),
                    keyboard_.isAltPressed());
            updateActiveElement(element);
        }
        catch (final IOException e) {
            throw new WebDriverException(e);
        }
        catch (final ScriptException e) {
            // TODO(simon): This isn't good enough.
            System.out.println(e.getMessage());
            // Press on regardless
        }
        catch (final RuntimeException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof SocketTimeoutException) {
                throw new TimeoutException(cause);
            }
            throw e;
        }
    }

    private void moveOutIfNeeded(final DomElement element) {
        try {
            if (currentActiveElement_ != element) {
                if (currentActiveElement_ != null) {
                    currentActiveElement_.mouseOver(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(),
                            keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);

                    currentActiveElement_.mouseOut(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(),
                            keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);
                }

                if (element != null) {
                    element.mouseMove(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                            MouseEvent.BUTTON_LEFT);
                    element.mouseOver(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                            MouseEvent.BUTTON_LEFT);
                }
            }
        }
        catch (final ScriptException ignored) {
            System.out.println(ignored.getMessage());
        }
    }

    private void updateActiveElement(final DomElement element) {
        if (element != null) {
            currentActiveElement_ = element;
        }
    }

    public void doubleClick(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.doubleClick(element);
    }

    void doubleClick(final DomElement element) {

        moveOutIfNeeded(element);

        // Send the state of modifier keys to the dblClick method.
        try {
            element.dblClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());
            updateActiveElement(element);
        }
        catch (final IOException e) {
            // TODO(eran.mes): What should we do in case of error?
            e.printStackTrace();
        }
    }

    public void contextClick(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);

        moveOutIfNeeded(element);

        // HA #2142 start
        /*
        element.rightClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());
        */
        if (currentMousePosition == null)
        {
            element.rightClick(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed());
        }
        else
        {
            final int xPos = currentMousePosition.getX();
            final int yPos = currentMousePosition.getY();
            HtmlUnitElementUtils.fireMouseEvent(element, "mousedown", xPos, yPos, MouseEvent.BUTTON_RIGHT);
            HtmlUnitElementUtils.fireMouseEvent(element, "mouseup", xPos, yPos, MouseEvent.BUTTON_RIGHT);
            HtmlUnitElementUtils.fireMouseEvent(element, "contextmenu", xPos, yPos, MouseEvent.BUTTON_RIGHT);
        }
        // HA #2142 end

        updateActiveElement(element);
    }

    public void mouseDown(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseDown(element);
    }

    void mouseDown(final DomElement element) {
        moveOutIfNeeded(element);

        element.mouseDown(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);

        updateActiveElement(element);
    }

    public void mouseUp(final Coordinates elementCoordinates) {
        final DomElement element = getElementForOperation(elementCoordinates);
        parent_.mouseUp(element);
    }

    void mouseUp(final DomElement element) {
        moveOutIfNeeded(element);

        element.mouseUp(keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(),
                MouseEvent.BUTTON_LEFT);

        updateActiveElement(element);
    }

    public void mouseMove(final Coordinates elementCoordinates) {
        // HA #2142 start
        /*
        final DomElement element = (DomElement) elementCoordinates.getAuxiliary();
        parent_.mouseMove(element);
        */
        mouseMove(elementCoordinates, 0 ,0);
        // HA #2142 end
    }

    void mouseMove(final DomElement element) {
        moveOutIfNeeded(element);

        updateActiveElement(element);
    }

    public void mouseMove(final Coordinates where, final long xOffset, final long yOffset) {
        // HA #2039 start
        /*
        throw new UnsupportedOperationException("Moving to arbitrary X,Y coordinates not supported.");
        */

        final DomElement e = getElementForOperation(where);
        final Point p = where.onPage();
        final long coordX = p.getX() + xOffset;
        final long coordY = p.getY() + yOffset;

        moveOutIfNeeded(e, coordX, coordY);
        updateMousePosition(new Point((int)coordX, (int)coordY)); // HA #2142 
        updateActiveElement(e);
        // HA #2039 end
    }

    // HA #2039 start
    private void moveOutIfNeeded(DomElement element, final long coordX, final long coordY) {
        try {
            if ((currentActiveElement_ != element)) {
                if (currentActiveElement_ != null) {
                    currentActiveElement_.mouseOver(keyboard_.isShiftPressed(),
                        keyboard_.isCtrlPressed(), keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);

                    currentActiveElement_.mouseOut(keyboard_.isShiftPressed(),
                        keyboard_.isCtrlPressed(), keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);

                    currentActiveElement_.blur();
                }

                if (element != null) {
                    mouseMove(element, coordX, coordY);
                }
            }
        } 
        catch (ScriptException ignored) {
            System.out.println(ignored.getMessage());
        }
    }

    private void mouseMove(DomElement element, final long coordX, final long coordY) {
        if (element instanceof DisabledElement && ((DisabledElement) element).isDisabled()) {
            return;
        }
      
        {
            final MouseEvent event = new MouseEvent(element, MouseEvent.TYPE_MOUSE_MOVE, keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);
            event.setClientX((int)coordX);
            event.setClientY((int) coordY);
            element.fireEvent(event);
        }
      
        {
            final MouseEvent event = new MouseEvent(element, MouseEvent.TYPE_MOUSE_OVER, keyboard_.isShiftPressed(), keyboard_.isCtrlPressed(), keyboard_.isAltPressed(), MouseEvent.BUTTON_LEFT);
            event.setClientX((int)coordX);
            event.setClientY((int) coordY);
            element.fireEvent(event);
        }
    }
    // HA #2039 end

    // HA #2142 start
    private void updateMousePosition(final Point newPosition) {
        if (newPosition != null) {
            currentMousePosition = newPosition;
        }
    }
    // HA #2142 end
}
