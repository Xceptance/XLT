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
