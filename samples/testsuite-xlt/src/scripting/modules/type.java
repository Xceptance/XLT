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

/**
 * TODO: Add class description
 */
public class type extends AbstractWebDriverModule
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
        // ~~~ events ~~~
        //
        startAction("events");
        type("id=in_txt_1", "foo");
        // for change and blur event
        click("xpath=/html/body");
        assertText("id=cc_focus", "focus (in_txt_1)*");
        assertText("id=cc_keydown", "keydown (in_txt_1) fo");
        assertText("id=cc_keyup", "keyup (in_txt_1) foo");
        assertText("id=cc_keypress", "keypress (in_txt_1) fo");
        assertText("id=cc_change", "change (in_txt_1) foo");
        assertText("id=cc_blur", "blur (in_txt_1) foo");
        // type("id=fileInput","c:\\bar");
        // type("id=fileInput","/home/hardy/Desktop/foo.js");
        // assertText("id=cc_change","change (fileInput)*");
        //
        // ~~~ input_keyspace_lower ~~~
        //
        startAction("input_keyspace_lower");
        type("css=div#in_text>input", "^1234567890ß´qwertzuiopü+asdfghjklöä#yxcvbnm,.-");
        assertText("id=cc_keyup", "keyup (in_txt_1)  ^1234567890ß´qwertzuiopü+asdfghjklöä#yxcvbnm,.-");
        //
        // ~~~ input_keyspace_upper ~~~
        //
        startAction("input_keyspace_upper");
        type("name=in_txt_1", "°!\"§$%&/()=?`QWERTZUIOPÜ*ASDFGHJKLÖÄ'YXCVBNM;:_");
        assertText("id=cc_keyup", "exact:keyup (in_txt_1)  °!\"§$%&/()=?`QWERTZUIOPÜ*ASDFGHJKLÖÄ'YXCVBNM;:_");
        //
        // ~~~ input_keyspace_altgr ~~~
        //
        startAction("input_keyspace_altgr");
        type("xpath=//div[@id='input']//input[@id='in_txt_1']", "²³{[]}\\@€~|");
        assertText("id=cc_keyup", "keyup (in_txt_1)  ²³{[]}\\@€~|");
        //
        // ~~~ textarea_keypsace ~~~
        //
        startAction("textarea_keypsace");
        type("id=in_ta_1", "^1234567890ß´qwertzuiopü+asdfghjklöä#yxcvbnm,.-");
        assertText("id=cc_keyup", "keyup (in_ta_1)  ^1234567890ß´qwertzuiopü+asdfghjklöä#yxcvbnm,.-");
        //
        // ~~~ input_keyspace_upper ~~~
        //
        startAction("input_keyspace_upper");
        type("id=in_ta_1", "°!\"§$%&/()=?`QWERTZUIOPÜ*ASDFGHJKLÖÄ'YXCVBNM;:_");
        assertText("id=cc_keyup", "exact:keyup (in_ta_1)  °!\"§$%&/()=?`QWERTZUIOPÜ*ASDFGHJKLÖÄ'YXCVBNM;:_");
        //
        // ~~~ input_keyspace_altgr ~~~
        //
        startAction("input_keyspace_altgr");
        type("id=in_ta_1", "²³{[]}\\@€~|");
        assertText("id=cc_keyup", "keyup (in_ta_1)  ²³{[]}\\@€~|");
        //
        // ~~~ clear_input ~~~
        //
        startAction("clear_input");
        type("dom=document.getElementById('in_txt_1')", "some content");
        assertText("id=cc_keyup", "keyup (in_txt_1)  some content");
        type("id=in_txt_1", "");
        assertText("id=cc_keyup", "keyup (in_txt_1)");
        //
        // ~~~ emptyValueTarget ~~~
        //
        startAction("emptyValueTarget");
        type("name=equal_name_input_text value=", "12");
        assertText("name=cc_keyup", "keyup (equal_name_input_text_5) 12");
        //
        // ~~~ HTML5inputTypes ~~~
        //
        startAction("HTML5inputTypes");
        // type("id=in_email_1","foo@bar.de");
        // assertText("name=cc_keyup","keyup (in_email_1) foo@bar.de");
        // type("id=in_tel_1","08001234567");
        // assertText("name=cc_keyup","keyup (in_tel_1) 08001234567");
        // type("id=in_tel_1","foo");
        // assertText("name=cc_keyup","keyup (in_tel_1) foo");
        // type("id=in_url_1","http://www.xceptance.de");
        // assertText("name=cc_keyup","glob:keyup (in_url_1) http://www.xceptance.de");
        // type("id=in_url_1","bar");
        // assertText("name=cc_keyup","keyup (in_url_1) bar");
        type("id=in_datetime_1", "01/01/2012 01:23:45");
        assertText("name=cc_keyup", "glob:keyup (in_datetime_1) 01/01/2012 01:23:45");
        type("id=in_datetime_1", "01.01.2012 01:23:45");
        assertText("name=cc_keyup", "glob:keyup (in_datetime_1) 01.01.2012 01:23:45");
        type("id=in_datetime_1", "foo");
        assertText("name=cc_keyup", "keyup (in_datetime_1) foo");
        // type("id=in_date_1","01");
        // assertText("name=cc_keyup","keyup (in_date_1) 01");
        // type("id=in_date_1","Monday");
        // assertText("name=cc_keyup","keyup (in_date_1) Monday");
        // type("id=in_date_1","bar");
        // assertText("name=cc_keyup","keyup (in_date_1) bar");
        // type("id=in_month_1","12");
        // assertText("name=cc_keyup","keyup (in_month_1) 12");
        // type("id=in_month_1","December");
        // assertText("name=cc_keyup","keyup (in_month_1) December");
        // type("id=in_month_1","foo");
        // assertText("name=cc_keyup","keyup (in_month_1) foo");
        // type("id=in_week_1","12");
        // assertText("name=cc_keyup","keyup (in_week_1) 12");
        // type("id=in_week_1","bar");
        // assertText("name=cc_keyup","keyup (in_week_1) bar");
        // type("id=in_time_1","01:23:45");
        // assertText("name=cc_keyup","glob:keyup (in_time_1) 01:23:45");
        // type("id=in_time_1","foo");
        // assertText("name=cc_keyup","keyup (in_time_1) foo");
        // type("id=in_datetime-local_1","bar");
        // assertText("name=cc_keyup","keyup (in_datetime-local_1) bar");
        // type("id=in_number_1","12345678901234567890");
        // assertText("name=cc_keyup","keyup (in_number_1) 12345678901234567890");
        // type("id=in_number_1","0");
        // assertText("name=cc_keyup","keyup (in_number_1) 0");
        // type("id=in_number_1","-12345");
        // assertText("name=cc_keyup","keyup (in_number_1) -12345");
        // type("id=in_number_1","foo");
        // assertText("name=cc_keyup","keyup (in_number_1) foo");
        // type("id=in_color_1","red");
        // assertText("name=cc_keyup","keyup (in_color_1) red");
        // type("id=in_color_1","#aabbcc");
        // assertText("name=cc_change","change (in_color_1) #aabbcc");
        // type("id=in_color_1","foo");
        // assertText("name=cc_keyup","keyup (in_color_1) foo");
        //
        // ~~~ strange ~~~
        //
        startAction("strange");
        // div tag
        type("id=page_headline", "Y");
        // body
        type("xpath=//body", "Z");

    }
}