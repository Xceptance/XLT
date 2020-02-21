<%
	response.setHeader("Cache-Control","no-cache"); 
	/*--This is used for HTTP 1.1 --*/
	response.setHeader("Pragma","no-cache");
	 /*--This is used for HTTP 1.0 --*/
	response.setDateHeader ("Expires", 0); 
	/*---- This is used to prevents caching at the proxy server */
%>

<% 
	final long startTime = System.currentTimeMillis();
%>

<html>
<body>

	<h1>Performance Timing Test</h1>

	<% 
		// get the time and sleep
		final String waitingTime = request.getParameter("sleep"); 
		
		final long startSleep = System.currentTimeMillis();
		
		Thread.currentThread().sleep(Long.valueOf(waitingTime));
		
		final long endSleep = System.currentTimeMillis();
	%>
 
	<% 
		final long endTime = System.currentTimeMillis();
		final String runtime = String.valueOf(endTime - startTime);
	%>

	<div>Waiting time planned: <span id="waitingTimePlanned"><%=waitingTime%></span> ms</div>
	<div>Waiting time actual: <span id="waitingTimeActual"><%=runtime%></span> ms</div>
	<div>Runtime: <span id="runtime"><%=runtime%></span> ms</div>

</body>
</html>
