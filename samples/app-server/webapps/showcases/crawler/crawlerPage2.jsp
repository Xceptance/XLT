
<html>
  <head>
  <meta http-equiv="cache-control" content="no-cache">
  <title>crawler</title>
  </head>
  
  <h2>Crawler Page 2</h1>
  <body>
    <form action="crawlerPage2.jsp" method="POST">
      <input type="text" name="message">
      <input type="submit" value="submit">
    </form>
  
 <%
   String message;
   if(request.getParameter("message")!=null &&request.getParameter("message")!="")
   {
     message=request.getParameter("message");
     message = message.replaceAll("<", "&lt;");
     message = message.replaceAll(">", "&gt;");
   } 
   else
   {
     message="Please enter message!";
   }
   %>
   
   <p>
   <%= message %>
    </p>

  </body>
</html>
