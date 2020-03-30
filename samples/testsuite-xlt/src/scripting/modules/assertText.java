/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
public class assertText extends AbstractWebDriverModule
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
        assertText("name=anc_sel1", "anc_sel1");
        assertText("link=anc_sel1", "anc_sel1");
        assertText("link=*_sel1", "anc_sel1");
        assertText("dom=document.getElementById('anc_sel1')", "anc_sel1");
        assertText("css=#anc_sel1", "anc_sel1");
        assertText("id=anc_sel7", "*sel7");

        //
        // ~~~ whitespaces ~~~
        //
        startAction("whitespaces");
        assertText("css=span#ws1_single_ws", "This text contains just single spaces.");
        assertText("id=ws1_multiple_ws", "This text contains multiple spaces.");
        assertText("id=ws1_single_tab", "This text contains single tabulators.");
        assertText("id=ws1_multiple_tab", "This text contains multiple tabulators.");
        assertText("id=ws1_line_break", "This text contains line breaks.");
        assertText("id=ws1_single_html_spaces", "This text contains single HTML encoded spaces.");
        assertText("id=ws1_multiple_html_spaces", "This text contains multiple HTML encoded spaces.");
        assertText("id=ws1_alternating_spaces", "This text contains alternating spaces.");
        assertText("id=ws2_274", "This text contains 274 spaces in row.");
        assertText("id=ws2_mixed_spaces", "This text contains mixed white spaces.");
        assertText("id=ws2_spaces_only", "");
        assertText("id=ws2_spaces_only", "          ");
        assertText("id=ws2_html_spaces_only", "");
        assertText("id=ws2_html_spaces_only", "          ");
        assertText("id=ws3_paragraph", "This text contains paragraph tags.");
        assertText("id=ws4_br", "This text contains HTML encoded line breaks.");
        assertText("id=ws5_a", "This text contains many div tags.");
        assertText("id=ws7_div", "Each word has its own div.");

        //
        // ~~~ glob_RegEx ~~~
        //
        startAction("glob_RegEx");
        assertText("id=specialchar_1", "Lorem ipsum * dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "Lorem ipsum ??? dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "regexp:Lorem ipsum [XYZ]{3} dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "regexpi:lorem IPSUM [XYZ]{3} dolor SIT amet, consectetueR adipiscinG elit.");
        assertText("id=specialchar_1", "regexp:^.* [XYZ]{3} .*$");
        assertText("id=specialchar_1", "regexpi:^.* [xyz]{3} .*$");
        assertText("id=specialchar_1", "exact:Lorem ipsum XYZ dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "glob:Lorem ipsum ??? dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=sc_s2_2", "glob:*:*");

        //
        // ~~~ keyspace ~~~
        //
        startAction("keyspace");
        assertText("id=sc_s2_1", "glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        assertText("xpath=id('sc_s2_2')/pre[1]", "glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        assertText("id=sc_s2_3", "glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");

        //
        // ~~~ pangram ~~~
        //
        startAction("pangram");
        assertText("id=sc_s2_4", "Zwei flinke Boxer jagen die quirlige Eva und ihren Mops durch Sylt. Franz jagt im komplett verwahrlosten Taxi quer durch Bayern. Zwölf Boxkämpfer jagen Viktor quer über den großen Sylter Deich. Vogel Quax zwickt Johnys Pferd Bim. Sylvia wagt quick den Jux bei Pforzheim. Polyfon zwitschernd aßen Mäxchens Vögel Rüben, Joghurt und Quark. \"Fix, Schwyz! \" quäkt Jürgen blöd vom Paß. Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich. Falsches Üben von Xylophonmusik quält jeden größeren Zwerg. Heizölrückstoßabdämpfung.");
        assertText("id=sc_s2_5", "Zwei flinke Boxer jagen die quirlige Eva und ihren Mops durch Sylt. Franz jagt im komplett verwahrlosten Taxi quer durch Bayern. Zwölf Boxkämpfer jagen Viktor quer über den großen Sylter Deich. Vogel Quax zwickt Johnys Pferd Bim. Sylvia wagt quick den Jux bei Pforzheim. Polyfon zwitschernd aßen Mäxchens Vögel Rüben, Joghurt und Quark. \" Fix, Schwyz! \" quäkt Jürgen blöd vom Paß. Victor jagt zwölf Boxkämpfer quer über den großen Sylter Deich. Falsches Üben von Xylophonmusik quält jeden größeren Zwerg. Heizölrückstoßabdämpfung.");

        //
        // ~~~ format_bold ~~~
        //
        startAction("format_bold");
        assertText("id=format1a", "*aaa bbbb ccc*");
        assertText("id=format1a", "*bb ccc*");

        //
        // ~~~ format_underline ~~~
        //
        startAction("format_underline");
        assertText("id=format1b", "*aaa bbbb cccc*");
        assertText("id=format1b", "*bb ccc*");

        //
        // ~~~ format_italic ~~~
        //
        startAction("format_italic");
        assertText("xpath=id('format1b')/u[2]", "*bb cccc dd*");
        assertText("id=format1c", "*aa bbbb ccc*");

        //
        // ~~~ format_mixed ~~~
        //
        startAction("format_mixed");
        assertText("id=format1c", "*bb ccc*");
        assertText("id=format1d", "*aa bbbb cccc dddd eeee ffff gggg hhh*");

        //
        // ~~~ format_lineBreaks ~~~
        //
        startAction("format_lineBreaks");
        assertText("id=format2a", "*aaaaa bbbb cccc dddd eeee ffff gggg hhhh*");

        //
        // ~~~ format_table ~~~
        //
        startAction("format_table");
        assertText("xpath=id('format2b1')/tbody[1]", "*aaaaa bbbb cccc dddd eeee ffff gggg hhhh iiii kkkk mmmm nnnn oooo pppp rrrr ssss*");
        assertText("xpath=id('format2b1')/tbody[1]", "*dd eeee ffff gggg hhhh iiii kkkk mmmm nnnn oooo pppp rrr*");

        //
        // ~~~ emptyDiv ~~~
        //
        startAction("emptyDiv");
        assertText("xpath=id('invisible_empty_div')", "exact:");
        assertText("xpath=id('invisible_empty_div')", "glob:");
        assertText("xpath=id('invisible_empty_div')", "*");
        assertText("xpath=id('invisible_empty_div')", "");

        //
        // ~~~ emptyDivVisible ~~~
        //
        startAction("emptyDivVisible");
        click("id=invisible_showEmptyDiv");
        assertText("xpath=id('invisible_empty_div')", "exact:");
        assertText("xpath=id('invisible_empty_div')", "glob:");
        assertText("xpath=id('invisible_empty_div')", "*");
        assertText("xpath=id('invisible_empty_div')", "");

        //
        // ~~~ invisibleDiv ~~~
        //
        startAction("invisibleDiv");
        assertText("xpath=id('invisible_visibility')", "");
        assertText("xpath=id('invisible_visibility')", "     ");
        assertText("xpath=id('invisible_display')", "");
        assertText("xpath=id('invisible_display')", "     ");

        //
        // ~~~ matching_strategy ~~~
        //
        startAction("matching_strategy");
        assertText("id=sc_s3_2", "glob:foo:bar");
        assertText("id=sc_s3_7", "glob:exact:foobar");
        assertText("id=sc_s3_8", "glob:glob:foobar");
        assertText("id=sc_s3_9", "glob:regexp:foobar");

        //
        // ~~~ textfield ~~~
        //
        startAction("textfield");
        assertText("id=in_txt_1", "in_txt_1");
        assertText("id=in_txt_1", "regexp:in_[tx]{3}_1");
        assertText("id=in_txt_1", "in_t?t_1");
        assertText("id=in_txt_5", "");
        assertText("id=in_ta_1", "");
        assertText("id=in_ta_2", "in_ta_2");
        assertText("id=in_ta_2", "regexp:in_\\w+?_2");
        assertText("id=in_ta_2", "in_t?_2");

        //
        // ~~~ popup ~~~
        //
        startAction("popup");
        final Open_popup_w2 _open_popup_w2 = new Open_popup_w2();
        _open_popup_w2.execute();

        final SelectWindow_popup_w2 _selectWindow_popup_w2 = new SelectWindow_popup_w2();
        _selectWindow_popup_w2.execute();

        selectFrame("index=0");
        assertText("xpath=//body", "*This is frame 1.*");
        close();

        //
        // ~~~ iframe1 ~~~
        //
        startAction("iframe1");
        final SelectFrame_iframe_1 _selectFrame_iframe_1 = new SelectFrame_iframe_1();
        _selectFrame_iframe_1.execute();

        assertText("id=f1", "*This is iframe 1.*");

        //
        // ~~~ iframe2 ~~~
        //
        startAction("iframe2");
        final SelectFrame_iframe_12 _selectFrame_iframe_12 = new SelectFrame_iframe_12();
        _selectFrame_iframe_12.execute();

        assertText("id=f2", "*This is iframe 2.*");

        //
        // ~~~ iframe3 ~~~
        //
        startAction("iframe3");
        final SelectFrame_iframe_123 _selectFrame_iframe_123 = new SelectFrame_iframe_123();
        _selectFrame_iframe_123.execute();

        assertText("id=f3", "*This is iframe 3.*");

    }
}