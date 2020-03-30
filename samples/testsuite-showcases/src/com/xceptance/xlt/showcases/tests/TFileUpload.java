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
package com.xceptance.xlt.showcases.tests;

import org.junit.Test;

import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.showcases.actions.GoToShowCase;
import com.xceptance.xlt.showcases.actions.OpenPage;
import com.xceptance.xlt.showcases.actions.UploadFile;

/**
 * This test case demonstrate a file upload. Our page is quite simple and provides a form for the path of the file and a
 * submit button. So we enter the file path to the input and submit the form. As result we will get a success message.
 * Afterwards we should find a test.txt file in the samples/app/webapps/showcases/upload/uploads/ folder.
 */
public class TFileUpload extends AbstractTestCase
{
    /**
     * Demonstrating File uploading
     */
    @Test
    public void uploading() throws Throwable
    {
        // read the start url from properties
        final String startUrl = XltProperties.getInstance().getProperty("com.xceptance.xlt.showcases.tests.showcases-url");

        // open showcases homepage
        final OpenPage homepage = new OpenPage(startUrl);
        homepage.run();

        // go the the upload page
        final GoToShowCase uploadPage = new GoToShowCase(homepage, "upload");
        uploadPage.run();

        // now upload the file
        final UploadFile uploadFilePage = new UploadFile(uploadPage);
        uploadFilePage.run();
    }
}
