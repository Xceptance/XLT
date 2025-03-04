#Signature file v4.1
#Version 8.2.0

CLSS public abstract com.xceptance.xlt.api.actions.AbstractAction
cons protected init(com.xceptance.xlt.api.actions.AbstractAction,java.lang.String)
meth protected abstract void execute() throws java.lang.Exception
meth protected abstract void postValidate() throws java.lang.Exception
meth protected void executeThinkTime() throws java.lang.InterruptedException
meth public abstract void preValidate() throws java.lang.Exception
meth public boolean preValidateSafe()
meth public com.xceptance.xlt.api.actions.AbstractAction getPreviousAction()
meth public java.lang.String getTimerName()
meth public long getThinkTime()
meth public long getThinkTimeDeviation()
meth public void run() throws java.lang.Throwable
meth public void setThinkTime(int)
meth public void setThinkTime(long)
meth public void setThinkTimeDeviation(int)
meth public void setThinkTimeDeviation(long)
meth public void setTimerName(java.lang.String)
supr java.lang.Object
hfds THINKTIME_DEVIATION_PROPERTY_NAME,THINKTIME_PROPERTY_NAME,preValidateExecuted,preValidationFailed,previousAction,runAlreadyExecuted,thinkTime,thinkTimeDeviation,timerName

CLSS public abstract com.xceptance.xlt.api.actions.AbstractHtmlPageAction
cons protected init(com.xceptance.xlt.api.actions.AbstractWebAction,java.lang.String)
cons protected init(java.lang.String)
meth protected java.util.List<com.xceptance.xlt.api.engine.NetworkData> getNetworkDataSet()
meth protected void loadPage(java.lang.String) throws java.lang.Exception
meth protected void loadPage(java.lang.String,long) throws java.lang.Exception
meth protected void loadPage(java.net.URL) throws java.lang.Exception
meth protected void loadPage(java.net.URL,long) throws java.lang.Exception
meth protected void loadPage(java.net.URL,org.htmlunit.HttpMethod,java.util.List<org.htmlunit.util.NameValuePair>) throws java.lang.Exception
meth protected void loadPage(java.net.URL,org.htmlunit.HttpMethod,java.util.List<org.htmlunit.util.NameValuePair>,long) throws java.lang.Exception
meth protected void loadPageByClick(org.htmlunit.html.HtmlElement) throws java.lang.Exception
meth protected void loadPageByClick(org.htmlunit.html.HtmlElement,long) throws java.lang.Exception
meth protected void loadPageByDragAndDrop(org.htmlunit.html.HtmlElement,org.htmlunit.html.HtmlElement) throws java.lang.Exception
meth protected void loadPageByDragAndDrop(org.htmlunit.html.HtmlElement,org.htmlunit.html.HtmlElement,long) throws java.lang.Exception
meth protected void loadPageByFormClick(org.htmlunit.html.HtmlForm,java.lang.String) throws java.lang.Exception
meth protected void loadPageByFormClick(org.htmlunit.html.HtmlForm,java.lang.String,boolean) throws java.lang.Exception
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected void loadPageByFormClick(org.htmlunit.html.HtmlForm,java.lang.String,boolean,long) throws java.lang.Exception
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected void loadPageByFormClick(org.htmlunit.html.HtmlForm,java.lang.String,long) throws java.lang.Exception
meth protected void loadPageByFormSubmit(org.htmlunit.html.HtmlForm) throws java.lang.Exception
meth protected void loadPageByFormSubmit(org.htmlunit.html.HtmlForm,long) throws java.lang.Exception
meth protected void loadPageByFormSubmit(org.htmlunit.html.HtmlForm,org.htmlunit.html.SubmittableElement) throws java.lang.Exception
meth protected void loadPageByFormSubmit(org.htmlunit.html.HtmlForm,org.htmlunit.html.SubmittableElement,long) throws java.lang.Exception
meth protected void loadPageBySelect(org.htmlunit.html.HtmlSelect,java.lang.String) throws java.lang.Exception
meth protected void loadPageBySelect(org.htmlunit.html.HtmlSelect,java.lang.String,long) throws java.lang.Exception
meth protected void loadPageBySelect(org.htmlunit.html.HtmlSelect,org.htmlunit.html.HtmlOption)
meth protected void loadPageBySelect(org.htmlunit.html.HtmlSelect,org.htmlunit.html.HtmlOption,long) throws java.lang.Exception
meth protected void loadPageByTypingKeys(org.htmlunit.html.HtmlElement,java.lang.String) throws java.lang.Exception
meth protected void loadPageByTypingKeys(org.htmlunit.html.HtmlElement,java.lang.String,long) throws java.lang.Exception
meth public com.xceptance.xlt.api.actions.AbstractHtmlPageAction getPreviousAction()
meth public org.htmlunit.html.HtmlPage getHtmlPage()
meth public void run() throws java.lang.Throwable
meth public void setHtmlPage(org.htmlunit.html.HtmlPage)
meth public void setHtmlPage(org.htmlunit.html.HtmlPage,long)
supr com.xceptance.xlt.api.actions.AbstractWebAction
hfds DEFAULT_JS_BACKGROUND_ACTIVITY_WAITINGTIME,PROP_JS_BACKGROUND_ACTIVITY_WAITINGTIME,htmlPage,netStats

CLSS public abstract com.xceptance.xlt.api.actions.AbstractLightWeightPageAction
cons protected init(com.xceptance.xlt.api.actions.AbstractWebAction,java.lang.String)
cons protected init(java.lang.String)
meth protected void loadPage(java.lang.String) throws java.lang.Exception
meth protected void loadPage(java.net.URL) throws java.lang.Exception
meth protected void loadPage(java.net.URL,org.htmlunit.HttpMethod,java.util.List<org.htmlunit.util.NameValuePair>) throws java.lang.Exception
meth public com.xceptance.xlt.api.actions.AbstractLightWeightPageAction getPreviousAction()
meth public com.xceptance.xlt.api.htmlunit.LightWeightPage getLightWeightPage()
meth public int getHttpResponseCode()
meth public java.lang.String getContent()
meth public java.net.URL getURL()
meth public void run() throws java.lang.Throwable
meth public void setLightWeightPage(com.xceptance.xlt.api.htmlunit.LightWeightPage)
supr com.xceptance.xlt.api.actions.AbstractWebAction
hfds page,url

CLSS public abstract com.xceptance.xlt.api.actions.AbstractWebAction
cons protected init(com.xceptance.xlt.api.actions.AbstractWebAction,java.lang.String)
cons protected init(java.lang.String)
fld protected final static java.util.List<org.htmlunit.util.NameValuePair> EMPTY_PARAMETER_LIST
meth protected org.htmlunit.WebRequest createWebRequestSettings(java.net.URL,org.htmlunit.HttpMethod,java.util.List<org.htmlunit.util.NameValuePair>) throws java.net.MalformedURLException
meth public com.xceptance.xlt.api.actions.AbstractWebAction getPreviousAction()
meth public org.htmlunit.WebClient getWebClient()
meth public void addResponseProcessor(com.xceptance.xlt.api.util.ResponseProcessor)
meth public void closeWebClient()
meth public void run() throws java.lang.Throwable
supr com.xceptance.xlt.api.actions.AbstractAction
hfds webClient

CLSS public abstract com.xceptance.xlt.api.actions.AbstractXmlPageAction
cons protected init(com.xceptance.xlt.api.actions.AbstractWebAction,java.lang.String)
cons protected init(java.lang.String)
meth protected void loadXMLPage(java.net.URL) throws java.lang.Exception
meth protected void validateHttpResponseCode(int) throws java.lang.Exception
meth public org.htmlunit.xml.XmlPage getXmlPage()
supr com.xceptance.xlt.api.actions.AbstractWebAction
hfds xmlPage

CLSS public com.xceptance.xlt.api.actions.ElementMissingException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public com.xceptance.xlt.api.actions.RunMethodStateException
cons public init()
cons public init(java.lang.String)
supr java.lang.IllegalStateException
hfds serialVersionUID

CLSS public com.xceptance.xlt.api.actions.UnexpectedPageTypeException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public com.xceptance.xlt.api.data.Account
cons public init()
meth public java.lang.String getAddress1()
meth public java.lang.String getAddress2()
meth public java.lang.String getBirthday()
meth public java.lang.String getCity()
meth public java.lang.String getCountry()
meth public java.lang.String getEmail()
meth public java.lang.String getFirstName()
meth public java.lang.String getLastName()
meth public java.lang.String getLogin()
meth public java.lang.String getPassword()
meth public java.lang.String getPhone()
meth public java.lang.String getZip()
meth public void setAddress1(java.lang.String)
meth public void setAddress2(java.lang.String)
meth public void setBirthday(java.lang.String)
meth public void setCity(java.lang.String)
meth public void setCountry(java.lang.String)
meth public void setEmail(java.lang.String)
meth public void setFirstName(java.lang.String)
meth public void setLastName(java.lang.String)
meth public void setLogin(java.lang.String)
meth public void setPassword(java.lang.String)
meth public void setPhone(java.lang.String)
meth public void setZip(java.lang.String)
supr java.lang.Object
hfds address1,address2,birthday,city,country,email,firstName,lastName,login,password,phone,zip

CLSS public com.xceptance.xlt.api.data.DataPool<%0 extends java.lang.Object>
cons public init()
cons public init(int,int)
meth public boolean add({com.xceptance.xlt.api.data.DataPool%0})
meth public boolean add({com.xceptance.xlt.api.data.DataPool%0},int)
meth public int getExpireRate()
meth public int getMax()
meth public int getSize()
meth public void clear()
meth public void setExpireRate(int)
meth public void setMax(int)
meth public {com.xceptance.xlt.api.data.DataPool%0} getDataElement()
supr java.lang.Object
hfds dataPool,expirationRate,max

CLSS public com.xceptance.xlt.api.data.DataProvider
cons public init(java.lang.String) throws java.io.IOException
cons public init(java.lang.String,java.lang.String) throws java.io.IOException
cons public init(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
fld public final static java.lang.String DEFAULT = "default"
fld public final static java.lang.String DEFAULT_FILE_ENCODING = "UTF-8"
fld public final static java.lang.String DEFAULT_LINE_COMMENT_MARKER = "#"
meth protected java.util.List<java.lang.String> loadData(java.io.File,java.lang.String) throws java.io.IOException
meth protected java.util.List<java.lang.String> processLines(java.util.List<java.lang.String>)
meth public boolean removeRow(java.lang.String)
meth public int getSize()
meth public java.lang.String getRandomRow()
meth public java.lang.String getRandomRow(boolean)
meth public java.lang.String getRow(boolean,int)
meth public java.lang.String getRow(int)
meth public java.lang.String removeRow(int)
meth public java.util.List<java.lang.String> getAllRows()
meth public static com.xceptance.xlt.api.data.DataProvider getInstance(java.lang.String) throws java.io.IOException
meth public void addRow(int,java.lang.String)
meth public void addRow(java.lang.String)
supr java.lang.Object
hfds dataProviders,dataRows,lineCommentMarker

CLSS public abstract interface com.xceptance.xlt.api.data.DataSetProvider
meth public abstract java.util.List<java.util.Map<java.lang.String,java.lang.String>> getAllDataSets(java.io.File)

CLSS public com.xceptance.xlt.api.data.DataSetProviderException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr com.xceptance.xlt.api.util.XltException
hfds serialVersionUID

CLSS public com.xceptance.xlt.api.data.ExclusiveDataProvider<%0 extends java.lang.Object>
cons public init(java.lang.String,boolean,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{com.xceptance.xlt.api.data.ExclusiveDataProvider%0}>) throws java.io.IOException
cons public init(java.lang.String,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{com.xceptance.xlt.api.data.ExclusiveDataProvider%0}>) throws java.io.IOException
cons public init(java.lang.String,java.lang.String,boolean,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{com.xceptance.xlt.api.data.ExclusiveDataProvider%0}>) throws java.io.IOException
cons public init(java.lang.String,java.lang.String,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{com.xceptance.xlt.api.data.ExclusiveDataProvider%0}>) throws java.io.IOException
fld public final static com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<java.lang.String> DEFAULT_PARSER
innr public abstract static Parser
meth protected static <%0 extends java.lang.Object> java.util.List<{%%0}> loadData(java.lang.String,java.lang.String,boolean,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{%%0}>) throws java.io.IOException
meth protected static <%0 extends java.lang.Object> java.util.List<{%%0}> loadData(java.lang.String,java.lang.String,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{%%0}>) throws java.io.IOException
meth protected static <%0 extends java.lang.Object> java.util.List<{%%0}> parse(java.util.List<java.lang.String>,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{%%0}>)
meth public int size()
meth public static <%0 extends java.lang.Object> com.xceptance.xlt.api.data.ExclusiveDataProvider<{%%0}> getInstance(java.lang.String,boolean,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{%%0}>) throws java.io.IOException
meth public static <%0 extends java.lang.Object> com.xceptance.xlt.api.data.ExclusiveDataProvider<{%%0}> getInstance(java.lang.String,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{%%0}>) throws java.io.IOException
meth public static <%0 extends java.lang.Object> com.xceptance.xlt.api.data.ExclusiveDataProvider<{%%0}> getInstance(java.lang.String,java.lang.String,boolean,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{%%0}>) throws java.io.IOException
meth public static <%0 extends java.lang.Object> com.xceptance.xlt.api.data.ExclusiveDataProvider<{%%0}> getInstance(java.lang.String,java.lang.String,com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<{%%0}>) throws java.io.IOException
meth public static com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<java.lang.String> getDefaultParser()
meth public static com.xceptance.xlt.api.data.ExclusiveDataProvider<java.lang.String> getInstance(java.lang.String) throws java.io.IOException
meth public static com.xceptance.xlt.api.data.ExclusiveDataProvider<java.lang.String> getInstance(java.lang.String,boolean) throws java.io.IOException
meth public static com.xceptance.xlt.api.data.ExclusiveDataProvider<java.lang.String> getInstance(java.lang.String,java.lang.String) throws java.io.IOException
meth public static com.xceptance.xlt.api.data.ExclusiveDataProvider<java.lang.String> getNewInstance(java.lang.String) throws java.io.IOException
meth public static com.xceptance.xlt.api.data.ExclusiveDataProvider<java.lang.String> getNewInstance(java.lang.String,java.lang.String) throws java.io.IOException
meth public void add({com.xceptance.xlt.api.data.ExclusiveDataProvider%0})
meth public {com.xceptance.xlt.api.data.ExclusiveDataProvider%0} get()
meth public {com.xceptance.xlt.api.data.ExclusiveDataProvider%0} getRandom()
supr java.lang.Object
hfds EXCLUSIVE_DATA_PROVIDERS,dataItems
hcls Key

CLSS public abstract static com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser<%0 extends java.lang.Object>
 outer com.xceptance.xlt.api.data.ExclusiveDataProvider
cons public init()
meth public abstract java.util.List<{com.xceptance.xlt.api.data.ExclusiveDataProvider$Parser%0}> parse(java.util.List<java.lang.String>)
supr java.lang.Object

CLSS public com.xceptance.xlt.api.data.GeneralDataProvider
meth public java.lang.String getCompany(boolean)
meth public java.lang.String getCountry(boolean)
meth public java.lang.String getDEPhoneNumber()
meth public java.lang.String getEmail()
meth public java.lang.String getEmail(java.lang.String)
meth public java.lang.String getEmail(java.lang.String,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getFirstName(boolean)
meth public java.lang.String getLastName(boolean)
meth public java.lang.String getPredefinedEmail()
meth public java.lang.String getSentence(boolean)
meth public java.lang.String getStreet(boolean)
meth public java.lang.String getText(int,boolean)
meth public java.lang.String getText(int,int,boolean)
meth public java.lang.String getTown(boolean)
meth public java.lang.String getUSPhoneNumber()
meth public java.lang.String getUniqueEmail(java.lang.String)
meth public java.lang.String getUniqueEmail(java.lang.String,java.lang.String,int)
meth public java.lang.String getUniqueUserName()
meth public java.lang.String getZip(int)
meth public static com.xceptance.xlt.api.data.GeneralDataProvider getInstance()
supr java.lang.Object
hfds AT,SPACE,uuidCleanerPattern
hcls DataCategory,SingletonHolder

CLSS public abstract com.xceptance.xlt.api.engine.AbstractCustomSampler
cons public init()
meth public abstract double execute()
meth public java.lang.String getName()
meth public java.util.Properties getProperties()
meth public long getInterval()
meth public void initialize()
meth public void setInterval(java.lang.String)
meth public void setInterval(long)
meth public void setName(java.lang.String)
meth public void setProperties(java.util.Properties)
meth public void shutdown()
supr java.lang.Object
hfds interval,name,properties

CLSS public abstract com.xceptance.xlt.api.engine.AbstractData
cons public init(char)
cons public init(java.lang.String,char)
intf com.xceptance.xlt.api.engine.Data
meth public char getTypeCode()
meth public final void setAllValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public java.lang.String getAgentName()
meth public java.lang.String getName()
meth public java.lang.String getTransactionName()
meth public java.util.List<java.lang.String> toList()
meth public long getTime()
meth public void setAgentName(java.lang.String)
meth public void setBaseValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public void setName(java.lang.String)
meth public void setTime(long)
meth public void setTransactionName(java.lang.String)
supr java.lang.Object
hfds agentName,name,time,transactionName,typeCode

CLSS public com.xceptance.xlt.api.engine.ActionData
cons public init()
cons public init(java.lang.String)
supr com.xceptance.xlt.api.engine.TimerData
hfds TYPE_CODE

CLSS public com.xceptance.xlt.api.engine.CustomData
cons public init()
cons public init(java.lang.String)
supr com.xceptance.xlt.api.engine.TimerData
hfds TYPE_CODE

CLSS public com.xceptance.xlt.api.engine.CustomValue
cons public init()
cons public init(java.lang.String)
meth public double getValue()
meth public java.util.List<java.lang.String> toList()
meth public void setRemainingValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public void setValue(double)
supr com.xceptance.xlt.api.engine.AbstractData
hfds TYPE_CODE,value

CLSS public abstract interface com.xceptance.xlt.api.engine.Data
meth public abstract char getTypeCode()
meth public abstract java.lang.String getAgentName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getTransactionName()
meth public abstract java.util.List<java.lang.String> toList()
meth public abstract long getTime()
meth public abstract void setAgentName(java.lang.String)
meth public abstract void setBaseValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public abstract void setName(java.lang.String)
meth public abstract void setRemainingValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public abstract void setTime(long)
meth public abstract void setTransactionName(java.lang.String)

CLSS public abstract interface com.xceptance.xlt.api.engine.DataManager
meth public abstract boolean isLoggingEnabled()
meth public abstract long getEndOfLoggingPeriod()
meth public abstract long getStartOfLoggingPeriod()
meth public abstract void disableLogging()
meth public abstract void enableLogging()
meth public abstract void logDataRecord(com.xceptance.xlt.api.engine.Data)
meth public abstract void logEvent(java.lang.String,java.lang.String)
meth public abstract void setEndOfLoggingPeriod(long)
meth public abstract void setLoggingEnabled(boolean)
meth public abstract void setStartOfLoggingPeriod(long)

CLSS public abstract interface !annotation com.xceptance.xlt.api.engine.DataSetIndex
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int value()

CLSS public com.xceptance.xlt.api.engine.EventData
cons public init()
cons public init(java.lang.String)
meth public java.lang.String getMessage()
meth public java.lang.String getTestCaseName()
meth public java.util.List<java.lang.String> toList()
meth public void setMessage(java.lang.String)
meth public void setRemainingValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public void setTestCaseName(java.lang.String)
supr com.xceptance.xlt.api.engine.AbstractData
hfds TYPE_CODE,message,testCaseName

CLSS public com.xceptance.xlt.api.engine.GlobalClock
cons public init()
meth public static java.time.Clock get()
meth public static java.time.Clock install(java.time.Clock)
meth public static java.time.Clock installFixed(long)
meth public static java.time.Clock installWithOffset(long)
meth public static java.time.Clock reset()
meth public static long millis()
meth public static long offset()
supr java.lang.Object
hfds clock,clockHandle,offsetMillis,offsetMillisHandle

CLSS public com.xceptance.xlt.api.engine.NetworkData
cons public init(org.htmlunit.WebRequest,org.htmlunit.WebResponse)
meth public int getResponseStatusCode()
meth public java.lang.String getContentAsString()
meth public java.lang.String getContentType()
meth public java.lang.String getRequestBody()
meth public java.lang.String getResponseStatusMessage()
meth public java.net.URL getURL()
meth public java.util.List<org.htmlunit.util.NameValuePair> getRequestParameters()
meth public java.util.List<org.htmlunit.util.NameValuePair> getResponseHeaders()
meth public java.util.Map<java.lang.String,java.lang.String> getAdditionalRequestHeaders()
meth public org.htmlunit.HttpMethod getRequestMethod()
meth public org.htmlunit.WebRequest getRequest()
meth public org.htmlunit.WebResponse getResponse()
supr java.lang.Object
hfds request,response

CLSS public abstract interface com.xceptance.xlt.api.engine.NetworkDataManager
meth public abstract java.util.List<com.xceptance.xlt.api.engine.NetworkData> getData()
meth public abstract java.util.List<com.xceptance.xlt.api.engine.NetworkData> getData(com.xceptance.xlt.api.engine.RequestFilter)
meth public abstract void addData(com.xceptance.xlt.api.engine.NetworkData)
meth public abstract void clear()

CLSS public com.xceptance.xlt.api.engine.PageLoadTimingData
cons public init()
cons public init(java.lang.String)
supr com.xceptance.xlt.api.engine.TimerData
hfds TYPE_CODE

CLSS public com.xceptance.xlt.api.engine.RequestData
cons public init()
cons public init(java.lang.String)
fld public final static com.xceptance.xlt.api.util.XltCharBuffer UNKNOWN_HOST
meth public com.xceptance.xlt.api.util.XltCharBuffer getContentType()
meth public com.xceptance.xlt.api.util.XltCharBuffer getFormData()
meth public com.xceptance.xlt.api.util.XltCharBuffer getFormDataEncoding()
meth public com.xceptance.xlt.api.util.XltCharBuffer getHost()
meth public com.xceptance.xlt.api.util.XltCharBuffer getHttpMethod()
meth public com.xceptance.xlt.api.util.XltCharBuffer getUrl()
meth public com.xceptance.xlt.api.util.XltCharBuffer getUsedIpAddress()
meth public int getBytesReceived()
meth public int getBytesSent()
meth public int getConnectTime()
meth public int getDnsTime()
meth public int getReceiveTime()
meth public int getResponseCode()
meth public int getSendTime()
meth public int getServerBusyTime()
meth public int getTimeToFirstBytes()
meth public int getTimeToLastBytes()
meth public int hashCodeOfUrlWithoutFragment()
meth public java.lang.String getRequestId()
meth public java.lang.String getResponseId()
meth public java.lang.String[] getIpAddresses()
meth public java.util.List<java.lang.String> toList()
meth public void setBytesReceived(int)
meth public void setBytesSent(int)
meth public void setConnectTime(int)
meth public void setContentType(com.xceptance.xlt.api.util.XltCharBuffer)
meth public void setContentType(java.lang.String)
meth public void setDnsTime(int)
meth public void setFormData(com.xceptance.xlt.api.util.XltCharBuffer)
meth public void setFormData(java.lang.String)
meth public void setFormDataEncoding(com.xceptance.xlt.api.util.XltCharBuffer)
meth public void setFormDataEncoding(java.lang.String)
meth public void setHttpMethod(com.xceptance.xlt.api.util.XltCharBuffer)
meth public void setHttpMethod(java.lang.String)
meth public void setIpAddresses(java.lang.String[])
meth public void setReceiveTime(int)
meth public void setRemainingValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public void setRequestId(com.xceptance.xlt.api.util.XltCharBuffer)
meth public void setRequestId(java.lang.String)
meth public void setResponseCode(int)
meth public void setResponseId(com.xceptance.xlt.api.util.XltCharBuffer)
meth public void setResponseId(java.lang.String)
meth public void setSendTime(int)
meth public void setServerBusyTime(int)
meth public void setTimeToFirstBytes(int)
meth public void setTimeToLastBytes(int)
meth public void setUrl(com.xceptance.xlt.api.util.XltCharBuffer)
meth public void setUrl(java.lang.String)
meth public void setUsedIpAddress(com.xceptance.xlt.api.util.XltCharBuffer)
meth public void setUsedIpAddress(java.lang.String)
supr com.xceptance.xlt.api.engine.TimerData
hfds IP_ADDRESSES_SEPARATOR,TYPE_CODE,bytesReceived,bytesSent,connectTime,contentType,dnsTime,formData,formDataEncoding,hashCodeOfUrlWithoutFragment,host,httpMethod,ipAddresses,receiveTime,requestId,responseCode,responseId,sendTime,serverBusyTime,timeToFirstBytes,timeToLastBytes,url,usedIpAddress

CLSS public com.xceptance.xlt.api.engine.RequestFilter
cons public init()
meth public boolean accepts(org.htmlunit.WebRequest)
meth public int getPort()
meth public java.lang.String getHostPattern()
meth public java.lang.String getPathPattern()
meth public java.lang.String getProtocol()
meth public java.lang.String getQueryPattern()
meth public java.lang.String getUrlPattern()
meth public void setHostPattern(java.lang.String)
meth public void setPathPattern(java.lang.String)
meth public void setPort(int)
meth public void setProtocol(java.lang.String)
meth public void setQueryPattern(java.lang.String)
meth public void setUrlPattern(java.lang.String)
supr java.lang.Object
hfds hostPattern,pathPattern,port,protocol,queryPattern,urlPattern

CLSS public abstract com.xceptance.xlt.api.engine.Session
cons public init()
meth public abstract boolean hasFailed()
meth public abstract boolean isLoadTest()
meth public abstract com.xceptance.xlt.api.engine.DataManager getDataManager()
meth public abstract com.xceptance.xlt.api.engine.NetworkDataManager getNetworkDataManager()
meth public abstract int getAbsoluteUserNumber()
meth public abstract int getAgentNumber()
meth public abstract int getTotalAgentCount()
meth public abstract int getTotalUserCount()
meth public abstract int getUserCount()
meth public abstract int getUserNumber()
meth public abstract java.lang.String getAgentID()
meth public abstract java.lang.String getCurrentActionName()
meth public abstract java.lang.String getID()
meth public abstract java.lang.String getTestCaseClassName()
meth public abstract java.lang.String getUserID()
meth public abstract java.lang.String getUserName()
meth public abstract java.lang.String getWebDriverActionName()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.nio.file.Path getResultsDirectory()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getValueLog()
meth public abstract void addShutdownListener(com.xceptance.xlt.api.engine.SessionShutdownListener)
meth public abstract void clear()
meth public abstract void removeShutdownListener(com.xceptance.xlt.api.engine.SessionShutdownListener)
meth public abstract void setFailed()
meth public abstract void setFailed(boolean)
meth public abstract void setID(java.lang.String)
meth public abstract void setNotFailed()
meth public abstract void setWebDriverActionName(java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract void startAction(java.lang.String)
meth public abstract void stopAction()
meth public final static void logEvent(java.lang.String,java.lang.String)
meth public static com.xceptance.xlt.api.engine.Session getCurrent()
supr java.lang.Object

CLSS public abstract interface com.xceptance.xlt.api.engine.SessionShutdownListener
meth public abstract void shutdown()

CLSS public abstract com.xceptance.xlt.api.engine.TimerData
cons public init(char)
cons public init(java.lang.String,char)
meth public boolean hasFailed()
meth public int getRunTime()
meth public java.util.List<java.lang.String> toList()
meth public long getEndTime()
meth public void setFailed(boolean)
meth public void setRemainingValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public void setRunTime(int)
meth public void setRunTime(long)
supr com.xceptance.xlt.api.engine.AbstractData
hfds failed,runTime

CLSS public com.xceptance.xlt.api.engine.TransactionData
cons public init()
cons public init(java.lang.String)
meth public java.lang.String getDirectoryName()
meth public java.lang.String getDumpDirectoryPath()
meth public java.lang.String getFailedActionName()
meth public java.lang.String getFailureMessage()
meth public java.lang.String getFailureStackTrace()
meth public java.lang.String getTestUserNumber()
meth public java.util.List<java.lang.String> toList()
meth public void setDirectoryName(java.lang.String)
meth public void setFailedActionName(java.lang.String)
meth public void setFailureStackTrace(java.lang.String)
meth public void setFailureStackTrace(java.lang.Throwable)
meth public void setRemainingValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public void setTestUserNumber(java.lang.String)
supr com.xceptance.xlt.api.engine.TimerData
hfds TYPE_CODE,directoryName,failedActionName,stackTrace,testUserNumber

CLSS public com.xceptance.xlt.api.engine.WebVitalData
cons public init()
cons public init(java.lang.String)
meth public double getValue()
meth public java.util.List<java.lang.String> toList()
meth public void setRemainingValues(java.util.List<com.xceptance.xlt.api.util.XltCharBuffer>)
meth public void setValue(double)
supr com.xceptance.xlt.api.engine.AbstractData
hfds TYPE_CODE,value

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule
cons public init()
meth protected abstract com.xceptance.xlt.api.actions.AbstractHtmlPageAction execute(com.xceptance.xlt.api.actions.AbstractHtmlPageAction) throws java.lang.Throwable
meth public com.xceptance.xlt.api.actions.AbstractHtmlPageAction run(com.xceptance.xlt.api.actions.AbstractHtmlPageAction) throws java.lang.Throwable
supr com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptModule

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule
cons public init()
meth protected abstract org.htmlunit.html.HtmlPage execute(org.htmlunit.html.HtmlPage) throws java.lang.Exception
meth protected org.htmlunit.html.HtmlPage addSelection(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage check(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage checkAndWait(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage click(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage clickAndWait(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage contextMenu(java.lang.String)
meth protected org.htmlunit.html.HtmlPage contextMenuAt(java.lang.String,int,int)
meth protected org.htmlunit.html.HtmlPage contextMenuAt(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage doubleClick(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage doubleClickAndWait(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage mouseDown(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseDownAt(java.lang.String,int,int)
meth protected org.htmlunit.html.HtmlPage mouseDownAt(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseMove(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseMoveAt(java.lang.String,int,int)
meth protected org.htmlunit.html.HtmlPage mouseMoveAt(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseOut(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseOver(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseUp(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseUpAt(java.lang.String,int,int)
meth protected org.htmlunit.html.HtmlPage mouseUpAt(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage open(java.lang.String) throws java.lang.Exception
meth protected org.htmlunit.html.HtmlPage open(java.net.URL) throws java.lang.Exception
meth protected org.htmlunit.html.HtmlPage pause(java.lang.String)
meth protected org.htmlunit.html.HtmlPage pause(long)
meth protected org.htmlunit.html.HtmlPage removeSelection(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage select(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage selectAndWait(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage selectFrame(java.lang.String)
meth protected org.htmlunit.html.HtmlPage selectWindow()
meth protected org.htmlunit.html.HtmlPage selectWindow(java.lang.String)
meth protected org.htmlunit.html.HtmlPage submit(java.lang.String) throws java.lang.Exception
meth protected org.htmlunit.html.HtmlPage submitAndWait(java.lang.String)
meth protected org.htmlunit.html.HtmlPage type(java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage typeAndWait(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage uncheck(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage uncheckAndWait(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage waitForAttribute(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForChecked(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForClass(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForElementCount(java.lang.String,int)
meth protected org.htmlunit.html.HtmlPage waitForElementCount(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForElementPresent(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForEval(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotAttribute(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotChecked(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotClass(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotElementCount(java.lang.String,int)
meth protected org.htmlunit.html.HtmlPage waitForNotElementCount(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotElementPresent(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotEval(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotSelectedId(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotSelectedIndex(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotSelectedLabel(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotSelectedValue(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotStyle(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotText(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotTextPresent(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotTitle(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotVisible(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotXpathCount(java.lang.String,int)
meth protected org.htmlunit.html.HtmlPage waitForNotXpathCount(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForPageToLoad()
meth protected org.htmlunit.html.HtmlPage waitForSelectedId(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForSelectedIndex(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForSelectedLabel(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForSelectedValue(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForStyle(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForText(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForTextPresent(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForTitle(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForValue(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForVisible(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForXpathCount(java.lang.String,int)
meth protected org.htmlunit.html.HtmlPage waitForXpathCount(java.lang.String,java.lang.String)
meth protected void assertAttribute(java.lang.String,java.lang.String)
meth protected void assertAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected void assertChecked(java.lang.String)
meth protected void assertClass(java.lang.String,java.lang.String)
meth protected void assertElementCount(java.lang.String,int)
meth protected void assertElementCount(java.lang.String,java.lang.String)
meth protected void assertElementPresent(java.lang.String)
meth protected void assertEval(java.lang.String,java.lang.String)
meth protected void assertLoadTime(java.lang.String)
meth protected void assertLoadTime(long)
meth protected void assertNotAttribute(java.lang.String,java.lang.String)
meth protected void assertNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected void assertNotChecked(java.lang.String)
meth protected void assertNotClass(java.lang.String,java.lang.String)
meth protected void assertNotElementCount(java.lang.String,int)
meth protected void assertNotElementCount(java.lang.String,java.lang.String)
meth protected void assertNotElementPresent(java.lang.String)
meth protected void assertNotEval(java.lang.String,java.lang.String)
meth protected void assertNotSelectedId(java.lang.String,java.lang.String)
meth protected void assertNotSelectedIndex(java.lang.String,java.lang.String)
meth protected void assertNotSelectedLabel(java.lang.String,java.lang.String)
meth protected void assertNotSelectedValue(java.lang.String,java.lang.String)
meth protected void assertNotStyle(java.lang.String,java.lang.String)
meth protected void assertNotText(java.lang.String,java.lang.String)
meth protected void assertNotTextPresent(java.lang.String)
meth protected void assertNotTitle(java.lang.String)
meth protected void assertNotValue(java.lang.String,java.lang.String)
meth protected void assertNotVisible(java.lang.String)
meth protected void assertNotXpathCount(java.lang.String,int)
meth protected void assertNotXpathCount(java.lang.String,java.lang.String)
meth protected void assertPageSize(java.lang.String)
meth protected void assertPageSize(long)
meth protected void assertSelectedId(java.lang.String,java.lang.String)
meth protected void assertSelectedIndex(java.lang.String,java.lang.String)
meth protected void assertSelectedLabel(java.lang.String,java.lang.String)
meth protected void assertSelectedValue(java.lang.String,java.lang.String)
meth protected void assertStyle(java.lang.String,java.lang.String)
meth protected void assertText(java.lang.String,java.lang.String)
meth protected void assertTextPresent(java.lang.String)
meth protected void assertTitle(java.lang.String)
meth protected void assertValue(java.lang.String,java.lang.String)
meth protected void assertVisible(java.lang.String)
meth protected void assertXpathCount(java.lang.String,int)
meth protected void assertXpathCount(java.lang.String,java.lang.String)
meth protected void close()
meth protected void createCookie(java.lang.String)
meth protected void createCookie(java.lang.String,java.lang.String)
meth protected void deleteAllVisibleCookies()
meth protected void deleteCookie(java.lang.String)
meth protected void deleteCookie(java.lang.String,java.lang.String)
meth protected void echo(java.lang.String)
meth protected void setTimeout(java.lang.String)
meth protected void setTimeout(long)
meth protected void store(java.lang.String,java.lang.String)
meth protected void storeAttribute(java.lang.String,java.lang.String)
meth protected void storeAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected void storeElementCount(java.lang.String,java.lang.String)
meth protected void storeEval(java.lang.String,java.lang.String)
meth protected void storeText(java.lang.String,java.lang.String)
meth protected void storeTitle(java.lang.String)
meth protected void storeValue(java.lang.String,java.lang.String)
meth protected void storeXpathCount(java.lang.String,java.lang.String)
meth protected void waitForNotValue(java.lang.String,java.lang.String)
meth protected void waitForPopUp()
meth protected void waitForPopUp(java.lang.String)
meth protected void waitForPopUp(java.lang.String,java.lang.String)
meth protected void waitForPopUp(java.lang.String,long)
meth public org.htmlunit.html.HtmlPage run(org.htmlunit.html.HtmlPage) throws java.lang.Exception
supr com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptModule

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction
cons public init(com.xceptance.xlt.api.actions.AbstractWebAction)
cons public init(com.xceptance.xlt.api.actions.AbstractWebAction,java.lang.String)
cons public init(java.lang.String)
meth protected boolean evaluatesToTrue(java.lang.String)
meth protected java.lang.String getBaseUrl()
meth protected java.lang.String resolve(java.lang.String)
meth protected java.lang.String resolveKey(java.lang.String)
meth protected org.htmlunit.html.HtmlPage addSelection(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage check(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage checkAndWait(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage click(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage clickAndWait(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage contextMenu(java.lang.String)
meth protected org.htmlunit.html.HtmlPage contextMenuAt(java.lang.String,int,int)
meth protected org.htmlunit.html.HtmlPage contextMenuAt(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage doubleClick(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage doubleClickAndWait(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage mouseDown(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseDownAt(java.lang.String,int,int)
meth protected org.htmlunit.html.HtmlPage mouseDownAt(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseMove(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseMoveAt(java.lang.String,int,int)
meth protected org.htmlunit.html.HtmlPage mouseMoveAt(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseOut(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseOver(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseUp(java.lang.String)
meth protected org.htmlunit.html.HtmlPage mouseUpAt(java.lang.String,int,int)
meth protected org.htmlunit.html.HtmlPage mouseUpAt(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage open(java.lang.String) throws java.lang.Exception
meth protected org.htmlunit.html.HtmlPage open(java.net.URL) throws java.lang.Exception
meth protected org.htmlunit.html.HtmlPage pause(java.lang.String)
meth protected org.htmlunit.html.HtmlPage pause(long)
meth protected org.htmlunit.html.HtmlPage removeSelection(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage select(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage selectAndWait(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage selectFrame(java.lang.String)
meth protected org.htmlunit.html.HtmlPage selectWindow()
meth protected org.htmlunit.html.HtmlPage selectWindow(java.lang.String)
meth protected org.htmlunit.html.HtmlPage submit(java.lang.String) throws java.lang.Exception
meth protected org.htmlunit.html.HtmlPage submitAndWait(java.lang.String)
meth protected org.htmlunit.html.HtmlPage type(java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage typeAndWait(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage uncheck(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage uncheckAndWait(java.lang.String) throws java.io.IOException
meth protected org.htmlunit.html.HtmlPage waitForAttribute(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForChecked(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForClass(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForElementCount(java.lang.String,int)
meth protected org.htmlunit.html.HtmlPage waitForElementCount(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForElementPresent(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForEval(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotAttribute(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotChecked(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotClass(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotElementCount(java.lang.String,int)
meth protected org.htmlunit.html.HtmlPage waitForNotElementCount(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotElementPresent(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotEval(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotSelectedId(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotSelectedIndex(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotSelectedLabel(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotSelectedValue(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotStyle(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotText(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotTextPresent(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotTitle(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotValue(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotVisible(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForNotXpathCount(java.lang.String,int)
meth protected org.htmlunit.html.HtmlPage waitForNotXpathCount(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForPageToLoad()
meth protected org.htmlunit.html.HtmlPage waitForSelectedId(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForSelectedIndex(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForSelectedLabel(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForSelectedValue(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForStyle(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForText(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForTextPresent(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForTitle(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForValue(java.lang.String,java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForVisible(java.lang.String)
meth protected org.htmlunit.html.HtmlPage waitForXpathCount(java.lang.String,int)
meth protected org.htmlunit.html.HtmlPage waitForXpathCount(java.lang.String,java.lang.String)
meth protected void assertAttribute(java.lang.String,java.lang.String)
meth protected void assertAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected void assertChecked(java.lang.String)
meth protected void assertClass(java.lang.String,java.lang.String)
meth protected void assertElementCount(java.lang.String,int)
meth protected void assertElementCount(java.lang.String,java.lang.String)
meth protected void assertElementPresent(java.lang.String)
meth protected void assertEval(java.lang.String,java.lang.String)
meth protected void assertLoadTime(java.lang.String)
meth protected void assertLoadTime(long)
meth protected void assertNotAttribute(java.lang.String,java.lang.String)
meth protected void assertNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected void assertNotChecked(java.lang.String)
meth protected void assertNotClass(java.lang.String,java.lang.String)
meth protected void assertNotElementCount(java.lang.String,int)
meth protected void assertNotElementCount(java.lang.String,java.lang.String)
meth protected void assertNotElementPresent(java.lang.String)
meth protected void assertNotEval(java.lang.String,java.lang.String)
meth protected void assertNotSelectedId(java.lang.String,java.lang.String)
meth protected void assertNotSelectedIndex(java.lang.String,java.lang.String)
meth protected void assertNotSelectedLabel(java.lang.String,java.lang.String)
meth protected void assertNotSelectedValue(java.lang.String,java.lang.String)
meth protected void assertNotStyle(java.lang.String,java.lang.String)
meth protected void assertNotText(java.lang.String,java.lang.String)
meth protected void assertNotTextPresent(java.lang.String)
meth protected void assertNotTitle(java.lang.String)
meth protected void assertNotValue(java.lang.String,java.lang.String)
meth protected void assertNotVisible(java.lang.String)
meth protected void assertNotXpathCount(java.lang.String,int)
meth protected void assertNotXpathCount(java.lang.String,java.lang.String)
meth protected void assertPageSize(java.lang.String)
meth protected void assertPageSize(long)
meth protected void assertSelectedId(java.lang.String,java.lang.String)
meth protected void assertSelectedIndex(java.lang.String,java.lang.String)
meth protected void assertSelectedLabel(java.lang.String,java.lang.String)
meth protected void assertSelectedValue(java.lang.String,java.lang.String)
meth protected void assertStyle(java.lang.String,java.lang.String)
meth protected void assertText(java.lang.String,java.lang.String)
meth protected void assertTextPresent(java.lang.String)
meth protected void assertTitle(java.lang.String)
meth protected void assertValue(java.lang.String,java.lang.String)
meth protected void assertVisible(java.lang.String)
meth protected void assertXpathCount(java.lang.String,int)
meth protected void assertXpathCount(java.lang.String,java.lang.String)
meth protected void close()
meth protected void createCookie(java.lang.String)
meth protected void createCookie(java.lang.String,java.lang.String)
meth protected void deleteAllVisibleCookies()
meth protected void deleteCookie(java.lang.String)
meth protected void deleteCookie(java.lang.String,java.lang.String)
meth protected void echo(java.lang.String)
meth protected void setTimeout(java.lang.String)
meth protected void setTimeout(long)
meth protected void store(java.lang.String,java.lang.String)
meth protected void storeAttribute(java.lang.String,java.lang.String)
meth protected void storeAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected void storeElementCount(java.lang.String,java.lang.String)
meth protected void storeEval(java.lang.String,java.lang.String)
meth protected void storeText(java.lang.String,java.lang.String)
meth protected void storeTitle(java.lang.String)
meth protected void storeValue(java.lang.String,java.lang.String)
meth protected void storeXpathCount(java.lang.String,java.lang.String)
meth protected void waitForPopUp()
meth protected void waitForPopUp(java.lang.String)
meth protected void waitForPopUp(java.lang.String,java.lang.String)
meth protected void waitForPopUp(java.lang.String,long)
supr com.xceptance.xlt.api.actions.AbstractHtmlPageAction

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptModule
cons public init()
meth protected boolean evaluatesToTrue(java.lang.String)
meth protected java.lang.String resolve(java.lang.String)
meth protected java.lang.String resolveKey(java.lang.String)
supr java.lang.Object

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase
cons public init()
cons public init(java.lang.String)
meth protected boolean evaluatesToTrue(java.lang.String)
meth protected java.lang.String resolve(java.lang.String)
meth protected java.lang.String resolveKey(java.lang.String)
meth public final void __cleanUpAbstractHtmlUnitScriptTestCase()
meth public final void __setUpAbstractHtmlUnitScriptTestCase()
supr com.xceptance.xlt.api.tests.AbstractTestCase
hfds _adapter

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractScriptTestCase
cons public init()
meth protected void executeScript(java.lang.String) throws java.lang.Exception
meth public final void __cleanUpAbstractScriptTestCase()
meth public final void __setUpAbstractScriptTestCase()
meth public java.lang.String getBaseUrl()
meth public java.lang.String getScriptName()
meth public void setBaseUrl(java.lang.String)
meth public void setScriptName(java.lang.String)
meth public void setWebDriver(org.openqa.selenium.WebDriver)
meth public void test() throws java.lang.Exception
supr com.xceptance.xlt.api.tests.AbstractWebDriverTestCase
hfds DEFAULT_IMPLICIT_WAIT_TIMEOUT,scriptName

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule
cons public init()
intf com.xceptance.xlt.api.engine.scripting.ScriptCommands
meth protected abstract !varargs void doCommands(java.lang.String[]) throws java.lang.Exception
meth protected final org.openqa.selenium.WebDriver getWebDriver()
meth public !varargs void execute(java.lang.String[]) throws java.lang.Exception
meth public boolean evaluatesToTrue(java.lang.String)
meth public boolean hasAttribute(java.lang.String,java.lang.String)
meth public boolean hasAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public boolean hasClass(java.lang.String,java.lang.String)
meth public boolean hasNotClass(java.lang.String,java.lang.String)
meth public boolean hasNotStyle(java.lang.String,java.lang.String)
meth public boolean hasStyle(java.lang.String,java.lang.String)
meth public boolean hasText(java.lang.String,java.lang.String)
meth public boolean hasTitle(java.lang.String)
meth public boolean hasValue(java.lang.String,java.lang.String)
meth public boolean isChecked(java.lang.String)
meth public boolean isElementPresent(java.lang.String)
meth public boolean isEnabled(java.lang.String)
meth public boolean isEvalMatching(java.lang.String,java.lang.String)
meth public boolean isTextPresent(java.lang.String)
meth public boolean isVisible(java.lang.String)
meth public int getElementCount(java.lang.String)
meth public int getXpathCount(java.lang.String)
meth public java.lang.String evaluate(java.lang.String)
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getAttribute(java.lang.String,java.lang.String)
meth public java.lang.String getPageText()
meth public java.lang.String getText(java.lang.String)
meth public java.lang.String getTitle()
meth public java.lang.String getValue(java.lang.String)
meth public java.lang.String resolve(java.lang.String)
meth public java.lang.String resolveKey(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElements(java.lang.String)
meth public org.openqa.selenium.WebElement findElement(java.lang.String)
meth public void addSelection(java.lang.String,java.lang.String)
meth public void assertAttribute(java.lang.String,java.lang.String)
meth public void assertAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void assertChecked(java.lang.String)
meth public void assertClass(java.lang.String,java.lang.String)
meth public void assertElementCount(java.lang.String,int)
meth public void assertElementCount(java.lang.String,java.lang.String)
meth public void assertElementPresent(java.lang.String)
meth public void assertEval(java.lang.String,java.lang.String)
meth public void assertLoadTime(java.lang.String)
meth public void assertLoadTime(long)
meth public void assertNotAttribute(java.lang.String,java.lang.String)
meth public void assertNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void assertNotChecked(java.lang.String)
meth public void assertNotClass(java.lang.String,java.lang.String)
meth public void assertNotElementCount(java.lang.String,int)
meth public void assertNotElementCount(java.lang.String,java.lang.String)
meth public void assertNotElementPresent(java.lang.String)
meth public void assertNotEval(java.lang.String,java.lang.String)
meth public void assertNotSelectedId(java.lang.String,java.lang.String)
meth public void assertNotSelectedIndex(java.lang.String,java.lang.String)
meth public void assertNotSelectedLabel(java.lang.String,java.lang.String)
meth public void assertNotSelectedValue(java.lang.String,java.lang.String)
meth public void assertNotStyle(java.lang.String,java.lang.String)
meth public void assertNotText(java.lang.String,java.lang.String)
meth public void assertNotTextPresent(java.lang.String)
meth public void assertNotTitle(java.lang.String)
meth public void assertNotValue(java.lang.String,java.lang.String)
meth public void assertNotVisible(java.lang.String)
meth public void assertNotXpathCount(java.lang.String,int)
meth public void assertNotXpathCount(java.lang.String,java.lang.String)
meth public void assertPageSize(java.lang.String)
meth public void assertPageSize(long)
meth public void assertSelectedId(java.lang.String,java.lang.String)
meth public void assertSelectedIndex(java.lang.String,java.lang.String)
meth public void assertSelectedLabel(java.lang.String,java.lang.String)
meth public void assertSelectedValue(java.lang.String,java.lang.String)
meth public void assertStyle(java.lang.String,java.lang.String)
meth public void assertText(java.lang.String,java.lang.String)
meth public void assertTextPresent(java.lang.String)
meth public void assertTitle(java.lang.String)
meth public void assertValue(java.lang.String,java.lang.String)
meth public void assertVisible(java.lang.String)
meth public void assertXpathCount(java.lang.String,int)
meth public void assertXpathCount(java.lang.String,java.lang.String)
meth public void check(java.lang.String)
meth public void checkAndWait(java.lang.String)
meth public void click(java.lang.String)
meth public void clickAndWait(java.lang.String)
meth public void close()
meth public void contextMenu(java.lang.String)
meth public void contextMenuAt(java.lang.String,int,int)
meth public void contextMenuAt(java.lang.String,java.lang.String)
meth public void createCookie(java.lang.String)
meth public void createCookie(java.lang.String,java.lang.String)
meth public void deleteAllVisibleCookies()
meth public void deleteCookie(java.lang.String)
meth public void deleteCookie(java.lang.String,java.lang.String)
meth public void doubleClick(java.lang.String)
meth public void doubleClickAndWait(java.lang.String)
meth public void echo(java.lang.String)
meth public void mouseDown(java.lang.String)
meth public void mouseDownAt(java.lang.String,int,int)
meth public void mouseDownAt(java.lang.String,java.lang.String)
meth public void mouseMove(java.lang.String)
meth public void mouseMoveAt(java.lang.String,int,int)
meth public void mouseMoveAt(java.lang.String,java.lang.String)
meth public void mouseOut(java.lang.String)
meth public void mouseOver(java.lang.String)
meth public void mouseUp(java.lang.String)
meth public void mouseUpAt(java.lang.String,int,int)
meth public void mouseUpAt(java.lang.String,java.lang.String)
meth public void open(java.lang.String)
meth public void open(java.net.URL)
meth public void pause(java.lang.String)
meth public void pause(long)
meth public void removeSelection(java.lang.String,java.lang.String)
meth public void select(java.lang.String,java.lang.String)
meth public void selectAndWait(java.lang.String,java.lang.String)
meth public void selectFrame(java.lang.String)
meth public void selectWindow()
meth public void selectWindow(java.lang.String)
meth public void setTimeout(java.lang.String)
meth public void setTimeout(long)
meth public void startAction(java.lang.String)
meth public void stopAction()
meth public void store(java.lang.String,java.lang.String)
meth public void storeAttribute(java.lang.String,java.lang.String)
meth public void storeAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void storeElementCount(java.lang.String,java.lang.String)
meth public void storeEval(java.lang.String,java.lang.String)
meth public void storeText(java.lang.String,java.lang.String)
meth public void storeTitle(java.lang.String)
meth public void storeValue(java.lang.String,java.lang.String)
meth public void storeXpathCount(java.lang.String,java.lang.String)
meth public void submit(java.lang.String)
meth public void submitAndWait(java.lang.String)
meth public void type(java.lang.String,java.lang.String)
meth public void typeAndWait(java.lang.String,java.lang.String)
meth public void uncheck(java.lang.String)
meth public void uncheckAndWait(java.lang.String)
meth public void waitForAttribute(java.lang.String,java.lang.String)
meth public void waitForAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void waitForChecked(java.lang.String)
meth public void waitForClass(java.lang.String,java.lang.String)
meth public void waitForElementCount(java.lang.String,int)
meth public void waitForElementCount(java.lang.String,java.lang.String)
meth public void waitForElementPresent(java.lang.String)
meth public void waitForEval(java.lang.String,java.lang.String)
meth public void waitForNotAttribute(java.lang.String,java.lang.String)
meth public void waitForNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void waitForNotChecked(java.lang.String)
meth public void waitForNotClass(java.lang.String,java.lang.String)
meth public void waitForNotElementCount(java.lang.String,int)
meth public void waitForNotElementCount(java.lang.String,java.lang.String)
meth public void waitForNotElementPresent(java.lang.String)
meth public void waitForNotEval(java.lang.String,java.lang.String)
meth public void waitForNotSelectedId(java.lang.String,java.lang.String)
meth public void waitForNotSelectedIndex(java.lang.String,java.lang.String)
meth public void waitForNotSelectedLabel(java.lang.String,java.lang.String)
meth public void waitForNotSelectedValue(java.lang.String,java.lang.String)
meth public void waitForNotStyle(java.lang.String,java.lang.String)
meth public void waitForNotText(java.lang.String,java.lang.String)
meth public void waitForNotTextPresent(java.lang.String)
meth public void waitForNotTitle(java.lang.String)
meth public void waitForNotValue(java.lang.String,java.lang.String)
meth public void waitForNotVisible(java.lang.String)
meth public void waitForNotXpathCount(java.lang.String,int)
meth public void waitForNotXpathCount(java.lang.String,java.lang.String)
meth public void waitForPageToLoad()
meth public void waitForPopUp()
meth public void waitForPopUp(java.lang.String)
meth public void waitForPopUp(java.lang.String,java.lang.String)
meth public void waitForPopUp(java.lang.String,long)
meth public void waitForSelectedId(java.lang.String,java.lang.String)
meth public void waitForSelectedIndex(java.lang.String,java.lang.String)
meth public void waitForSelectedLabel(java.lang.String,java.lang.String)
meth public void waitForSelectedValue(java.lang.String,java.lang.String)
meth public void waitForStyle(java.lang.String,java.lang.String)
meth public void waitForText(java.lang.String,java.lang.String)
meth public void waitForTextPresent(java.lang.String)
meth public void waitForTitle(java.lang.String)
meth public void waitForValue(java.lang.String,java.lang.String)
meth public void waitForVisible(java.lang.String)
meth public void waitForXpathCount(java.lang.String,int)
meth public void waitForXpathCount(java.lang.String,java.lang.String)
supr java.lang.Object
hfds _adapter

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase
cons public init()
cons public init(java.lang.String)
cons public init(org.openqa.selenium.WebDriver)
cons public init(org.openqa.selenium.WebDriver,java.lang.String)
intf com.xceptance.xlt.api.engine.scripting.ScriptCommands
meth public boolean evaluatesToTrue(java.lang.String)
meth public boolean hasAttribute(java.lang.String,java.lang.String)
meth public boolean hasAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public boolean hasClass(java.lang.String,java.lang.String)
meth public boolean hasNotClass(java.lang.String,java.lang.String)
meth public boolean hasNotStyle(java.lang.String,java.lang.String)
meth public boolean hasStyle(java.lang.String,java.lang.String)
meth public boolean hasText(java.lang.String,java.lang.String)
meth public boolean hasTitle(java.lang.String)
meth public boolean hasValue(java.lang.String,java.lang.String)
meth public boolean isChecked(java.lang.String)
meth public boolean isElementPresent(java.lang.String)
meth public boolean isEnabled(java.lang.String)
meth public boolean isEvalMatching(java.lang.String,java.lang.String)
meth public boolean isTextPresent(java.lang.String)
meth public boolean isVisible(java.lang.String)
meth public final void __cleanUpAbstractWebDriverScriptTestCase()
meth public final void __setUpAbstractWebDriverScriptTestCase()
meth public int getElementCount(java.lang.String)
meth public int getXpathCount(java.lang.String)
meth public java.lang.String evaluate(java.lang.String)
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getAttribute(java.lang.String,java.lang.String)
meth public java.lang.String getPageText()
meth public java.lang.String getText(java.lang.String)
meth public java.lang.String getTitle()
meth public java.lang.String getValue(java.lang.String)
meth public java.lang.String resolve(java.lang.String)
meth public java.lang.String resolveKey(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElements(java.lang.String)
meth public org.openqa.selenium.WebElement findElement(java.lang.String)
meth public void addSelection(java.lang.String,java.lang.String)
meth public void assertAttribute(java.lang.String,java.lang.String)
meth public void assertAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void assertChecked(java.lang.String)
meth public void assertClass(java.lang.String,java.lang.String)
meth public void assertElementCount(java.lang.String,int)
meth public void assertElementCount(java.lang.String,java.lang.String)
meth public void assertElementPresent(java.lang.String)
meth public void assertEval(java.lang.String,java.lang.String)
meth public void assertLoadTime(java.lang.String)
meth public void assertLoadTime(long)
meth public void assertNotAttribute(java.lang.String,java.lang.String)
meth public void assertNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void assertNotChecked(java.lang.String)
meth public void assertNotClass(java.lang.String,java.lang.String)
meth public void assertNotElementCount(java.lang.String,int)
meth public void assertNotElementCount(java.lang.String,java.lang.String)
meth public void assertNotElementPresent(java.lang.String)
meth public void assertNotEval(java.lang.String,java.lang.String)
meth public void assertNotSelectedId(java.lang.String,java.lang.String)
meth public void assertNotSelectedIndex(java.lang.String,java.lang.String)
meth public void assertNotSelectedLabel(java.lang.String,java.lang.String)
meth public void assertNotSelectedValue(java.lang.String,java.lang.String)
meth public void assertNotStyle(java.lang.String,java.lang.String)
meth public void assertNotText(java.lang.String,java.lang.String)
meth public void assertNotTextPresent(java.lang.String)
meth public void assertNotTitle(java.lang.String)
meth public void assertNotValue(java.lang.String,java.lang.String)
meth public void assertNotVisible(java.lang.String)
meth public void assertNotXpathCount(java.lang.String,int)
meth public void assertNotXpathCount(java.lang.String,java.lang.String)
meth public void assertPageSize(java.lang.String)
meth public void assertPageSize(long)
meth public void assertSelectedId(java.lang.String,java.lang.String)
meth public void assertSelectedIndex(java.lang.String,java.lang.String)
meth public void assertSelectedLabel(java.lang.String,java.lang.String)
meth public void assertSelectedValue(java.lang.String,java.lang.String)
meth public void assertStyle(java.lang.String,java.lang.String)
meth public void assertText(java.lang.String,java.lang.String)
meth public void assertTextPresent(java.lang.String)
meth public void assertTitle(java.lang.String)
meth public void assertValue(java.lang.String,java.lang.String)
meth public void assertVisible(java.lang.String)
meth public void assertXpathCount(java.lang.String,int)
meth public void assertXpathCount(java.lang.String,java.lang.String)
meth public void check(java.lang.String)
meth public void checkAndWait(java.lang.String)
meth public void click(java.lang.String)
meth public void clickAndWait(java.lang.String)
meth public void close()
meth public void contextMenu(java.lang.String)
meth public void contextMenuAt(java.lang.String,int,int)
meth public void contextMenuAt(java.lang.String,java.lang.String)
meth public void createCookie(java.lang.String)
meth public void createCookie(java.lang.String,java.lang.String)
meth public void deleteAllVisibleCookies()
meth public void deleteCookie(java.lang.String)
meth public void deleteCookie(java.lang.String,java.lang.String)
meth public void doubleClick(java.lang.String)
meth public void doubleClickAndWait(java.lang.String)
meth public void echo(java.lang.String)
meth public void mouseDown(java.lang.String)
meth public void mouseDownAt(java.lang.String,int,int)
meth public void mouseDownAt(java.lang.String,java.lang.String)
meth public void mouseMove(java.lang.String)
meth public void mouseMoveAt(java.lang.String,int,int)
meth public void mouseMoveAt(java.lang.String,java.lang.String)
meth public void mouseOut(java.lang.String)
meth public void mouseOver(java.lang.String)
meth public void mouseUp(java.lang.String)
meth public void mouseUpAt(java.lang.String,int,int)
meth public void mouseUpAt(java.lang.String,java.lang.String)
meth public void open(java.lang.String)
meth public void open(java.net.URL)
meth public void pause(java.lang.String)
meth public void pause(long)
meth public void removeSelection(java.lang.String,java.lang.String)
meth public void select(java.lang.String,java.lang.String)
meth public void selectAndWait(java.lang.String,java.lang.String)
meth public void selectFrame(java.lang.String)
meth public void selectWindow()
meth public void selectWindow(java.lang.String)
meth public void setTimeout(java.lang.String)
meth public void setTimeout(long)
meth public void startAction(java.lang.String)
meth public void stopAction()
meth public void store(java.lang.String,java.lang.String)
meth public void storeAttribute(java.lang.String,java.lang.String)
meth public void storeAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void storeElementCount(java.lang.String,java.lang.String)
meth public void storeEval(java.lang.String,java.lang.String)
meth public void storeText(java.lang.String,java.lang.String)
meth public void storeTitle(java.lang.String)
meth public void storeValue(java.lang.String,java.lang.String)
meth public void storeXpathCount(java.lang.String,java.lang.String)
meth public void submit(java.lang.String)
meth public void submitAndWait(java.lang.String)
meth public void type(java.lang.String,java.lang.String)
meth public void typeAndWait(java.lang.String,java.lang.String)
meth public void uncheck(java.lang.String)
meth public void uncheckAndWait(java.lang.String)
meth public void waitForAttribute(java.lang.String,java.lang.String)
meth public void waitForAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void waitForChecked(java.lang.String)
meth public void waitForClass(java.lang.String,java.lang.String)
meth public void waitForElementCount(java.lang.String,int)
meth public void waitForElementCount(java.lang.String,java.lang.String)
meth public void waitForElementPresent(java.lang.String)
meth public void waitForEval(java.lang.String,java.lang.String)
meth public void waitForNotAttribute(java.lang.String,java.lang.String)
meth public void waitForNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public void waitForNotChecked(java.lang.String)
meth public void waitForNotClass(java.lang.String,java.lang.String)
meth public void waitForNotElementCount(java.lang.String,int)
meth public void waitForNotElementCount(java.lang.String,java.lang.String)
meth public void waitForNotElementPresent(java.lang.String)
meth public void waitForNotEval(java.lang.String,java.lang.String)
meth public void waitForNotSelectedId(java.lang.String,java.lang.String)
meth public void waitForNotSelectedIndex(java.lang.String,java.lang.String)
meth public void waitForNotSelectedLabel(java.lang.String,java.lang.String)
meth public void waitForNotSelectedValue(java.lang.String,java.lang.String)
meth public void waitForNotStyle(java.lang.String,java.lang.String)
meth public void waitForNotText(java.lang.String,java.lang.String)
meth public void waitForNotTextPresent(java.lang.String)
meth public void waitForNotTitle(java.lang.String)
meth public void waitForNotValue(java.lang.String,java.lang.String)
meth public void waitForNotVisible(java.lang.String)
meth public void waitForNotXpathCount(java.lang.String,int)
meth public void waitForNotXpathCount(java.lang.String,java.lang.String)
meth public void waitForPageToLoad()
meth public void waitForPopUp()
meth public void waitForPopUp(java.lang.String)
meth public void waitForPopUp(java.lang.String,java.lang.String)
meth public void waitForPopUp(java.lang.String,long)
meth public void waitForSelectedId(java.lang.String,java.lang.String)
meth public void waitForSelectedIndex(java.lang.String,java.lang.String)
meth public void waitForSelectedLabel(java.lang.String,java.lang.String)
meth public void waitForSelectedValue(java.lang.String,java.lang.String)
meth public void waitForStyle(java.lang.String,java.lang.String)
meth public void waitForText(java.lang.String,java.lang.String)
meth public void waitForTextPresent(java.lang.String)
meth public void waitForTitle(java.lang.String)
meth public void waitForValue(java.lang.String,java.lang.String)
meth public void waitForVisible(java.lang.String)
meth public void waitForXpathCount(java.lang.String,int)
meth public void waitForXpathCount(java.lang.String,java.lang.String)
supr com.xceptance.xlt.api.tests.AbstractWebDriverTestCase
hfds _adapter

CLSS public abstract interface com.xceptance.xlt.api.engine.scripting.ScriptCommands
meth public abstract void addSelection(java.lang.String,java.lang.String)
meth public abstract void assertAttribute(java.lang.String,java.lang.String)
meth public abstract void assertAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void assertChecked(java.lang.String)
meth public abstract void assertClass(java.lang.String,java.lang.String)
meth public abstract void assertElementCount(java.lang.String,int)
meth public abstract void assertElementCount(java.lang.String,java.lang.String)
meth public abstract void assertElementPresent(java.lang.String)
meth public abstract void assertEval(java.lang.String,java.lang.String)
meth public abstract void assertLoadTime(java.lang.String)
meth public abstract void assertLoadTime(long)
meth public abstract void assertNotAttribute(java.lang.String,java.lang.String)
meth public abstract void assertNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void assertNotChecked(java.lang.String)
meth public abstract void assertNotClass(java.lang.String,java.lang.String)
meth public abstract void assertNotElementCount(java.lang.String,int)
meth public abstract void assertNotElementCount(java.lang.String,java.lang.String)
meth public abstract void assertNotElementPresent(java.lang.String)
meth public abstract void assertNotEval(java.lang.String,java.lang.String)
meth public abstract void assertNotSelectedId(java.lang.String,java.lang.String)
meth public abstract void assertNotSelectedIndex(java.lang.String,java.lang.String)
meth public abstract void assertNotSelectedLabel(java.lang.String,java.lang.String)
meth public abstract void assertNotSelectedValue(java.lang.String,java.lang.String)
meth public abstract void assertNotStyle(java.lang.String,java.lang.String)
meth public abstract void assertNotText(java.lang.String,java.lang.String)
meth public abstract void assertNotTextPresent(java.lang.String)
meth public abstract void assertNotTitle(java.lang.String)
meth public abstract void assertNotValue(java.lang.String,java.lang.String)
meth public abstract void assertNotVisible(java.lang.String)
meth public abstract void assertNotXpathCount(java.lang.String,int)
meth public abstract void assertNotXpathCount(java.lang.String,java.lang.String)
meth public abstract void assertPageSize(java.lang.String)
meth public abstract void assertPageSize(long)
meth public abstract void assertSelectedId(java.lang.String,java.lang.String)
meth public abstract void assertSelectedIndex(java.lang.String,java.lang.String)
meth public abstract void assertSelectedLabel(java.lang.String,java.lang.String)
meth public abstract void assertSelectedValue(java.lang.String,java.lang.String)
meth public abstract void assertStyle(java.lang.String,java.lang.String)
meth public abstract void assertText(java.lang.String,java.lang.String)
meth public abstract void assertTextPresent(java.lang.String)
meth public abstract void assertTitle(java.lang.String)
meth public abstract void assertValue(java.lang.String,java.lang.String)
meth public abstract void assertVisible(java.lang.String)
meth public abstract void assertXpathCount(java.lang.String,int)
meth public abstract void assertXpathCount(java.lang.String,java.lang.String)
meth public abstract void check(java.lang.String)
meth public abstract void checkAndWait(java.lang.String)
meth public abstract void click(java.lang.String)
meth public abstract void clickAndWait(java.lang.String)
meth public abstract void close()
meth public abstract void contextMenu(java.lang.String)
meth public abstract void contextMenuAt(java.lang.String,int,int)
meth public abstract void contextMenuAt(java.lang.String,java.lang.String)
meth public abstract void createCookie(java.lang.String)
meth public abstract void createCookie(java.lang.String,java.lang.String)
meth public abstract void deleteAllVisibleCookies()
meth public abstract void deleteCookie(java.lang.String)
meth public abstract void deleteCookie(java.lang.String,java.lang.String)
meth public abstract void doubleClick(java.lang.String)
meth public abstract void doubleClickAndWait(java.lang.String)
meth public abstract void echo(java.lang.String)
meth public abstract void mouseDown(java.lang.String)
meth public abstract void mouseDownAt(java.lang.String,int,int)
meth public abstract void mouseDownAt(java.lang.String,java.lang.String)
meth public abstract void mouseMove(java.lang.String)
meth public abstract void mouseMoveAt(java.lang.String,int,int)
meth public abstract void mouseMoveAt(java.lang.String,java.lang.String)
meth public abstract void mouseOut(java.lang.String)
meth public abstract void mouseOver(java.lang.String)
meth public abstract void mouseUp(java.lang.String)
meth public abstract void mouseUpAt(java.lang.String,int,int)
meth public abstract void mouseUpAt(java.lang.String,java.lang.String)
meth public abstract void open(java.lang.String)
meth public abstract void pause(java.lang.String)
meth public abstract void pause(long)
meth public abstract void removeSelection(java.lang.String,java.lang.String)
meth public abstract void select(java.lang.String,java.lang.String)
meth public abstract void selectAndWait(java.lang.String,java.lang.String)
meth public abstract void selectFrame(java.lang.String)
meth public abstract void selectWindow()
meth public abstract void selectWindow(java.lang.String)
meth public abstract void setTimeout(java.lang.String)
meth public abstract void setTimeout(long)
meth public abstract void startAction(java.lang.String)
meth public abstract void store(java.lang.String,java.lang.String)
meth public abstract void storeAttribute(java.lang.String,java.lang.String)
meth public abstract void storeAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void storeElementCount(java.lang.String,java.lang.String)
meth public abstract void storeEval(java.lang.String,java.lang.String)
meth public abstract void storeText(java.lang.String,java.lang.String)
meth public abstract void storeTitle(java.lang.String)
meth public abstract void storeValue(java.lang.String,java.lang.String)
meth public abstract void storeXpathCount(java.lang.String,java.lang.String)
meth public abstract void submit(java.lang.String)
meth public abstract void submitAndWait(java.lang.String)
meth public abstract void type(java.lang.String,java.lang.String)
meth public abstract void typeAndWait(java.lang.String,java.lang.String)
meth public abstract void uncheck(java.lang.String)
meth public abstract void uncheckAndWait(java.lang.String)
meth public abstract void waitForAttribute(java.lang.String,java.lang.String)
meth public abstract void waitForAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void waitForChecked(java.lang.String)
meth public abstract void waitForClass(java.lang.String,java.lang.String)
meth public abstract void waitForElementCount(java.lang.String,int)
meth public abstract void waitForElementCount(java.lang.String,java.lang.String)
meth public abstract void waitForElementPresent(java.lang.String)
meth public abstract void waitForEval(java.lang.String,java.lang.String)
meth public abstract void waitForNotAttribute(java.lang.String,java.lang.String)
meth public abstract void waitForNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void waitForNotChecked(java.lang.String)
meth public abstract void waitForNotClass(java.lang.String,java.lang.String)
meth public abstract void waitForNotElementCount(java.lang.String,int)
meth public abstract void waitForNotElementCount(java.lang.String,java.lang.String)
meth public abstract void waitForNotElementPresent(java.lang.String)
meth public abstract void waitForNotEval(java.lang.String,java.lang.String)
meth public abstract void waitForNotSelectedId(java.lang.String,java.lang.String)
meth public abstract void waitForNotSelectedIndex(java.lang.String,java.lang.String)
meth public abstract void waitForNotSelectedLabel(java.lang.String,java.lang.String)
meth public abstract void waitForNotSelectedValue(java.lang.String,java.lang.String)
meth public abstract void waitForNotStyle(java.lang.String,java.lang.String)
meth public abstract void waitForNotText(java.lang.String,java.lang.String)
meth public abstract void waitForNotTextPresent(java.lang.String)
meth public abstract void waitForNotTitle(java.lang.String)
meth public abstract void waitForNotValue(java.lang.String,java.lang.String)
meth public abstract void waitForNotVisible(java.lang.String)
meth public abstract void waitForNotXpathCount(java.lang.String,int)
meth public abstract void waitForNotXpathCount(java.lang.String,java.lang.String)
meth public abstract void waitForPageToLoad()
meth public abstract void waitForPopUp()
meth public abstract void waitForPopUp(java.lang.String)
meth public abstract void waitForPopUp(java.lang.String,java.lang.String)
meth public abstract void waitForPopUp(java.lang.String,long)
meth public abstract void waitForSelectedId(java.lang.String,java.lang.String)
meth public abstract void waitForSelectedIndex(java.lang.String,java.lang.String)
meth public abstract void waitForSelectedLabel(java.lang.String,java.lang.String)
meth public abstract void waitForSelectedValue(java.lang.String,java.lang.String)
meth public abstract void waitForStyle(java.lang.String,java.lang.String)
meth public abstract void waitForText(java.lang.String,java.lang.String)
meth public abstract void waitForTextPresent(java.lang.String)
meth public abstract void waitForTitle(java.lang.String)
meth public abstract void waitForValue(java.lang.String,java.lang.String)
meth public abstract void waitForVisible(java.lang.String)
meth public abstract void waitForXpathCount(java.lang.String,int)
meth public abstract void waitForXpathCount(java.lang.String,java.lang.String)

CLSS public abstract interface !annotation com.xceptance.xlt.api.engine.scripting.ScriptName
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public com.xceptance.xlt.api.engine.scripting.ScriptTestCaseSuite
cons public init()
meth public static java.util.List<java.lang.String> getTestCases()
supr java.lang.Object
hfds PROP_TEST_CASES

CLSS public com.xceptance.xlt.api.engine.scripting.StaticScriptCommands
cons public init()
meth public final static org.openqa.selenium.WebDriver getWebDriver()
meth public static boolean evaluatesToTrue(java.lang.String)
meth public static boolean hasAttribute(java.lang.String,java.lang.String)
meth public static boolean hasAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public static boolean hasClass(java.lang.String,java.lang.String)
meth public static boolean hasNotClass(java.lang.String,java.lang.String)
meth public static boolean hasNotStyle(java.lang.String,java.lang.String)
meth public static boolean hasStyle(java.lang.String,java.lang.String)
meth public static boolean hasText(java.lang.String,java.lang.String)
meth public static boolean hasTitle(java.lang.String)
meth public static boolean hasValue(java.lang.String,java.lang.String)
meth public static boolean isChecked(java.lang.String)
meth public static boolean isElementPresent(java.lang.String)
meth public static boolean isEnabled(java.lang.String)
meth public static boolean isEvalMatching(java.lang.String,java.lang.String)
meth public static boolean isTextPresent(java.lang.String)
meth public static boolean isVisible(java.lang.String)
meth public static int getElementCount(java.lang.String)
meth public static int getXpathCount(java.lang.String)
meth public static java.lang.String evaluate(java.lang.String)
meth public static java.lang.String getAttribute(java.lang.String)
meth public static java.lang.String getAttribute(java.lang.String,java.lang.String)
meth public static java.lang.String getPageText()
meth public static java.lang.String getText(java.lang.String)
meth public static java.lang.String getTitle()
meth public static java.lang.String getValue(java.lang.String)
meth public static java.lang.String resolve(java.lang.String)
meth public static java.lang.String resolveKey(java.lang.String)
meth public static java.util.List<org.openqa.selenium.WebElement> findElements(java.lang.String)
meth public static org.openqa.selenium.WebElement findElement(java.lang.String)
meth public static void addSelection(java.lang.String,java.lang.String)
meth public static void assertAttribute(java.lang.String,java.lang.String)
meth public static void assertAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertChecked(java.lang.String)
meth public static void assertClass(java.lang.String,java.lang.String)
meth public static void assertElementCount(java.lang.String,int)
meth public static void assertElementCount(java.lang.String,java.lang.String)
meth public static void assertElementPresent(java.lang.String)
meth public static void assertEval(java.lang.String,java.lang.String)
meth public static void assertLoadTime(java.lang.String)
meth public static void assertLoadTime(long)
meth public static void assertNotAttribute(java.lang.String,java.lang.String)
meth public static void assertNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public static void assertNotChecked(java.lang.String)
meth public static void assertNotClass(java.lang.String,java.lang.String)
meth public static void assertNotElementCount(java.lang.String,int)
meth public static void assertNotElementCount(java.lang.String,java.lang.String)
meth public static void assertNotElementPresent(java.lang.String)
meth public static void assertNotEval(java.lang.String,java.lang.String)
meth public static void assertNotSelectedId(java.lang.String,java.lang.String)
meth public static void assertNotSelectedIndex(java.lang.String,java.lang.String)
meth public static void assertNotSelectedLabel(java.lang.String,java.lang.String)
meth public static void assertNotSelectedValue(java.lang.String,java.lang.String)
meth public static void assertNotStyle(java.lang.String,java.lang.String)
meth public static void assertNotText(java.lang.String,java.lang.String)
meth public static void assertNotTextPresent(java.lang.String)
meth public static void assertNotTitle(java.lang.String)
meth public static void assertNotValue(java.lang.String,java.lang.String)
meth public static void assertNotVisible(java.lang.String)
meth public static void assertNotXpathCount(java.lang.String,int)
meth public static void assertNotXpathCount(java.lang.String,java.lang.String)
meth public static void assertPageSize(java.lang.String)
meth public static void assertPageSize(long)
meth public static void assertSelectedId(java.lang.String,java.lang.String)
meth public static void assertSelectedIndex(java.lang.String,java.lang.String)
meth public static void assertSelectedLabel(java.lang.String,java.lang.String)
meth public static void assertSelectedValue(java.lang.String,java.lang.String)
meth public static void assertStyle(java.lang.String,java.lang.String)
meth public static void assertText(java.lang.String,java.lang.String)
meth public static void assertTextPresent(java.lang.String)
meth public static void assertTitle(java.lang.String)
meth public static void assertValue(java.lang.String,java.lang.String)
meth public static void assertVisible(java.lang.String)
meth public static void assertXpathCount(java.lang.String,int)
meth public static void assertXpathCount(java.lang.String,java.lang.String)
meth public static void check(java.lang.String)
meth public static void checkAndWait(java.lang.String)
meth public static void click(java.lang.String)
meth public static void clickAndWait(java.lang.String)
meth public static void close()
meth public static void contextMenu(java.lang.String)
meth public static void contextMenuAt(java.lang.String,int,int)
meth public static void contextMenuAt(java.lang.String,java.lang.String)
meth public static void createCookie(java.lang.String)
meth public static void createCookie(java.lang.String,java.lang.String)
meth public static void deleteAllVisibleCookies()
meth public static void deleteCookie(java.lang.String)
meth public static void deleteCookie(java.lang.String,java.lang.String)
meth public static void doubleClick(java.lang.String)
meth public static void doubleClickAndWait(java.lang.String)
meth public static void echo(java.lang.String)
meth public static void mouseDown(java.lang.String)
meth public static void mouseDownAt(java.lang.String,int,int)
meth public static void mouseDownAt(java.lang.String,java.lang.String)
meth public static void mouseMove(java.lang.String)
meth public static void mouseMoveAt(java.lang.String,int,int)
meth public static void mouseMoveAt(java.lang.String,java.lang.String)
meth public static void mouseOut(java.lang.String)
meth public static void mouseOver(java.lang.String)
meth public static void mouseUp(java.lang.String)
meth public static void mouseUpAt(java.lang.String,int,int)
meth public static void mouseUpAt(java.lang.String,java.lang.String)
meth public static void open(java.lang.String)
meth public static void pause(java.lang.String)
meth public static void pause(long)
meth public static void removeSelection(java.lang.String,java.lang.String)
meth public static void select(java.lang.String,java.lang.String)
meth public static void selectAndWait(java.lang.String,java.lang.String)
meth public static void selectFrame(java.lang.String)
meth public static void selectWindow()
meth public static void selectWindow(java.lang.String)
meth public static void setTimeout(java.lang.String)
meth public static void setTimeout(long)
meth public static void startAction(java.lang.String)
meth public static void stopAction()
meth public static void store(java.lang.String,java.lang.String)
meth public static void storeAttribute(java.lang.String,java.lang.String)
meth public static void storeAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public static void storeElementCount(java.lang.String,java.lang.String)
meth public static void storeEval(java.lang.String,java.lang.String)
meth public static void storeText(java.lang.String,java.lang.String)
meth public static void storeTitle(java.lang.String)
meth public static void storeValue(java.lang.String,java.lang.String)
meth public static void storeXpathCount(java.lang.String,java.lang.String)
meth public static void submit(java.lang.String)
meth public static void submitAndWait(java.lang.String)
meth public static void type(java.lang.String,java.lang.String)
meth public static void typeAndWait(java.lang.String,java.lang.String)
meth public static void uncheck(java.lang.String)
meth public static void uncheckAndWait(java.lang.String)
meth public static void waitForAttribute(java.lang.String,java.lang.String)
meth public static void waitForAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public static void waitForChecked(java.lang.String)
meth public static void waitForClass(java.lang.String,java.lang.String)
meth public static void waitForElementCount(java.lang.String,int)
meth public static void waitForElementCount(java.lang.String,java.lang.String)
meth public static void waitForElementPresent(java.lang.String)
meth public static void waitForEval(java.lang.String,java.lang.String)
meth public static void waitForNotAttribute(java.lang.String,java.lang.String)
meth public static void waitForNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth public static void waitForNotChecked(java.lang.String)
meth public static void waitForNotClass(java.lang.String,java.lang.String)
meth public static void waitForNotElementCount(java.lang.String,int)
meth public static void waitForNotElementCount(java.lang.String,java.lang.String)
meth public static void waitForNotElementPresent(java.lang.String)
meth public static void waitForNotEval(java.lang.String,java.lang.String)
meth public static void waitForNotSelectedId(java.lang.String,java.lang.String)
meth public static void waitForNotSelectedIndex(java.lang.String,java.lang.String)
meth public static void waitForNotSelectedLabel(java.lang.String,java.lang.String)
meth public static void waitForNotSelectedValue(java.lang.String,java.lang.String)
meth public static void waitForNotStyle(java.lang.String,java.lang.String)
meth public static void waitForNotText(java.lang.String,java.lang.String)
meth public static void waitForNotTextPresent(java.lang.String)
meth public static void waitForNotTitle(java.lang.String)
meth public static void waitForNotValue(java.lang.String,java.lang.String)
meth public static void waitForNotVisible(java.lang.String)
meth public static void waitForNotXpathCount(java.lang.String,int)
meth public static void waitForNotXpathCount(java.lang.String,java.lang.String)
meth public static void waitForPageToLoad()
meth public static void waitForPopUp()
meth public static void waitForPopUp(java.lang.String)
meth public static void waitForPopUp(java.lang.String,java.lang.String)
meth public static void waitForPopUp(java.lang.String,long)
meth public static void waitForSelectedId(java.lang.String,java.lang.String)
meth public static void waitForSelectedIndex(java.lang.String,java.lang.String)
meth public static void waitForSelectedLabel(java.lang.String,java.lang.String)
meth public static void waitForSelectedValue(java.lang.String,java.lang.String)
meth public static void waitForStyle(java.lang.String,java.lang.String)
meth public static void waitForText(java.lang.String,java.lang.String)
meth public static void waitForTextPresent(java.lang.String)
meth public static void waitForTitle(java.lang.String)
meth public static void waitForValue(java.lang.String,java.lang.String)
meth public static void waitForVisible(java.lang.String)
meth public static void waitForXpathCount(java.lang.String,int)
meth public static void waitForXpathCount(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public abstract interface com.xceptance.xlt.api.engine.scripting.WebDriverCustomModule
meth public abstract !varargs void execute(org.openqa.selenium.WebDriver,java.lang.String[])

CLSS public com.xceptance.xlt.api.htmlunit.LightWeightPage
cons public init(org.htmlunit.WebResponse,java.lang.String)
meth public int getHttpResponseCode()
meth public java.lang.String getContent()
meth public java.lang.String getContentCharset()
meth public java.lang.String getTimerName()
meth public java.nio.charset.Charset getCharset()
meth public org.htmlunit.WebResponse getWebResponse()
supr java.lang.Object
hfds charset,name,response

CLSS public abstract com.xceptance.xlt.api.report.AbstractReportProvider
cons public init()
intf com.xceptance.xlt.api.report.ReportProvider
meth public boolean lock()
meth public com.xceptance.xlt.api.report.ReportProviderConfiguration getConfiguration()
meth public void processAll(com.xceptance.xlt.api.report.PostProcessedDataContainer)
meth public void setConfiguration(com.xceptance.xlt.api.report.ReportProviderConfiguration)
meth public void unlock()
supr java.lang.Object
hfds configuration,lock

CLSS public com.xceptance.xlt.api.report.PostProcessedDataContainer
cons public init(int,int)
fld public final int sampleFactor
fld public final java.util.List<com.xceptance.xlt.api.engine.Data> data
fld public int droppedLines
meth public final long getMaximumTime()
meth public final long getMinimumTime()
meth public java.util.List<com.xceptance.xlt.api.engine.Data> getData()
meth public void add(com.xceptance.xlt.api.engine.Data)
supr java.lang.Object
hfds maximumTime,minimumTime

CLSS public abstract interface com.xceptance.xlt.api.report.ReportCreator
meth public abstract java.lang.Object createReportFragment()

CLSS public abstract interface com.xceptance.xlt.api.report.ReportProvider
intf com.xceptance.xlt.api.report.ReportCreator
meth public abstract boolean lock()
meth public abstract void processAll(com.xceptance.xlt.api.report.PostProcessedDataContainer)
meth public abstract void processDataRecord(com.xceptance.xlt.api.engine.Data)
meth public abstract void setConfiguration(com.xceptance.xlt.api.report.ReportProviderConfiguration)
meth public abstract void unlock()
meth public boolean wantsDataRecords()

CLSS public abstract interface com.xceptance.xlt.api.report.ReportProviderConfiguration
meth public abstract boolean shouldChartsGenerated()
meth public abstract int getChartHeight()
meth public abstract int getChartWidth()
meth public abstract int getMovingAveragePercentage()
meth public abstract java.io.File getChartDirectory()
meth public abstract java.io.File getCsvDirectory()
meth public abstract java.io.File getReportDirectory()
meth public abstract java.util.Properties getProperties()
meth public abstract long getChartEndTime()
meth public abstract long getChartStartTime()

CLSS public abstract com.xceptance.xlt.api.report.external.AbstractLineParser
cons public init()
meth protected java.text.DateFormat getDateFormat()
meth protected long parseTime(java.lang.String)
meth public abstract com.xceptance.xlt.api.report.external.ValueSet parse(java.lang.String)
meth public java.util.Properties getProperties()
meth public java.util.Set<java.lang.String> getValueNames()
meth public void setProperties(java.util.Properties)
meth public void setValueNames(java.util.Set<java.lang.String>)
supr java.lang.Object
hfds FORMAT,PROP_FORMAT_PATTERN,PROP_FORMAT_TIMEZONE,properties,valueNames

CLSS public com.xceptance.xlt.api.report.external.HeadedCsvParser
cons public init()
meth protected java.lang.String getName(int)
meth public com.xceptance.xlt.api.report.external.ValueSet parse(java.lang.String)
supr com.xceptance.xlt.api.report.external.SimpleCsvParser
hfds heads

CLSS public com.xceptance.xlt.api.report.external.NamedData
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String,double)
meth public double getValue()
meth public java.lang.String getName()
supr java.lang.Object
hfds name,value

CLSS public com.xceptance.xlt.api.report.external.PlainDataTableCsvParser
cons public init()
meth protected char getFieldSeparator()
meth protected java.lang.String getName(int)
meth public com.xceptance.xlt.api.report.external.ValueSet parse(java.lang.String)
supr com.xceptance.xlt.api.report.external.AbstractLineParser
hfds PROP_FIELD_SEPARATOR,fieldSeparator

CLSS public com.xceptance.xlt.api.report.external.SimpleCsvParser
cons public init()
meth protected char getFieldSeparator()
meth protected java.lang.String getName(int)
meth public com.xceptance.xlt.api.report.external.ValueSet parse(java.lang.String)
supr com.xceptance.xlt.api.report.external.AbstractLineParser
hfds PROP_FIELD_SEPARATOR,fieldSeparator

CLSS public com.xceptance.xlt.api.report.external.ValueSet
cons public init(long)
meth public java.util.Map<java.lang.String,java.lang.Object> getValues()
meth public long getTime()
meth public void addValue(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds data,timestamp

CLSS public abstract com.xceptance.xlt.api.tests.AbstractTestCase
cons public init()
meth protected java.lang.String getEffectiveKey(java.lang.String)
meth protected java.lang.String getSimpleName()
meth protected java.lang.String getTestName()
meth protected void setTestName()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected void setTestName(java.lang.String)
meth public boolean getProperty(java.lang.String,boolean)
meth public final void __setup()
meth public final void __tearDown()
meth public int getProperty(java.lang.String,int)
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String getProperty(java.lang.String,java.lang.String)
meth public java.lang.String reconfigureStartUrl(java.lang.String) throws java.lang.Exception
meth public java.util.Map<java.lang.String,java.lang.String> getTestDataSet()
meth public void setTestDataSet(java.util.Map<java.lang.String,java.lang.String>)
meth public void setUp()
meth public void tearDown()
supr java.lang.Object
hfds startTime,testDataSet,testName

CLSS public abstract com.xceptance.xlt.api.tests.AbstractWebDriverTestCase
cons public init()
meth public final void __quitWebDriver()
meth public org.openqa.selenium.WebDriver getWebDriver()
meth public void setWebDriver(org.openqa.selenium.WebDriver)
supr com.xceptance.xlt.api.tests.AbstractTestCase
hfds autoClose,webDriver

CLSS public abstract com.xceptance.xlt.api.util.AbstractResponseProcessor
cons public init()
intf com.xceptance.xlt.api.util.ResponseProcessor
meth protected org.htmlunit.WebResponse createWebResponse(org.htmlunit.WebResponse,byte[])
meth protected org.htmlunit.WebResponse createWebResponse(org.htmlunit.WebResponse,java.lang.String)
supr java.lang.Object
hcls ModifiedWebResponseData

CLSS public com.xceptance.xlt.api.util.BasicPageUtils
cons public init()
meth public static <%0 extends java.lang.Object> {%%0} pickOneRandomly(java.util.List<{%%0}>)
meth public static <%0 extends java.lang.Object> {%%0} pickOneRandomly(java.util.List<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> {%%0} pickOneRandomly(java.util.List<{%%0}>,boolean,boolean)
meth public static java.lang.String getAbsoluteUrl(java.lang.String,java.lang.String) throws java.net.MalformedURLException
meth public static java.lang.String getAbsoluteUrl(java.net.URL,java.lang.String) throws java.net.MalformedURLException
supr java.lang.Object

CLSS public com.xceptance.xlt.api.util.HtmlPageUtils
cons public init()
meth public !varargs static org.htmlunit.html.HtmlPage getFramePage(org.htmlunit.html.HtmlPage,java.lang.String[])
meth public static <%0 extends java.lang.Object> {%%0} findHtmlElementsAndPickOne(org.htmlunit.html.HtmlPage,java.lang.String)
meth public static <%0 extends java.lang.Object> {%%0} findHtmlElementsAndPickOne(org.htmlunit.html.HtmlPage,java.lang.String,boolean)
meth public static <%0 extends org.htmlunit.html.HtmlElement> java.util.List<{%%0}> findHtmlElements(org.htmlunit.html.HtmlElement,java.lang.String)
meth public static <%0 extends org.htmlunit.html.HtmlElement> java.util.List<{%%0}> findHtmlElements(org.htmlunit.html.HtmlPage,java.lang.String)
meth public static <%0 extends org.htmlunit.html.HtmlElement> java.util.List<{%%0}> waitForHtmlElements(org.htmlunit.html.HtmlPage,java.lang.String,long) throws java.lang.InterruptedException
meth public static <%0 extends org.htmlunit.html.HtmlElement> {%%0} createHtmlElement(java.lang.String,org.htmlunit.html.HtmlElement)
meth public static <%0 extends org.htmlunit.html.HtmlElement> {%%0} findHtmlElementsAndPickOne(org.htmlunit.html.HtmlElement,java.lang.String)
meth public static <%0 extends org.htmlunit.html.HtmlElement> {%%0} findHtmlElementsAndPickOne(org.htmlunit.html.HtmlElement,java.lang.String,boolean)
meth public static <%0 extends org.htmlunit.html.HtmlElement> {%%0} findHtmlElementsAndPickOne(org.htmlunit.html.HtmlElement,java.lang.String,boolean,boolean)
meth public static <%0 extends org.htmlunit.html.HtmlElement> {%%0} findHtmlElementsAndPickOne(org.htmlunit.html.HtmlPage,java.lang.String,boolean,boolean)
meth public static <%0 extends org.htmlunit.html.HtmlElement> {%%0} findSingleHtmlElementByID(org.htmlunit.html.HtmlPage,java.lang.String)
meth public static <%0 extends org.htmlunit.html.HtmlElement> {%%0} findSingleHtmlElementByXPath(org.htmlunit.html.HtmlElement,java.lang.String)
meth public static <%0 extends org.htmlunit.html.HtmlElement> {%%0} findSingleHtmlElementByXPath(org.htmlunit.html.HtmlPage,java.lang.String)
meth public static <%0 extends org.htmlunit.html.HtmlInput> {%%0} getInputEndingWith(org.htmlunit.html.HtmlForm,java.lang.String)
meth public static <%0 extends org.htmlunit.html.HtmlInput> {%%0} getInputStartingWith(org.htmlunit.html.HtmlForm,java.lang.String)
meth public static boolean isElementPresent(org.htmlunit.html.HtmlElement,java.lang.String)
meth public static boolean isElementPresent(org.htmlunit.html.HtmlPage,java.lang.String)
meth public static int countElementsByXPath(org.htmlunit.html.HtmlPage,java.lang.String)
meth public static java.util.List<org.htmlunit.html.HtmlForm> getFormsByIDRegExp(org.htmlunit.html.HtmlPage,java.util.regex.Pattern)
meth public static java.util.List<org.htmlunit.html.HtmlForm> getFormsByNameRegExp(org.htmlunit.html.HtmlPage,java.util.regex.Pattern)
meth public static org.htmlunit.html.HtmlAnchor getAnchorWithText(org.htmlunit.html.HtmlPage,java.lang.String)
meth public static org.htmlunit.html.HtmlInput createInput(org.htmlunit.html.HtmlForm,java.lang.String,java.lang.String,java.lang.String)
meth public static org.htmlunit.html.HtmlSelect getSelectEndingWith(org.htmlunit.html.HtmlForm,java.lang.String)
meth public static org.htmlunit.html.HtmlSelect getSelectStartingWith(org.htmlunit.html.HtmlForm,java.lang.String)
meth public static void checkRadioButton(org.htmlunit.html.HtmlForm,java.lang.String,int)
meth public static void checkRadioButton(org.htmlunit.html.HtmlForm,java.lang.String,java.lang.String)
meth public static void checkRadioButtonRandomly(org.htmlunit.html.HtmlForm,java.lang.String)
meth public static void checkRadioButtonRandomly(org.htmlunit.html.HtmlForm,java.lang.String,boolean,boolean)
meth public static void select(org.htmlunit.html.HtmlForm,java.lang.String,java.lang.String)
meth public static void selectRandomly(org.htmlunit.html.HtmlForm,java.lang.String)
meth public static void selectRandomly(org.htmlunit.html.HtmlForm,java.lang.String,boolean)
meth public static void selectRandomly(org.htmlunit.html.HtmlForm,java.lang.String,boolean,boolean)
meth public static void setCheckBoxValue(org.htmlunit.html.HtmlForm,java.lang.String,boolean)
meth public static void setInputValue(org.htmlunit.html.HtmlForm,java.lang.String,java.lang.String)
supr com.xceptance.xlt.api.util.BasicPageUtils

CLSS public com.xceptance.xlt.api.util.LightweightHtmlPageUtils
cons public init()
meth public static java.util.List<java.lang.String> getAllAnchorLinks(java.lang.String)
meth public static java.util.List<java.lang.String> getAllImageLinks(java.lang.String)
meth public static java.util.List<java.lang.String> getAllLinkLinks(java.lang.String)
meth public static java.util.List<java.lang.String> getAllScriptLinks(java.lang.String)
supr com.xceptance.xlt.api.util.BasicPageUtils

CLSS public com.xceptance.xlt.api.util.ResponseContentProcessor
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.util.regex.Pattern,java.lang.String)
cons public init(java.util.regex.Pattern,java.lang.String,java.util.regex.Pattern)
meth public org.htmlunit.WebResponse processResponse(org.htmlunit.WebResponse)
supr com.xceptance.xlt.api.util.AbstractResponseProcessor
hfds contentPattern,replacement,urlPattern

CLSS public abstract interface com.xceptance.xlt.api.util.ResponseProcessor
meth public abstract org.htmlunit.WebResponse processResponse(org.htmlunit.WebResponse)

CLSS public com.xceptance.xlt.api.util.SimpleArrayList<%0 extends java.lang.Object>
cons public init(int)
intf java.util.List<{com.xceptance.xlt.api.util.SimpleArrayList%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({com.xceptance.xlt.api.util.SimpleArrayList%0})
meth public boolean addAll(int,java.util.Collection<? extends {com.xceptance.xlt.api.util.SimpleArrayList%0}>)
meth public boolean addAll(java.util.Collection<? extends {com.xceptance.xlt.api.util.SimpleArrayList%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public int size()
meth public java.lang.Object[] toArray()
meth public java.util.Iterator<{com.xceptance.xlt.api.util.SimpleArrayList%0}> iterator()
meth public java.util.List<java.util.List<{com.xceptance.xlt.api.util.SimpleArrayList%0}>> partition(int)
meth public java.util.List<{com.xceptance.xlt.api.util.SimpleArrayList%0}> subList(int,int)
meth public java.util.ListIterator<{com.xceptance.xlt.api.util.SimpleArrayList%0}> listIterator()
meth public java.util.ListIterator<{com.xceptance.xlt.api.util.SimpleArrayList%0}> listIterator(int)
meth public void add(int,{com.xceptance.xlt.api.util.SimpleArrayList%0})
meth public void clear()
meth public {com.xceptance.xlt.api.util.SimpleArrayList%0} get(int)
meth public {com.xceptance.xlt.api.util.SimpleArrayList%0} remove(int)
meth public {com.xceptance.xlt.api.util.SimpleArrayList%0} set(int,{com.xceptance.xlt.api.util.SimpleArrayList%0})
supr java.lang.Object
hfds data,size
hcls Partition

CLSS public com.xceptance.xlt.api.util.URLUtils
cons public init()
meth public static java.lang.String makeLinkAbsolute(java.lang.String,java.lang.String)
meth public static java.lang.String makeLinkAbsolute(java.net.URI,java.lang.String)
supr java.lang.Object

CLSS public com.xceptance.xlt.api.util.XltCharBuffer
cons public init(char[])
cons public init(char[],int,int)
fld public final static com.xceptance.xlt.api.util.XltCharBuffer EMPTY
intf java.lang.CharSequence
intf java.lang.Comparable<com.xceptance.xlt.api.util.XltCharBuffer>
meth public !varargs static com.xceptance.xlt.api.util.XltCharBuffer valueOf(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public boolean endsWith(com.xceptance.xlt.api.util.XltCharBuffer)
meth public boolean equals(java.lang.Object)
meth public boolean startsWith(com.xceptance.xlt.api.util.XltCharBuffer)
meth public char charAt(int)
meth public char peakAhead(int)
meth public char[] toCharArray()
meth public com.xceptance.xlt.api.util.XltCharBuffer put(int,char)
meth public com.xceptance.xlt.api.util.XltCharBuffer substring(int)
meth public com.xceptance.xlt.api.util.XltCharBuffer substring(int,int)
meth public com.xceptance.xlt.api.util.XltCharBuffer viewByLength(int,int)
meth public com.xceptance.xlt.api.util.XltCharBuffer viewFromTo(int,int)
meth public int compareTo(com.xceptance.xlt.api.util.XltCharBuffer)
meth public int hashCode()
meth public int indexOf(char)
meth public int indexOf(com.xceptance.xlt.api.util.XltCharBuffer)
meth public int indexOf(com.xceptance.xlt.api.util.XltCharBuffer,int)
meth public int lastIndexOf(com.xceptance.xlt.api.util.XltCharBuffer)
meth public int lastIndexOf(com.xceptance.xlt.api.util.XltCharBuffer,int)
meth public int length()
meth public java.lang.CharSequence subSequence(int,int)
meth public java.lang.String toDebugString()
meth public java.lang.String toString()
meth public java.util.List<com.xceptance.xlt.api.util.XltCharBuffer> split(char)
meth public static com.xceptance.xlt.api.util.XltCharBuffer empty()
meth public static com.xceptance.xlt.api.util.XltCharBuffer emptyWhenNull(com.xceptance.xlt.api.util.XltCharBuffer)
meth public static com.xceptance.xlt.api.util.XltCharBuffer valueOf(char[])
meth public static com.xceptance.xlt.api.util.XltCharBuffer valueOf(com.xceptance.xlt.api.util.XltCharBuffer,char)
meth public static com.xceptance.xlt.api.util.XltCharBuffer valueOf(com.xceptance.xlt.api.util.XltCharBuffer,com.xceptance.xlt.api.util.XltCharBuffer)
meth public static com.xceptance.xlt.api.util.XltCharBuffer valueOf(com.xceptance.xlt.api.util.XltCharBuffer,com.xceptance.xlt.api.util.XltCharBuffer,com.xceptance.xlt.api.util.XltCharBuffer)
meth public static com.xceptance.xlt.api.util.XltCharBuffer valueOf(java.lang.String)
meth public static com.xceptance.xlt.api.util.XltCharBuffer valueOf(java.lang.String,java.lang.String)
meth public static com.xceptance.xlt.api.util.XltCharBuffer valueOf(java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds EMPTY_ARRAY,from,hashCode,length,src

CLSS public com.xceptance.xlt.api.util.XltException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public final com.xceptance.xlt.api.util.XltLogger
cons public init()
fld public final static org.slf4j.Logger reportLogger
fld public final static org.slf4j.Logger runTimeLogger
supr java.lang.Object

CLSS public abstract com.xceptance.xlt.api.util.XltProperties
cons public init()
fld public final static java.lang.String DEFAULT_PROPERTIES = "DEFAULT"
fld public final static java.lang.String DEVELOPMENT_PROPERTIES = "DEVELOPMENT"
fld public final static java.lang.String PROJECT_PROPERTIES = "PROJECT"
fld public final static java.lang.String SECRET_PROPERTIES = "SECRET"
fld public final static java.lang.String SYSTEM_PROPERTIES = "SYSTEM"
fld public final static java.lang.String TEST_PROPERTIES = "TEST"
meth public abstract boolean containsKey(java.lang.String)
meth public abstract boolean getProperty(java.lang.String,boolean)
meth public abstract boolean isDevMode()
meth public abstract boolean isLoadTest()
meth public abstract com.xceptance.xlt.api.util.XltProperties clear()
meth public abstract int getProperty(java.lang.String,int)
meth public abstract java.lang.String getEffectiveKey(com.xceptance.xlt.api.engine.Session,java.lang.String)
meth public abstract java.lang.String getEffectiveKey(java.lang.String,java.lang.String,java.lang.String)
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.lang.String getProperty(java.lang.String,java.lang.String)
meth public abstract java.lang.String getPropertyRandomValue(java.lang.String,java.lang.String)
meth public abstract java.lang.String getVersion()
meth public abstract java.util.LinkedHashMap<java.lang.String,java.util.Properties> getPropertyBuckets()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getPropertiesForKey(java.lang.String)
meth public abstract java.util.Optional<java.lang.String> getProperty(com.xceptance.xlt.api.engine.Session,java.lang.String)
meth public abstract java.util.Properties getCopyOfProperties()
meth public abstract java.util.Properties getProperties()
meth public abstract long getProperty(java.lang.String,long)
meth public abstract long getStartTime()
meth public abstract void removeProperty(java.lang.String)
meth public abstract void setProperties(java.util.Properties)
meth public abstract void setProperty(java.lang.String,java.lang.String)
meth public static com.xceptance.xlt.api.util.XltProperties getInstance()
supr java.lang.Object

CLSS public com.xceptance.xlt.api.util.XltRandom
cons public init()
meth public static boolean nextBoolean()
meth public static boolean nextBoolean(int)
meth public static double nextDouble()
meth public static double nextGaussian()
meth public static float nextFloat()
meth public static int getRandom(int[])
meth public static int nextInt()
meth public static int nextInt(int)
meth public static int nextInt(int,int)
meth public static int nextIntWithDeviation(int,int)
meth public static java.util.Random getRandom()
meth public static long getSeed()
meth public static long nextLong()
meth public static void nextBytes(byte[])
meth public static void reseed()
meth public static void setSeed(long)
supr java.lang.Object
hfds random
hcls InternalRandom

CLSS public com.xceptance.xlt.api.validators.ContentLengthValidator
cons public init()
meth public static com.xceptance.xlt.api.validators.ContentLengthValidator getInstance()
meth public void validate(com.xceptance.xlt.api.htmlunit.LightWeightPage)
meth public void validate(org.htmlunit.html.HtmlPage)
supr java.lang.Object
hfds PROPERTY_NAME
hcls ContentLengthValidator_Singleton,DisabledContentLengthValidator

CLSS public com.xceptance.xlt.api.validators.HtmlEndTagValidator
meth public static com.xceptance.xlt.api.validators.HtmlEndTagValidator getInstance()
meth public void validate(com.xceptance.xlt.api.htmlunit.LightWeightPage)
meth public void validate(java.lang.String)
meth public void validate(org.htmlunit.html.HtmlPage)
supr java.lang.Object
hfds CL_HTML_REGEX,PROPERTY_NAME,REGULAR_TRAILING_CONTENT_REGEX,pattern,trailingContentPattern
hcls DisabledHtmlEndTagValidator,HtmlEndTagValidator_Singleton

CLSS public com.xceptance.xlt.api.validators.HttpResponseCodeValidator
cons public init()
cons public init(int)
meth public boolean equals(java.lang.Object)
meth public int getHttpResponseCode()
meth public int hashCode()
meth public static com.xceptance.xlt.api.validators.HttpResponseCodeValidator getInstance()
meth public void validate(com.xceptance.xlt.api.htmlunit.LightWeightPage)
meth public void validate(org.htmlunit.html.HtmlPage)
supr java.lang.Object
hfds httpResponseCode
hcls HttpResponseCodeValidator_Singleton

CLSS public com.xceptance.xlt.api.validators.StandardValidator
cons public init()
meth public static com.xceptance.xlt.api.validators.StandardValidator getInstance()
meth public void validate(org.htmlunit.html.HtmlPage) throws java.lang.Exception
supr java.lang.Object
hcls StandardValidator_Singleton

CLSS public com.xceptance.xlt.api.validators.XHTMLValidator
cons public init(boolean,boolean)
meth public static com.xceptance.xlt.api.validators.XHTMLValidator getInstance()
meth public void validate(com.xceptance.xlt.api.htmlunit.LightWeightPage) throws java.lang.Exception
meth public void validate(java.lang.String) throws java.lang.Exception
meth public void validate(org.htmlunit.html.HtmlPage) throws java.lang.Exception
supr java.lang.Object
hfds PROPERTY_NAME,breakOnErrors,breakOnWarnings
hcls DisabledXHTMLValidator,LocalEntityResolver,LocalErrorHandler,XHTMLValidator_Singleton

CLSS public com.xceptance.xlt.api.webdriver.XltChromeDriver
cons public init()
cons public init(org.openqa.selenium.chrome.ChromeDriverService)
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.chrome.ChromeOptions)
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.chrome.ChromeOptions,boolean)
cons public init(org.openqa.selenium.chrome.ChromeOptions)
cons public init(org.openqa.selenium.chrome.ChromeOptions,boolean)
innr public final static Builder
meth public static com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder xltBuilder()
meth public void close()
meth public void quit()
supr org.openqa.selenium.chrome.ChromeDriver
hfds CONNECT_RETRY_BASE_TIMEOUT,CONNECT_RETRY_COUNT,CONNECT_RETRY_TIMEOUT_FACTOR,EXTENSION_FILE_ENDING,EXTENSION_FILE_NAME,FIELD_NAME_ENVIRONMENT,HEADLESS_ENABLED,IGNORE_MISSING_DATA,LOG,PROPERTY_DOMAIN,PROPERTY_HEADLESS,PROPERTY_IGNORE_MISSING_DATA,PROPERTY_RECORD_INCOMPLETE,PROPERTY_USE_SESSION_STORAGE,RECORD_INCOMPLETE_ENABLED,USE_SESSION_STORAGE,connectionHandler,extensionFile

CLSS public final static com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder
 outer com.xceptance.xlt.api.webdriver.XltChromeDriver
cons public init()
meth public com.xceptance.xlt.api.webdriver.XltChromeDriver build()
meth public com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder setHeadless(boolean)
meth public com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder setOptions(org.openqa.selenium.chrome.ChromeOptions)
meth public com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder setService(org.openqa.selenium.chrome.ChromeDriverService)
supr java.lang.Object
hfds headless,options,service

CLSS public final com.xceptance.xlt.api.webdriver.XltDriver
cons public init()
cons public init(boolean)
cons public init(org.htmlunit.BrowserVersion)
cons public init(org.htmlunit.BrowserVersion,boolean)
meth protected org.htmlunit.WebClient newWebClient(org.htmlunit.BrowserVersion)
meth public java.lang.String getCurrentUrl()
meth public java.lang.String getPageSource()
meth public java.lang.String getTitle()
meth public java.lang.String getWindowHandle()
meth public java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By)
meth public java.util.Set<java.lang.String> getWindowHandles()
meth public org.htmlunit.WebClient getWebClient()
meth public org.openqa.selenium.WebDriver$Navigation navigate()
meth public org.openqa.selenium.WebDriver$Options manage()
meth public org.openqa.selenium.WebElement findElement(org.openqa.selenium.By)
meth public void close()
meth public void get(java.lang.String)
meth public void quit()
supr com.xceptance.xlt.engine.xltdriver.HtmlUnitDriver

CLSS public final com.xceptance.xlt.api.webdriver.XltFirefoxDriver
cons public init()
cons public init(org.openqa.selenium.firefox.FirefoxOptions)
cons public init(org.openqa.selenium.firefox.FirefoxOptions,boolean)
innr public final static Builder
meth protected void startSession(org.openqa.selenium.Capabilities)
meth public static com.xceptance.xlt.api.webdriver.XltFirefoxDriver$Builder xltBuilder()
meth public void close()
meth public void quit()
supr org.openqa.selenium.firefox.FirefoxDriver
hfds EXTENSION_FILE_ENDING,EXTENSION_FILE_NAME,FIELD_NAME_ENVIRONMENT,FIELD_NAME_SERVICE,HEADLESS_CAPABILITY,HEADLESS_ENABLED,LOG,OVERRIDE_RESPONSE_TIMEOUT,PROPERTY_DOMAIN,PROPERTY_HEADLESS,PROPERTY_RECORD_INCOMPLETE,PROPERTY_RESPONSE_TIMEOUT,RECORD_INCOMPLETE_ENABLED,connectionHandler,extensionFile

CLSS public final static com.xceptance.xlt.api.webdriver.XltFirefoxDriver$Builder
 outer com.xceptance.xlt.api.webdriver.XltFirefoxDriver
cons public init()
meth public com.xceptance.xlt.api.webdriver.XltFirefoxDriver build()
meth public com.xceptance.xlt.api.webdriver.XltFirefoxDriver$Builder setBinary(org.openqa.selenium.firefox.FirefoxBinary)
meth public com.xceptance.xlt.api.webdriver.XltFirefoxDriver$Builder setHeadless(boolean)
meth public com.xceptance.xlt.api.webdriver.XltFirefoxDriver$Builder setProfile(org.openqa.selenium.firefox.FirefoxProfile)
supr java.lang.Object
hfds binary,headless,options,profile

CLSS public com.xceptance.xlt.engine.xltdriver.HtmlUnitDriver
cons public init()
cons public init(boolean)
cons public init(org.htmlunit.BrowserVersion)
cons public init(org.htmlunit.BrowserVersion,boolean)
cons public init(org.openqa.selenium.Capabilities)
cons public init(org.openqa.selenium.Capabilities,org.openqa.selenium.Capabilities)
fld public final static java.lang.String BROWSER_LANGUAGE_CAPABILITY = "browserLanguage"
fld public final static java.lang.String DOWNLOAD_IMAGES_CAPABILITY = "downloadImages"
fld public final static java.lang.String JAVASCRIPT_ENABLED = "javascriptEnabled"
innr protected HtmlUnitWebDriverOptions
innr protected abstract interface static JavaScriptResultsCollection
innr protected static ElementsMap
intf org.openqa.selenium.HasCapabilities
intf org.openqa.selenium.JavascriptExecutor
intf org.openqa.selenium.WebDriver
intf org.openqa.selenium.interactions.Interactive
meth protected <%0 extends java.lang.Object> {%%0} implicitlyWaitFor(java.util.concurrent.Callable<{%%0}>)
meth protected com.xceptance.xlt.engine.xltdriver.HtmlUnitWebElement toWebElement(org.htmlunit.html.DomElement)
meth protected org.htmlunit.WebClient modifyWebClient(org.htmlunit.WebClient)
meth protected org.htmlunit.WebClient newWebClient(org.htmlunit.BrowserVersion)
meth protected void assertElementNotStale(org.htmlunit.html.DomElement)
meth protected void get(java.net.URL)
meth protected void runAsync(java.lang.Runnable)
meth protected void switchToDefaultContentOfWindow(org.htmlunit.WebWindow)
meth public !varargs java.lang.Object executeAsyncScript(java.lang.String,java.lang.Object[])
meth public !varargs java.lang.Object executeScript(java.lang.String,java.lang.Object[])
meth public !varargs void sendKeys(com.xceptance.xlt.engine.xltdriver.HtmlUnitWebElement,java.lang.CharSequence[])
meth public boolean isAcceptInsecureCerts()
meth public boolean isDownloadImages()
meth public boolean isJavascriptEnabled()
meth public com.xceptance.xlt.engine.xltdriver.HtmlUnitAlert getAlert()
meth public com.xceptance.xlt.engine.xltdriver.HtmlUnitDriver$ElementsMap getElementsMap()
meth public com.xceptance.xlt.engine.xltdriver.HtmlUnitKeyboard getKeyboard()
meth public com.xceptance.xlt.engine.xltdriver.HtmlUnitMouse getMouse()
meth public com.xceptance.xlt.engine.xltdriver.HtmlUnitWebElement toWebElement(java.lang.String)
meth public com.xceptance.xlt.engine.xltdriver.HtmlUnitWindow getCurrentWindow()
meth public java.lang.String getCurrentUrl()
meth public java.lang.String getPageSource()
meth public java.lang.String getTitle()
meth public java.lang.String getWindowHandle()
meth public java.util.List<org.openqa.selenium.WebElement> findElements(com.xceptance.xlt.engine.xltdriver.HtmlUnitWebElement,org.openqa.selenium.By)
meth public java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By)
meth public java.util.Set<java.lang.String> getWindowHandles()
meth public org.htmlunit.BrowserVersion getBrowserVersion()
meth public org.htmlunit.WebClient getWebClient()
meth public org.openqa.selenium.Capabilities getCapabilities()
meth public org.openqa.selenium.WebDriver$Navigation navigate()
meth public org.openqa.selenium.WebDriver$Options manage()
meth public org.openqa.selenium.WebDriver$TargetLocator switchTo()
meth public org.openqa.selenium.WebElement findElement(com.xceptance.xlt.engine.xltdriver.HtmlUnitWebElement,org.openqa.selenium.By)
meth public org.openqa.selenium.WebElement findElement(org.openqa.selenium.By)
meth public void click(org.htmlunit.html.DomElement,boolean)
meth public void close()
meth public void doubleClick(org.htmlunit.html.DomElement)
meth public void get(java.lang.String)
meth public void mouseDown(org.htmlunit.html.DomElement)
meth public void mouseMove(org.htmlunit.html.DomElement)
meth public void mouseUp(org.htmlunit.html.DomElement)
meth public void openNewWindow()
meth public void perform(java.util.Collection<org.openqa.selenium.interactions.Sequence>)
meth public void quit()
meth public void resetInputState()
meth public void setAcceptInsecureCerts(boolean)
meth public void setAutoProxy(java.lang.String)
meth public void setCurrentWindow(org.htmlunit.WebWindow)
meth public void setDownloadImages(boolean)
meth public void setExecutor(java.util.concurrent.Executor)
meth public void setHTTPProxy(java.lang.String,int,java.util.List<java.lang.String>)
meth public void setJavascriptEnabled(boolean)
meth public void setProxy(java.lang.String,int)
meth public void setProxySettings(org.openqa.selenium.Proxy)
meth public void setSocksProxy(java.lang.String,int)
meth public void setSocksProxy(java.lang.String,int,java.util.List<java.lang.String>)
meth public void submit(com.xceptance.xlt.engine.xltdriver.HtmlUnitWebElement)
supr java.lang.Object
hfds alert_,asyncScriptExecutor_,conditionLock_,currentWindow_,defaultExecutor_,elementFinder_,elementsMap_,exception_,executor_,inputProcessor_,keyboard_,mainCondition_,mouse_,options_,pageLoadStrategy_,runAsyncRunning_,sleepTime,targetLocator_,webClient_
hcls HtmlUnitNavigation,PageLoadStrategy

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.CharSequence
meth public abstract char charAt(int)
meth public abstract int length()
meth public abstract java.lang.CharSequence subSequence(int,int)
meth public abstract java.lang.String toString()
meth public java.util.stream.IntStream chars()
meth public java.util.stream.IntStream codePoints()
meth public static int compare(java.lang.CharSequence,java.lang.CharSequence)

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, MODULE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean forRemoval()
meth public abstract !hasdefault java.lang.String since()

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public java.lang.IllegalStateException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Inherited
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray(java.util.function.IntFunction<{%%0}[]>)
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

CLSS public abstract interface java.util.List<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.List%0}>
meth public !varargs static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.List%0})
meth public abstract boolean addAll(int,java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int indexOf(java.lang.Object)
meth public abstract int lastIndexOf(java.lang.Object)
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.List%0}> iterator()
meth public abstract java.util.List<{java.util.List%0}> subList(int,int)
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator()
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator(int)
meth public abstract void add(int,{java.util.List%0})
meth public abstract void clear()
meth public abstract {java.util.List%0} get(int)
meth public abstract {java.util.List%0} remove(int)
meth public abstract {java.util.List%0} set(int,{java.util.List%0})
meth public java.util.Spliterator<{java.util.List%0}> spliterator()
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> copyOf(java.util.Collection<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of()
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0})
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0})
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.List%0}>)
meth public void sort(java.util.Comparator<? super {java.util.List%0}>)

CLSS public abstract interface !annotation org.jspecify.annotations.NullMarked
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[MODULE, PACKAGE, TYPE, METHOD, CONSTRUCTOR])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation org.junit.runner.RunWith
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends org.junit.runner.Runner> value()

CLSS public abstract interface !annotation org.openqa.selenium.Beta
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE, CONSTRUCTOR, FIELD, METHOD, TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface org.openqa.selenium.HasAuthentication
meth public abstract void register(java.util.function.Predicate<java.net.URI>,java.util.function.Supplier<org.openqa.selenium.Credentials>)
meth public void register(java.util.function.Supplier<org.openqa.selenium.Credentials>)

CLSS public abstract interface org.openqa.selenium.HasCapabilities
meth public abstract org.openqa.selenium.Capabilities getCapabilities()

CLSS public abstract interface org.openqa.selenium.HasDownloads
meth public abstract java.util.List<java.lang.String> getDownloadableFiles()
meth public abstract void deleteDownloadableFiles()
meth public abstract void downloadFile(java.lang.String,java.nio.file.Path) throws java.io.IOException
meth public void requireDownloadsEnabled(org.openqa.selenium.Capabilities)

CLSS public abstract interface org.openqa.selenium.JavascriptExecutor

CLSS public abstract interface org.openqa.selenium.PrintsPage
meth public abstract org.openqa.selenium.Pdf print(org.openqa.selenium.print.PrintOptions)

CLSS public abstract interface org.openqa.selenium.SearchContext
 anno 0 org.jspecify.annotations.NullMarked()
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By)
meth public abstract org.openqa.selenium.WebElement findElement(org.openqa.selenium.By)

CLSS public abstract interface org.openqa.selenium.TakesScreenshot
 anno 0 org.jspecify.annotations.NullMarked()
meth public abstract <%0 extends java.lang.Object> {%%0} getScreenshotAs(org.openqa.selenium.OutputType<{%%0}>)

CLSS public abstract interface org.openqa.selenium.WebDriver
intf org.openqa.selenium.SearchContext

CLSS public abstract interface org.openqa.selenium.bidi.HasBiDi
meth public abstract java.util.Optional<org.openqa.selenium.bidi.BiDi> maybeGetBiDi()
meth public org.openqa.selenium.bidi.BiDi getBiDi()

CLSS public org.openqa.selenium.chrome.ChromeDriver
cons public init()
cons public init(org.openqa.selenium.chrome.ChromeDriverService)
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.chrome.ChromeOptions)
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.chrome.ChromeOptions,org.openqa.selenium.remote.http.ClientConfig)
cons public init(org.openqa.selenium.chrome.ChromeOptions)
meth public static org.openqa.selenium.remote.RemoteWebDriverBuilder builder()
 anno 0 org.openqa.selenium.Beta()
supr org.openqa.selenium.chromium.ChromiumDriver
hcls ChromeDriverCommandExecutor

CLSS public org.openqa.selenium.chromium.ChromiumDriver
cons protected init(org.openqa.selenium.remote.CommandExecutor,org.openqa.selenium.Capabilities,java.lang.String)
fld protected org.openqa.selenium.chromium.HasCasting casting
fld protected org.openqa.selenium.chromium.HasCdp cdp
fld public final static java.util.function.Predicate<java.lang.String> IS_CHROMIUM_BROWSER
intf org.openqa.selenium.HasAuthentication
intf org.openqa.selenium.bidi.HasBiDi
intf org.openqa.selenium.chromium.HasCasting
intf org.openqa.selenium.chromium.HasCdp
intf org.openqa.selenium.chromium.HasLaunchApp
intf org.openqa.selenium.chromium.HasNetworkConditions
intf org.openqa.selenium.chromium.HasPermissions
intf org.openqa.selenium.devtools.HasDevTools
intf org.openqa.selenium.html5.LocationContext
intf org.openqa.selenium.html5.WebStorage
intf org.openqa.selenium.logging.HasLogEvents
meth public !varargs java.lang.Object executeScript(org.openqa.selenium.ScriptKey,java.lang.Object[])
meth public <%0 extends java.lang.Object> void onLogEvent(org.openqa.selenium.logging.EventType<{%%0}>)
meth public java.lang.String getCastIssueMessage()
meth public java.util.List<java.util.Map<java.lang.String,java.lang.String>> getCastSinks()
meth public java.util.Map<java.lang.String,java.lang.Object> executeCdpCommand(java.lang.String,java.util.Map<java.lang.String,java.lang.Object>)
meth public java.util.Optional<org.openqa.selenium.bidi.BiDi> maybeGetBiDi()
meth public java.util.Optional<org.openqa.selenium.devtools.DevTools> maybeGetDevTools()
meth public java.util.Set<org.openqa.selenium.ScriptKey> getPinnedScripts()
meth public org.openqa.selenium.Capabilities getCapabilities()
meth public org.openqa.selenium.ScriptKey pin(java.lang.String)
meth public org.openqa.selenium.chromium.ChromiumNetworkConditions getNetworkConditions()
meth public org.openqa.selenium.html5.LocalStorage getLocalStorage()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.openqa.selenium.html5.Location location()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.openqa.selenium.html5.SessionStorage getSessionStorage()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void deleteNetworkConditions()
meth public void launchApp(java.lang.String)
meth public void quit()
meth public void register(java.util.function.Predicate<java.net.URI>,java.util.function.Supplier<org.openqa.selenium.Credentials>)
meth public void selectCastSink(java.lang.String)
meth public void setFileDetector(org.openqa.selenium.remote.FileDetector)
meth public void setLocation(org.openqa.selenium.html5.Location)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void setNetworkConditions(org.openqa.selenium.chromium.ChromiumNetworkConditions)
meth public void setPermission(java.lang.String,java.lang.String)
meth public void startDesktopMirroring(java.lang.String)
meth public void startTabMirroring(java.lang.String)
meth public void stopCasting(java.lang.String)
meth public void unpin(org.openqa.selenium.ScriptKey)
supr org.openqa.selenium.remote.RemoteWebDriver
hfds LOG,biDi,biDiUri,capabilities,connection,devTools,launch,locationContext,networkConditions,permissions,scriptKeys,webStorage

CLSS public abstract interface org.openqa.selenium.chromium.HasCasting
 anno 0 org.openqa.selenium.Beta()
meth public abstract java.lang.String getCastIssueMessage()
meth public abstract java.util.List<java.util.Map<java.lang.String,java.lang.String>> getCastSinks()
meth public abstract void selectCastSink(java.lang.String)
meth public abstract void startDesktopMirroring(java.lang.String)
meth public abstract void startTabMirroring(java.lang.String)
meth public abstract void stopCasting(java.lang.String)

CLSS public abstract interface org.openqa.selenium.chromium.HasCdp
 anno 0 org.openqa.selenium.Beta()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> executeCdpCommand(java.lang.String,java.util.Map<java.lang.String,java.lang.Object>)

CLSS public abstract interface org.openqa.selenium.chromium.HasLaunchApp
 anno 0 org.openqa.selenium.Beta()
meth public abstract void launchApp(java.lang.String)

CLSS public abstract interface org.openqa.selenium.chromium.HasNetworkConditions
 anno 0 org.openqa.selenium.Beta()
meth public abstract org.openqa.selenium.chromium.ChromiumNetworkConditions getNetworkConditions()
meth public abstract void deleteNetworkConditions()
meth public abstract void setNetworkConditions(org.openqa.selenium.chromium.ChromiumNetworkConditions)

CLSS public abstract interface org.openqa.selenium.chromium.HasPermissions
 anno 0 org.openqa.selenium.Beta()
meth public abstract void setPermission(java.lang.String,java.lang.String)

CLSS public abstract interface org.openqa.selenium.devtools.HasDevTools
meth public abstract java.util.Optional<org.openqa.selenium.devtools.DevTools> maybeGetDevTools()
meth public org.openqa.selenium.devtools.DevTools getDevTools()

CLSS public abstract interface org.openqa.selenium.federatedcredentialmanagement.HasFederatedCredentialManagement
 anno 0 org.openqa.selenium.Beta()
meth public abstract org.openqa.selenium.federatedcredentialmanagement.FederatedCredentialManagementDialog getFederatedCredentialManagementDialog()
meth public abstract void resetCooldown()
meth public abstract void setDelayEnabled(boolean)

CLSS public org.openqa.selenium.firefox.FirefoxDriver
cons public init()
cons public init(org.openqa.selenium.firefox.FirefoxDriverService)
cons public init(org.openqa.selenium.firefox.FirefoxDriverService,org.openqa.selenium.firefox.FirefoxOptions)
cons public init(org.openqa.selenium.firefox.FirefoxDriverService,org.openqa.selenium.firefox.FirefoxOptions,org.openqa.selenium.remote.http.ClientConfig)
cons public init(org.openqa.selenium.firefox.FirefoxOptions)
innr public final static SystemProperty
intf org.openqa.selenium.bidi.HasBiDi
intf org.openqa.selenium.firefox.HasContext
intf org.openqa.selenium.firefox.HasExtensions
intf org.openqa.selenium.firefox.HasFullPageScreenshot
intf org.openqa.selenium.html5.WebStorage
meth public <%0 extends java.lang.Object> {%%0} getFullPageScreenshotAs(org.openqa.selenium.OutputType<{%%0}>)
meth public java.lang.String installExtension(java.nio.file.Path)
meth public java.lang.String installExtension(java.nio.file.Path,java.lang.Boolean)
meth public java.util.Optional<org.openqa.selenium.bidi.BiDi> maybeGetBiDi()
meth public org.openqa.selenium.Capabilities getCapabilities()
meth public org.openqa.selenium.bidi.BiDi getBiDi()
meth public org.openqa.selenium.firefox.FirefoxCommandContext getContext()
meth public org.openqa.selenium.html5.LocalStorage getLocalStorage()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.openqa.selenium.html5.SessionStorage getSessionStorage()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.openqa.selenium.remote.RemoteWebDriverBuilder builder()
 anno 0 org.openqa.selenium.Beta()
meth public void quit()
meth public void setContext(org.openqa.selenium.firefox.FirefoxCommandContext)
meth public void setFileDetector(org.openqa.selenium.remote.FileDetector)
meth public void uninstallExtension(java.lang.String)
supr org.openqa.selenium.remote.RemoteWebDriver
hfds LOG,biDi,biDiUri,capabilities,context,extensions,fullPageScreenshot,webStorage
hcls FirefoxDriverCommandExecutor

CLSS public abstract interface org.openqa.selenium.firefox.HasContext
 anno 0 org.openqa.selenium.Beta()
meth public abstract org.openqa.selenium.firefox.FirefoxCommandContext getContext()
meth public abstract void setContext(org.openqa.selenium.firefox.FirefoxCommandContext)

CLSS public abstract interface org.openqa.selenium.firefox.HasExtensions
 anno 0 org.openqa.selenium.Beta()
meth public abstract java.lang.String installExtension(java.nio.file.Path)
meth public abstract java.lang.String installExtension(java.nio.file.Path,java.lang.Boolean)
meth public abstract void uninstallExtension(java.lang.String)

CLSS public abstract interface org.openqa.selenium.firefox.HasFullPageScreenshot
 anno 0 org.openqa.selenium.Beta()
meth public abstract <%0 extends java.lang.Object> {%%0} getFullPageScreenshotAs(org.openqa.selenium.OutputType<{%%0}>)

CLSS public abstract interface org.openqa.selenium.html5.LocationContext
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract org.openqa.selenium.html5.Location location()
meth public abstract void setLocation(org.openqa.selenium.html5.Location)

CLSS public abstract interface org.openqa.selenium.html5.WebStorage
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract org.openqa.selenium.html5.LocalStorage getLocalStorage()
meth public abstract org.openqa.selenium.html5.SessionStorage getSessionStorage()

CLSS public abstract interface org.openqa.selenium.interactions.Interactive
meth public abstract void perform(java.util.Collection<org.openqa.selenium.interactions.Sequence>)
meth public abstract void resetInputState()

CLSS public abstract interface org.openqa.selenium.logging.HasLogEvents
meth public abstract <%0 extends java.lang.Object> void onLogEvent(org.openqa.selenium.logging.EventType<{%%0}>)

CLSS public abstract interface !annotation org.openqa.selenium.remote.Augmentable
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public org.openqa.selenium.remote.RemoteWebDriver
cons protected init()
cons public init(java.net.URL,org.openqa.selenium.Capabilities)
cons public init(java.net.URL,org.openqa.selenium.Capabilities,boolean)
cons public init(org.openqa.selenium.Capabilities)
cons public init(org.openqa.selenium.Capabilities,boolean)
cons public init(org.openqa.selenium.remote.CommandExecutor,org.openqa.selenium.Capabilities)
fld protected org.openqa.selenium.Capabilities capabilities
innr protected RemoteTargetLocator
innr protected RemoteWebDriverOptions
innr public final static !enum When
intf org.openqa.selenium.HasCapabilities
intf org.openqa.selenium.HasDownloads
intf org.openqa.selenium.JavascriptExecutor
intf org.openqa.selenium.PrintsPage
intf org.openqa.selenium.TakesScreenshot
intf org.openqa.selenium.WebDriver
intf org.openqa.selenium.federatedcredentialmanagement.HasFederatedCredentialManagement
intf org.openqa.selenium.interactions.Interactive
intf org.openqa.selenium.virtualauthenticator.HasVirtualAuthenticator
meth protected org.openqa.selenium.remote.ExecuteMethod getExecuteMethod()
meth protected org.openqa.selenium.remote.JsonToWebElementConverter getElementConverter()
meth protected org.openqa.selenium.remote.Response execute(java.lang.String)
meth protected org.openqa.selenium.remote.Response execute(java.lang.String,java.util.Map<java.lang.String,?>)
meth protected org.openqa.selenium.remote.Response execute(org.openqa.selenium.remote.CommandPayload)
meth protected void log(org.openqa.selenium.remote.SessionId,java.lang.String,java.lang.Object,org.openqa.selenium.remote.RemoteWebDriver$When)
meth protected void setCommandExecutor(org.openqa.selenium.remote.CommandExecutor)
meth protected void setElementConverter(org.openqa.selenium.remote.JsonToWebElementConverter)
meth protected void setFoundBy(org.openqa.selenium.SearchContext,org.openqa.selenium.WebElement,java.lang.String,java.lang.String)
meth protected void setSessionId(java.lang.String)
meth protected void startSession(org.openqa.selenium.Capabilities)
meth public !varargs java.lang.Object executeAsyncScript(java.lang.String,java.lang.Object[])
meth public !varargs java.lang.Object executeScript(java.lang.String,java.lang.Object[])
meth public <%0 extends java.lang.Object> {%%0} getScreenshotAs(org.openqa.selenium.OutputType<{%%0}>)
meth public java.lang.String getCurrentUrl()
meth public java.lang.String getPageSource()
meth public java.lang.String getTitle()
meth public java.lang.String getWindowHandle()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getDownloadableFiles()
meth public java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By)
meth public java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.SearchContext,java.util.function.BiFunction<java.lang.String,java.lang.Object,org.openqa.selenium.remote.CommandPayload>,org.openqa.selenium.By)
meth public java.util.Set<java.lang.String> getWindowHandles()
meth public org.openqa.selenium.Capabilities getCapabilities()
meth public org.openqa.selenium.Pdf print(org.openqa.selenium.print.PrintOptions)
meth public org.openqa.selenium.WebDriver$Navigation navigate()
meth public org.openqa.selenium.WebDriver$Options manage()
meth public org.openqa.selenium.WebDriver$TargetLocator switchTo()
meth public org.openqa.selenium.WebElement findElement(org.openqa.selenium.By)
meth public org.openqa.selenium.federatedcredentialmanagement.FederatedCredentialManagementDialog getFederatedCredentialManagementDialog()
meth public org.openqa.selenium.remote.CommandExecutor getCommandExecutor()
meth public org.openqa.selenium.remote.ErrorHandler getErrorHandler()
meth public org.openqa.selenium.remote.FileDetector getFileDetector()
meth public org.openqa.selenium.remote.Network network()
meth public org.openqa.selenium.remote.Script script()
meth public org.openqa.selenium.remote.SessionId getSessionId()
meth public org.openqa.selenium.virtualauthenticator.VirtualAuthenticator addVirtualAuthenticator(org.openqa.selenium.virtualauthenticator.VirtualAuthenticatorOptions)
meth public static org.openqa.selenium.remote.RemoteWebDriverBuilder builder()
 anno 0 org.openqa.selenium.Beta()
meth public void close()
meth public void deleteDownloadableFiles()
meth public void downloadFile(java.lang.String,java.nio.file.Path) throws java.io.IOException
meth public void get(java.lang.String)
meth public void perform(java.util.Collection<org.openqa.selenium.interactions.Sequence>)
meth public void quit()
meth public void removeVirtualAuthenticator(org.openqa.selenium.virtualauthenticator.VirtualAuthenticator)
meth public void resetCooldown()
meth public void resetInputState()
meth public void setDelayEnabled(boolean)
meth public void setErrorHandler(org.openqa.selenium.remote.ErrorHandler)
meth public void setFileDetector(org.openqa.selenium.remote.FileDetector)
meth public void setLogLevel(java.util.logging.Level)
supr java.lang.Object
hfds LOG,WEBDRIVER_REMOTE_ENABLE_TRACING,converter,elementLocation,errorHandler,executeMethod,executor,fileDetector,level,localLogs,remoteLogs,remoteNetwork,remoteScript,sessionId
hcls RemoteAlert,RemoteNavigation,RemoteVirtualAuthenticator

CLSS public abstract interface org.openqa.selenium.virtualauthenticator.HasVirtualAuthenticator
 anno 0 org.jspecify.annotations.NullMarked()
meth public abstract org.openqa.selenium.virtualauthenticator.VirtualAuthenticator addVirtualAuthenticator(org.openqa.selenium.virtualauthenticator.VirtualAuthenticatorOptions)
meth public abstract void removeVirtualAuthenticator(org.openqa.selenium.virtualauthenticator.VirtualAuthenticator)

