/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.javascript.host.canvas;

import static org.htmlunit.junit.BrowserRunner.TestedBrowser.CHROME;
import static org.htmlunit.junit.BrowserRunner.TestedBrowser.EDGE;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.htmlunit.WebDriverTestCase;
import org.htmlunit.junit.BrowserRunner;
import org.htmlunit.junit.BrowserRunner.Alerts;
import org.htmlunit.junit.BrowserRunner.HtmlUnitNYI;
import org.htmlunit.junit.BrowserRunner.NotYetImplemented;
import org.htmlunit.util.MimeType;
import org.htmlunit.util.NameValuePair;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link CanvasRenderingContext2D}.
 *
 * @author Ahmed Ashour
 * @author Marc Guillemot
 * @author Frank Danek
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class CanvasRenderingContext2DTest extends WebDriverTestCase {

    /**
     * Closes the real IE; the drawImage test are producing strange
     * results without this.
     */
    @After
    public void shutDownRealBrowsersAfter() {
        shutDownRealIE();
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("done")
    public void test() throws Exception {
        final String html =
            "<html><head><script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "function test() {\n"
            + "  var canvas = document.getElementById('myCanvas');\n"
            + "  try {\n"
            + "    var ctx = canvas.getContext('2d');\n"
            + "    ctx.fillStyle = 'rgb(200,0,0)';\n"
            + "    ctx.fillRect(10, 10, 55, 50);\n"
            + "    ctx.fillStyle = 'rgba(0, 0, 200, 0.5)';\n"
            + "    ctx.fillRect(30, 30, 55, 50);\n"
            + "    ctx.drawImage(canvas, 1, 2);\n"
            + "    ctx.drawImage(canvas, 1, 2, 3, 4);\n"
            + "    ctx.drawImage(canvas, 1, 1, 1, 1, 1, 1, 1, 1);\n"
            + "    ctx.translate(10, 10);\n"
            + "    ctx.scale(10, 10);\n"
            + "    ctx.fillRect(30, 30, 55, 50);\n"
            + "    ctx.beginPath();\n"
            + "    ctx.moveTo(0, 10);\n"
            + "    ctx.lineTo(10, 10);\n"
            + "    ctx.quadraticCurveTo(0, 10, 15, 10);\n"
            + "    ctx.closePath();\n"
            + "    ctx.rotate(1.234);\n"
            + "    log('done');\n"
            + "  } catch(e) { log('exception'); }\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "  <canvas id='myCanvas'></canvas></body>\n"
            + LOG_TEXTAREA
            + "</html>";
        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = {"addHitRegion", "drawCustomFocusRing", "drawSystemFocusRing", "removeHitRegion",
                       "scrollPathIntoView", "36 methods"},
            IE = {"addHitRegion", "drawCustomFocusRing", "drawSystemFocusRing", "ellipse",
                  "removeHitRegion", "scrollPathIntoView", "35 methods"})
    public void methods() throws Exception {
        final String[] methods = {"addHitRegion", "arc", "arcTo", "beginPath", "bezierCurveTo", "clearRect", "clip",
            "closePath", "createImageData", "createLinearGradient", "createPattern", "createRadialGradient",
            "drawImage", "drawCustomFocusRing", "drawSystemFocusRing", "ellipse", "fill", "fillRect", "fillText",
            "getImageData", "getLineDash", "isPointInPath", "lineTo", "measureText", "moveTo", "putImageData",
            "quadraticCurveTo", "rect", "removeHitRegion", "restore", "rotate", "save", "scale", "scrollPathIntoView",
            "setLineDash", "setTransform", "stroke", "strokeRect", "strokeText", "transform", "translate" };
        final String html = "<html><body>\n"
            + "<canvas id='myCanvas'></canvas>\n"
            + LOG_TEXTAREA
            + "<script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "  var canvas = document.getElementById('myCanvas');\n"
            + "  var nbMethods = 0;\n"
            + "  var methods = ['" + String.join("', '", methods) + "'];\n"
            + "  try {\n"
            + "    var ctx = canvas.getContext('2d');\n"
            + "    for (var i = 0; i < methods.length; i++) {\n"
            + "      if (typeof ctx[methods[i]] == 'function')\n"
            + "        nbMethods++;\n"
            + "      else\n"
            + "        log(methods[i]);\n"
            + "    }\n"
            + "    log(nbMethods + ' methods');\n"
            + "  } catch(e) { log('exception'); }\n"
            + "</script></body></html>";

        loadPageVerifyTextArea2(html);
    }

    private void drawImage(final String png, final String canvasSetup, final String drawJS) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(png)) {
            final byte[] directBytes = IOUtils.toByteArray(is);
            final List<NameValuePair> emptyList = Collections.emptyList();
            getMockWebConnection().setResponse(URL_SECOND, directBytes, 200, "ok", MimeType.IMAGE_PNG, emptyList);
            getMockWebConnection().setDefaultResponse("Test");
        }

        final String html = "<html><head>\n"
            + "<script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "  function test() {\n"
            + "    var img = document.getElementById('myImage');\n"
            + "    var canvas = document.createElement('canvas');\n"
            + canvasSetup
            + "    if (canvas.getContext) {\n"
            + "      var context = canvas.getContext('2d');\n"
            + drawJS
            + "      log(canvas.toDataURL());\n"
            + "    }\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='test()'>\n"
            + "  <img id='myImage' src='" + URL_SECOND + "'>\n"
            + LOG_TEXTAREA
            + "</body></html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAutJREFUO"
                    + "E99k19sU1Ucxz/3du1du94tLcE1IjIUHlASRcEOtnVccWyIJWRmuhD02cBQlMTnvfsiIUQy9uIDCf+hWYFN3Q0mBAIJSCQy"
                    + "yIgORMtGHe1u71273T/m3qYN2wPn5OSc3znn90nO+X2/AsDF8XGp5vGT/QFJahBFEVEE0T14vtkwndfuf9Sp/Pj8tuAGF0Z"
                    + "GExveXferaVoIgoAggOD28qI8Ow6iKHD1xs3Dya4P+ioQD3AilVbaNzar86aF6AEqo5xcGQG/D8uyuHzlxnfdyc5v3VwPcC"
                    + "Y1rLRu2qDOmyaiIHoJmYJJtlS+EpFgZURC1wsYuo7P53P+fPRwfdeWLbeqgLaW91T3CaYNV/8tUQzIyLVBHBxkDMyiwVuxE"
                    + "HUBHwIOw+qVZG/39nQVkHABls21v3UyVpBwnUy0XiImWQTMWXIFg4fPSux8cwn+mhpSl0YXAVrj6jNjjsHbOfyhMCuW1LNU"
                    + "riXeKDKllZjRNCayM7yzPMrrS8OkLv6yENDeGlfHJgucvJcnFAoRDtaxIiLR0RQk7LO5l8lx7PcpNq+M0vVGI+cv/LwQsLk"
                    + "trv6R0bj56D86VkcY+Qf+0mws2+bjVUG0gs7315/wdUsT29bGOJdeDEg0q5pR4sDQGH3rGjhyd47peQHHceh6VWIiq3F7qs"
                    + "jgJ2/zWmMDZxcA0sOK0tKszhaLnLo+jjpR4Lenc95ngYNgWZ46e9ZEOLB1LXK9TOrST8meHdUqpBWlvU21TItsNsvZOxlOj"
                    + "+WYnjWxbYew5KN7TZTP17/CspdjSAGJ80MjyZ5KGY+dSCkfdiqqKxvbtjEMg+kZjQdPdWwHVr0kE4vKyLJMwO9HFH0cP51K"
                    + "7u7tLuvg0A9H3/9s16ejrgkqUva07jgIokDZYGJVpa4nDh4+suObL/cOeYB4IrF63xd7ByORSF3Vad5J2ZMLnClAbiY/u7/"
                    + "vqz2Tk4/veNf6+/tr8vm8f7GDXxTrum4ODAzM/w8TKTogggbXQgAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                    + "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC6UlEQVQ4T12TXUgUURTH//s57rqj7Yq5VKaWgpbRl7p+rl+"
                    + "ZWihi6VP1qqREJvTsey9FBIIS9CBEZbVpqJkTihgKpSSlsYWa2ppu6u7sjrs6M9ud2davGc69d+7c8+Occ/9HAfLY7XZqZm"
                    + "Hpdhili1QqyQYZpGn/43Kz38sv5D/Zva+QPt72DVrTz58a5HkBCoWCGEBGeU2m4BwIEK4CI2PjjyrKihpDEBnQ2d1bmJORx"
                    + "mwRgFIGhCzoHDKtRgVBEDA0PHavqqL0ruS7Dci1pBMATwBK2cHh4eH0B48YKSCBDF6vB5zXC7VaHfg5t5hWVmz9vA3Iy8xg"
                    + "pBR4ERj57YdPS4MO0yFAXhoceB+H02Y9wrUqggygf2i4oqbycvc2wCoBBBEf571wCDoYwmmYIiiYKQFafgPrHg5za35UnYy"
                    + "CRq1Gd//ADsBGapCdZWHWuE20T6xDozcgLioC0XQYLDFKLLN+uFkWs043zsWacDzagO537/dGkE8AU388eDbtgl6vh0EXjj"
                    + "iSd0k8iUYlYtqxjo4vyyhIMKHsRAy6+j4QQOlOCgXZFuarg8WnX39RkmRE3yIww4oQRBFXEnVgPV7cH11CU048ylPNeNPbv"
                    + "zeCgpxMhuX8aO6aQuPZSLR+28TqFikXuf+yoxQJn8XEsg/ttWdwLCYStt5dEUg1yCOADZ8Pz0ftYGY9GF/ZlItFFAQFuXtJ"
                    + "oTUpRjRfTAUdQcPWw+xNoTA3ixHINTqdTrycdODF1DpWN3iIYgAGSoXqFBNupB3B4UNmUFoKr3v6dlLo6LQVXiouZCTZiCR"
                    + "njuOw6mbxY8UL4o/EgzTMJqILmoZWoyHRqPD0la3iWk11sIgP2x4XXa+9OiA1QUjKstZJ/gqif6XUXJL9V6nUEw9a2yrvNN"
                    + "R3yQCr1ZpUV9/QfsBkDN/uNPlPsCf3d6bb7d5obrp1c2FhYVI+1tLSona5XJqgcyxiY3c37N71/Py8vJGcnMzX1dVt/QNcN"
                    + "Toga4nOWgAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                    + "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC6UlEQVQ4T12TXUgUURTH//s57rqj7Yq5VKaWgpbRl7p+rl+"
                    + "ZWihi6VP1qqREJvTsey9FBIIS9CBEZbVpqJkTihgKpSSlsYWa2ppu6u7sjrs6M9ud2davGc69d+7c8+Occ/9HAfLY7XZqZm"
                    + "Hpdhili1QqyQYZpGn/43Kz38sv5D/Zva+QPt72DVrTz58a5HkBCoWCGEBGeU2m4BwIEK4CI2PjjyrKihpDEBnQ2d1bmJORx"
                    + "mwRgFIGhCzoHDKtRgVBEDA0PHavqqL0ruS7Dci1pBMATwBK2cHh4eH0B48YKSCBDF6vB5zXC7VaHfg5t5hWVmz9vA3Iy8xg"
                    + "pBR4ERj57YdPS4MO0yFAXhoceB+H02Y9wrUqggygf2i4oqbycvc2wCoBBBEf571wCDoYwmmYIiiYKQFafgPrHg5za35UnYy"
                    + "CRq1Gd//ADsBGapCdZWHWuE20T6xDozcgLioC0XQYLDFKLLN+uFkWs043zsWacDzagO537/dGkE8AU388eDbtgl6vh0EXjj"
                    + "iSd0k8iUYlYtqxjo4vyyhIMKHsRAy6+j4QQOlOCgXZFuarg8WnX39RkmRE3yIww4oQRBFXEnVgPV7cH11CU048ylPNeNPbv"
                    + "zeCgpxMhuX8aO6aQuPZSLR+28TqFikXuf+yoxQJn8XEsg/ttWdwLCYStt5dEUg1yCOADZ8Pz0ftYGY9GF/ZlItFFAQFuXtJ"
                    + "oTUpRjRfTAUdQcPWw+xNoTA3ixHINTqdTrycdODF1DpWN3iIYgAGSoXqFBNupB3B4UNmUFoKr3v6dlLo6LQVXiouZCTZiCR"
                    + "njuOw6mbxY8UL4o/EgzTMJqILmoZWoyHRqPD0la3iWk11sIgP2x4XXa+9OiA1QUjKstZJ/gqif6XUXJL9V6nUEw9a2yrvNN"
                    + "R3yQCr1ZpUV9/QfsBkDN/uNPlPsCf3d6bb7d5obrp1c2FhYVI+1tLSona5XJqgcyxiY3c37N71/Py8vJGcnMzX1dVt/QNcN"
                    + "Toga4nOWgAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                    + "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAKeSURBVDhP"
                    + "jVJNTxNRFD3zTSm1UyKlCsEIRCC6EVFijC5MFE1kqTv1B+jChXsSE6Nx48qYsDPRxI0rjR8JGDFGhRBRdiAhliKlLVRmhmk7"
                    + "nS/vey0IO8/Me2/m3nfuue++K4Dwen5ek5dXb6maFhdFEfSCO3YiAIqmNTc8dPZJ3cLB9716N37mxLGjE57nQxAEGszBVv5R"
                    + "W8OQAgv4PDX9aPjCuZucTSAtYLNcloIggE+DrUEQIiBCEAbEC/lgUWVZwqnBgRsvXr59wNkEHkCmZ1uZrwKymx5m1zz8KHhI"
                    + "Gy63bRgmcrk8jvR03X4zNta/HYCBbRAFER6ddSJTwawdgSU3w5R1FH0Vk8s2JC2KVGofWpNJYcN09jPevwBsUJDplRLWiSCI"
                    + "KtQGGQfiIlJRCU2ahPeLJiRJhKZpNRJhOwCxYVV9fM1WsLLpwvE8eK6PtqiIUKwdEUEVSxsO37uFXRnkrCrKrktEBzmjBMu2"
                    + "IQQeuvYIiIgBvq2WsLBWqgWrY0cN2E2F6Iy4uNotohFlTC0VcedDBpPpIpaLFmZ+G/w6d/B3H6FD1/ApbaCw/gdTiznMrazh"
                    + "Z3Yd39MFjM/l4Fcd9LZEeLZbkLdmZlTEEKdTEh5+WcVMoQpFZu4QjzM+787LfQm0NEpcjFqCo5YBFYwZVUXFUG8rBtsasZcK"
                    + "7VcrcCsVqHT+K306rg+0QWvQSIzkfM6sZVCpeNwok2IymcS1k024dNjCQsGmbgS6kzGkmmOIxWIkopCWAMeh2yDwDIrFPC+s"
                    + "QL2uKDJ0PY7Ojnac7z+Ei8d70HOwHYlEgmcoULOxvfl8lpeCZ/Ds+dNMQo9/bNYTUfbPwd18qs91kKRhGuX7d+/9Yr/cNzIy"
                    + "IpumqbDv/4Vt297o6Kj7F1Q7+m7gqVhgAAAAAElFTkSuQmCC")
    public void drawImage() throws Exception {
        drawImage("html.png",
                "canvas.width = img.width; canvas.height = img.height;\n",
                "context.drawImage(img, 0, 0, canvas.width, canvas.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"rendering...", "...done"})
    public void drawImage_noImage() throws Exception {
        final String html = "<html><body>\n"
            + "<img id='myImage'>\n"
            + "<canvas id='myCanvas'></canvas>\n"
            + LOG_TEXTAREA
            + "<script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "try {\n"
            + "  var img = document.getElementById('myImage');\n"
            + "  var canvas = document.getElementById('myCanvas');\n"
            + "  var context = canvas.getContext('2d');\n"
            + "  log('rendering...');\n"
            + "  context.drawImage(img, 0, 0, 10, 10);\n"
            + "  log('...done');\n"
            + "} catch (e) { log('exception'); }\n"
            + "</script></body></html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"rendering...", "...done"})
    public void drawImage_invalidImage() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("invalid.png")) {
            final byte[] directBytes = IOUtils.toByteArray(is);
            final List<NameValuePair> emptyList = Collections.emptyList();
            getMockWebConnection().setResponse(URL_SECOND, directBytes, 200, "ok", MimeType.IMAGE_PNG, emptyList);
            getMockWebConnection().setDefaultResponse("Test");
        }

        final String html = "<html><body>\n"
            + "<img id='myImage'>\n"
            + "<canvas id='myCanvas'></canvas>\n"
            + LOG_TEXTAREA
            + "<script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "try {\n"
            + "  var img = document.getElementById('myImage');\n"
            + "  var canvas = document.getElementById('myCanvas');\n"
            + "  var context = canvas.getContext('2d');\n"
            + "  log('rendering...');\n"
            + "  context.drawImage(img, 0, 0, 10, 10);\n"
            + "  log('...done');\n"
            + "} catch (e) { log('exception'); }\n"
            + "</script></body></html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IArs4c6QAAAA1JREFUGFdj+M/A8B8ABQAB/6Zcm10A"
                + "AAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIW2P4z8DwHwAFAAH/F1FwBgAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIW2P4z8DwHwAFAAH/F1FwBgAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAANSURBVBhXY"
                + "/jPwPAfAAUAAf+mXJtdAAAAAElFTkSuQmCC")
    // The output depends on the deflation algorithm
    // check the output of: $pngcheck -v file.png
    // chrome gives: zlib: deflated, 256-byte window, fast compression
    // java gives:   zlib: deflated, 32K window, maximum compression
    // https://bugs.openjdk.java.net/browse/JDK-8056093
    public void drawImage_1x1_32bits() throws Exception {
        drawImage("1x1red_32_bit_depth.png",
                "canvas.width = img.width; canvas.height = img.height;\n",
                "context.drawImage(img, 0, 0, canvas.width, canvas.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IArs4c6QAAAA1JREFUGFdj+M/A8B8ABQAB/6Zcm10A"
                + "AAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIW2P4z8DwHwAFAAH/F1FwBgAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIW2P4z8DwHwAFAAH/F1FwBgAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAANSURBVBhXY"
                + "/jPwPAfAAUAAf+mXJtdAAAAAElFTkSuQmCC")
    public void drawImage_1x1_24bits() throws Exception {
        drawImage("1x1red_24_bit_depth.png",
                "canvas.width = img.width; canvas.height = img.height;\n",
                "context.drawImage(img, 0, 0, canvas.width, canvas.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAABdJREFUGFdj/M/A8J+RgYGRAQrgDOI"
                + "FAIPmAgWeg1g2AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAF0lEQVQIW2P8z8Dwn5GBAYggAM4gXgAAg+YCBUgKw5EAAAAA"
                + "SUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAF0lEQVQIW2P8z8Dwn5GBAYggAM4gXgAAg+YCBUgKw5EAAAAA"
                + "SUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAQSURBVBh"
                + "XY/jPAEIUAAYGAILBAf8lQkk6AAAAAElFTkSuQmCC")
    public void drawImage3Arguments() throws Exception {
        drawImage("1x1red_32_bit_depth.png",
                "canvas.width = 4; canvas.height = 4;\n",
                "context.drawImage(img, 0, 0);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAABpJREFUGFdjZEADjFgF/jMw/GdkYABL"
                + "YqgAADfmAgXboMAzAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAGUlEQVQIW2NkQAOMWAX+MzD8B8qAJTFUAAA35gIFxQ+VugAA"
                + "AABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAGUlEQVQIW2NkQAOMWAX+MzD8B8qAJTFUAAA35gIFxQ+VugAA"
                + "AABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAARSURBVBhX"
                + "YyAO/AcjrICBAQA3DQH/+OkmUAAAAABJRU5ErkJggg==")
    public void drawImage3ArgumentsPlacement() throws Exception {
        drawImage("1x1red_32_bit_depth.png",
                "canvas.width = 4; canvas.height = 4;\n",
                "context.drawImage(img, 1, 2);\n");
    }


    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAAC1JREFUGFc1yNENACAQwlAYxUllU0ep4aL9"
                + "ap6RMEv4yJKdhJ2oDXQABtqHt76KMBEFpp4gUgAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAL0lEQVQIWzXI0REAEBAD0Y1OVCqd6kQ4w/vaWQXS0lmaCCTb"
                + "GTbljoocd5Q/XmoDijQRBTzmAeYAAAAASUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAL0lEQVQIWzXI0REAEBAD0Y1OVCqd6kQ4w/vaWQXS0lmaCCTb"
                + "GTbljoocd5Q/XmoDijQRBTzmAeYAAAAASUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAqSURBVBhXY/jP"
                + "AET/Ff+DaRBoaGgAc+ACIPAfCkBsJrAIEDAyMgJJBgYAY/AUlK91orEAAAAASUVORK5CYII=")
    public void drawImage3ArgumentsPlacementNegative() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 4; canvas.height = 4;\n",
                "context.drawImage(img, -1, -2);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAAXNSR0IArs4c6QAAACpJREFUGFctyLERACAMxDD/KDTMGbbk0jC"
                + "KKUClwlQ7RJANwaE5b1YV4buKVA5Xbm45TAAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAK0lEQVQIWy3IsRUAEBQEwd1SJOr8uvQkOnECJhzpSZYYCBM0"
                + "Lcf9ZlQh3wWKWA5Xk6MBwQAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAK0lEQVQIWy3IsRUAEBQEwd1SJOr8uvQkOnECJhzpSZYYCBM0"
                + "Lcf9ZlQh3wWKWA5Xk6MBwQAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAlSURBVBhXY2B"
                + "Q+///PwPDf0YQwXCDAQj+KyJEGhsaQEIgwMAAAI7uDpdlPpgpAAAAAElFTkSuQmCC")
    public void drawImage3ArgumentsImageTooLarge() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 2; canvas.height = 5;\n",
                "context.drawImage(img, 0, 0);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAABdJREFUGFdj/M/A8J+RgYGRAQrgDOI"
                + "FAIPmAgWeg1g2AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAF0lEQVQIW2P8z8Dwn5GBAYggAM4gXgAAg+YCBUgKw5EAAAAA"
                + "SUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAF0lEQVQIW2P8z8Dwn5GBAYggAM4gXgAAg+YCBUgKw5EAAAAA"
                + "SUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAQSURBVBh"
                + "XY/jPAEIUAAYGAILBAf8lQkk6AAAAAElFTkSuQmCC")
    public void drawImage5Arguments() throws Exception {
        drawImage("1x1red_32_bit_depth.png",
                "canvas.width = 4; canvas.height = 4;\n",
                "context.drawImage(img, 0, 0, img.width, img.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAABpJREFUGFdjZEADjFgF/jMw/GdkYABL"
                + "YqgAADfmAgXboMAzAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAGUlEQVQIW2NkQAOMWAX+MzD8B8qAJTFUAAA35gIFxQ+VugAA"
                + "AABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAGUlEQVQIW2NkQAOMWAX+MzD8B8qAJTFUAAA35gIFxQ+VugAA"
                + "AABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAARSURBVBhX"
                + "YyAO/AcjrICBAQA3DQH/+OkmUAAAAABJRU5ErkJggg==")
    public void drawImage5ArgumentsPlacement() throws Exception {
        drawImage("1x1red_32_bit_depth.png",
                "canvas.width = 4; canvas.height = 4;\n",
                "context.drawImage(img, 1, 2, img.width, img.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAACVJREFUGFdj/H+D4T+jBgMjAxQw/mdg+M/I"
                + "gFfg////jIyMcC0AnzULBWgjtygAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAJElEQVQIW2P8f4PhP6MGAyMDFDD+ZwAKMOAV+P//PyMQwLQAAJ81"
                + "CwUd8tccAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAJElEQVQIW2P8f4PhP6MGAyMDFDD+ZwAKMOAV+P//PyMQwLQAAJ81"
                + "CwUd8tccAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAfSURBVBhXY/h"
                + "/g+E/AzIA8lAEmKA0HDAx/EdWwMAAADhoBtfglrXyAAAAAElFTkSuQmCC")
    public void drawImage5ArgumentsPlacementNegative() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 4; canvas.height = 4;\n",
                "context.drawImage(img, -3, -1, img.width, img.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAABZJREFUGFdj/M/A8J8BCTAic0BswgIAg"
                + "2sCAzQ3b+cAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAFklEQVQIW2P8z8AARAjAiMwBsQkLAACDawID+0h1rgAAAABJ"
                + "RU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAFklEQVQIW2P8z8AARAjAiMwBsQkLAACDawID+0h1rgAAAABJ"
                + "RU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAQAAAAECAYAAACp8Z5+AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAARSURBVBhXY/z"
                + "PwABEFAAGBgCDBQIA1ym4lAAAAABJRU5ErkJggg==")
    public void drawImageStretch() throws Exception {
        drawImage("1x1red_32_bit_depth.png",
                "canvas.width = 4; canvas.height = 4;\n",
                "context.drawImage(img, 0, 0, img.width, img.height, 0, 0, canvas.width, img.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAAXNSR0IArs4c6QAAACpJREFUGFctyLERACAMxDD/KDTMGbbk0jC"
                + "KKUClwlQ7RJANwaE5b1YV4buKVA5Xbm45TAAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAK0lEQVQIWy3IsRUAEBQEwd1SJOr8uvQkOnECJhzpSZYYCBM0Lcf"
                + "9ZlQh3wWKWA5Xk6MBwQAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAK0lEQVQIWy3IsRUAEBQEwd1SJOr8uvQkOnECJhzpSZYYCBM0Lcf"
                + "9ZlQh3wWKWA5Xk6MBwQAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAlSURBVBhXY2B"
                + "Q+///PwPDf0YQwXCDAQj+KyJEGhsaQEIgwMAAAI7uDpdlPpgpAAAAAElFTkSuQmCC")
    public void drawImage5ArgumentsImageTooLarge() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 2; canvas.height = 5;\n",
                "context.drawImage(img, 0, 0, 4, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAJFJREFUKFONkMENAjEMBNclIBoIH0QF"
                + "9z0XRA+Re6CgpZM0gO538DM4CaeDV1aRrI0mazuCQUnlzu66Evlh1dox1xpen4QAUkGPMwG49vhbr+EnQC4dhJ9c+UK2pSXm"
                + "Q0u0BXH/k1hSwl21AjOJVMo2/Qbu93H3oGPI9urTQUL/S39BkmpmINnYwd+JOcc0DL4B84cwC7JlTrYAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAkUlEQVQYV62PwQ0CMQwE7RIQDYQPooL7nguih8g9UJDpJA2c"
                + "+AE/s3ai04GExAMrkbPWeLVh+rE4uaO73I3qoil1X7OHlocRIE7Q40x4nIf9ZfTQmPNpgOwHn+1JVW/dse66I7Rg/ubYSqGr"
                + "SAKzGZXW1vQruP2PI26Y4fYtFEdtocw7QDMTVSX0zn6C3/T/wRfzizALwpK/7gAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAkUlEQVQYV62PwQ0CMQwE7RIQDYQPooL7nguih8g9UJDpJA2c"
                + "+AE/s3ai04GExAMrkbPWeLVh+rE4uaO73I3qoil1X7OHlocRIE7Q40x4nIf9ZfTQmPNpgOwHn+1JVW/dse66I7Rg/ubYSqGr"
                + "SAKzGZXW1vQruP2PI26Y4fYtFEdtocw7QDMTVSX0zn6C3/T/wRfzizALwpK/7gAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACCSURBVChTvY7"
                + "BDcIwDEV/OLUjIHFIL6gTcMXZh3HYx2GTTBL327SoUrlw4SmO/3esr+A3rmZyUdNRolxv3gAeIPkV5kbxcEeea3fPeZrfe1ybTP"
                + "RsKkOU680fElvOeIm4xb1W5NZCO1xaE3cYv9t7V/YPPj/F6xcqU0spSOkY9leABXrPQu6AWg+SAAAAAElFTkSuQmCC")
    public void drawImage5ArgumentsStretchX() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 10; canvas.height = 10;\n",
                "context.drawImage(img, 1, 1, 8, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAFFJREFUKFNjZCASMILUNQg3/K+PbGBoXN7"
                + "AAKIZpzCAxZEBRGGDwH/7AwYMBx0uMNQ3fACpwq4QpvP/////GxsbGRoaGvArxOdcDJ24FA8FhQDPFBULZWbihAAAAABJRU5Erk"
                + "Jggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAATklEQVQYV2NkIBIwgtQ1CDf8r49sYGhc3sAAohmnMIDFkQFE"
                + "YYPAf/sDBgwHHS4w1Dd8AKnCrhCm8z8QNDY2AjU24FeIz7kYOnEpHgoKAc8UFQug8KnwAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAATklEQVQYV2NkIBIwgtQ1CDf8r49sYGhc3sAAohmnMIDFkQFE"
                + "YYPAf/sDBgwHHS4w1Dd8AKnCrhCm8z8QNDY2AjU24FeIz7kYOnEpHgoKAc8UFQug8KnwAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAA1SURBVChTYyA"
                + "JNAg3/P+fw/AfRkOFUQATmMydwHDgigOYZpgCFsEP/gNBQ0MDVhNHImBgAADHvxPYgDOEzgAAAABJRU5ErkJggg==")
    public void drawImage5ArgumentsShrinkY() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 10; canvas.height = 10;\n",
                "context.drawImage(img, 1, 1, 4, 3);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAAXNSR0IArs4c6QAAADhJREFUGFcBLQDS/wEAJv//QPbAAAFAHL/"
                + "/MAfQAAG/CUD/ECfwAAG/QAj/EBj+AAFAvxn/MN76AI+DEbp3TIWcAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAANElEQVQIW2NkUPv/3+HbAQZGB5n9/w3YLzAw7ud0+C+g/gHI"
                + "cOD4LyDxDyi1X/K/wb1fDACPgxG6JQ+97QAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAANElEQVQIW2NkUPv/3+HbAQZGB5n9/w3YLzAw7ud0+C+g/gHI"
                + "cOD4LyDxDyi1X/K/wb1fDACPgxG6JQ+97QAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAIAAAAFCAYAAABvsz2cAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAA0SURBVBhXY2R"
                + "Q+//f4dsBBiYQYcB+gYFxP6fDfwH1DwwsDOYnGBgk/jEwOuyX/G9w7xcDAHxREIP4we06AAAAAElFTkSuQmCC")
    public void drawImage5ArgumentsStretchImageTooLarge() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 2; canvas.height = 5;\n",
                "context.drawImage(img, 0, 0, 8, 12);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAFpJREFUKFO10EEKgDAMRNE/R+mm9JbGW4ob"
                + "jzJiUFetdGM2A+GRwIjJ0aTjD1ht70LVZNL/IoPZgAZXqg0gLrYO5ELm18U1giUiCxjCpx7bTih1m3iXF7xNF57OsB8LLeYw9AAA"
                + "AABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAX0lEQVQYV7WQQQqAMBADE3/SS+kvrb8UL/7EmIp62kovBkJg"
                + "GXbDEoPiIIc/wCxpI5iFKxFfoQBhddNiO1k6IJV0cMekhJafG5daMdtNXfB5j5u6hUEretk7bODNhOAJzrgfCzJhsNQAAAAA"
                + "SUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAX0lEQVQYV7WQQQqAMBADE3/SS+kvrb8UL/7EmIp62kovBkJg"
                + "GXbDEoPiIIc/wCxpI5iFKxFfoQBhddNiO1k6IJV0cMekhJafG5daMdtNXfB5j5u6hUEretk7bODNhOAJzrgfCzJhsNQAAAAA"
                + "SUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABHSURBVChTYxgS"
                + "QO3///8MDP/hNC4Akvx/A4ihNFQYC/ivCDEJRuMCIMmGhgaIifgUwgDQhWAA5WIAJigNBoyMjFAWOmBgAAAEXSqdGQ47LgAAAABJ"
                + "RU5ErkJggg==")
    public void drawImage5ArgumentsNegativeWidth() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 10; canvas.height = 10;\n",
                "context.drawImage(img, 4, 4, -4, 6);\n");
    }


    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAFlJREFUKFPFzkEKgDAMRNE/R+mm9JbGW4ob"
                + "jzJiUChixZ0hZDaPJOJe1fYqVE0mR5+jtwazAA2OVBtAXGxtyIXMt41zBFNEHhrC6w3bTig9/9jD3qT+Uj/CHbu+Hws0CMhTAAAA"
                + "AElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAX0lEQVQYV8WOQQqAMBADE3/SS+kvrb8UL/7EmJYKRap4M+w2"
                + "sAxNiLuipI1gFKqjTHt6VoCw+pK8dqYHkAo6uGNSQPHXH5ecMXtb5Dj6quGmbuFoa9ixB3um0l/0I3gCu8YfCw1yrNcAAAAA"
                + "SUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAX0lEQVQYV8WOQQqAMBADE3/SS+kvrb8UL/7EmJYKRap4M+w2"
                + "sAxNiLuipI1gFKqjTHt6VoCw+pK8dqYHkAo6uGNSQPHXH5ecMXtb5Dj6quGmbuFoa9ixB3um0l/0I3gCu8YfCw1yrNcAAAAA"
                + "SUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABGSURBVChTY8AA"
                + "av///2dg+A+ncQGQ5P8bQAylocJYwH9FiEkwGhcASTY0NEBMxKcQBoAuBAMol4EJSmMFjIyMUNZQAAwMAKgVKp2FLYz1AAAAAElF"
                + "TkSuQmCC")
    public void drawImage5ArgumentsNegativeHeight() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 10; canvas.height = 10;\n",
                "context.drawImage(img, 4, 6, 4, -6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAFxJREFUKFOl0EEKgDAMRNE/R+mm9JbGW4ob"
                + "jzJiRSjUSsFsZvMIk4jJ0aSjh9n2LpRNTW7TQYPZgAJXqgwgTrYO5ETNr41rBEtErT+Ez3G2XaH03rGFrfnxnsFjT80oHwvhRbeC"
                + "AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAY0lEQVQYV6WQXQqAMAyDE2+yl+EtnbcUX7yJMf7BYJsMLJQ89KNJ"
                + "S3QWOzmUYJS0EozCpbiZAhQgLJ6MbivHBkgF7dwwKODUz41zSpjcj2Xd+j3OSZ3C1q5qxhzMmR/vaTz2AM0wHwuiBt/PAAAAAElF"
                + "TkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAY0lEQVQYV6WQXQqAMAyDE2+yl+EtnbcUX7yJMf7BYJsMLJQ89KNJ"
                + "S3QWOzmUYJS0EozCpbiZAhQgLJ6MbivHBkgF7dwwKODUz41zSpjcj2Xd+j3OSZ3C1q5qxhzMmR/vaTz2AM0wHwuiBt/PAAAAAElF"
                + "TkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABGSURBVChTY6AD"
                + "UPv//z8Dw384jQuAJP/fAGIoDRXGAv4rQkyC0bgASLKhoQFiIj6FMAB0IRhAuQxMUBorYGRkhLKoDxgYAAFaKp1TOe14AAAAAElF"
                + "TkSuQmCC")
    public void drawImage9Arguments() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 10; canvas.height = 10;\n",
                "context.drawImage(img, 0, 0, img.width, img.height, 4, 2, img.width, img.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAIpJREFUKFNjPBXA/p+BgYFhWgQfiGJY"
                + "EPGaEcxAA4zrA0zACh8ovGAQ+PCBIXHBF+wKFyQkXGHl4OC2v3ZNVPrtW27Gq1dxKIyP1zaNiDBVkZDIYmNlNWXU0cGuEGTt"
                + "////HYBU/YMHDxwUFRUJKzxw4ICDo6MjboXYfInha2IUgdRgtQZrOBJrIgAUwCcL+6R7MwAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAiUlEQVQYV2M8FcD+nwEIpkXwgSiGBRGvGcEMNMC4IcAErPCB"
                + "wgsGgQ8fGBIWfMGucEFCwhVWDg5u+2vXRKXfvuVmvHoVh8L4eG3TiAhTFQmJLDZWVlNGHR3sCkHW/v//3wFI1T948MBBUVGR"
                + "sMIDBw44ODo64laIzZcYviZGEUgNVmuwhiOxJgIAFNAnC2GN/IoAAAAASUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAiUlEQVQYV2M8FcD+nwEIpkXwgSiGBRGvGcEMNMC4IcAErPCB"
                + "wgsGgQ8fGBIWfMGucEFCwhVWDg5u+2vXRKXfvuVmvHoVh8L4eG3TiAhTFQmJLDZWVlNGHR3sCkHW/v//3wFI1T948MBBUVGR"
                + "sMIDBw44ODo64laIzZcYviZGEUgNVmuwhiOxJgIAFNAnC2GN/IoAAAAASUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABzSURBVChTYzwV"
                + "wP6fAQimRfCBKIYFEa8ZwQw0wPSUQZcBhPVPsDPY7/gOFcYETB8EdK5+lTB5EHJW8WvCaXmoMBawIC5O+8r27Qk/zp8/9f/KFbAz"
                + "cIL///87/Pv3b//9+/dxKmSC0mDw4MEDKGsoAgYGAFZxKhyfkxO8AAAAAElFTkSuQmCC")
    public void drawImage9ArgumentsCrop() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 10; canvas.height = 10;\n",
                "context.drawImage(img, 1, 2, 2, 4, 0, 0, img.width, img.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAFxJREFUKFNjZCASMBKpjmHoKlT7/z/h0wK"
                + "G+reNDIq/H4D9gdUz2WJT/ptLnWTwUtrGILLuLW6F+x04/kuo/mVQMPzDwJn1H7dC0Vdy/0PW/GQomPCNQf3WZ7BCAChZHAtgVD"
                + "PiAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAX0lEQVQYV2NkIBIwEqmOYegqVP+fn/hhoVbduwYGxd8P0kEe"
                + "xuqZbPGpq8wlT4R6KW1jEFn3FqwGq8IDjpyrxFX+hCoY/mHgzPqPW6H4K7lVQWt+hhZM+MagfuszWCEAHxEcC87YTb4AAAAA"
                + "SUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAX0lEQVQYV2NkIBIwEqmOYegqVP+fn/hhoVbduwYGxd8P0kEe"
                + "xuqZbPGpq8wlT4R6KW1jEFn3FqwGq8IDjpyrxFX+hCoY/mHgzPqPW6H4K7lVQWt+hhZM+MagfuszWCEAHxEcC87YTb4AAAAA"
                + "SUVORK5CYII=",
            IE = {})
    @HtmlUnitNYI(IE = "data:image/png;base64,"
            + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAPklEQVR42mNgGAUwoPb/f4LE/P//GRj+41WXJTb1/yL92P//Iwko"
            + "PGDP8f9GCuv//4sJKGT4r/g/Yb4oitUApWwZ6B07eGEAAAAASUVORK5CYII=")
    public void drawImage9ArgumentsCropNegativStart() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 10; canvas.height = 10;\n",
                "context.drawImage(img, -1, -2, 3, 5, 4, 4, img.width, img.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAABdJREFUKFNjZCASMBKpjmFUId6QIjp4"
                + "AAppAAuXjCs4AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAF0lEQVQYV2NkIBIwEqmOYVQh3pAiOngACmkAC8i6MuwAAAAA"
                + "SUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAF0lEQVQYV2NkIBIwEqmOYVQh3pAiOngACmkAC8i6MuwAAAAA"
                + "SUVORK5CYII=",
            IE = {})
    @HtmlUnitNYI(IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAADklEQVR42mNgGAWDEwAAAZoAAQDqGN4AAAAASUVORK5CYII=")
    public void drawImage9ArgumentsCropNegativWidth() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 10; canvas.height = 10;\n",
                "context.drawImage(img, 0, 0, -3, 5, 4, 4, img.width, img.height);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAEhJREFUKFNjnK+Z8J+BgYEh8foCRhCNCzDO"
                + "r+aBKGz9gl/hAQeH/fYMDA6MBw7gVwgybf78+f8TExMJK8TnNpgcXlOQDRgKCgFraBALDri0wwAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAASElEQVQYV2Ocr5nwnwEIEq8vYATRuADjgmoesMKE1i/4FR5w"
                + "cNhvz8DgwHjgAH6FINPmz5//PzExkbBCfG6DyeE1BdmAoaAQAGtwEAvm0nZdAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAASElEQVQYV2Ocr5nwnwEIEq8vYATRuADjgmoesMKE1i/4FR5w"
                + "cNhvz8DgwHjgAH6FINPmz5//PzExkbBCfG6DyeE1BdmAoaAQAGtwEAvm0nZdAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAvSURBVChTY5iv"
                + "mfAfhBkIACaGoDUMYEwIHHBw2P/fwYGgiWAwf/584hSOMMDAAAB1Kw2mrjRWYgAAAABJRU5ErkJggg==")
    public void drawImage9ArgumentsStretch() throws Exception {
        drawImage("4x6.png",
                "canvas.width = 10; canvas.height = 10;\n",
                "context.drawImage(img, 0, 0, img.width, img.height, 0, 0, 2, 4);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAHBJREFUKFOd0LENglAUBdDzSmLNCO5g"
                + "4QTG1jWo7JjCFehchgSdQxcg5BsSKPQHQ3j1efcmN6y8WOlkMLGfnovgMQd9wcQJFUo0wW0JvrDDgGdwyGDiiPuU1qPDOXiP"
                + "+Le6xgUtrjPK4L8Fts+zlPoB1NMSC7MwsngAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAaklEQVQYV2NkIBIwEqmOAUPhfwYGVahmTqDkJZhBKAqBiryA"
                + "EnlALArEi4CSE3EpfAuU4Abiv0B8GajQAkMh0DQboOBKqGm/gfRFIPYBKn4HUoxudTVQLBSIzwFxCUwRhkJ8IUB+8OAyFQDU"
                + "0xILsSkm0QAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAaklEQVQYV2NkIBIwEqmOAUPhfwYGVahmTqDkJZhBKAqBiryA"
                + "EnlALArEi4CSE3EpfAuU4Abiv0B8GajQAkMh0DQboOBKqGm/gfRFIPYBKn4HUoxudTVQLBSIzwFxCUwRhkJ8IUB+8OAyFQDU"
                + "0xILsSkm0QAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABdSURBVChT"
                + "Y6A6YITScPCPgUEVyuRkYmC4BGWjAqAiLyDeAcRngTgfKowJgJJvgfgHEH8F4hNQYTAAmg4B/xkYbIDUDyCGif0DKhaCslEB"
                + "UKIaiC8A8TyciugNGBgAAdYaCnzestwAAAAASUVORK5CYII=")
    public void drawImageDataUrlPng() throws Exception {
        final String html = "<html><head>\n"
            + "<script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "  function test() {\n"
            + "    var img = document.getElementById('myImage');\n"
            + "    var canvas = document.createElement('canvas');\n"
            + "    canvas.width = img.width;\n"
            + "    canvas.height = img.height;\n"
            + "    if (canvas.getContext) {\n"
            + "      var context = canvas.getContext('2d');\n"
            + "      context.drawImage(img, 0, 0, canvas.width, canvas.height);\n"
            + "      log(canvas.toDataURL());\n"
            + "    }\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='test()'>\n"
            + "  <img id='myImage' src='data:image/png;base64,"
                        + "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAABGdBTUEAALGP"
                        + "C/xhBQAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9YGARc5KB0XV+IA"
                        + "AAAddEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIFRoZSBHSU1Q72QlbgAAAF1J"
                        + "REFUGNO9zL0NglAAxPEfdLTs4BZM4DIO4C7OwQg2JoQ9LE1exdlYvBBeZ7jq"
                        + "ch9//q1uH4TLzw4d6+ErXMMcXuHWxId3KOETnnXXV6MJpcq2MLaI97CER3N0"
                        + "vr4MkhoXe0rZigAAAABJRU5ErkJggg==' alt='red dot' />\n"
            + LOG_TEXTAREA
            + "</body></html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAL1JREFUOE9jZEAFjgwMDOZQoZMMDAz7"
                + "0eQJchmhKnhFGBi2CDAw6AcwMHCDxDYwMHz9wMBw8Q0Dgw8DA8NngiZBFYANFGFgOJjAwGDVzcDAgqyxlIHhzwIGhmNvGBjs"
                + "STHQUYWBYf1tBgZ+bJpUGRg+3mFgCCTW+yAXVpQwMDSjuw5mOMiVPQwMtQwMDB3EuJImBlLdy1SPFFDQUDfZIAU21RI2MRFI"
                + "lBpYTiFKMTGKRg0kJpTwqxkNQ8rDEABatjIVyjXhJwAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA4klEQVQ4T+2STQ4BQRCFPyT+V27AESQyC5cwQqzY2zuGvT0b"
                + "QnAJCyGO4AhiIRgJUU1P4mdkesJSbzpV/frlvaoX4vlYUuZ1ayX3/OXdtwxpRCIDvR2UauCoZh9iSZhuoS7lwZdJA26EQjbM"
                + "QXkB4cePBbisYbKBShBCKwozByJen2JwPkHR1L5S2BRP7S6Iw/fTgH0PUkEUmhC2hLBjQqoU/tyyWsooC/bSeyljWUrVRJ3C"
                + "uLGJ69jYKjbqYSCxSd83rGJzDEro4n8WbFMBvjjXsi/QFPAnNJ3UZ9x/ht/P8AqUpzEVnisiKgAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA6ElEQVQ4T+2SzQoBURiGH5F/NyIrKQv3YOOnJpStnetwA1aU"
                + "klixcgcWJLdgx05ZYTLlO2OmmGiOWDqb03e+9zy930+A55OXMOs8beReevK+YcBRxGLQu0KlBMcQhCcQT8HsAA3RnHxJjsAG"
                + "CmyUhuoago8fc2Bt79DyJ8B8GBYXD8wFRARqQkG3fOWwVYPOEJKvXDTBHIBw9Y4usC24rg5SAX9esj2UDBgrjwU1lD3Md1DU"
                + "cac07tpEBdq3ZNKGQFRiDKEETGXCdQnPnwJd/c8WW9eAr84t2VeoK/gDdTv1Xvfv4fc9vAGT7zEVBadocQAAAABJRU5ErkJg"
                + "gg==",
            IE = {})
    @HtmlUnitNYI(IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAzUlEQVR42u2SvwpBYRyGn8Eiyqos/kxGVrkDo8VkJYNcgUtwD"
                + "crA7M+CLMoFGJQTirIapAgd78mmQ32nbOetZ/p+Pf2+7/3gX9lBvAGLHJxjcMvANgpVT7I9pPJwL4A9EZLbA5GFaxB6xsIKrI"
                + "oS2B/cRRKOGikbCdPwWLoIHbqw0UjfdElXmcMWLJ0fjGwq4WJ9EbZhqJGxkTABnRo8XYSn0FtWN71yWG3uSip8rW0d0RymERj"
                + "pbCYCXn5PWLR4v5mcqCeaXmV+/Pj5lRf9o2ZS6A1d/wAAAABJRU5ErkJggg==")
    public void drawImageDataUrlSvg() throws Exception {
        final String html = "<html><head>\n"
            + "<script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "  function test() {\n"
            + "    var img = document.getElementById('myImage');\n"
            + "    var canvas = document.createElement('canvas');\n"
            + "    canvas.width = 20;\n"
            + "    canvas.height = 20;\n"
            + "    if (canvas.getContext) {\n"
            + "      var context = canvas.getContext('2d');\n"
            + "      context.drawImage(img, 0, 0, img.width, img.height);\n"
            + "      log(canvas.toDataURL());\n"
            + "    }\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='test()'>\n"
            + "  <img id='myImage' src='data:image/svg+xml,"
                    + "<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\" "
                            + "overflow=\"hidden\" width=\"10\" height=\"10\">"
                    + "  <circle cx=\"5\" cy=\"5\" r=\"4\" stroke=\"black\" stroke-width=\"1\" fill=\"red\" />"
                    + "</svg>' />\n"
            + LOG_TEXTAREA
            + "</body></html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({"exception", "0", "true", "true"})
    public void measureText() throws Exception {
        final String html =
            "<html>\n"
            + "  <head>\n"
            + "    <script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "      function test() {\n"
            + "        var canvas = document.getElementById('myCanvas');\n"
            + "        if (canvas.getContext){\n"
            + "          ctx = canvas.getContext('2d');\n"
            + "          try {\n"
            + "            log(ctx.measureText());\n"
            + "          } catch(e) { log('exception'); }\n"

            + "          var metrics = ctx.measureText('');\n"
            + "          log(metrics.width);\n"

            + "          metrics = ctx.measureText('a');\n"
            + "          log(metrics.width > 5);\n"

            + "          metrics = ctx.measureText('abc');\n"
            + "          log(metrics.width > 10);\n"
            + "        }\n"
            + "      }\n"
            + "    </script>\n"
            + "  </head>\n"
            + "  <body onload='test()'>\n"
            + "    <canvas id='myCanvas'></canvas>\n"
            + LOG_TEXTAREA
            + "  </body>\n"
            + "</html>";

        loadPageVerifyTextArea2(html);
    }

    private void draw(final String canvasSetup, final String drawJS) throws Exception {
        final String html = "<html><head>\n"
            + "<script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "  function test() {\n"
            + "    var canvas = document.getElementById('myCanvas');\n"
            + "    if (canvas.getContext) {\n"
            + "      var context = canvas.getContext('2d');\n"
            + drawJS
            + "      log(canvas.toDataURL());\n"
            + "    }\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='test()'>\n"
            + canvasSetup
            + LOG_TEXTAREA
            + "</body></html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAACoAAAAqCAYAAADFw8lbAAAAAXNSR0IArs4c6QAAAVpJREFUWEft1L1KHVEUxfHffQTJC4iN"
                + "dgFbrQSNBNLEwuKa2qCVYCepQjpBSBGwlVhoYSlYWNqm1s4XSG2rW+bCOOj9gK02e6qZc2avs/b/rHN6nj6z+IyD1nC/eT/G"
                + "Mv7hf6eu+/kBW9jHd5zjpvkp5n5h7wWddm34uYva3gRGz7CLP69stG1pZ9DkJEZD4C8OcYpVrDSq2/iBT1jE9ZhEQ+ML1hud"
                + "du0VLnGB/nNGT/CxQ3oDbaJz+IboeL4xGRGJ8aWG+jhbH0Zn8BMLw2onIdo1Oo3I7WCBWGyQ8aMJiN4+ZD/oDa19C6NfW9Ri"
                + "o9qNxS68itG4DYLkb0yNSTTMRd1mE6fHzDUHcpTROLxrkeEu0RG3zvtNl9Fs9kW0iGYTyNarjBbRbALZepXRIppNIFuvMlpE"
                + "swlk61VGi2g2gWy9ymgRzSaQrVcZLaLZBLL1KqNFNJtAtt49X4NjZWyu/AkAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAACoAAAAqCAYAAADFw8lbAAABOUlEQVRYR+2UMW7CUBBE4R5UhGMALTlCSEWFBAeAJiUVB0iu"
                + "QMEJUgYppzAUXARmpHW0WHYSKyORYiyt/jf2DvPfjtzt3F5D3L6gHtPPq9hvsb6ilpWeptsnPBjH+z/p1mnk/k63hdHPmkN8"
                + "5/mvRrP2sI3ROTofUG+oj6C1CLUR1kPsp1h3qN8aPePdGWoS/WusnF7uL+qMln+YT8TmTJQiG9QgBMs9R/wc425rlHHro96T"
                + "7ld02hCtGs35K83lLGajNMB8V7NPmrxKLe6LexqlgQuK0TmFORriyHtqo3sIHlE5o/wC5HE3EaU3PsuxqstiE9F+dfRx2P+3"
                + "2Kh6JiZqomoCaj1n1ETVBNR6zqiJqgmo9ZxRE1UTUOs5oyaqJqDWc0ZNVE1AreeMmqiagFrPGTVRNQG13hX4a00ZAyWoGQAA"
                + "AABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAACoAAAAqCAYAAADFw8lbAAABOUlEQVRYR+2UMW7CUBBE4R5UhGMALTlCSEWFBAeAJiUVB0iu"
                + "QMEJUgYppzAUXARmpHW0WHYSKyORYiyt/jf2DvPfjtzt3F5D3L6gHtPPq9hvsb6ilpWeptsnPBjH+z/p1mnk/k63hdHPmkN8"
                + "5/mvRrP2sI3ROTofUG+oj6C1CLUR1kPsp1h3qN8aPePdGWoS/WusnF7uL+qMln+YT8TmTJQiG9QgBMs9R/wc425rlHHro96T"
                + "7ld02hCtGs35K83lLGajNMB8V7NPmrxKLe6LexqlgQuK0TmFORriyHtqo3sIHlE5o/wC5HE3EaU3PsuxqstiE9F+dfRx2P+3"
                + "2Kh6JiZqomoCaj1n1ETVBNR6zqiJqgmo9ZxRE1UTUOs5oyaqJqDWc0ZNVE1AreeMmqiagFrPGTVRNQG13hX4a00ZAyWoGQAA"
                + "AABJRU5ErkJggg==",
            IE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACoAAAAqCAYAAADFw8lbAAAAAXNSR0I"
                + "Ars4c6QAAAARnQU1BAACxjwv8YQUAAADvSURBVFhH7ZJBDsFAFIbLURrHwBJHwMqWA3AGDmBtxxGwsHE"
                + "NbUTcg/+v92QibdKRWPm/5PNPTeZ15nWSD9pw/xq+mZtkbVmHhyXrsW6Iz1Xh80NYrG3yJ4Kp5a9pWE4"
                + "sozbq3bxAnpTPPDnlqX3MubqwBjse1iFepw/PMC3bKCd9IV1CMrNsWfYgTz6CGxt3YBfGcIJeZ8w/jB0"
                + "8QNbMyzbKSS50F7CMo+Ud+piklk5uGZJZkpsl61QSe0e/4QrDTvGT8vpEEbtRdiL2JSvILvtV2sIBrA"
                + "O/RnFHiychhBBCCCGEEEIIIYQQ4l9Ikiccki+D/HzKtwAAAABJRU5ErkJggg==")
    public void fillText() throws Exception {
        draw("<canvas id='myCanvas' width='42' height='42'>\n",
                "context.fillText('HtmlUnit', 3, 7);\n");
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("[object CanvasGradient]")
    public void createLinearGradient() throws Exception {
        final String html =
            "<html><head><script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "function test() {\n"
            + "  var canvas = document.getElementById('myCanvas');\n"
            + "  var ctx = canvas.getContext('2d');\n"
            + "  var gradient = ctx.createLinearGradient(0, 0, 200, 0);\n"
            + "  log(gradient);\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "  <canvas id='myCanvas'></canvas>\n"
            + LOG_TEXTAREA
            + "</body>\n"
            + "</html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("[object CanvasGradient]")
    public void createRadialGradient() throws Exception {
        final String html =
            "<html><head><script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "function test() {\n"
            + "  var canvas = document.getElementById('myCanvas');\n"
            + "  var ctx = canvas.getContext('2d');\n"
            + "  var gradient = ctx.createRadialGradient(100, 100, 100, 100, 100, 0);\n"
            + "  log(gradient);\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "  <canvas id='myCanvas'></canvas>\n"
            + LOG_TEXTAREA
            + "</body>\n"
            + "</html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = {"1", "0.5", "0", "0.699999988079071", "0"},
            CHROME = {"1", "0.5", "0", "0.7", "0"},
            EDGE = {"1", "0.5", "0", "0.7", "0"})
    @NotYetImplemented({CHROME, EDGE})
    public void globalAlpha() throws Exception {
        final String html =
            "<html><head><script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "function test() {\n"
            + "  var canvas = document.getElementById('myCanvas');\n"
            + "  try {\n"
            + "    var ctx = canvas.getContext('2d');\n"
            + "    log(ctx.globalAlpha);\n"
            + "    ctx.globalAlpha = 0.5;\n"
            + "    log(ctx.globalAlpha);\n"
            + "    ctx.globalAlpha = 0;\n"
            + "    log(ctx.globalAlpha);\n"
            + "    ctx.globalAlpha = 0.7;\n"
            + "    log(ctx.globalAlpha);\n"
            + "    ctx.globalAlpha = null;\n"
            + "    log(ctx.globalAlpha);\n"
            + "  } catch(e) { log('exception'); }\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "  <canvas id='myCanvas'></canvas>\n"
            + LOG_TEXTAREA
            + "</body>\n"
            + "</html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({"0.5", "0.5", "0.5", "0.5"})
    public void globalAlphaInvalid() throws Exception {
        final String html =
            "<html><head><script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "function test() {\n"
            + "  var canvas = document.getElementById('myCanvas');\n"
            + "  try {\n"
            + "    var ctx = canvas.getContext('2d');\n"
            + "    ctx.globalAlpha = 0.5;\n"
            + "    log(ctx.globalAlpha);\n"
            + "    ctx.globalAlpha = -1;\n"
            + "    log(ctx.globalAlpha);\n"
            + "    ctx.globalAlpha = 'test';\n"
            + "    log(ctx.globalAlpha);\n"
            + "    ctx.globalAlpha = undefined;\n"
            + "    log(ctx.globalAlpha);\n"
            + "  } catch(e) { log('exception'); }\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "  <canvas id='myCanvas'></canvas>\n"
            + LOG_TEXTAREA
            + "</body>\n"
            + "</html>";

        loadPageVerifyTextArea2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAADxJREFUOE9jZKAyYISa10AlcxtoZmA9"
                + "AwMDyHByXQrTB3ch1Q2E20BmWGK4kGYGkulAuDbaxTKlLoPrBwDnlwwLU3fVIAAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAP0lEQVQoU2NkoDJghJrnAKRBmBJwAKj5AMzABiDHHogPkmki"
                + "TG8DsoEgs0AGkwNg+ka6gVSNFKonG3JiFqseAAklEgtbZz3HAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAP0lEQVQoU2NkoDJghJrnAKRBmBJwAKj5AMzABiDHHogPkmki"
                + "TG8DsoEgs0AGkwNg+ka6gVSNFKonG3JiFqseAAklEgtbZz3HAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABTSURBVDhP"
                + "Y6A2YITSDlBMCTgAwkwQNtgwewiTLADSi+KgBigmF8D1w1xINTD4DYTFMsj/oIA9COaRDmB6G5jBXAiAGU4OeAjEoGTzAMyj"
                + "HmBgAADuaAqOh9GTVgAAAABJRU5ErkJggg==")
    public void strokeRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='10' style='border: 1px solid red;'></canvas>\n",
                "context.strokeRect(2, 2, 16, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAChJREFUOE9jZKAyYKSyeQzIBv6n0HCw"
                + "WaMGUhSKtA9DipwH00z1dAgAPKcGC8XskzoAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAKElEQVQoU2NkoDJgpLJ5DMgG/qfQcLBZowZSFIq0D0OKnAfT"
                + "TPV0CAA8pwYL+jNAvAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAKElEQVQoU2NkoDJgpLJ5DMgG/qfQcLBZowZSFIq0D0OKnAfT"
                + "TPV0CAA8pwYL+jNAvAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAhSURBVDhP"
                + "Yxj0gBFKg8B/KE0uAJvFBGZSEYwaOPwBAwMAkWIBDOc7sxkAAAAASUVORK5CYII=")
    public void fillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='10' style='border: 1px solid red;'></canvas>\n",
                "context.fillRect(2, 2, 16, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAACxJREFUOE9jZICA/1CaUoqRcdTA0TAk"
                + "OQRGkw3JQYahYTQMR8OQjBCgfrIBABkEFBUHz6OrAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAALElEQVQ4T2NkgID/UJpSipFx1MDRMCQ5BEaTDclBhqFhNAxH"
                + "w5CMEKB+sgEAGQQUFQfPo6sAAAAASUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAALElEQVQ4T2NkgID/UJpSipFx1MDRMCQ5BEaTDclBhqFhNAxH"
                + "w5CMEKB+sgEAGQQUFQfPo6sAAAAASUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAArSURBVDhP"
                + "Y2SAgP9QmlLAyARlUA2MGkg5GDWQcjBqIOVg1EDKwWA3kIEBAND2AShZ31fQAAAAAElFTkSuQmCC")
    public void fillRectWidthHeight() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.fillRect(1, 0, 18, 20);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAADdJREFUOE9jZKAyYKSyeQzIBv6n0HCw"
                + "WaMGUhSKtA9DipwH00zTdDjqQvJCYDRSyAs3ZF2DPwwBbfkGFYXESWwAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAN0lEQVQ4T2NkoDJgpLJ5DMgG/qfQcLBZowZSFIq0D0OKnAfT"
                + "TNN0OOpC8kJgNFLICzdkXYM/DAFt+QYVhcRJbAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAN0lEQVQ4T2NkoDJgpLJ5DMgG/qfQcLBZowZSFIq0D0OKnAfT"
                + "TNN0OOpC8kJgNFLICzdkXYM/DAFt+QYVhcRJbAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAnSURBVDhP"
                + "Yxj0gBFKg8B/KE0uAJvFBGZSEYwaOApGwSgYBSDAwAAA4YcBDBgCJpwAAAAASUVORK5CYII=")
    public void fillRectRotate() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.fillRect(2, 2, 16, 6); context.rotate(.5);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAANRJREFUOE/d1CEPgUEYwPH/q/kCvoRA"
                + "kRQbCkUQTJFtNp2MpmmCTZIQRJIkapIoaT4A4bnt3c3dvffuCrfddtvz3O+e5267iMAjCuzxm2ATOKS9Cr3lEnAB9sBU1l62"
                + "Dm6BVkxYCnxPqsbBhqXVmcAvFxwHz0DZsuEp6NyGKrAHrFynS/wGTID1t3wFZoCRzGxC+ARU9Vz9UXKCDhOgNeDoAlU8L3DX"
                + "AG+Ajq1lU0EVgetaQgG4pgHVnjYwBorAAhiYKvD9HPofdAc8QoHOt/Kt8A/AN1+PHBVxaGY3AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA0ElEQVQ4T2NkoDJgpLJ5DEPTQFVgMNwmNyjQvWwKNOgUEG8E"
                + "4lYgPk2qwegGrgcaEIBkyBwguw2I7xNrMLKBXkBNW3FobIca/IWQwcgGHgEqtsaj4S3U0D58hsIMBHkT5F1iwE1o+C7GphjZ"
                + "hdVABSDMSYypQDULgDgRXS16pIgBFVQBcT4RhroA1ewlZCBMXgvq2igcBq8EikcQ8jI2eXuowa5okvpA/iVyDITpCYEGhSGQ"
                + "ngrEObiChNTCIQ1o0HIg/kwtAwnGFakuHAYGAgBd6xwVKif+GwAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA0ElEQVQ4T2NkoDJgpLJ5DEPTQFVgMNwmNyjQvWwKNOgUEG8E"
                + "4lYgPk2qwegGrgcaEIBkyBwguw2I7xNrMLKBXkBNW3FobIca/IWQwcgGHgEqtsaj4S3U0D58hsIMBHkT5F1iwE1o+C7GphjZ"
                + "hdVABSDMSYypQDULgDgRXS16pIgBFVQBcT4RhroA1ewlZCBMXgvq2igcBq8EikcQ8jI2eXuowa5okvpA/iVyDITpCYEGhSGQ"
                + "ngrEObiChNTCIQ1o0HIg/kwtAwnGFakuHAYGAgBd6xwVKif+GwAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACWSURBVDhP"
                + "7cytDoJQGIfxcwFY6QSLwUY20EwGK5ULoNrNBJqBxEaTbDabuQgyFZ4zDxtj78Z349l+O+9O+Ku9TTqad5Vc1MjNvbg39GDr"
                + "BQezuqI71vWEhUl9IY21SoQY1Q3SiKSAj8EeqCCNSBIMZiOCNNDnYXQnpJCGtAyzuuCD/uAZi7rjBz0W64+1CnD4n3vbpFQD"
                + "eftHtWY6PUwAAAAASUVORK5CYII=")
    public void rotateFillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.rotate(.5); context.fillRect(6, 2, 12, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAADdJREFUOE9jZKAyYKSyeQzIBv6n0HCw"
                + "WaMGUhSKtA9DipwH00zTdDjqQvJCYDRSyAs3ZF2DPwwBbfkGFYXESWwAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAN0lEQVQ4T2NkoDJgpLJ5DMgG/qfQcLBZowZSFIq0D0OKnAfT"
                + "TNN0OOpC8kJgNFLICzdkXYM/DAFt+QYVhcRJbAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAN0lEQVQ4T2NkoDJgpLJ5DMgG/qfQcLBZowZSFIq0D0OKnAfT"
                + "TNN0OOpC8kJgNFLICzdkXYM/DAFt+QYVhcRJbAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAnSURBVDhP"
                + "Yxj0gBFKg8B/KE0uAJvFBGZSEYwaOApGwSgYBSDAwAAA4YcBDBgCJpwAAAAASUVORK5CYII=")
    public void fillRectTranslate() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.fillRect(2, 2, 16, 6); context.translate(3, 4);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAADFJREFUOE9jZKAyYKSyeQyjBlIeorjC"
                + "8D+5Ro8aCA+5wR+G5EbyaNYjO+QQGkdg8QUAebwGD4ULeHoAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAMUlEQVQ4T2NkoDJgpLJ5DKMGUh6iuMLwP7lGjxoID7nBH4bk"
                + "RvJo1iM75BAaR2DxBQB5vAYPhQt4egAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAMUlEQVQ4T2NkoDJgpLJ5DKMGUh6iuMLwP7lGjxoID7nBH4bk"
                + "RvJo1iM75BAaR2DxBQB5vAYPhQt4egAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAkSURBVDhP"
                + "YxgFIwAwQml08B9KkwyYoDTVwKiBo2AUDAfAwAAAf3YBCwCfFioAAAAASUVORK5CYII=")
    public void translateFillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.translate(3, 4); context.fillRect(2, 2, 16, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAMxJREFUOE/t1LEJAjEUxvH/gYKVjStY"
                + "WYlOYOMGCnYOYeEKYu8INo4gOICgnaWCYCEIghvIgzx4hODlTBrBQLjj8vJ7+VJcQeZRZPb4g+k3+lt3OAIOwCUluI18AjrA"
                + "w8FH95Qm19gmCjaB14dNd4Nro1uoXsEBsIs9hasT0KbYSzoFZ8CyImjLz0BbPii4BiYJ4AYYW3AF9IA+UPsCngMLC6ohJxZU"
                + "cXmXWTaGwDYEhjbWvQbSrOsVtoBnLBhq0vCSTG3EsjiV1rP/HN50hx8VahodiQAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAxElEQVQ4T2NkoDJgpLJ5DKMGUh6iQysMtYAe/gHE9yjxOLKX"
                + "rwINAhn6GojPAfFZJPyQWEtgBvIBNXzEo+kF1HBki55gUw8z0BEouY9YV0DVgQxEtuAAkP8VZmAJkNNNooHIyu8COSogAZiB"
                + "y4DsSAoMXA3UG4ZsYDOQ4wrExkDMQobBFUA9ncgGwswAuRhkqBGUBrFBmBBwAyrYjc1AbBpZ0SwAWWaAplAYyH9HrIHYLOFA"
                + "8okCkF2M7EVC3iFJnuqFAwBzIx8VNVMmtgAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAxElEQVQ4T2NkoDJgpLJ5DKMGUh6iQysMtYAe/gHE9yjxOLKX"
                + "rwINAhn6GojPAfFZJPyQWEtgBvIBNXzEo+kF1HBki55gUw8z0BEouY9YV0DVgQxEtuAAkP8VZmAJkNNNooHIyu8COSogAZiB"
                + "y4DsSAoMXA3UG4ZsYDOQ4wrExkDMQobBFUA9ncgGwswAuRhkqBGUBrFBmBBwAyrYjc1AbBpZ0SwAWWaAplAYyH9HrIHYLOFA"
                + "8okCkF2M7EVC3iFJnuqFAwBzIx8VNVMmtgAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAC3SURBVDhP"
                + "7dIxCsIwFIDhqAgudvECzk6iXsChV/ECehkvI66CurnpIHQQBMXBTdD/vSYQNEKsi0J/+EiyvLSlpqzsB6rZVeqgibOeClax"
                + "q7SBDD1ijZVnj6jcwASXfBvsABnsX5ThJTdwiFm+jU4G+hfMcYU2wf0LW2hVu/bsWjR5Us0N3GGBm54+T145mHzTAUaYYonQ"
                + "Kz5Lofm/zbvqkE/St2TfhV8LJ9nEDAzVgBvexhh/kTEPbs42HzJH+t4AAAAASUVORK5CYII=")
    public void rotateTranslateFillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.rotate(0.2); context.translate(0, 4); context.fillRect(4, 4, 16, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAALZJREFUOE/tkjEKAjEQRd8ewXoLK23s"
                + "1kZ7j+IRbG3st/M6tlYq2CxYCl7CTgZ2liEMMm5WsDCQIiR5b+YnBQOPYmAef2B+ol/LcAzc8+uje+U1sAfOwMXM66cSbbkE"
                + "Hs7lZwu3orcSm+ERWAYqUol0oqJOYoFbYBcAekdqYCMbFjgHTj2BDTBLgbK+AdOeUAE23j8cAVUyIxJpuY5+bJVILCqbJJ0c"
                + "gFUU6KXgSRY5QDfq3we+AOdFHSeUoHcMAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAoUlEQVQ4T2NkoDJgpLJ5DKMGUh6iNAtDAaDbPlDuPgZ4LCcA"
                + "DZsPxBuA+AEQXwTiC1BMkj0wL4Nc+B6HTpIsQQ7D+0ADFYh0Dk5LkA1sABpWT6SB6MoWAAUSQYLIBuLzNiF7QBEqiG4giL8f"
                + "iB0I6cYhrwiKUFzpMAAoaQDE+lCamLAtBKqdQErCJmTJAaCBjqQYiM2n6JYYUmoghiWD30AAk4cbJe1EwXcAAAAASUVORK5C"
                + "YII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAoUlEQVQ4T2NkoDJgpLJ5DKMGUh6iNAtDAaDbPlDuPgZ4LCcA"
                + "DZsPxBuA+AEQXwTiC1BMkj0wL4Nc+B6HTpIsQQ7D+0ADFYh0Dk5LkA1sABpWT6SB6MoWAAUSQYLIBuLzNiF7QBEqiG4giL8f"
                + "iB0I6cYhrwiKUFzpMAAoaQDE+lCamLAtBKqdQErCJmTJAaCBjqQYiM2n6JYYUmoghiWD30AAk4cbJe1EwXcAAAAASUVORK5C"
                + "YII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACgSURBVDhP"
                + "YxgFo4B8IAClKQbMUDoCiM8DsQEQWwCxJBCDwAsoTTRghNIgF76HMDHABiB+AMQXgfgCFOMEMANB4D4QK0CYBAFRljQA8X8y"
                + "8XwgxgAgb2NTTAzGFVwM+4EYmwZiMDi4YLEMAwuBuBGIQWFzE4g/ADEHEBOTrB4C8QnkSCEEAoAYlKz0oTR6BB4AYkdSDMQG"
                + "0C0xBOKRBRgYAItyQgsDc+IZAAAAAElFTkSuQmCC")
    public void transformTranslateFillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.setTransform(1, .2, .3, 1, 0, 0); context.translate(-5, 4); context.fillRect(4, 4, 16, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAC1JREFUOE9jZKAyYKSyeQxwA88wMPyn"
                + "xHATBohZowaSH4q0D0Py3Yaqk+rpEAAqPQwLveqeQwAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAALUlEQVQoU2NkoDJgpLJ5DHADzzAw/KfEcBMGiFmjBpIfirQP"
                + "Q/LdhqqT6ukQACo9DAtpppU3AAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAALUlEQVQoU2NkoDJgpLJ5DHADzzAw/KfEcBMGiFmjBpIfirQP"
                + "Q/LdhqqT6ukQACo9DAtpppU3AAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAlSURBVDhP"
                + "Yxj0gBFKM5xhYPgPZZIFTKBmMYF5VASjBg5/wMAAAAOPAgybsqQOAAAAAElFTkSuQmCC")
    public void fillStyleNullFillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='10' style='border: 1px solid red;'></canvas>\n",
                "context.fillStyle = '#cc0000'; context.fillStyle = null; context.fillRect(2, 2, 16, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAC1JREFUOE9jZKAyYKSyeQxwA88wMPyn"
                + "xHATBohZowaSH4q0D0Py3Yaqk+rpEAAqPQwLveqeQwAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAALUlEQVQoU2NkoDJgpLJ5DHADzzAw/KfEcBMGiFmjBpIfirQP"
                + "Q/LdhqqT6ukQACo9DAtpppU3AAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAALUlEQVQoU2NkoDJgpLJ5DHADzzAw/KfEcBMGiFmjBpIfirQP"
                + "Q/LdhqqT6ukQACo9DAtpppU3AAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAlSURBVDhP"
                + "Yxj0gBFKM5xhYPgPZZIFTKBmMYF5VASjBg5/wMAAAAOPAgybsqQOAAAAAElFTkSuQmCC")
    public void fillStyleUndefinedFillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='10' style='border: 1px solid red;'></canvas>\n",
                "context.fillStyle = '#cc0000'; context.fillStyle = undefined; context.fillRect(2, 2, 16, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAC1JREFUOE9jZKAyYKSyeQxwA88wMPyn"
                + "xHATBohZowaSH4q0D0Py3Yaqk+rpEAAqPQwLveqeQwAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAALUlEQVQoU2NkoDJgpLJ5DHADzzAw/KfEcBMGiFmjBpIfirQP"
                + "Q/LdhqqT6ukQACo9DAtpppU3AAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAALUlEQVQoU2NkoDJgpLJ5DHADzzAw/KfEcBMGiFmjBpIfirQP"
                + "Q/LdhqqT6ukQACo9DAtpppU3AAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAlSURBVDhP"
                + "Yxj0gBFKM5xhYPgPZZIFTKBmMYF5VASjBg5/wMAAAAOPAgybsqQOAAAAAElFTkSuQmCC")
    public void fillStyleUnknownFillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='10' style='border: 1px solid red;'></canvas>\n",
                "context.fillStyle = '#cc0000'; context.fillStyle = 'pipi'; context.fillRect(2, 2, 16, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAC1JREFUOE9jZKAyYKSyeQzIBv6n0HCw"
                + "WXQxEFcwEPIBTheOIAMpjGSIdqqnQwA/UgoLxnfNlgAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAALUlEQVQoU2NkoDJgpLJ5DMgG/qfQcLBZdDEQVzAQ8gFOF44g"
                + "AymMZIh2qqdDAD9SCgsSO8biAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAALUlEQVQoU2NkoDJgpLJ5DMgG/qfQcLBZdDEQVzAQ8gFOF44g"
                + "AymMZIh2qqdDAD9SCgsSO8biAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAtSURBVDhP"
                + "Yxj0gBFKg8B/KE0uAJvFBGZSEWBzIbIYMiDkA9q4cPAbONgBAwMAI5ADC8QrQ2IAAAAASUVORK5CYII=")
    public void clearRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='10' style='border: 1px solid red;'></canvas>\n",
                "context.fillRect(2, 2, 16, 6); context.clearRect(4, 4, 6, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAF1JREFUOE9j/O/C8J+BioBx1ECKQ3Mk"
                + "hyHjHgZGWABSkpTgYYhsILaYIdYSog0k1hKUWCbkSnxpCuaDIWYgupdICQKsXiYm3+GzBGQoVbIezBKqGYjsM6q4kKYGAgB9"
                + "kkg955Wz+gAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAxUlEQVQ4T2P878Lwn4GKgHHUQIpDc+iE4QegXwUo9i/QAIiX"
                + "/zMsYNzLkPjfmSGAgZFBAcjXB9IGQHkQJgnAwvAD4x4GQWw6SbUEESksDIqMOxgeEOMcfJYgx3ID0JWNxBiIrua/K0MCMJjm"
                + "g8SRDcTpbUKW/HcARigLw3t0A0F8R6ArDxAyAEdY3wdFKK6EvQGo6QLQGxcZWBkuEBO2wNTSD9RTQEpOwWsJ0EAHoIH7STEQ"
                + "m09RLGH4w3CeUgMxLBn8BgIAZv1hLbrWWZAAAAAASUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAxUlEQVQ4T2P878Lwn4GKgHHUQIpDc+iE4QegXwUo9i/QAIiX"
                + "/zMsYNzLkPjfmSGAgZFBAcjXB9IGQHkQJgnAwvAD4x4GQWw6SbUEESksDIqMOxgeEOMcfJYgx3ID0JWNxBiIrua/K0MCMJjm"
                + "g8SRDcTpbUKW/HcARigLw3t0A0F8R6ArDxAyAEdY3wdFKK6EvQGo6QLQGxcZWBkuEBO2wNTSD9RTQEpOwWsJ0EAHoIH7STEQ"
                + "m09RLGH4w3CeUgMxLBn8BgIAZv1hLbrWWZAAAAAASUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAADcSURBVDhP"
                + "7ZTRDYJADIb/kviuG+gEMoIRfdcN3EDdwA1cwREYgMQ4gbiBI/CskVrOItEQ4XLHm19y4b9e0vZKr8QzMDwS6Ncbf4fudONQ"
                + "+iYzOw+UGcaUgJBjKc63sg7MSPXMCtPYRYZBgoHaPuApFhxgKHIsVwmJEL5O6qleyh0jOuFqdAM/g5gMixVhpyZreI5V6ef9"
                + "l5mwVmnPDbGqqm0I6EuWE91aIaXKpG6mXLXTRgwxMS4iUjyQtqltHmEvtdy0Hl9NQcztCEenefgdhHs4Ozmso5u37A/gCR//"
                + "XBF+op2TAAAAAElFTkSuQmCC")
    public void transformTranslateClearRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.fillStyle = '#ff4400'; context.fillRect(0, 0, 20, 20); "
                + "context.setTransform(1, .2, .3, 1, 0, 0); "
                + "context.translate(-5, 4); context.clearRect(4, 4, 16, 6);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAHVJREFUOE9jZKAyYEQyL5GBgWEXAwPD"
                + "U0rsgBmozMDA0MLAwODGwMBwF2owyPBDpBqO7EKYXjuowSDDQRaBDIZhgq7HZiCyo6SRDHdnYGC4Q8j1hAxE9zHM9SDDlaCG"
                + "10CDCayWVAOxuX4+siAlBmKNr8FvIAB+fhILDX78/QAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAcElEQVQoU2NkoDJgRDLPF8g+B8RPKbEDZqAI0JDJQOwKxPeA"
                + "eBcUHyLVcGQXwvTaARluUKyMZDjIEoKux2YgsqOkkQwHWXKXkOsJGYjuY2TXKwEldwNxLhC/gSkk1UB01xsBBTYjC1JiINb4"
                + "GvwGAgB9VhILFc2B9QAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAe0lEQVQoU2NkoDJgRDLPCsh+CMRPKbED2cAVQINcgfgeEO8E"
                + "4l1AfIhUw5ENhOm1hxrsBqSVoQaDDN8NxE8IWYDNQGQ9MkiGgyy4C7UAZPhBbIYTMhBdjx1QAGSwOxArQV0dgayIVAOR9UoD"
                + "OXJAfJxaBmINTkpcSB8DAS8zEQuBFGGjAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAKCAYAAAC0VX7mAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABZSURBVDhP"
                + "Y6A2YIbSIOALxF+B+DOYRyEQAeLlQPwGiE8BcQsQ2wExVQDIIJCBIIPfAjHIokQglgZiigHIEJBhIENBhtPU9aDgAVkECi6q"
                + "AJDrQRE5sgEDAwCWpRA/yw9O8gAAAABJRU5ErkJggg==")
    public void moveToLineToStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='10' style='border: 1px solid red;'></canvas>\n",
                "context.moveTo(2, 2); context.lineTo(16, 6); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAOlJREFUOE+t0z1KRDEQAOBvS+32AHoI"
                + "EbzAoh7ARhAR3N6txUL8W7CzshEbwcbOxhU9idVaWNsoFoL6Igm8Qpbn20kzIWG+TEKmI3h0gj0F3M9wia3PKeAlvrHdWsqJ"
                + "dfAL/SjwAJ84jAKP8IHjKDBB7ziJAq8wg7UocAG3WMcrXnL8t1//2DtYRhdzeMMI9zk2wid1Sqp6FStYwg2uq/ndJLlp681i"
                + "CwM8Vbc4w8NfcFOwnpu6KX2z9ObD6qBxfbMNWPJPsYk9XJTFacBkLOI8P8MunqcFS2GpITYwHwUmuIfHSPC32nDwBzgGJBXR"
                + "dXO7AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA3ElEQVQ4T2NkoDJgpLJ5DDADG4AG/wfiRkotgBkIMujfoDfw"
                + "L9CFTdT0MlUNrAa6jBOIa6jlwkygQXpADKIpArBYVgeacgqIs4D4HBA/BOJv5JiMnLBTgQZ4A7EGEMsD8XUgPgDFm4g1HF9O"
                + "sQIaYg/EDkBsDMSLgHgmEN/EZzixWU8VaEgcEBcA8XIg7of6AMNsYg2EaeQHMgqBuBKIu4G4DohBOQwOSDUQplEOyABlV1CQ"
                + "VADxKpgEuQbC9LtBvX8U6vKvlBoIMpgJiCcAMSiFKFPDQJSIGfwGAgAmkSMVCV26awAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA3ElEQVQ4T2NkoDJgpLJ5DDADG4AG/wfiRkotgBkIMujfoDfw"
                + "L9CFTdT0MlUNrAa6jBOIa6jlwkygQXpADKIpArBYVgeacgqIs4D4HBA/BOJv5JiMnLBTgQZ4A7EGEMsD8XUgPgDFm4g1HF9O"
                + "sQIaYg/EDkBsDMSLgHgmEN/EZzixWU8VaEgcEBcA8XIg7of6AMNsYg2EaeQHMgqBuBKIu4G4DohBOQwOSDUQplEOyABlV1CQ"
                + "VADxKpgEuQbC9LtBvX8U6vKvlBoIMpgJiCcAMSiFKFPDQJSIGfwGAgAmkSMVCV26awAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAC1SURBVDhP"
                + "5dOhCgJBEMbx1QOLwWC/JuIrWGxGo2/gQ1wTNBuMFl/BavQFbMIVLVaxCHecCvqfu12wzwiCH/yY3Q3D3jLnfj6Rr1MMsCt3"
                + "itR9rfmqTmhols+GL19V+doN72hUS11Cwyva1VKXMDY3LHHGExkeUGWCDVLk2GOBEUzSR4ItLpDmXZikgxnkaVbowSQtyG9a"
                + "YA6zsYuxxgljObDKEAfIMzTlwCLy2TJyx3L3b3HuDVsZGxJRyvPSAAAAAElFTkSuQmCC")
    public void moveToBezierCurveToStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.moveTo(2, 2); context.bezierCurveTo(2, 17, 1, 4, 19, 17); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAOhJREFUOE/N1LErxVEUB/DP+xNke4OS"
                + "LAZlNkmxSIoy2ZRks5n0bDIoI6NNBmWQUW+RgZXF8AazySi/I7+6vXp63juDW3e59f3cM5xzGpJPo/B28YrnnzvQVyV4jElM"
                + "YQT3uMMtnvrVS7DMjGIW81jCO85xio/f8F5gd2YOm1jEIY56of2CdX4a+1WlTezgsRv+K1jnt3CCdVyV6KBgGAu4xgpuanQY"
                + "MIxlnGEGb/EwLBjGAcaxkQWG08EaHjIqDHAPY9jOAieqIWhHO2WBUeULVjPBy2oPXGSCsVw6mWALn5ng97D8f/ALi58fh0da"
                + "GhQAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA4klEQVQ4T2NkoDJghJrHBKSzgPgaFL8g1x5kA6cCDdGC4r9A"
                + "+gQQ74DiB8RaADMQXb00UMAeiD2A2B2InwDxFCCeT8hgXAai6/MCCmQDsQbU4H5cBhNrIEy/FZBRA8T/gTgHiO+jG0yqgTD9"
                + "VUBGGRCHA/FOZEPJNRBkhj8QLwZiFyA+BTOUEgNBZiQCcQMQGwPxG5AApQaCzJgAdV0BtQwUgUaOJZC+Qg0XghxWB8R8QFxC"
                + "LQPNgYbNAWJdahkIciUoNzlR08DtQAMnUdPAhUAD91HTQFDiXk1NA8HJcfAbCABA6CBYte9o5AAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA4klEQVQ4T2NkoDJghJrHBKSzgPgaFL8g1x5kA6cCDdGC4r9A"
                + "+gQQ74DiB8RaADMQXb00UMAeiD2A2B2InwDxFCCeT8hgXAai6/MCCmQDsQbU4H5cBhNrIEy/FZBRA8T/gTgHiO+jG0yqgTD9"
                + "VUBGGRCHA/FOZEPJNRBkhj8QLwZiFyA+BTOUEgNBZiQCcQMQGwPxG5AApQaCzJgAdV0BtQwUgUaOJZC+Qg0XghxWB8R8QFxC"
                + "LQPNgYbNAWJdahkIciUoNzlR08DtQAMnUdPAhUAD91HTQFDiXk1NA8HJcfAbCABA6CBYte9o5AAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAC8SURBVDhP"
                + "5dOxCkFRHMfxg2xG3cVCSpGZMliVxW4zm6zewwt4B5InsChlVpSFSYhJ+f7OPV7g3v/mW5/uOWf4d+re66zLhWcWYxTwxhOJ"
                + "yoSnBs7QCD7YYBUckaoShpjjgi1GMKuPBQ6Y6MCqDpbQ8IoOrJrihp7fGTXAHS2/M0ov6oSi39HvO0zaDmV0oc/LJN3ugaY2"
                + "aW+oXsijjbUOLNKwfby064ya/mGrdMOq5cArIsuBdUTx8o9y7gudKxyCiC+bbQAAAABJRU5ErkJggg==")
    public void moveToQuadraticCurveToStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.moveTo(2, 2); context.quadraticCurveTo(19, 4, 19, 17); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAADRJREFUOE9jZKAyYKSyeQyjBlIeokMr"
                + "DP9T6GGwb5G9PAINpDAIIdqHVrIZ9TJ5IUD1WAYAYokEFfnLcNAAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAANElEQVQ4T2NkoDJgpLJ5DKMGUh6iQysM/1PoYbBvkb08Ag2k"
                + "MAgh2odWshn1MnkhQPVYBgBiiQQV+ctw0AAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAANElEQVQ4T2NkoDJgpLJ5DKMGUh6iQysM/1PoYbBvkb08Ag2k"
                + "MAgh2odWshn1MnkhQPVYBgBiiQQV+ctw0AAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAoSURBVDhP"
                + "YxgFo2AQAEYoDQL/oTS5AGwWE5hJRTD4DRwFo2DgAQMDAN0JAQjYZYmBAAAAAElFTkSuQmCC")
    public void lineWidthMoveToLineToStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.lineWidth = 4; context.moveTo(2, 10); context.lineTo(18, 10); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAL9JREFUOE/tkr0NwkAMhb+0SNRQsAM/"
                + "PWILaGGMlClAbAEFDYzBAEAmgBK2QJZO4ohs4qCjw80V9/T5PdsZiStLzOPnwBYwAU7A4xv3msML0AeuAXwOr6uJBlwBueHu"
                + "FsGXmkYDjoGjI+4AKKs6ayl3oFMDlRRrL3ADzGuAkkIW+FaWwymwd8TuVq/BAraBHTACeh/AC2Ab/3sOW4DDAJcGcZMDMGsK"
                + "1AzGTYoUQHMKnsiO3bwkf2Cjcani5DN8AnFEGRVy9qdcAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAApUlEQVQ4T2NkoDJgpLJ5DHQx0AHo6gPkuhybC+8DDVMA4gdA"
                + "fAGIL0ItIMoSbAb2Aw0owOFCZEsasKnBZqABUOF5IrysCPUFilJckQLzNj5zC4GSE9AV4DIQn7dhZoDC15BYAwOACteT421c"
                + "LhSAGghKQiR5m5iEDUpCoIgCYXsgRrZkA5AfiGwjMQZicyHMEhCNEjHkGogzGEYNJCLlEVAy+MMQAEwqFxVGqYQqAAAAAElF"
                + "TkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAApUlEQVQ4T2NkoDJgpLJ5DHQx0AHo6gPkuhybC+8DDVMA4gdA"
                + "fAGIL0ItIMoSbAb2Aw0owOFCZEsasKnBZqABUOF5IrysCPUFilJckQLzNj5zC4GSE9AV4DIQn7dhZoDC15BYAwOACteT421c"
                + "LhSAGghKQiR5m5iEDUpCoIgCYXsgRrZkA5AfiGwjMQZicyHMEhCNEjHkGogzGEYNJCLlEVAy+MMQAEwqFxVGqYQqAAAAAElF"
                + "TkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACKSURBVDhP"
                + "7dHBDUAwFMbxikWM4OhoBKMYwQZGMQJHN0yAmy34XrwmTRVP49h/8kudXltVISrn1auIV7MZElhghAk65lUN+w3arIEKxKXg"
                + "GmajW4ijk7iGmEoQ93RtbQBxBbiG2C7Xjnm12yCDt/+0Qn9+yqOhdGJ62RbME9KL/5Le5NPDhELOlDoAyg88S5w3VsoAAAAA"
                + "SUVORK5CYII=")
    public void setTransformFillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.setTransform(1, .2, .3, 1, 0, 0); context.fillRect(3, 3, 10, 7);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAL9JREFUOE/tkr0NwkAMhb+0SNRQsAM/"
                + "PWILaGGMlClAbAEFDYzBAEAmgBK2QJZO4ohs4qCjw80V9/T5PdsZiStLzOPnwBYwAU7A4xv3msML0AeuAXwOr6uJBlwBueHu"
                + "FsGXmkYDjoGjI+4AKKs6ayl3oFMDlRRrL3ADzGuAkkIW+FaWwymwd8TuVq/BAraBHTACeh/AC2Ab/3sOW4DDAJcGcZMDMGsK"
                + "1AzGTYoUQHMKnsiO3bwkf2Cjcani5DN8AnFEGRVy9qdcAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAApUlEQVQ4T2NkoDJgpLJ5DHQx0AHo6gPkuhybC+8DDVMA4gdA"
                + "fAGIL0ItIMoSbAb2Aw0owOFCZEsasKnBZqABUOF5IrysCPUFilJckQLzNj5zC4GSE9AV4DIQn7dhZoDC15BYAwOACteT421c"
                + "LhSAGghKQiR5m5iEDUpCoIgCYXsgRrZkA5AfiGwjMQZicyHMEhCNEjHkGogzGEYNJCLlEVAy+MMQAEwqFxVGqYQqAAAAAElF"
                + "TkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAApUlEQVQ4T2NkoDJgpLJ5DHQx0AHo6gPkuhybC+8DDVMA4gdA"
                + "fAGIL0ItIMoSbAb2Aw0owOFCZEsasKnBZqABUOF5IrysCPUFilJckQLzNj5zC4GSE9AV4DIQn7dhZoDC15BYAwOACteT421c"
                + "LhSAGghKQiR5m5iEDUpCoIgCYXsgRrZkA5AfiGwjMQZicyHMEhCNEjHkGogzGEYNJCLlEVAy+MMQAEwqFxVGqYQqAAAAAElF"
                + "TkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACKSURBVDhP"
                + "7dHBDUAwFMbxikWM4OhoBKMYwQZGMQJHN0yAmy34XrwmTRVP49h/8kudXltVISrn1auIV7MZElhghAk65lUN+w3arIEKxKXg"
                + "GmajW4ijk7iGmEoQ93RtbQBxBbiG2C7Xjnm12yCDt/+0Qn9+yqOhdGJ62RbME9KL/5Le5NPDhELOlDoAyg88S5w3VsoAAAAA"
                + "SUVORK5CYII=")
    public void transformFillRect() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.transform(1, .2, .3, 1, 0, 0); context.fillRect(3, 3, 10, 7);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAEFJREFUOE9jZKAyYKSyeQyjBlIeoiM4"
                + "DBUYGBiSSAjBOlxqkcOwiQQD5zEwMDzApn4ERwoJwYdf6WgYUh6UVA9DAJn9AxWRWUx3AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAPUlEQVQ4T2NkoDJgpLJ5DKMGUh6iIzwMG0gIQZxqkcMQpIif"
                + "SEMbgeo+YFM7wiOFyODDr2w0DCkPRqqHIQCa5QMVIJiVTAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAPUlEQVQ4T2NkoDJgpLJ5DKMGUh6iIzwMG0gIQZxqkcMQpIif"
                + "SEMbgeo+YFM7wiOFyODDr2w0DCkPRqqHIQCa5QMVIJiVTAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAqSURBVDhP"
                + "YxgFo2A4AkYoDQINUJoYgFMtE5SGAX4isQAQj4JRMFIAAwMAXz4BqtdD7oMAAAAASUVORK5CYII=")
    public void moveToLineToTransformStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.moveTo(2, 10); context.lineTo(13, 10);"
                + "context.transform(1, .2, .3, 1, 0, 0); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAGZJREFUOE9jZKAyYKSyeQyjBlIeoqNh"
                + "CA5DFgYGhj/khiauMHzAwMBwm4GB4QISvkqMJfgiRZuBgcEADSNbAGJjWEJqLKNbosrAwKCA7HJSDcTma5Qwp4aBKJaMGkhM"
                + "SsOvhuphCABLbg0VZ88YdgAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAZ0lEQVQ4T2NkoDJgpLJ5DKMGUh6io2EIDkMBIP5AbmjiCsP3"
                + "QAMPAPEDID4IxBegbIL24IsUB6BuBSDWB2IDIAbxNxCyhNRYRrcEZJEgsrNJNRCbl1HCnBoGolgyaiDBZEZQAdXDEABMtg0V"
                + "HTQi5gAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAZ0lEQVQ4T2NkoDJgpLJ5DKMGUh6io2EIDkMBIP5AbmjiCsP3"
                + "QAMPAPEDID4IxBegbIL24IsUB6BuBSDWB2IDIAbxNxCyhNRYRrcEZJEgsrNJNRCbl1HCnBoGolgyaiDBZEZQAdXDEABMtg0V"
                + "HTQi5gAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABWSURBVDhP"
                + "YxgFo2CkAAEoTRZghtLI4AcQvwdiCyjmhIp9AGKCgBFKYwMOQKwAxPpAbADlbwDiB0B8EIgvQNkoAJ+B2AC6JSAsCMRUBRSF"
                + "+SgYeoCBAQD2+wpun2ZPIwAAAABJRU5ErkJggg==")
    public void transformMoveToLineToStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.transform(1, .2, .3, 1, 0, 0); context.moveTo(2, 10);"
                + "context.lineTo(13, 10); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAADNJREFUOE9jZKAyYKSyeQyjBlIeoiM8"
                + "DBsoDEGwfuQwpLqBFDoQon2Ex/JoGJIXAlRPNgCWIAIVwMrcFwAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAM0lEQVQ4T2NkoDJgpLJ5DKMGUh6iIzwMGygMQbB+5DCkuoEU"
                + "OhCifYTH8mgYkhcCVE82AJYgAhXAytwXAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAM0lEQVQ4T2NkoDJgpLJ5DKMGUh6iIzwMGygMQbB+5DCkuoEU"
                + "OhCifYTH8mgYkhcCVE82AJYgAhXAytwXAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAkSURBVDhP"
                + "YxgFo2A4AkYoDQINUJpcANbPBGaOglEwCvABBgYAX1ABBKc2Rr8AAAAASUVORK5CYII=")
    public void moveToLineToRotateStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.moveTo(2, 10); context.lineTo(18, 10); context.rotate(90); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAALBJREFUOE/t0rEJAkEUhOH/cjGwAhEs"
                + "wSYMLMEmNBCNNbcJ0RosQrAEMRYzOTCQB/Nkg3PZXQ0M7sU7H8OwFdADbvzoKmAP3IENcPnWNbADLIGF0DXwKIUN9OsDK2Ai"
                + "eFuChqDnR2o8FLzLgZtAz48FPwGb4ZgCx0DPTzXFWY1PMTgF9PxMjQ+Cr01wDmj5rtB58CPqEM4FPTsQbLta4/eVgh9nbMGU"
                + "rxt/0274hxu+AE0JGRUWB25NAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAqklEQVQ4T2NkYGAQB+KXQEwVwAg0ZR4Q/wLiNiB+RKmpIAO5"
                + "gbgaiCuAuBVq8HdyDQYZCAMKQEYVEPtDDZ1IjqHIBsL0m0INVoUavIwUg7EZCNPvCQ2KP9Cg2E2MwfgMhOmPg7r4MtTF5/EZ"
                + "TIyBMP3FUINXQg1+gs1gUgwE6eeDGloCNbQJSIOCBA5INRCmUQlq8HogvZUaBuIMRnJdOGog5bE8GoY0DEMATKUZFZcLTogA"
                + "AAAASUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAo0lEQVQ4T2NkoDJgBJo3FYiZgbgNiB9Raj7IQB4grgLiCqih"
                + "rUD6O7kGgwyEAUWowX5QgyeSYyiygTD9ZlCDVaAGLyPFYGwGwvR7QQ3+AzV4FzEG4zMQpj8eavAlqMHn8RlMjIEw/cVARjUQ"
                + "rwRiUMQ9wWYwKQaC9PNBXVsCdW0duqGkGgjTrwR17WIgfQDZUHINxBmMowYSk3TxqxkNw0EYhgB8LRYV8M9b/wAAAABJRU5E"
                + "rkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACESURBVDhP"
                + "7cixCYNQFIXhV9hIIK2NhJANUjiChbiBRVZIkQ0UQiYQCeICFq5gkRFSZAULNxDJf91AOOX74btwj6PIjrIODU7bJ+iAJ1ZU"
                + "CCHpjDcm3G1QlWDAF4UNqjJ8MCK1QdUNP/S42qDqgRk1YhsUHfHCghIBJF3QIt8+n8+3K+f+D8wRAv4wDRcAAAAASUVORK5C"
                + "YII=")
    public void rotateMoveToLineToStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.rotate(.5); context.moveTo(1, 1); context.lineTo(18, 1); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAADdJREFUOE9jZKAyYKSyeQzIBv6n0HCw"
                + "WaMGUhSKtA9DipwH00zTdDjqQvJCYDRSyAs3ZF2DPwwBbfkGFYXESWwAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAN0lEQVQ4T2NkoDJgpLJ5DMgG/qfQcLBZowZSFIq0D0OKnAfT"
                + "TNN0OOpC8kJgNFLICzdkXYM/DAFt+QYVhcRJbAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAN0lEQVQ4T2NkoDJgpLJ5DMgG/qfQcLBZowZSFIq0D0OKnAfT"
                + "TNN0OOpC8kJgNFLICzdkXYM/DAFt+QYVhcRJbAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAnSURBVDhP"
                + "Yxj0gBFKg8B/KE0uAJvFBGZSEYwaOApGwSgYBSDAwAAA4YcBDBgCJpwAAAAASUVORK5CYII=")
    public void rectFill() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.rect(2, 2, 16, 6); context.fill();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAPxJREFUOE/F08EqRVEUAND1PkL5AEli"
                + "qgxRb2Cg5AswMREDmZqLwctE6T2lZCDlB0wYGBmIJF9ARr6Au3WVbq577uvGGZ7a6+x99t4tDZ9Ww54/Adf4fGgRT7jAGV5T"
                + "qilmGNg0dnJsEksYze/2q9AiuI4bXBUCx9HFIzbxXAYXwVu08VISsIcxTKWCpzjGeVVpqeAKZjHXFDiAa8zjrh/0pzmMLKOz"
                + "E02B4RzhDat10bJNGcRJNpOX2KqD/rZ6QzjAfZ1Mq3Y5Mt3GCJZTGlUFflUbjdrAA3r5JAznjdv9/iWpYMTESC1kazmDwA6z"
                + "/31Hp18wqTd1Mvwf8AMXuSUVr2KpOAAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAABAElEQVQ4T72UPQuBURSAfazMTAblP4jBKCOF1eAPIJvB7iMx"
                + "KYMyIbKJzeRH2GxmVvKcQrlxP0pOPcP7vvc8nXPvPa/X8+Pw/tjn+YswQ9VlCMABdjC37UStUGSSXIMjxKAIfujCzCRWhQsS"
                + "tjBUEnM8N2EDdZ1UFUpCD9YfkoK8G4MPst+kqnDw2Le+pooQ3062wjwLK5Aw7ZWtUNbtoQ2yn87x6R4WsDQgCWdX47eLLRVG"
                + "QU7XKXSTssIUhriLUSeUSZnADUq27dvMcgtZGpqwNFVrIxSHHFQVrjAFmfEIdEA6eYWt8Jkg9zQFMuMXGMHbVLkKTR3/539o"
                + "rEK34A43YyQVd6W5xwAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAABAElEQVQ4T72UPQuBURSAfazMTAblP4jBKCOF1eAPIJvB7iMx"
                + "KYMyIbKJzeRH2GxmVvKcQrlxP0pOPcP7vvc8nXPvPa/X8+Pw/tjn+YswQ9VlCMABdjC37UStUGSSXIMjxKAIfujCzCRWhQsS"
                + "tjBUEnM8N2EDdZ1UFUpCD9YfkoK8G4MPst+kqnDw2Le+pooQ3062wjwLK5Aw7ZWtUNbtoQ2yn87x6R4WsDQgCWdX47eLLRVG"
                + "QU7XKXSTssIUhriLUSeUSZnADUq27dvMcgtZGpqwNFVrIxSHHFQVrjAFmfEIdEA6eYWt8Jkg9zQFMuMXGMHbVLkKTR3/539o"
                + "rEK34A43YyQVd6W5xwAAAABJRU5ErkJggg==",
            IE = "context.ellipse not supported")
    public void ellipseStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "if (!context.ellipse) { log('context.ellipse not supported'); return; }\n"
                 + "context.ellipse(10, 10, 8, 4, Math.PI / 4, 0, 1.5 * Math.PI); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAOlJREFUOE/NlL0OAVEQRs+UCrVai8Qb"
                + "aK0HUCslOk+iU6hovQC1JyBCq9cqlMPdrGTt/dtNNmLqmXPnm/nmCjWH1MzjN0BVEqAJXEW4VFFhdajKHhjmICdgIcKmDPgL"
                + "mHW28xSah2Yi3ELgInAMbAMFTyAR4eDLKQJ7wDki7S5CqxTQJKlyBPoR6EgknbUVrqVMgHUEuBJhWgqYdWkWY6zjC69sp7FV"
                + "aUPqv0YA6pTtvRRVBpDOyQd1yg6eXgTqlB295Uz+0jNTS3YU+JmhKmb784KlLNmlgTlw931NHeDh8mJlYOyD+H/gC77COxVH"
                + "Ilo+AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA7ElEQVQ4T73UgQ2CMBQE0HYD3aAjOIIjwAayiU5g3MANYAPd"
                + "QDfQDXQDvEtKUuH3/5IQmhBMgMddafVu4eEX9tw6YN+7gOTBe3ef22CSENgZyAHHJmJEL8C7EvwPjMkeCZYaHdDaQsfgDg8Q"
                + "zI0nLjSAeRbHGGTNj5HiC3BbBPIm1GZCJtVGnZtT6aNYtfmiK8BGeqO4DpHyhpv3SsRs7RzIuSSqVRdrZ3dKXEIvJaVYW916"
                + "QJmwxREEWKxt7mWgrE9UmtNJbRMckgGu8JvbMk17wtc+pumLwQQmyKl4SztmNmgs+JX+D60U2vUf7SQ5FX401SMAAAAASUVO"
                + "RK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA7ElEQVQ4T73UgQ2CMBQE0HYD3aAjOIIjwAayiU5g3MANYAPd"
                + "QDfQDXQDvEtKUuH3/5IQmhBMgMddafVu4eEX9tw6YN+7gOTBe3ef22CSENgZyAHHJmJEL8C7EvwPjMkeCZYaHdDaQsfgDg8Q"
                + "zI0nLjSAeRbHGGTNj5HiC3BbBPIm1GZCJtVGnZtT6aNYtfmiK8BGeqO4DpHyhpv3SsRs7RzIuSSqVRdrZ3dKXEIvJaVYW916"
                + "QJmwxREEWKxt7mWgrE9UmtNJbRMckgGu8JvbMk17wtc+pumLwQQmyKl4SztmNmgs+JX+D60U2vUf7SQ5FX401SMAAAAASUVO"
                + "RK5CYII=",
            IE = "context.ellipse not supported")
    public void ellipseFill() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "if (!context.ellipse) { log('context.ellipse not supported'); return; }\n"
                + "context.fillStyle = 'yellow';"
                + "context.ellipse(10, 10, 8, 4, Math.PI / 4, 0, 1.5 * Math.PI); context.fill();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAMZJREFUOE/t1CFuAkEYxfHfolAIAjcg"
                + "wXEGHAh0UdXQNEgcqqoXaBoSHKo9QWXPUFfFASCIKkQFu8mSkGV3s5uMZJIRk3nf/5vv5WUigVcUmOcOzHW0jWNVr8s87GGJ"
                + "HV5DADf4x7wqLNEVvXCEBSZ1YGXAFRrxfgkFfMYAswLgGj94y94XjTzFE4YFwG+846MqMNF94Q8PmaJPtDDOa1YWmy62cWSa"
                + "+E2L+zjhEfu6wIs+Gb+THg55Y16D759D3Rjf6oN7eAYSbhYVZnnVXgAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAt0lEQVQ4T2NkoDJgpLJ5DKMGUh6i+MKQE2j8TCDeD8TzibUK"
                + "n4GLgYaoA7EZsYaB1OEy0AQotxCItUkxDJ+BZUBJGSDOo5aBGUCDDIE4HYeBoLC9BMRT0eVxedkGqHARECvhMPAqUBxk6WFi"
                + "DQSp2wDEL6AakfVNAXKEgDgKm2WEst5aqNe3A2lTIBYE4pNAnAjEv8kxEKQHlGxAsc4DxLuB+DyOYAALE3IhPr1Y5UYNJDnI"
                + "MDQAAHzHFBVJ6doGAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAtElEQVQ4T2NkoDJgpLJ5DKMGUh6ihMKwB2jFMyDuI9YqfAau"
                + "AhrCBsTRQPyVUgMlgAbcAmI+Yg2CqcPlwkSgAg8gDqeWgZlAg4yBOAWHgbOB4ueAeDq6PC4XmgMV7gdiLhwGfgOKOwLxSWIN"
                + "BKlbAMRaQGyGpukUkH8NiBOwWYYvljmhXgoD0kuBGKQ2CohBsQ8Kku+kGghTD/K+EZQDCjcMbyIbTChh4whC3MKjBpIcZBga"
                + "AFvHFRVbN/HSAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVDhP"
                + "YxgFIwAwQ2lsgBOI5wGxABBfAAlQChYD8SkIk3JgAsRXIUzSABOURgdOQLwXwiQN4DLwExCzQ5hYwUwgzoYwiQM2QHwPwsQK"
                + "QMFhC2ESDzYC8QwIEwVMAeJlECbpYC0Qg1w6FYhBMX4biJcAMSsQYwWMUBofMANiUKzzAPFuID4PxKNg6AAGBgDVbRIf3/D2"
                + "vgAAAABJRU5ErkJggg==")
    public void arcStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.arc(10, 10, 4, 0, 4.3); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAALNJREFUOE/l0jEOAVEUheFPIVGpLECn"
                + "U1mEVkNlLUarF7EEGluwAIlEJxo9tUgUeC+qiUnMZBTiJq95uefPvfecCpLnK1J3VNLC8BGARaFpXvKnwNHzuOMirrzR/OsN"
                + "fyeHAzRezp2xKOB8dHmCNmrYvyAtXDHEKQc4Ag/Yop8SLlFHNy/wiGaGaI1ZjvXjhBt0MoBz7DD9cMrvAEtfuXRTQmx6qCLk"
                + "L1TI4w0rXD68X2x7AH8dMhXIajp1AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAsUlEQVQ4T2NkYGDYD8QHgZgawJ4RaEoDFFPDwIZRAykOxtEw"
                + "pDgIGWgbhuZABxpBHXkOSJ8kw8FgF7YAsQwQhwHxUqgh0UB6FRBnAvF3EgwGG3geiH8DsRmaxlNA/jUgTiDVQJBhrDg0fQOK"
                + "O5LgfbALzwKxMQ4D5wDFU0h1ISgCYJGBrnc2UAAkP51IQ8EupLqXqR4poOKfD4h1gPgSEINcrQvEV4B4CxD/JdK7IGX2AF1J"
                + "M5M1IFdKAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAs0lEQVQ4T2NkYGDYD8QHgZgawJ4RaEoDFFPDwIZRAykOxtEw"
                + "pDgIGWgbhuZABxpBHXkOSJ8kw8FgF7YAsQwQhwHxMiD+D8TRQLwKiDOB+DsJBoMNPA/Ev4HYDE3jKSD/GhAnkGogyDBWHJq+"
                + "AcUdSfA+2IVngdgYh4FzgOIppLoQFAGwyEDXOxsoAJKfTqShYBdS3ctUjxRQ8c8HxDpAfAmIQa7WBeIrQLwViP8Q6V2QMnsA"
                + "0W80k6fnBs0AAAAASUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACjSURBVDhP"
                + "Y2RgYNgPxAeBmBrAHkQ0gJnUAQ1MUAbVwKiBlINRAykHyAYqAnEyFIPYZAGYgfOA+BgQO0ExiA0SIxmADPQHYl4glgTiaCgG"
                + "sUFiZBn6GUpjA8+BmBTvgwuH+xA2VrAPiPHJYwBiYhkUSUQDkIH4vASKIJAriQYgA+8A8WowDxWAxLYDMUlehlUBAkCsAsQw"
                + "zSBXgyzaCOYRDRjsAb/2Fyf2EU9MAAAAAElFTkSuQmCC")
    public void arcCircleStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.arc(4, 16, 4, 0, 2 * Math.PI); context.stroke(); context.strokeRect(0, 0, 20, 20);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAHlJREFUOE/t1LENwjAQBdDnkiZtGno6"
                + "qgzBEmnZCCFGgCmYIWuEAVKGtJGDLMcFha/30/c/6YLCEwp7KhhttMMZB3zw/NV7SodXtDjihAk9xhicAq7fvdDgUgpcnPc3"
                + "9T32/ZyEC/jAgNs6ZS64uZcK7j8VtcM/7HAGnYcKFQzb6fQAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAcElEQVQ4T2NkoDJgpLJ5DKMGYg1RSaCoExBLAfE6IL6LL9yJ"
                + "CcNKoAH6QPwBiIOB+AAQh+IylBgD0fUuBwp8BeIUbIaSYyDInEdA7ALEt9ANJdfAxUCDDgLxHGoZiDNeyHXhqIGIEBgNQ8qL"
                + "W6qHIQCR/wsV2AyBPAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAbUlEQVQ4T2NkoDJgpLJ5DKMGYg1RWaCoNxCDguccEJ/EF+7E"
                + "hGEx0AAtIP4HxNFAvAqIM4H4OzaDiTEQXd8poMA1IE6gloEgc74BsSM275PjQpCBc4D4LBBPR3cluQbijJdRAykvKkbDcBCG"
                + "IQDFswwV4F8onAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABUSURBVDhP"
                + "YxgFgw8wQ2l8QBKI/YHYA4hfAvF7IMYJmKA0PpAAxL5ArAzEJ4B4NRBTFSwH4jkQJvXAQyBWgzBRATFhiA0YQ+lzUHoUjIJR"
                + "QAJgYAAAKqEIH3WHwEcAAAAASUVORK5CYII=")
    public void arcAnticlockwiseStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.arc(10, 10, 4, 0, 4.3, true); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAJpJREFUOE/t1KENAkEQRuHvHAoFNeCo"
                + "AUkFoNCEXD2EoFFQAZIacNQACoUkmxyCy+4mXDaoWzuZl33/TKZS+FWFeXpgNNElRk3lgWMu91yGYxwwwK2BTPDCCvcYOAc8"
                + "44lFq/GEIea/AIPmBrOE3gW7mH7qhzWmWCeAe1yxbdf/BiyuHEyKDiUAi6/NJ+9ii93pEPXXplNsX01vh5geFQJIQ80AAAAA"
                + "SUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAsUlEQVQ4T2NkoDJgpLJ5DKMGYg1Rc6CoEVTmHJA+iS/c8YUh"
                + "J1DjdCAOA+KlUEOigfQqIM4E4u/YDMZn4AKgBi0gNkPTeArIvwbECaQYCPLmfiDmwuG9b0BxR2zex+VCkJdA4ZaKw8DZQHFQ"
                + "eIKCBAXgM9AYqDIFh4FzcMnhMpDqXgY5jKqRAjKQA4hnADEs2YB8EwXEoGSTAcQ/SIllZLVUS9g44gO/8GhpQ1awoWgCAGzL"
                + "HRVrJ2zIAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAp0lEQVQ4T2NkoDJgpLJ5DKMGYg1Rc6CoEVTmHJA+iS/c8YUh"
                + "J1DjdCAOA+JlQPwfiKOBeBUQZwLxd2wG4zNwAVCDFhCboWk8BeRfA+IEUgwEeXM/EHPh8N43oLgjNu/jciHIS6BwS8Vh4Byg"
                + "+FlokKAowWegMVBlCh4DscrhMpDqXgY5jKqRAjIQOdksBfJBvomiJNnAgo9qCRtHfOAXHi1tyAo2FE0ArxceFbIaufkAAAAA"
                + "SUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACJSURBVDhP"
                + "YxgFgw8wQml8wByIjSBMhnNAfBLCJB1wAvECIP4GxHOAeDaUDRIDyZEMQBpPQZgoACQGkiMJgLwJcg0uAJIDqcEAzFAaHfgB"
                + "8Rsg3gzmYQJFKH0GSsMBE5TGBghF2HQoTRQg28v4AFUjBQSQkw0oyYCSDogNEsOZbOiasEfBgAAGBgDFsxrAzDMHqwAAAABJ"
                + "RU5ErkJggg==")
    public void arcCircleAnticlockwiseStroke() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.arc(10, 10, 4, 0, 2 * Math.PI, true); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAHpJREFUOE/tlMENgCAMRR8upFN50CW6"
                + "hB6cSlwIQ/SgScsBG0/0COmDPn4IOFdw5tGA342WHQoTMAI9kIAD2BBW62gbeMEWo3G2oCXgft9MY0aEQdsoAfOIViWEzhMI"
                + "omf415GdHyULescmr8T62FRmvH0OleIebe4OT5UrGhUWjbLDAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAfElEQVQ4T2NkoDJgpLJ5DKMGUh6i+MOwmkGegZVhItAaU6hV"
                + "pxl+M+QztDI8xGU1bgMhht0AauRA0/wDaKgGLkNxG9jAsAFokD8Ol2xkaGAIwCaHz8CnQA1SOAx8BjRQesANpLKXqR4poACi"
                + "arIhM42PFg5kBhySNqqHIQDrciAVd504BAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAlElEQVQ4T2NkoDJgpLJ5DKMGUh6i+MOwgUEAaEU9EDsAMYh9"
                + "AIgbGRoYHuCyGreBEMPuQw1C1v8ByDHEZSg+A+cDNSbgcMkCoIGJ2OTwGQhynQIOAz8ADRQccAPxeXkD0IWBpLoQFCnvsWgC"
                + "RYoj0MALpBkIUg2J6X4gDoBqBhmSSF6ywZXQCIiPFg5kBhySNqqHIQBu3hwVauHmwwAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAClSURBVDhP"
                + "YxgFIwAwQmnsoJ5BAEgWAFX5A2kQewPDf4ZGIPwAksYGcBsIMoyRYT+QZQARgIMHQEMNcRnKBKWxgQIgRjcMBBSAFvVD2RgA"
                + "t4GMDPFQFjYQAKUxAD4X4gM4wxCfgRugNDZwAEpjANwGgmKTgeEChIMCQJECksMKmKE0JjjI8IPBnmElMCw5gDwNIP4BxDuA"
                + "hkUCjXsAUjIKRiZgYAAArLcaWxWRPP8AAAAASUVORK5CYII=")
    public void arcFillPath() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.fillStyle = 'green'; context.beginPath();"
                + "context.arc(10, 10, 4, 0, 2 * Math.PI); context.fill();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAQlJREFUOE/N1EtKA0EUheGvxYUoug4F"
                + "0W0IKuguzEXcg61oEEfOXICCj4kLcKRCsgdHghIf3Qlp207SCUG8oyo49XPq1j2VmHIlU+YZDNyzrGMdS5jDLNq4+dqfCndV"
                + "ZqqB4RhbI9wfCTtlzW9guMRqzVZcCWtF7U9gOMR2TVhP1hT92/SBkfXqdkxYLk+saLjOl70KTWxMBMwfKTtbBLYwPyGwJSyU"
                + "gR8TwvJjkZsrOnzrztq43HMzUrt5/4vAZyzWpL0i9S6177F6bMIJNocCE20dqUQqvAxPSh6171hV1X3mKJyNukF5sMuRu5A4"
                + "0MjSU6sGRe+p2+iHWpSC6A+/r3GtdfX/3+EnUB03FTaS9zwAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAABBklEQVQ4T82UjRGCMAyFwwa6AW7gCDqBjuAIMoFhAmUC3UCd"
                + "QDaQDXQDGcEXpJgWKj/neebu3QGJX1/T1IC+HMGXeeQHMk2x2BJaQCGUQ3cog2Li4r0WdSDTCFUraAPJsy8Eym6yCXgsnXXp"
                + "RgrkXBfaQKY1ktsuJFVjOX0DXz27tGzTt9YETqW/6lCGuTML7ACMXKC4m/Xcrimveqm3fEM2HAjM4XDsOhwKTAGKAJT5tHp4"
                + "xbscTJeQoT5BiQGZH+ktMz7KMH8KASXQwZyqW6yBciseHpps61yCGq9c3aF84WKoZbhNCCiGMt/d9Ts0GaY9HkOoanRLG6z0"
                + "D/+++thStf/v8AmQKzYVpXVGrAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA/klEQVQ4T82U/RGCMAzFywa6AW7gCG6gbqAb6ASGCZQJZANx"
                + "Ah2BDXQDGcGXYjEUqqXHeebu/UE/fn1JWiI1cEQD85QbSCrGYQtoDk2hEiqgO5Qo0t+t6AaS2mDlDhp9yGAL6MGebwJJA/bQ"
                + "yrMUOaBLudYGsjMG9ok1oJnZ8AZW7m5f0nQdNAGUayuaQhiq6hYSdT2lwxNI3NWQqGspgZxuHELDnhL5je2UQ4FXgDhlvqON"
                + "Gl7wPfN0yJc6h1ID6uqyT1MYlEKZ6aptwL42D4dDTuv8AnU+ubZDHiH9Qo4CyqAEKlxv1+3QzJAGxrLQnnXVy374++pjS6z9"
                + "f4dPi7M2FSLXHwIAAAAASUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAECSURBVDhP"
                + "7ZPtEYIwDIaDLsAIuIFOoE4gI+gE4gTABI4gG3hOoG7ACG4gG+CbNkC1BZQ77/zhwzUfUN42JdDP44m3iWkKG2LGCj7AKDBu"
                + "GDmVlOLi3MIWjMmHXeNJDM+xGy2aSFZjCyZ0hA110ssF85cSK8biNTFFWCKS7B0CmuONK4SFZod8Zh6dEbWX2UZJE5TP50sj"
                + "dUOzwPhcjPFoK5EhqL/mULgjFOYOuTWG4hQcygVnWH9pU9DZqC3w3AxCM9U2KZpdaARLOknURaEaWgttTKEKs218ZHfJXuGy"
                + "eMEMIp2VPP8pCe1hzcZmoRQ+7xOqcP16B9gAQjtXSX++DdEDir42LCGgXtoAAAAASUVORK5CYII=")
    public void arcFillPathAngle() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.fillStyle = 'green'; context.beginPath();"
                + "context.arc(10, 10, 8, 2.3, 2 * Math.PI); context.fill();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAALhJREFUOE/Fk7ENAjEMRd/NQsEOLMAC"
                + "tFQ3AQwCE1DRsgALsMMV7AKW7lAUYn8XEaSKlO/n7+RnoPMaOvP4CfAKPJPOV2/tvtTWDnfAGbgkgSNwAG6LvjXyAziVIgdu"
                + "zY/AJnJoZ01hA9ps7D2Kcuk29YDKpdswio1XFDaLgF5heB0q2HWxugr5U2qAeiwJtLQsENt/5a6Okxq5zKXtZeAzQANNs5O1"
                + "+pJZ4HYG3XsBFedznnX4P+ALwKAjFWklBKcAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAr0lEQVQ4T2NkoDJgpLJ5DDQ3sIFMF8P1obswBGhgPxDPJdLg"
                + "ZKC6QiBeA1OPzcvHgJJ9yIpwGA6yvAiIrZDlsRmIVSEWQ7FajCtSCLkSp6W4DCTkSpwW4ks2uDThtQyfgbg04g0OQgkbXTOh"
                + "oCCYU9ANIBRZBA0EpRaYISA2RrpDT06EvAxSD3MliE0wwRNjIMigm1CXqBPKksQaCHIlCMDzLC6DiTWQkMPg8oPfQAB2pSIV"
                + "qh5VTAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAr0lEQVQ4T2NkoDJgpLJ5DDQ3sIFMF8P1obswBGhgPxDPJdLg"
                + "ZKC6QiBeA1OPzcvHgJJ9yIpwGA6yvAiIrZDlsRmIVSEWQ7FajCtSCLkSp6W4DCTkSpwW4ks2uDThtQyfgbg04g0OQgkbXTOh"
                + "oCCYU9ANIBRZBA0EpRaYISA2RrpDT06EvAxSD3MliE0wwRNjIMigm1CXqBPKksQaCHIlCMDzLC6DiTWQkMPg8oPfQAB2pSIV"
                + "qh5VTAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACASURBVDhP"
                + "7c7BCYAwDAXQqiu6gYs4mUcnEMENHMOkzVcotElbb/ogpEISv/ueTjrM0kvde7102Kim8DThWd7JWqnG8MziGZ5VWQetP/a0"
                + "YXM60BaK0kFqqTgdpBar0kG8XJ0O4gNN6QBHmtMBDr2SDg4p1SBdc1ItVLv/+j2cuwDuXB2rdVDEoAAAAABJRU5ErkJggg==")
    public void closePath() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.moveTo(4,4); context.lineTo(10,16);"
                + "context.lineTo(16,4); context.closePath(); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAChJREFUOE9jZKAyYKSyeQyjBlIeoqNh"
                + "OBqGZITAaLIhI9DQtIzAMAQASMYAFTvklLAAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAKElEQVQ4T2NkoDJgpLJ5DKMGUh6io2E4GoZkhMBosiEj0NC0"
                + "jMAwBABIxgAVO+SUsAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAKElEQVQ4T2NkoDJgpLJ5DKMGUh6io2E4GoZkhMBosiEj0NC0"
                + "jMAwBABIxgAVO+SUsAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAWSURBVDhP"
                + "YxgFo2AUjIJRMAqoAhgYAAZUAAE6HI5PAAAAAElFTkSuQmCC")
    public void closePathNoSubpath() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.closePath();context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAChJREFUOE9jZKAyYKSyeQyjBlIeoqNh"
                + "OBqGZITAaLIhI9DQtIzAMAQASMYAFTvklLAAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAKElEQVQ4T2NkoDJgpLJ5DKMGUh6io2E4GoZkhMBosiEj0NC0"
                + "jMAwBABIxgAVO+SUsAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAKElEQVQ4T2NkoDJgpLJ5DKMGUh6io2E4GoZkhMBosiEj0NC0"
                + "jMAwBABIxgAVO+SUsAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAWSURBVDhP"
                + "YxgFo2AUjIJRMAqoAhgYAAZUAAE6HI5PAAAAAElFTkSuQmCC")
    public void closePathPointOnly() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.moveTo(4,4); context.closePath(); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAChJREFUOE9jZKAyYKSyeQyjBlIeoqNh"
                + "OBqGZITAaLIhI9DQtIzAMAQASMYAFTvklLAAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAKElEQVQ4T2NkoDJgpLJ5DKMGUh6io2E4GoZkhMBosiEj0NC0"
                + "jMAwBABIxgAVO+SUsAAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAKElEQVQ4T2NkoDJgpLJ5DKMGUh6io2E4GoZkhMBosiEj0NC0"
                + "jMAwBABIxgAVO+SUsAAAAABJRU5ErkJggg==",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAWSURBVDhP"
                + "YxgFo2AUjIJRMAqoAhgYAAZUAAE6HI5PAAAAAElFTkSuQmCC")
    public void closePathTwice() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.closePath(); context.closePath(); context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAK9JREFUOE/tk7ERgkAQRR+1ENiDDdgA"
                + "qREVYCFSgZGpDdiAPRDYCy5z66zLyeGsIRcx/89/t3yWij+fyvAOwN3xrXYFns6vX9rRagpsgBOwd4EHcE5aD1yc3wIdcFPd"
                + "TqhhNf0lJX9iWuCvAH/BDCiCfcVSBTn/Y0IB6pTyLN29u0kdlfwZUHJDCu++bNSibzvUvKyKHL9Cq/wcMLTqGzBUX3axw8Tt"
                + "o4QrzP7LIeoI3pwhFeJpA+wAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAm0lEQVQ4T2NkoDJgJNK8BgLq4PIwA/mAGnYAsRWaxmNAvgcQ"
                + "uwFxPxDPRZNPBvILgXgNTBzZhSDNfUiSIUB2EZIlhOTBZiIbSKoB6BZgGAgSgCkCsZFdB/MRIXkUF4I0wVwJYiN7H2YgIXkM"
                + "A0Eab0J1q6NFAIyLVx5bsgG5AgTgMYdmMF55YtMhDsdiCo8aSHRQ4VQ4GoYjIQwBM+0eFQy1UnkAAAAASUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAm0lEQVQ4T2NkoDJgJNK8BgLq4PIwA/mAGnYAsRWaxmNAvgcQ"
                + "uwFxPxDPRZNPBvILgXgNTBzZhSDNfUiSIUB2EZIlhOTBZiIbSKoB6BZgGAgSgCkCsZFdB/MRIXkUF4I0wVwJYiN7H2YgIXkM"
                + "A0Eab0J1q6NFAIyLVx5bsgG5AgTgMYdmMF55YtMhDsdiCo8aSHRQ4VQ4GoYjIQwBM+0eFQy1UnkAAAAASUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAB2SURBVDhP"
                + "Y6A2YITShEADlMYF4PJMUJoPiI9BmCgAJAaSuwLEySABNAASA8lhBSDNIRAmGIDYyJYQkscApBqALo8VwBSha4YBQvIYAKYQ"
                + "phEdEJLHCm5CMS6AV54ZSiODF0C8D4ivgXmYgJD8KBgFo4DegIEBAO+EHxk/jOycAAAAAElFTkSuQmCC")
    public void closePathClosesOnlyLastSubpath() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.moveTo(2,2); context.lineTo(5,8); context.lineTo(8,2);"
                + "context.moveTo(10,2); context.lineTo(13,8); context.lineTo(16,2); context.closePath();"
                + "context.stroke();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAGZJREFUOE9jZNjH/J8BGTj9ZUThE+Kg"
                + "6WekuoH7GFBd6MRAmgvR9TNS3UB4EFEallCDEBFAbQMp9TrMp3AXUt1AQsmNWHnSEjERpo4aSEQgEVAyEsIQozykNNiobSBG"
                + "eUixA9EKaAA0fyYV+TYpjQAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAZklEQVQ4T2Nk2Mf8nwEZOP1lROET4qDpZ6S6gfsYUF3oxECa"
                + "C9H1M1LdQHgQURqWUIMQEUBtAyn1OsyncBdS3UBCyY1YedISMRGmjhpIRCARUDISwhCjPKQ02KhtIEZ5SLED0QpoADR/JhX5"
                + "NimNAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAZklEQVQ4T2Nk2Mf8nwEZOP1lROET4qDpZ6S6gfsYUF3oxECa"
                + "C9H1M1LdQHgQURqWUIMQEUBtAyn1OsyncBdS3UBCyY1YedISMRGmjhpIRCARUDISwhCjPKQ02KhtIEZ5SLED0QpoADR/JhX5"
                + "NimNAAAAAElFTkSuQmCC",
            IE = "no ctor")
    public void putImageDataInside() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "if (typeof ImageData != 'function') { log('no ctor'); return; }\n"
                + "      var arr = new Uint8ClampedArray(64);\n"
                + "      for (var i = 0; i < 32; i += 4) {\n"
                + "        arr[i + 0] = 0; arr[i + 1] = 190; arr[i + 2] = 3; arr[i + 3] = 255;\n"
                + "      }\n"
                + "      for (var i = 32; i < 64; i += 4) {\n"
                + "        arr[i + 0] = 190; arr[i + 1] = 0; arr[i + 2] = 3; arr[i + 3] = 255;\n"
                + "      }\n"

                + "      var imageData = new ImageData(arr, 4, 4);\n"
                + "      context.putImageData(imageData, 0, 0);\n"
                + "      context.putImageData(imageData, 2, 4);\n"
                + "      context.putImageData(imageData, 16, 0);\n"
                + "      context.putImageData(imageData, 16, 16);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAGtJREFUOE9jZNjH/J+BgYFhnxOIRAAn"
                + "hr+MqCLE8RipbuA+BogLyXURursZqW4g3AZoWML5TuSGIcwEahsI8zrMfHLDFJ40qG4gcamMsCqyEi8+Y0cNJBzohFSMhiGh"
                + "ECIsT70whBYuVDcQAEALHZVK+V18AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAaUlEQVQ4T2Nk2Mf8nwEI9jmBSARwYvjLiCpCHI+R6gbuY4C4"
                + "kFwXobubkeoGwm2AhiWc70RuGMJMoLaBMK/DzCc3TOFJg+oGEpfKCKsiK/HiM3bUQMKBTkjFaBgSCiHC8tQLQ2jhQnUDAUAL"
                + "HZUFr6Y/AAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAaUlEQVQ4T2Nk2Mf8nwEI9jmBSARwYvjLiCpCHI+R6gbuY4C4"
                + "kFwXobubkeoGwm2AhiWc70RuGMJMoLaBMK/DzCc3TOFJg+oGEpfKCKsiK/HiM3bUQMKBTkjFaBgSCiHC8tQLQ2jhQnUDAUAL"
                + "HZUFr6Y/AAAAAElFTkSuQmCC",
            IE = "no ctor")
    public void putImageDataOutside() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "if (typeof ImageData != 'function') { log('no ctor'); return; }\n"
                + "      var arr = new Uint8ClampedArray(64);\n"
                + "      for (var i = 0; i < 32; i += 4) {\n"
                + "        arr[i + 0] = 0; arr[i + 1] = 190; arr[i + 2] = 3; arr[i + 3] = 255;\n"
                + "      }\n"
                + "      for (var i = 32; i < 64; i += 4) {\n"
                + "        arr[i + 0] = 190; arr[i + 1] = 0; arr[i + 2] = 3; arr[i + 3] = 255;\n"
                + "      }\n"

                + "      var imageData = new ImageData(arr, 4, 4);\n"
                + "      context.putImageData(imageData, -2, 0);\n"
                + "      context.putImageData(imageData, 2, -2);\n"
                + "      context.putImageData(imageData, 2, 4);\n"
                + "      context.putImageData(imageData, 18, 18);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAF1JREFUOE/tkskJACAMBBPszMLTmSjR"
                + "jxBhPfap38AwDqtCfkrlWaodaJJqlvIMd84zZP4hHehwquES6Np+uG0aDOnAMCMbxpL3VoAb0oGHy8eGH4gK/IaoEL7TGzaf"
                + "lBRV53guuAAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAXklEQVQ4T+2SQQoAIAgEjX7Ww/tZVHoKCrZyj3pUGNZhk5An"
                + "UXk1dwNWyb1Ic8OV44asH9KBCqcmPAI1th5+nW4J6cCtRrMKtit3LcAO6cDH5uOEAUQGwiEyhO90hwOflBRVULNh6QAAAABJ"
                + "RU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAXklEQVQ4T+2SQQoAIAgEjX7Ww/tZVHoKCrZyj3pUGNZhk5An"
                + "UXk1dwNWyb1Ic8OV44asH9KBCqcmPAI1th5+nW4J6cCtRrMKtit3LcAO6cDH5uOEAUQGwiEyhO90hwOflBRVULNh6QAAAABJ"
                + "RU5ErkJggg==",
            IE = "no ctor")
    public void putImageDataDirty() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "if (typeof ImageData != 'function') { log('no ctor'); return; }\n"
                + "      var arr = new Uint8ClampedArray(64);\n"
                + "      for (var i = 0; i < 32; i += 4) {\n"
                + "        arr[i + 0] = 0; arr[i + 1] = 190; arr[i + 2] = 3; arr[i + 3] = 255;\n"
                + "      }\n"
                + "      for (var i = 32; i < 64; i += 4) {\n"
                + "        arr[i + 0] = 190; arr[i + 1] = 0; arr[i + 2] = 3; arr[i + 3] = 255;\n"
                + "      }\n"

                + "      var imageData = new ImageData(arr, 4, 4);\n"
                + "      context.putImageData(imageData, 0, 0, 1, 2, 1, 1);\n"
                + "      context.putImageData(imageData, 4, 4, 0, 2, 2, 2);\n"
                + "      context.putImageData(imageData, 8, 8, 0, 0, 2, 2);\n"
                + "      context.putImageData(imageData, 18, 0, 1, 1, 2, 3);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAYxJREFUOE+t1L9LV2EUx/HXl2gI+gPS"
                + "piBqMsFRoiGiEIKWIFAicNEahMIpUKPNxcYMoogIGhwFrSkQFUchiqBJlPYSDYf0OXCvPN2uelOf7d7nPO/z63NOyzGfVsa7"
                + "gQ8Vfvw7iemmfkvgbTxCd+XhV5xFF743geYRLmACU+jEIG4ioOcQTpcPgubAePAUq+jACXzCF/ShHfN4hpm9wDnwCR7jOeZq"
                + "ShD/VnAVLxIw7P85JTAurxew+4VVmX756DwW8QAPU70/1kED2INXuIBf+FYQLtYEEDV9jWuYTRH3V9MPYHh6g3cFIKQSpyqh"
                + "kj+MW5jEvSKzXd8B/JFq0pZFs31QJ7P7jUJqu90P4FvcPSRwE0N4Wb4PYDQk79j/RBi279F7XMDfGM8DOmrKawXsr5SP0pR1"
                + "XM5Hsk42TWu4lTS8hCu5KuqE3RQYQ3CnTtjhoBy9EPXPBjoMWCyJsaptdTkMpAk4sw/wD0J7ARvZbzmUdzHXo7iUNk/U6FT6"
                + "DkhEdDrtxM9FNnuurx0UblcV1Fz/YwAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAABiElEQVQ4T62UTStHQRSH/RdkIRtLRUlslFIWLKQsWIjEJ/Cy"
                + "sUCSslI2skFZUN6+ALKwULL0XvKykFLiM1DewvOrMxrj3v4X99TT3Jk585uZe86ZTE7Klsmi18X8E2wl3dcJFrJgG+qDhVf0"
                + "i6EGbpKI+ifcZ8E0rEEujEAfnEEZ1MJrNlFfUNcbgzvogEc4hFvrF9FuwLD5RGr7gr14LNgJN2kHgl+wR/8B6kx0KUrRCUps"
                + "Biah1Rzd9d26cj4OYArGYQiWQ1EJlsIlNNuCa3OqjDiBNluFfhOror33/SS4DucwYRP6lzIFJ8oUrHbYgWroDAXfGciHF5v4"
                + "iBGKG1ZGvLlJnfAUlGfOfiOopG+EI19wkY7y7S+C2rwNvipJJ/yPoBJ9EObTurLysgmOfcHUgxKmTdKgKCAn0OCH3yX2BYMt"
                + "oEpIKviMbwX8SGxt0AOzoGpRzWYz5Z0eibnQ0X8cJKr61dsYZ7qmbDRKTBPhi13C2AoocopgngmoigpgF7rh2zX93T8BvvJW"
                + "FWV1b98AAAAASUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAABfUlEQVQ4T62UTytFQRiH3UIWsrG0sKDs3JKFLKSULETiG8jm"
                + "LtBNykrZyEbKQkK+ALKwULL07yphI6XwHST/wvOrmdsYM53DvVNP5547M8/MnPd9J1NR5pZJ8I3Q/wJ7ade1wjom7EOnN/GG"
                + "9wZog7s0UneHx0xYhC2ohCkYg0tognb4SJK6Qh1vBh5gCJ7hFO7Nez3PHcjDY0zsCkcZtGp2uMtz3PsER7w/QYeRboSkVijZ"
                + "EsxDvxloj2/nNfPjBBZgFibhl1TCRriGPjPh1hhaAjvQYpuQg3Vo9Y8v4TZcwZwR6FuqKTihpmANwgFkYdgdJOEn1MCb6fiK"
                + "iGJ/V9FRjL6EF6A8s+0vQiV9N5zZyRKugfLtP0ItPgDFSipV+I5sAlbcHZZyZOVlDxRcYdmD4qdN2qAoIOfQ5YY/lNhpha+I"
                + "lPw/6tovvV4G6NZJaso7XRLL/kD/clD96m6MNR1TbTokU4d/Y6uuVfCKnCJYbQSqolo4BF0k0evrG77KVhVrq+aXAAAAAElF"
                + "TkSuQmCC",
            IE = "no ctor")
    public void clip() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "if (typeof ImageData != 'function') { log('no ctor'); return; }\n"
                + "context.moveTo(2,2); context.lineTo(5,8); context.lineTo(8,2);"
                + "context.arc(8, 12, 8, 0, 2 * Math.PI); context.stroke();"
                + "context.clip(); context.fillRect(4, 9, 19, 14);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAK1JREFUOE9jZKAyYKSyeQw4DfwvMsme"
                + "4T9THOPbnGRSLMVj4OT/IIMY3+SS5ItRAxHB/19k2IchzIswT8OSC7o4rjQJUw9PNv9FJt9lYGBQItPAe4xvcpXB6RZmwH/h"
                + "KXMZGP8nkWXgf8Z5sByF5MJJ9gwMjAfIMpDhvwPjm7yDKC4Ecf6LTJ7FwMCQipzliAjD2YxvctPgDsEVyEMhHVK5+CKlDERW"
                + "S1JZR4wlAO50XRUscFdZAAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAArUlEQVQ4T2NkoDJgpLJ5DDgN/C84UYeBmTmI8U1OEymW4jZQ"
                + "ZPJ/kEGMb3JJ8sWogYjg/z/8wxDoxadAD0vBPA1LLjCvE0iLz4DqpcHJDKbwv8ikTUCuL3kG/t/M+CbPD83AKXUMDP8byTOQ"
                + "sR6WoxAuBGc1pstkGfj3ny7j+/wrKC4Ecf6LTCkCkr3IWY5wGDIWA13XB3cIrsAeAumQ2sUXKWUgslqSyjpiLAEAdMteFUCj"
                + "kQkAAAAASUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAxElEQVQ4T2NkoDJgpLJ5DDgN/C861YDh/18Hxjd5E0ixFLeB"
                + "IpPfAw0SYHyTS5Iv8Bn4H+SyUQMJxs8QCsP/wlPuMzD+V4D5CRa7/0Umg2ObAPgAVC8IThUwhUAD5wMNTCDTwANAAx1RDRSZ"
                + "2MDAwFRPnoGMExjf5BSiGijQr8DAwnKfDAM/APUEAl14AMVAEOe/yKQCYCj0I+cQwmH4r5HxTT7QdxAwhJINerKAFF9/ApC9"
                + "Qyjt4PUyMZqxqSGprCPGEgABn2EVn+gHngAAAABJRU5ErkJggg==",
            IE = "no ctor")
    public void clipWindingEvenOdd() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "if (typeof ImageData != 'function') { log('no ctor'); return; }\n"
                + "context.rect(6, 2, 2, 16); context.rect(2, 10, 16, 5); context.clip('evenodd');"
                + "context.beginPath(); context.arc(10, 10, 8, 0, 2 * Math.PI);"
                + "context.fillStyle = 'deeppink';context.fill();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAO5JREFUOE/t1K9KREEUx/HP1C2CCIIg"
                + "GxbBJhiFxSb6Dgb/sAbBIBpkLSLYfIbV4isYhX0Cg8lkMG/VYLg66ywOV1gvF+P9lQPDme/85pwzE3CB9a8YNUyxdghpZ4RG"
                + "TWIDrF6BrIY7i9yMsIEVbBPuclTx3bwzbKb1/cAgzykDP3CMFq5wToiHjJWAB+ihjSOcBt4mOWXgw4+rIoLvCc8lYCe6KpiL"
                + "MFxOA1Zx+CcwDfbWDIfvrHZYmGd3wO1rXp8e7WVmT3hconXN2h7DEdHIWPHKGbD7Qv+pek9/Z06pYT1s8/Sa36bG5Pz72HwC"
                + "6OxElc5l3LEAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA20lEQVQ4T2NkYGBoAGJ7IAaBg1CabIoRqhNkKAjA6FEDiQ8B"
                + "pDCcbcHAkPIBqDUcqr2QgYFxArJR/xkYCoD8ECC2hor3Ag0oQVaDbqAW0BA5iIL/QP2MMHmICNRAoKANkG0EFNoAZEPVQ3Rh"
                + "cSFjBNTAI0A6D6jkHMwFUAMtgJrAaoD8R4QMJMaFBA0EpT1gwm6SYWCIZmdgUJKFuGjOTgaG1BPI4TObgcFCn4FBxoyBYQ1I"
                + "/D0DQ6EgA0M/ehgiGejxk4HBDKyYXIAnDMkzcjTrjZY2ZKQcqicbAN2xPZWYt5iSAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA20lEQVQ4T2NkYGBoAGJ7IAaBg1CabIoRqhNkKAjA6FEDiQ8B"
                + "pDCcbcHAkPIBqDUcqr2QgYFxArJR/xkYCoD8ECC2hor3Ag0oQVaDbqAW0BA5iIL/QP2MMHmICNRAoKANkG0EFNoAZEPVQ3Rh"
                + "cSFjBNTAI0A6D6jkHMwFUAMtgJrAaoD8R4QMJMaFBA0EpT1gwm6SYWCIZmdgUJKFuGjOTgaG1BPI4TObgcFCn4FBxoyBYQ1I"
                + "/D0DQ6EgA0M/ehgiGejxk4HBDKyYXIAnDMkzcjTrjZY2ZKQcqicbAN2xPZWYt5iSAAAAAElFTkSuQmCC",
            IE = "no ctor")
    @HtmlUnitNYI(FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAcElEQVR42mNgYGD4T2UMBygcaoCRaeB/EJ4NxO+AOBCPopVA"
                + "/AWIA4gxEKjovw8QX8ajyAeIg4D4FzEGsgExOxB/waOIGcYmxkAPIPYG4nO4FGFj4zNwGRC/BGJ3cg1ESuX/qZ1T/v8fzSmj"
                + "Bg4HAwGGsHedCdecqgAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAcElEQVR42mNgYGD4T2UMBygcaoCRaeB/EJ4NxO+AOBCPopVA"
                + "/AWIA4gxEKjovw8QX8ajyAeIg4D4FzEGsgExOxB/waOIGcYmxkAPIPYG4nO4FGFj4zNwGRC/BGJ3cg1ESuX/qZ1T/v8fzSmj"
                + "Bg4HAwGGsHedCdecqgAAAABJRU5ErkJggg==")
    public void fillTextAndTransform() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "if (typeof ImageData != 'function') { log('no ctor'); return; }\n"
                + "context.moveTo(0, 0);\n"
                + "      context.lineTo(20, 0);\n"
                + "      context.moveTo(2, 0);\n"
                + "      context.lineTo(2, 20);\n"
                + "      context.moveTo(0, 10);\n"
                + "      context.lineTo(20, 10);\n"
                + "      context.stroke();\n"

                + "      context.fillStyle = 'blue';\n"
                + "      context.fillText('p', 2, 10);\n"

                + "      context.fillStyle = 'red';\n"
                + "      context.setTransform(1.0, 0.0, -0.0, 1.0, 11.0, 10.0);\n"
                + "      context.fillText('n', 0, 0);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAJBJREFUOE+t1FsOgCAMRNFhc7pmXZ0p"
                + "CQSkj6GhP4YQL+erBYenHO5hDD4AruQDL4Bb/h2DEpNoZiQm0Sko54yy67RgRtl1WnBXOems4I5y0llBVrnovCCjXHReMFKq"
                + "uijoKVVdFLSUpo4JakpTxwT/SlfHBkelq2ODTSnfulG8YfdhW2t1o5wIRp1+zwrp4Ac9kx4V/3bxXQAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAmUlEQVQ4T63U0Q2AIAxFUdjAUVzBzd3AVRzBPhMJSFseDU1M"
                + "NITr4YecFk9e3Et18JL4HvzBKfsO7K2DiCEaGcQQbYL4jiiLTgtGlEWnBWeVjc4KzigbnRVklZ3OCzLKTucFR0pVNwp6SlU3"
                + "ClpKU8cENaWpY4J/patjg7XS1bHBT3nLy3ujeMPeh1Bu8uDIS4KjTllnhXTwAVWiHxW3nihqAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAmUlEQVQ4T63U0Q2AIAxFUdjAUVzBzd3AVRzBPhMJSFseDU1M"
                + "NITr4YecFk9e3Et18JL4HvzBKfsO7K2DiCEaGcQQbYL4jiiLTgtGlEWnBWeVjc4KzigbnRVklZ3OCzLKTucFR0pVNwp6SlU3"
                + "ClpKU8cENaWpY4J/patjg7XS1bHBT3nLy3ujeMPeh1Bu8uDIS4KjTllnhXTwAVWiHxW3nihqAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABvSURBVDhP"
                + "1c5BCoAwDETRHMkjeHOP4JE0XXwoGNMkLYgfZjeLJ7/q1F3FHbpHm846R7brzCpKU0cV5auOMkpXRxnlUEcRZUhHEWVYR54y"
                + "pSNPmdaRpSzpyFKWddQrp3TUK6d11JRLdNSUy3RfJnIDH55/ge/XbBAAAAAASUVORK5CYII=")
    public void pathFill() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "context.moveTo(2, 2);\n"
                + "      context.lineTo(10, 18);\n"
                + "      context.lineTo(18, 2);\n"
                + "      context.closePath();\n"
                + "      context.fill();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAQxJREFUOE+11M8qRVEUx/HPfRMTychE"
                + "KEJKGchUUqKYmLolioFS8gIGDIgkMwNlYsRAeQhFEV6CXffUdjp/9qlrT3at9eu79vq3W7p8Wl3miYFPGE4McI6lIm0M3MFe"
                + "AvATQ3itAw7iOQG4jqMyXb6GDxirgN5itipoHriJg4RX5iVnWA7GPDA0JTSnyQm1nMRLETDY7jHVgLiGk0xfNIfb2E8EXmIx"
                + "1hYBR/GYAHzHCN7qgHHad781nSmB/0m1KuXg28UGJnCI6Rz0GvN1gx37x9GHY6x27sz/jQF8NAHG2p5sJDrGFZymbkqZ7gZz"
                + "uMJCk00p07axhX58dQMYNqgXF3Xj9K8fbF3wJP8PpH0mFcFq5PIAAAAASUVORK5CYII=",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAzElEQVQ4T7WU0Q2DQAxDYQNGKJt0BTZgBDpJO0pH6AaMwAgw"
                + "AonUQyHEnE8CJMQH1ruz5aSuLn7qi3mVBU4Cf5AHfEXXRVoLfItgIICLaFp59Xt4LLCRvzMBfInmg3Q+w5xtaDUd4IGsbX/B"
                + "7SAPZG1b4C7TqDY52/52u0wjYIntQ6YRkLUd1gdNSrL9E39PUJGwPgiotvt/gcdggqhJsRdR2wrUAvtM6UlB5feZFk0KgqZM"
                + "iycFAW2m4VJAo3dmO2V6uj9uXbDE5spLVqfJJhVVgK1QAAAAAElFTkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAzElEQVQ4T7WU0Q2DQAxDYQNGKJt0BTZgBDpJO0pH6AaMwAgw"
                + "AonUQyHEnE8CJMQH1ruz5aSuLn7qi3mVBU4Cf5AHfEXXRVoLfItgIICLaFp59Xt4LLCRvzMBfInmg3Q+w5xtaDUd4IGsbX/B"
                + "7SAPZG1b4C7TqDY52/52u0wjYIntQ6YRkLUd1gdNSrL9E39PUJGwPgiotvt/gcdggqhJsRdR2wrUAvtM6UlB5feZFk0KgqZM"
                + "iycFAW2m4VJAo3dmO2V6uj9uXbDE5spLVqfJJhVVgK1QAAAAAElFTkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACUSURBVDhP"
                + "1ZLBCYAwDEULLuAIHl3DjdzEURzBURzBqzf9gQZKSMkPnvzwKIi2fd+UX+UED8kOwmzA+9hygRGEkZe8DSwroBNpU6ptWG1L"
                + "9yBWuyXsNPO3hbDTjDbVKatNj49EtY+6eqTGR7T1Bl6n6fGRjfQGttOUqhfbaUq1F9UOVYe6RpnADBZwy4OvaTv9dUp5Afx3"
                + "gfLtY6izAAAAAElFTkSuQmCC")
    public void pathFillTransform() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "      context.moveTo(2, 2);\n"
                + "      context.lineTo(6, 14);\n"
                + "      context.lineTo(14, 2);\n"
                + "      context.closePath();\n"

                + "      context.setTransform(1.0, 0.0, 0.0, 1.0, 4.0, 4.0);\n"
                + "      context.moveTo(2, 2);\n"
                + "      context.lineTo(6, 14);\n"
                + "      context.lineTo(14, 2);\n"
                + "      context.closePath();\n"

                + "      context.fill();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAOZJREFUOE+tlNENwjAMRC/zVBVD8MkM"
                + "LMASMExX4AuGYA+GCLrIjYzrYlc0P0hteL13cVuw8yo786CBLwCH5AMmAGdvrwbeAFwTwDeAAQB/F0sDmY4po8VkTOgu22Gk"
                + "vao60y2wa0dkE28q0qkFdu1sodLlUKRTb2xauGyhPO2iOvWAW7S76lqHvJ7VbuMzq/4C8l7TPgL3J3BamZAv1QhI7QsTVODh"
                + "vEEL1QhI7ZEDXAF74K5qBOyWVXUqF13VNJAbq3TKxPMAZ189d59ot07tqdo/pL6Hoj3qAf4rYfT50fdTCbcAP2uOMhXdBSkP"
                + "AAAAAElFTkSuQmCC",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA7UlEQVQ4T62UwQ3CMAxF3Q24caXzcGIDRqCT0FF6YY6OwJUb"
                + "bADfVhOlrk0MSqWqUvL1nP/ttKPGT9eYRyXwDvghWGCC7mRpS+AVgksA+IKmx8vfzVMCd9h9BoADNKOn0xnWbLtWUwENzLZr"
                + "ZHXCCSDJVAOz7WigS5Y9QJKpNTZyuGig0A6A5Ewt4C+2s1UvQ16P2pbxSVa/AXlPbO+Jbg+iozMiK6s1INs+8wneRDPDva7q"
                + "Yt5dZtsMHAHUDTet1k6YCwOoG25aDQNZCGia801Xo5ZXusW2ZKq7+i9QMi0H2Om8eVM8bWi9+R/7A246MhXPMagrAAAAAElF"
                + "TkSuQmCC",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA7UlEQVQ4T62UwQ3CMAxF3Q24caXzcGIDRqCT0FF6YY6OwJUb"
                + "bADfVhOlrk0MSqWqUvL1nP/ttKPGT9eYRyXwDvghWGCC7mRpS+AVgksA+IKmx8vfzVMCd9h9BoADNKOn0xnWbLtWUwENzLZr"
                + "ZHXCCSDJVAOz7WigS5Y9QJKpNTZyuGig0A6A5Ewt4C+2s1UvQ16P2pbxSVa/AXlPbO+Jbg+iozMiK6s1INs+8wneRDPDva7q"
                + "Yt5dZtsMHAHUDTet1k6YCwOoG25aDQNZCGia801Xo5ZXusW2ZKq7+i9QMi0H2Om8eVM8bWi9+R/7A246MhXPMagrAAAAAElF"
                + "TkSuQmCC",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACjSURBVDhP"
                + "1ZLBDYAgDEU7gifndBNG8eI+jMAG6i8JpKkNFPWgL/mBA/36CPQrInI4syJdAmIN6yRkQrrwIatAZ0Hc9LRdqpKqjeYDG1f2"
                + "xoeqNprNYR2UJazNO83aOGEW6KCwe6du7ZaqxKXtUZVk7Zlo00UlHlUJa+cHjMFolA0/H1bJf4DhoMqGVC/wsCocUjUp2ndU"
                + "TVj7saqEi15R/QBEJzGv1JNAAQutAAAAAElFTkSuQmCC")
    public void pathFillTransform2() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "      context.beginPath();\n"
                + "      context.moveTo(2, 2);\n"
                + "      context.lineTo(6, 14);\n"
                + "      context.lineTo(14, 2);\n"
                + "      context.closePath();\n"
                + "      context.fill();\n"

                + "      context.setTransform(1.0, 0.0, 0.0, 1.0, 4.0, 4.0);\n"
                + "      context.fillStyle = 'red';"
                + "      context.beginPath();\n"
                + "      context.moveTo(2, 2);\n"
                + "      context.lineTo(6, 14);\n"
                + "      context.lineTo(14, 2);\n"
                + "      context.closePath();\n"
                + "      context.fill();\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAHVJREFUOE/Vk8EOwFAEBNef+3M9lDY0"
                + "mhIOfccnmYwFYfjRMA9/BDLExcC1Lp4tbwGF/bgI30xTwzmgignOLClECtDrZqTFceCdoPipdw0XgX7aFoX9xumXT28NSGFP"
                + "oRfVNhwDXonGE9VC2XAPaORg2jdMgAdeyiQRQvsTZgAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAeklEQVQ4T9WTUQ7AQAREubmbqzS0pdHsCh/dT5K3YwaE5ofN"
                + "PPgjkICdDbQ3xXvkKSCTj0t+XvI7VdgHVGFi5OklBkul8qk0bbYDbwfZp15VOAj0aZsVVo3pL63CEzkGxLCnoBdVVtgGvMaP"
                + "J6qNbYVzQCMHpXWFCfAAXsokEVEKzTkAAAAASUVORK5CYII=",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAeklEQVQ4T9WTUQ7AQAREubmbqzS0pdHsCh/dT5K3YwaE5ofN"
                + "PPgjkICdDbQ3xXvkKSCTj0t+XvI7VdgHVGFi5OklBkul8qk0bbYDbwfZp15VOAj0aZsVVo3pL63CEzkGxLCnoBdVVtgGvMaP"
                + "J6qNbYVzQCMHpXWFCfAAXsokEVEKzTkAAAAASUVORK5CYII=",
            IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAABpSURBVDhP"
                + "7ZPRCsAgCEV1X+6fO2ladGkxWDQfdl7MEDmI0s9r2GNDSP11IYOaCYfHZdwaqpSsYoWPTDcYOqZZTBlGaj9T032GDQXFfIY9"
                + "MdvAGnQ98hgy7Glc1PeGFbx5J5FhAKaLDYlOKXoSF+jZIBEAAAAASUVORK5CYII=")
    public void saveRestore() throws Exception {
        draw("<canvas id='myCanvas' width='20', height='20' style='border: 1px solid red;'></canvas>\n",
                "      context.fillStyle = 'green';\n"
                + "      context.save();\n"
                + "      context.fillRect(4, 4, 4, 4);\n"

                + "      context.fillStyle = 'red';\n"
                + "      context.fillRect(6, 6, 4, 4);\n"
                + "      context.save();\n"

                + "      context.fillStyle = 'blue';\n"
                + "      context.fillRect(8, 8, 4, 4);\n"

                + "      context.restore();\n"
                + "      context.fillRect(12, 12, 4, 4);\n"

                + "      context.restore();\n"
                + "      context.fillRect(14, 14, 4, 4);\n"

                + "      context.restore();\n"
                + "      context.fillRect(16, 16, 4, 4);\n");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAAXNSR0IArs4c6QAAAL1JREFUOE9jZEAFjgwMDOZQoZMMDAz7"
                + "0eQJchmhKnhFGBi2CDAw6AcwMHCDxDYwMHz9wMBw8Q0Dgw8DA8NngiZBFYANFGFgOJjAwGDVzcDAgqyxlIHhzwIGhmNvGBjs"
                + "STHQUYWBYf1tBgZ+bJpUGRg+3mFgCCTW+yAXVpQwMDSjuw5mOMiVPQwMtQwMDB3EuJImBlLdy1SPFFDQUDfZIAU21RI2MRFI"
                + "lBpYTiFKMTGKRg0kJpTwqxkNQ8rDEABatjIVyjXhJwAAAABJRU5ErkJggg==",
            FF = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA4klEQVQ4T+2STQ4BQRCFPyT+V27AESQyC5cwQqzY2zuGvT0b"
                + "QnAJCyGO4AhiIRgJUU1P4mdkesJSbzpV/frlvaoX4vlYUuZ1ayX3/OXdtwxpRCIDvR2UauCoZh9iSZhuoS7lwZdJA26EQjbM"
                + "QXkB4cePBbisYbKBShBCKwozByJen2JwPkHR1L5S2BRP7S6Iw/fTgH0PUkEUmhC2hLBjQqoU/tyyWsooC/bSeyljWUrVRJ3C"
                + "uLGJ69jYKjbqYSCxSd83rGJzDEro4n8WbFMBvjjXsi/QFPAnNJ3UZ9x/ht/P8AqUpzEVnisiKgAAAABJRU5ErkJggg==",
            FF_ESR = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAA6ElEQVQ4T+2SzQoBURiGH5F/NyIrKQv3YOOnJpStnetwA1aU"
                + "klixcgcWJLdgx05ZYTLlO2OmmGiOWDqb03e+9zy930+A55OXMOs8beReevK+YcBRxGLQu0KlBMcQhCcQT8HsAA3RnHxJjsAG"
                + "CmyUhuoago8fc2Bt79DyJ8B8GBYXD8wFRARqQkG3fOWwVYPOEJKvXDTBHIBw9Y4usC24rg5SAX9esj2UDBgrjwU1lD3Md1DU"
                + "cac07tpEBdq3ZNKGQFRiDKEETGXCdQnPnwJd/c8WW9eAr84t2VeoK/gDdTv1Xvfv4fc9vAGT7zEVBadocQAAAABJRU5ErkJg"
                + "gg==",
            IE = {})
    @HtmlUnitNYI(IE = "data:image/png;base64,"
                + "iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAzUlEQVR42u2SvwpBYRyGn8Eiyqos/kxGVrkDo8VkJYNcgUtwD"
                + "crA7M+CLMoFGJQTirIapAgd78mmQ32nbOetZ/p+Pf2+7/3gX9lBvAGLHJxjcMvANgpVT7I9pPJwL4A9EZLbA5GFaxB6xsIKrI"
                + "oS2B/cRRKOGikbCdPwWLoIHbqw0UjfdElXmcMWLJ0fjGwq4WJ9EbZhqJGxkTABnRo8XYSn0FtWN71yWG3uSip8rW0d0RymERj"
                + "pbCYCXn5PWLR4v5mcqCeaXmV+/Pj5lRf9o2ZS6A1d/wAAAABJRU5ErkJggg==")
    public void imageOnLoad() throws Exception {
        final String html = "<html><head>\n"
            + "<script>\n"
            + LOG_TEXTAREA_FUNCTION
            + "  function test() {\n"
            + "    var img = new Image();\n"
            + "    img.onload = function() {\n"
            + "      var canvas = document.createElement('canvas');\n"
            + "      canvas.width = 20;\n"
            + "      canvas.height = 20;\n"
            + "      var context = canvas.getContext('2d');\n"
            + "      context.drawImage(img, 0, 0);\n"

            + "      log(canvas.toDataURL());\n"
            + "    }\n"

            + "    img.src = 'data:image/svg+xml,"
                    + "<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.w3.org/2000/svg\" "
                            + "overflow=\"hidden\" width=\"10\" height=\"10\">"
                    + "  <circle cx=\"5\" cy=\"5\" r=\"4\" stroke=\"black\" stroke-width=\"1\" fill=\"red\" />"
                    + "</svg>';"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + LOG_TEXTAREA
            + "</body></html>";

        loadPageVerifyTextArea2(html);
    }
}
