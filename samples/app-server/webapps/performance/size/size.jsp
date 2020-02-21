<%
	response.setHeader("Cache-Control","no-cache"); 
	/*--This is used for HTTP 1.1 --*/
	response.setHeader("Pragma","no-cache");
	 /*--This is used for HTTP 1.0 --*/
	response.setDateHeader ("Expires", 0); 
	/*---- This is used to prevents caching at the proxy server */
%>
<% 
	response.setContentType("text/plain");

	final String sizeParameter = request.getParameter("size"); 
	final int size = Integer.valueOf(sizeParameter) - 2;

	for (int i = 0; i < size; i++)
	{
		response.getWriter().print("A"); 
	}
%>
