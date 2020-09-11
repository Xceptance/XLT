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
// Copyright (c) 2005-2020 Xceptance Software Technologies GmbH

package com.xceptance.xlt.engine.xltdriver;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.InvalidCoordinatesException;
import org.openqa.selenium.interactions.Mouse;

import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.html.DisabledElement;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.javascript.host.event.MouseEvent;
import com.xceptance.xlt.engine.scripting.htmlunit.HtmlUnitElementUtils;

/**
 * Implements mouse operations using the HtmlUnit WebDriver.
 */
public class HtmlUnitMouse implements Mouse {
  private final HtmlUnitDriver parent;
  private final HtmlUnitKeyboard keyboard;
  private DomElement currentActiveElement;
  private Point currentMousePosition; // HA #2124

  public HtmlUnitMouse(HtmlUnitDriver parent, HtmlUnitKeyboard keyboard) {
    this.parent = parent;
    this.keyboard = keyboard;
  }

  private DomElement getElementForOperation(Coordinates potentialCoordinates) {
    if (potentialCoordinates != null) {
      return (DomElement) potentialCoordinates.getAuxiliary();
    }

    if (currentActiveElement == null) {
      throw new InvalidCoordinatesException("About to perform an interaction that relies"
          + " on the active element, but there isn't one.");
    }

    return currentActiveElement;
  }

  @Override
  public void click(Coordinates elementCoordinates) {
    DomElement element = getElementForOperation(elementCoordinates);
    parent.click(element, false);
  }

  /**
   * @param directClick {@code true} for {@link WebElement#click()}
   * or {@code false} for {@link Actions#click()}
   */
  void click(DomElement element, boolean directClick) {
    if (!element.isDisplayed()) {
      throw new ElementNotInteractableException("You may only interact with visible elements");
    }

    moveOutIfNeeded(element);

    try {
      /*
      element.mouseOver();
      element.mouseMove();
      */

      element.click(keyboard.isShiftPressed(),
          keyboard.isCtrlPressed() || (directClick && element instanceof HtmlOption),
          keyboard.isAltPressed());
      updateActiveElement(element);
    } catch (IOException e) {
      throw new WebDriverException(e);
    } catch (ScriptException e) {
      // TODO(simon): This isn't good enough.
      System.out.println(e.getMessage());
      // Press on regardless
    } catch (RuntimeException e) {
      Throwable cause = e.getCause();
      if (cause instanceof SocketTimeoutException) {
        throw new TimeoutException(cause);
      }
      throw e;
    }
  }

  private void moveOutIfNeeded(DomElement element) {
    try {
      if ((currentActiveElement != element)) {
        if (currentActiveElement != null) {
          currentActiveElement.mouseOver(keyboard.isShiftPressed(),
              keyboard.isCtrlPressed(), keyboard.isAltPressed(), MouseEvent.BUTTON_LEFT);

          currentActiveElement.mouseOut(keyboard.isShiftPressed(),
              keyboard.isCtrlPressed(), keyboard.isAltPressed(), MouseEvent.BUTTON_LEFT);
        }

        if (element != null) {
          element.mouseMove(keyboard.isShiftPressed(),
              keyboard.isCtrlPressed(), keyboard.isAltPressed(),
              MouseEvent.BUTTON_LEFT);
          element.mouseOver(keyboard.isShiftPressed(),
              keyboard.isCtrlPressed(), keyboard.isAltPressed(),
              MouseEvent.BUTTON_LEFT);
        }
      }
    } catch (ScriptException ignored) {
      System.out.println(ignored.getMessage());
    }
  }

  private void updateActiveElement(DomElement element) {
    if (element != null) {
      currentActiveElement = element;
    }
  }

  @Override
  public void doubleClick(Coordinates elementCoordinates) {
    DomElement element = getElementForOperation(elementCoordinates);
    parent.doubleClick(element);
  }

  void doubleClick(DomElement element) {

    moveOutIfNeeded(element);

    // Send the state of modifier keys to the dblClick method.
    try {
      element.dblClick(keyboard.isShiftPressed(),
          keyboard.isCtrlPressed(), keyboard.isAltPressed());
      updateActiveElement(element);
    } catch (IOException e) {
      // TODO(eran.mes): What should we do in case of error?
      e.printStackTrace();
    }
  }

  @Override
  public void contextClick(Coordinates elementCoordinates) {
    DomElement element = getElementForOperation(elementCoordinates);

    moveOutIfNeeded(element);

    // HA #2142 start
    /*
    element.rightClick(keyboard.isShiftPressed(),
        keyboard.isCtrlPressed(), keyboard.isAltPressed());
    */
    if(currentMousePosition == null)
    {
        element.rightClick(keyboard.isShiftPressed(),keyboard.isCtrlPressed(),
               keyboard.isAltPressed());
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

  @Override
  public void mouseDown(Coordinates elementCoordinates) {
    DomElement element = getElementForOperation(elementCoordinates);
    parent.mouseDown(element);
  }

  void mouseDown(DomElement element) {
    moveOutIfNeeded(element);

    element.mouseDown(keyboard.isShiftPressed(),
        keyboard.isCtrlPressed(), keyboard.isAltPressed(),
        MouseEvent.BUTTON_LEFT);

    updateActiveElement(element);
  }

  @Override
  public void mouseUp(Coordinates elementCoordinates) {
    DomElement element = getElementForOperation(elementCoordinates);
    parent.mouseUp(element);
  }

  void mouseUp(DomElement element) {
    moveOutIfNeeded(element);

    element.mouseUp(keyboard.isShiftPressed(),
        keyboard.isCtrlPressed(), keyboard.isAltPressed(),
        MouseEvent.BUTTON_LEFT);

    updateActiveElement(element);
  }

  @Override
  public void mouseMove(Coordinates elementCoordinates) {
    // HA #2142 start
    /*
    DomElement element = (DomElement) elementCoordinates.getAuxiliary();
    parent.mouseMove(element);
    */
    mouseMove(elementCoordinates, 0 ,0);
    // HA #2142 end
  }

  void mouseMove(DomElement element) {
    moveOutIfNeeded(element);

    updateActiveElement(element);
  }

  @Override
  public void mouseMove(Coordinates where, long xOffset, long yOffset) {
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
        if ((currentActiveElement != element)) {
          if (currentActiveElement != null) {
            currentActiveElement.mouseOver(keyboard.isShiftPressed(),
                keyboard.isCtrlPressed(), keyboard.isAltPressed(), MouseEvent.BUTTON_LEFT);
    
            currentActiveElement.mouseOut(keyboard.isShiftPressed(),
                keyboard.isCtrlPressed(), keyboard.isAltPressed(), MouseEvent.BUTTON_LEFT);
    
            currentActiveElement.blur();
          }
    
          if (element != null) {
            mouseMove(element, coordX, coordY);
          }
        }
      } catch (ScriptException ignored) {
        System.out.println(ignored.getMessage());
      }
    }

  private void mouseMove(DomElement element, final long coordX, final long coordY) {
      if(element instanceof DisabledElement && ((DisabledElement) element).isDisabled())
      {
          return;
      }
      
      {
          final MouseEvent event = new MouseEvent(element, MouseEvent.TYPE_MOUSE_MOVE, keyboard.isShiftPressed(), keyboard.isCtrlPressed(), keyboard.isAltPressed(), MouseEvent.BUTTON_LEFT);
          event.setClientX((int)coordX);
          event.setClientY((int) coordY);
          element.fireEvent(event);
      }
      
      {
          final MouseEvent event = new MouseEvent(element, MouseEvent.TYPE_MOUSE_OVER, keyboard.isShiftPressed(), keyboard.isCtrlPressed(), keyboard.isAltPressed(), MouseEvent.BUTTON_LEFT);
          event.setClientX((int)coordX);
          event.setClientY((int) coordY);
          element.fireEvent(event);

      }
    }
  // HA #2039 end

  // HA #2142 start
  private void updateMousePosition(final Point newPosition) {
      if(newPosition != null) {
          currentMousePosition = newPosition;
      }
  }
  // HA #2142 end
}
