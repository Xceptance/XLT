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
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(name = "FileUploadServlet", urlPatterns =
    {
        "/upload/fileupload.html"
    })
@MultipartConfig(location = "uploads")
public class FileUploadServlet extends HttpServlet
{
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        String message = "No file part found in request.";

        for (final Part part : request.getParts())
        {
            final String fileName = part.getSubmittedFileName();
            if (fileName != null)
            {
                part.write(fileName);
                message = "File successfully uploaded.";
                break;
            }
        }

        request.setAttribute("message", message);
        getServletContext().getRequestDispatcher("/upload/upload_response.jsp").forward(request, response);
    }
}
