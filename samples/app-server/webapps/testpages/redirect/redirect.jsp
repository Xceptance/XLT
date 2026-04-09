<%
    response.setHeader("Cache-Control", "no-cache"); 
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader ("Expires", 0); 

    // get parameters
    String method = request.getParameter("method"); 
    String statusCodes = request.getParameter("statusCodes"); 

    statusCodes = statusCodes == null ? "" : statusCodes; 

    if (method != null)
    {
        // returns a page that sends the initial request using the given method
        response.getWriter().println("<script>"); 
        response.getWriter().println("const req = new XMLHttpRequest();"); 
        response.getWriter().println("req.open(\"" + method + "\", \"redirect.jsp?statusCodes=" + statusCodes + "\");"); 
        response.getWriter().println("req.send();"); 
        response.getWriter().println("</script>"); 
    }
    else if (statusCodes.length() > 0)
    {
        // returns a redirect with the first status code from the list
        String[] s = statusCodes.split(",", 2);
        String statusCode = s[0];
        String remainingStatusCodes = s.length > 1 ? s[1] : "";
  
        response.setStatus(Integer.valueOf(statusCode));
        response.setHeader("Location", "redirect.jsp?statusCodes=" + remainingStatusCodes);
    }
    else
    {
        response.getWriter().println("Redirect chain done.");
    }
%>
