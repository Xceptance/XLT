/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package scripting.modules;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.Open_popup_w2;
import scripting.modules.SelectWindow_popup_w2;
import scripting.modules.SelectFrame_iframe_1;
import scripting.modules.SelectFrame_iframe_12;
import scripting.modules.SelectFrame_iframe_123;

/**
 * TODO: Add class description
 */
public class assertTextPresent extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        //
        // ~~~ link ~~~
        //
        startAction("link");
        assertTextPresent("anc_sel2");
        //
        // ~~~ whitespaces ~~~
        //
        startAction("whitespaces");
        assertTextPresent("This text contains just single spaces");
        assertTextPresent("This text contains multiple spaces");
        assertTextPresent("This text contains single tabulators");
        assertTextPresent("This text contains multiple tabulators");
        assertTextPresent("This text contains line breaks");
        assertTextPresent("This text contains single HTML encoded spaces");
        assertTextPresent("This text contains multiple HTML encoded spaces");
        assertTextPresent("This text contains alternating spaces");
        assertTextPresent("This text contains 274 spaces in row");
        assertTextPresent("This text contains mixed white spaces");
        assertTextPresent("This text contains paragraph tags.");
        assertTextPresent("This text contains HTML encoded line breaks.");
        assertTextPresent("This text contains many div tags.");
        assertTextPresent("Each word has its own div.");
        //
        // ~~~ glob_RegEx ~~~
        //
        startAction("glob_RegEx");
        assertTextPresent("Lorem ipsum * dolor sit amet");
        assertTextPresent("Lorem ipsum ??? dolor sit amet");
        assertTextPresent("regexp:Lorem ipsum [XYZ]{3} dolor sit amet");
        assertTextPresent("regexp:^.* [XYZ]{3} .*$");
        assertTextPresent("exact:Lorem ipsum XYZ dolor sit amet");
        assertTextPresent("glob:Lorem ipsum ??? dolor sit amet");
        //
        // ~~~ matchingStrategy ~~~
        //
        startAction("matchingStrategy");
        assertTextPresent("glob::");
        assertTextPresent("regexp:");
        assertTextPresent("glob:");
        assertTextPresent("exact:");
        assertTextPresent("*Lorem ipsum*");
        //
        // ~~~ empty ~~~
        //
        startAction("empty");
        assertTextPresent("");
        assertTextPresent("                       ");
        //
        // ~~~ keyspace ~~~
        //
        startAction("keyspace");
        assertTextPresent("glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        assertTextPresent("glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        assertTextPresent("glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        //
        // ~~~ pangram ~~~
        //
        startAction("pangram");
        assertTextPresent("Zwei flinke Boxer jagen die quirlige Eva und ihren Mops durch Sylt. Franz jagt im komplett verwahrlosten Taxi quer durch Bayern. Zwölf Boxkämpfer jagen Viktor quer über den großen Sylter Deich. Vogel Quax zwickt Johnys Pferd Bim. Sylvia wagt quick den Jux bei Pforzheim. Polyfon zwitschernd aßen Mäxchens Vögel Rüben, Joghurt und Quark. \"Fix, Schwyz! \" quäkt Jürgen blöd vom Paß. Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich. Falsches Üben von Xylophonmusik quält jeden größeren Zwerg. Heizölrückstoßabdämpfung.");
        assertTextPresent("Zwei flinke Boxer jagen die quirlige Eva und ihren Mops durch Sylt. Franz jagt im komplett verwahrlosten Taxi quer durch Bayern. Zwölf Boxkämpfer jagen Viktor quer über den großen Sylter Deich. Vogel Quax zwickt Johnys Pferd Bim. Sylvia wagt quick den Jux bei Pforzheim. Polyfon zwitschernd aßen Mäxchens Vögel Rüben, Joghurt und Quark. \" Fix, Schwyz! \" quäkt Jürgen blöd vom Paß. Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich. Falsches Üben von Xylophonmusik quält jeden größeren Zwerg. Heizölrückstoßabdämpfung.");
        //
        // ~~~ format_bold ~~~
        //
        startAction("format_bold");
        assertTextPresent("aaa bbbb ccc");
        assertTextPresent("bb ccc");
        //
        // ~~~ format_underline ~~~
        //
        startAction("format_underline");
        assertTextPresent("aaa bbbb cccc");
        assertTextPresent("bb ccc");
        //
        // ~~~ format_italic ~~~
        //
        startAction("format_italic");
        assertTextPresent("bb cccc dd");
        assertTextPresent("aa bbbb ccc");
        //
        // ~~~ format_mixed ~~~
        //
        startAction("format_mixed");
        assertTextPresent("bb ccc");
        assertTextPresent("aa bbbb cccc dddd eeee ffff gggg hhh");
        //
        // ~~~ format_lineBreaks ~~~
        //
        startAction("format_lineBreaks");
        assertTextPresent("aaaaa bbbb cccc dddd eeee ffff gggg hhhh");
        //
        // ~~~ format_table ~~~
        //
        startAction("format_table");
        assertTextPresent("aaaaa bbbb cccc dddd eeee ffff gggg hhhh iiii kkkk mmmm nnnn oooo pppp rrrr ssss");
        assertTextPresent("dd eeee ffff gggg hhhh iiii kkkk mmmm nnnn oooo pppp rrr");
        //
        // ~~~ popup ~~~
        //
        startAction("popup");
        final Open_popup_w2 _open_popup_w2 = new Open_popup_w2();
        _open_popup_w2.execute();

        final SelectWindow_popup_w2 _selectWindow_popup_w2 = new SelectWindow_popup_w2();
        _selectWindow_popup_w2.execute();

        selectFrame("index=0");
        assertTextPresent("This is frame 1.");
        close();
        //
        // ~~~ iframe1 ~~~
        //
        startAction("iframe1");
        final SelectFrame_iframe_1 _selectFrame_iframe_1 = new SelectFrame_iframe_1();
        _selectFrame_iframe_1.execute();

        assertTextPresent("This is iframe 1.");
        //
        // ~~~ iframe2 ~~~
        //
        startAction("iframe2");
        final SelectFrame_iframe_12 _selectFrame_iframe_12 = new SelectFrame_iframe_12();
        _selectFrame_iframe_12.execute();

        assertTextPresent("This is iframe 2.");
        //
        // ~~~ iframe3 ~~~
        //
        startAction("iframe3");
        final SelectFrame_iframe_123 _selectFrame_iframe_123 = new SelectFrame_iframe_123();
        _selectFrame_iframe_123.execute();

        assertTextPresent("This is iframe 3.");

    }
}