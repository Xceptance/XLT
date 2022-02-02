#Signature file v4.1
#Version 6.0.0-beta-1

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
meth protected void loadPage(java.net.URL,com.gargoylesoftware.htmlunit.HttpMethod,java.util.List<com.gargoylesoftware.htmlunit.util.NameValuePair>) throws java.lang.Exception
meth protected void loadPage(java.net.URL,com.gargoylesoftware.htmlunit.HttpMethod,java.util.List<com.gargoylesoftware.htmlunit.util.NameValuePair>,long) throws java.lang.Exception
meth protected void loadPage(java.net.URL,long) throws java.lang.Exception
meth protected void loadPageByClick(com.gargoylesoftware.htmlunit.html.HtmlElement) throws java.lang.Exception
meth protected void loadPageByClick(com.gargoylesoftware.htmlunit.html.HtmlElement,long) throws java.lang.Exception
meth protected void loadPageByDragAndDrop(com.gargoylesoftware.htmlunit.html.HtmlElement,com.gargoylesoftware.htmlunit.html.HtmlElement) throws java.lang.Exception
meth protected void loadPageByDragAndDrop(com.gargoylesoftware.htmlunit.html.HtmlElement,com.gargoylesoftware.htmlunit.html.HtmlElement,long) throws java.lang.Exception
meth protected void loadPageByFormClick(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String) throws java.lang.Exception
meth protected void loadPageByFormClick(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,boolean) throws java.lang.Exception
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected void loadPageByFormClick(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,boolean,long) throws java.lang.Exception
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth protected void loadPageByFormClick(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,long) throws java.lang.Exception
meth protected void loadPageByFormSubmit(com.gargoylesoftware.htmlunit.html.HtmlForm) throws java.lang.Exception
meth protected void loadPageByFormSubmit(com.gargoylesoftware.htmlunit.html.HtmlForm,com.gargoylesoftware.htmlunit.html.SubmittableElement) throws java.lang.Exception
meth protected void loadPageByFormSubmit(com.gargoylesoftware.htmlunit.html.HtmlForm,com.gargoylesoftware.htmlunit.html.SubmittableElement,long) throws java.lang.Exception
meth protected void loadPageByFormSubmit(com.gargoylesoftware.htmlunit.html.HtmlForm,long) throws java.lang.Exception
meth protected void loadPageBySelect(com.gargoylesoftware.htmlunit.html.HtmlSelect,com.gargoylesoftware.htmlunit.html.HtmlOption)
meth protected void loadPageBySelect(com.gargoylesoftware.htmlunit.html.HtmlSelect,com.gargoylesoftware.htmlunit.html.HtmlOption,long) throws java.lang.Exception
meth protected void loadPageBySelect(com.gargoylesoftware.htmlunit.html.HtmlSelect,java.lang.String) throws java.lang.Exception
meth protected void loadPageBySelect(com.gargoylesoftware.htmlunit.html.HtmlSelect,java.lang.String,long) throws java.lang.Exception
meth protected void loadPageByTypingKeys(com.gargoylesoftware.htmlunit.html.HtmlElement,java.lang.String) throws java.lang.Exception
meth protected void loadPageByTypingKeys(com.gargoylesoftware.htmlunit.html.HtmlElement,java.lang.String,long) throws java.lang.Exception
meth public com.gargoylesoftware.htmlunit.html.HtmlPage getHtmlPage()
meth public com.xceptance.xlt.api.actions.AbstractHtmlPageAction getPreviousAction()
meth public void run() throws java.lang.Throwable
meth public void setHtmlPage(com.gargoylesoftware.htmlunit.html.HtmlPage)
meth public void setHtmlPage(com.gargoylesoftware.htmlunit.html.HtmlPage,long)
supr com.xceptance.xlt.api.actions.AbstractWebAction
hfds DEFAULT_JS_BACKGROUND_ACTIVITY_WAITINGTIME,PROP_JS_BACKGROUND_ACTIVITY_WAITINGTIME,htmlPage,netStats

CLSS public abstract com.xceptance.xlt.api.actions.AbstractLightWeightPageAction
cons protected init(com.xceptance.xlt.api.actions.AbstractWebAction,java.lang.String)
cons protected init(java.lang.String)
meth protected void loadPage(java.lang.String) throws java.lang.Exception
meth protected void loadPage(java.net.URL) throws java.lang.Exception
meth protected void loadPage(java.net.URL,com.gargoylesoftware.htmlunit.HttpMethod,java.util.List<com.gargoylesoftware.htmlunit.util.NameValuePair>) throws java.lang.Exception
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
fld protected final static java.util.List<com.gargoylesoftware.htmlunit.util.NameValuePair> EMPTY_PARAMETER_LIST
meth protected com.gargoylesoftware.htmlunit.WebRequest createWebRequestSettings(java.net.URL,com.gargoylesoftware.htmlunit.HttpMethod,java.util.List<com.gargoylesoftware.htmlunit.util.NameValuePair>) throws java.net.MalformedURLException
meth public com.gargoylesoftware.htmlunit.WebClient getWebClient()
meth public com.xceptance.xlt.api.actions.AbstractWebAction getPreviousAction()
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
meth public com.gargoylesoftware.htmlunit.xml.XmlPage getXmlPage()
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
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
intf com.xceptance.xlt.api.engine.Data
meth protected int getMinNoCSVElements()
meth protected java.util.List<java.lang.String> addValues()
meth protected void parseValues(java.lang.String[])
meth public final java.lang.String toCSV()
meth public final void fromCSV(java.lang.String)
meth public java.lang.String getAgentName()
meth public java.lang.String getName()
meth public java.lang.String getTransactionName()
meth public java.lang.String getTypeCode()
meth public long getTime()
meth public void setAgentName(java.lang.String)
meth public void setName(java.lang.String)
meth public void setTime()
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
meth protected int getMinNoCSVElements()
meth protected java.util.List<java.lang.String> addValues()
meth protected void parseValues(java.lang.String[])
meth public double getValue()
meth public void setValue(double)
supr com.xceptance.xlt.api.engine.AbstractData
hfds TYPE_CODE,value

CLSS public abstract interface com.xceptance.xlt.api.engine.Data
fld public final static char DELIMITER = ','
meth public abstract java.lang.String getAgentName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getTransactionName()
meth public abstract java.lang.String getTypeCode()
meth public abstract java.lang.String toCSV()
meth public abstract long getTime()
meth public abstract void fromCSV(java.lang.String)
meth public abstract void setAgentName(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setTime()
meth public abstract void setTime(long)
meth public abstract void setTransactionName(java.lang.String)

CLSS public abstract interface com.xceptance.xlt.api.engine.DataManager
meth public abstract boolean isLoggingEnabled()
meth public abstract long getEndOfLoggingPeriod()
meth public abstract long getStartOfLoggingPeriod()
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
meth protected int getMinNoCSVElements()
meth protected java.util.List<java.lang.String> addValues()
meth protected void parseValues(java.lang.String[])
meth public java.lang.String getMessage()
meth public java.lang.String getTestCaseName()
meth public void setMessage(java.lang.String)
meth public void setTestCaseName(java.lang.String)
supr com.xceptance.xlt.api.engine.AbstractData
hfds TYPE_CODE,message,testCaseName

CLSS public abstract com.xceptance.xlt.api.engine.GlobalClock
cons public init()
meth public abstract long getTime()
meth public static com.xceptance.xlt.api.engine.GlobalClock getInstance()
supr java.lang.Object
hfds singleton

CLSS public com.xceptance.xlt.api.engine.NetworkData
cons public init(com.gargoylesoftware.htmlunit.WebRequest,com.gargoylesoftware.htmlunit.WebResponse)
meth public com.gargoylesoftware.htmlunit.HttpMethod getRequestMethod()
meth public com.gargoylesoftware.htmlunit.WebRequest getRequest()
meth public com.gargoylesoftware.htmlunit.WebResponse getResponse()
meth public int getResponseStatusCode()
meth public java.lang.String getContentAsString()
meth public java.lang.String getContentType()
meth public java.lang.String getRequestBody()
meth public java.lang.String getResponseStatusMessage()
meth public java.net.URL getURL()
meth public java.util.List<com.gargoylesoftware.htmlunit.util.NameValuePair> getRequestParameters()
meth public java.util.List<com.gargoylesoftware.htmlunit.util.NameValuePair> getResponseHeaders()
meth public java.util.Map<java.lang.String,java.lang.String> getAdditionalRequestHeaders()
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
meth protected int getMinNoCSVElements()
meth protected java.util.List<java.lang.String> addValues()
meth protected void parseValues(java.lang.String[])
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
meth public java.lang.String getContentType()
meth public java.lang.String getFormData()
meth public java.lang.String getFormDataEncoding()
meth public java.lang.String getHttpMethod()
meth public java.lang.String getId()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public java.lang.String getRequestId()
meth public java.lang.String getResponseId()
meth public java.lang.String getUrl()
meth public java.lang.String[] getIpAddresses()
meth public void setBytesReceived(int)
meth public void setBytesSent(int)
meth public void setConnectTime(int)
meth public void setContentType(java.lang.String)
meth public void setDnsTime(int)
meth public void setFormData(java.lang.String)
meth public void setFormDataEncoding(java.lang.String)
meth public void setHttpMethod(java.lang.String)
meth public void setId(java.lang.String)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void setIpAddresses(java.lang.String[])
meth public void setReceiveTime(int)
meth public void setRequestId(java.lang.String)
meth public void setResponseCode(int)
meth public void setResponseId(java.lang.String)
meth public void setSendTime(int)
meth public void setServerBusyTime(int)
meth public void setTimeToFirstBytes(int)
meth public void setTimeToLastBytes(int)
meth public void setUrl(java.lang.String)
supr com.xceptance.xlt.api.engine.TimerData
hfds IP_ADDRESSES_SEPARATOR,TYPE_CODE,bytesReceived,bytesSent,connectTime,contentType,dnsTime,formData,formDataEncoding,httpMethod,ipAddresses,receiveTime,requestId,responseCode,responseId,sendTime,serverBusyTime,timeToFirstBytes,timeToLastBytes,url

CLSS public com.xceptance.xlt.api.engine.RequestFilter
cons public init()
meth public boolean accepts(com.gargoylesoftware.htmlunit.WebRequest)
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
meth public abstract java.lang.String getUserID()
meth public abstract java.lang.String getUserName()
meth public abstract java.lang.String getWebDriverActionName()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getValueLog()
meth public abstract void addShutdownListener(com.xceptance.xlt.api.engine.SessionShutdownListener)
meth public abstract void clear()
meth public abstract void removeShutdownListener(com.xceptance.xlt.api.engine.SessionShutdownListener)
meth public abstract void setFailed(boolean)
meth public abstract void setID(java.lang.String)
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
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth protected int getMinNoCSVElements()
meth protected java.util.List<java.lang.String> addValues()
meth protected void parseValues(java.lang.String[])
meth public boolean hasFailed()
meth public long getEndTime()
meth public long getRunTime()
meth public void setFailed(boolean)
meth public void setRunTime()
meth public void setRunTime(long)
supr com.xceptance.xlt.api.engine.AbstractData
hfds failed,runTime

CLSS public com.xceptance.xlt.api.engine.TransactionData
cons public init()
cons public init(java.lang.String)
meth protected int getMinNoCSVElements()
meth protected java.util.List<java.lang.String> addValues()
meth protected void parseValues(java.lang.String[])
meth public java.lang.String getDirectoryName()
meth public java.lang.String getDumpDirectoryPath()
meth public java.lang.String getFailedActionName()
meth public java.lang.String getFailureMessage()
meth public java.lang.String getFailureStackTrace()
meth public java.lang.String getTestUserNumber()
meth public void setDirectoryName(java.lang.String)
meth public void setFailedActionName(java.lang.String)
meth public void setFailureStackTrace(java.lang.String)
meth public void setFailureStackTrace(java.lang.Throwable)
meth public void setTestUserNumber(java.lang.String)
supr com.xceptance.xlt.api.engine.TimerData
hfds TYPE_CODE,directoryName,failedActionName,stackTrace,testUserNumber

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule
cons public init()
meth protected abstract com.xceptance.xlt.api.actions.AbstractHtmlPageAction execute(com.xceptance.xlt.api.actions.AbstractHtmlPageAction) throws java.lang.Throwable
meth public com.xceptance.xlt.api.actions.AbstractHtmlPageAction run(com.xceptance.xlt.api.actions.AbstractHtmlPageAction) throws java.lang.Throwable
supr com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptModule

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule
cons public init()
meth protected abstract com.gargoylesoftware.htmlunit.html.HtmlPage execute(com.gargoylesoftware.htmlunit.html.HtmlPage) throws java.lang.Exception
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage addSelection(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage check(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage checkAndWait(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage click(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage clickAndWait(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage contextMenu(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage contextMenuAt(java.lang.String,int,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage contextMenuAt(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage doubleClick(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage doubleClickAndWait(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseDown(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseDownAt(java.lang.String,int,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseDownAt(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseMove(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseMoveAt(java.lang.String,int,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseMoveAt(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseOut(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseOver(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseUp(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseUpAt(java.lang.String,int,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseUpAt(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage open(java.lang.String) throws java.lang.Exception
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage open(java.net.URL) throws java.lang.Exception
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage pause(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage pause(long)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage removeSelection(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage select(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage selectAndWait(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage selectFrame(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage selectWindow()
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage selectWindow(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage submit(java.lang.String) throws java.lang.Exception
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage submitAndWait(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage type(java.lang.String,java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage typeAndWait(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage uncheck(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage uncheckAndWait(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForAttribute(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForChecked(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForClass(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForElementCount(java.lang.String,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForElementCount(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForElementPresent(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForEval(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotAttribute(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotChecked(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotClass(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotElementCount(java.lang.String,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotElementCount(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotElementPresent(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotEval(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotSelectedId(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotSelectedIndex(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotSelectedLabel(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotSelectedValue(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotStyle(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotText(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotTextPresent(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotTitle(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotVisible(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotXpathCount(java.lang.String,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotXpathCount(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForPageToLoad()
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForSelectedId(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForSelectedIndex(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForSelectedLabel(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForSelectedValue(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForStyle(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForText(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForTextPresent(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForTitle(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForValue(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForVisible(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForXpathCount(java.lang.String,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForXpathCount(java.lang.String,java.lang.String)
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
meth public com.gargoylesoftware.htmlunit.html.HtmlPage run(com.gargoylesoftware.htmlunit.html.HtmlPage) throws java.lang.Exception
supr com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptModule

CLSS public abstract com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction
cons public init(com.xceptance.xlt.api.actions.AbstractWebAction)
cons public init(com.xceptance.xlt.api.actions.AbstractWebAction,java.lang.String)
cons public init(java.lang.String)
meth protected boolean evaluatesToTrue(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage addSelection(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage check(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage checkAndWait(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage click(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage clickAndWait(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage contextMenu(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage contextMenuAt(java.lang.String,int,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage contextMenuAt(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage doubleClick(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage doubleClickAndWait(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseDown(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseDownAt(java.lang.String,int,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseDownAt(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseMove(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseMoveAt(java.lang.String,int,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseMoveAt(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseOut(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseOver(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseUp(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseUpAt(java.lang.String,int,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage mouseUpAt(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage open(java.lang.String) throws java.lang.Exception
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage open(java.net.URL) throws java.lang.Exception
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage pause(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage pause(long)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage removeSelection(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage select(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage selectAndWait(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage selectFrame(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage selectWindow()
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage selectWindow(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage submit(java.lang.String) throws java.lang.Exception
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage submitAndWait(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage type(java.lang.String,java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage typeAndWait(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage uncheck(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage uncheckAndWait(java.lang.String) throws java.io.IOException
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForAttribute(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForChecked(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForClass(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForElementCount(java.lang.String,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForElementCount(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForElementPresent(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForEval(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotAttribute(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotAttribute(java.lang.String,java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotChecked(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotClass(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotElementCount(java.lang.String,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotElementCount(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotElementPresent(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotEval(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotSelectedId(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotSelectedIndex(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotSelectedLabel(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotSelectedValue(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotStyle(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotText(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotTextPresent(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotTitle(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotValue(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotVisible(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotXpathCount(java.lang.String,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForNotXpathCount(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForPageToLoad()
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForSelectedId(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForSelectedIndex(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForSelectedLabel(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForSelectedValue(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForStyle(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForText(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForTextPresent(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForTitle(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForValue(java.lang.String,java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForVisible(java.lang.String)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForXpathCount(java.lang.String,int)
meth protected com.gargoylesoftware.htmlunit.html.HtmlPage waitForXpathCount(java.lang.String,java.lang.String)
meth protected java.lang.String getBaseUrl()
meth protected java.lang.String resolve(java.lang.String)
meth protected java.lang.String resolveKey(java.lang.String)
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
cons public init(com.gargoylesoftware.htmlunit.WebResponse,java.lang.String)
meth public com.gargoylesoftware.htmlunit.WebResponse getWebResponse()
meth public int getHttpResponseCode()
meth public java.lang.String getContent()
meth public java.lang.String getContentCharset()
meth public java.lang.String getTimerName()
meth public java.nio.charset.Charset getCharset()
supr java.lang.Object
hfds charset,name,response

CLSS public abstract com.xceptance.xlt.api.report.AbstractReportProvider
cons public init()
intf com.xceptance.xlt.api.report.ReportProvider
meth public com.xceptance.xlt.api.report.ReportProviderConfiguration getConfiguration()
meth public void setConfiguration(com.xceptance.xlt.api.report.ReportProviderConfiguration)
supr java.lang.Object
hfds configuration

CLSS public abstract interface com.xceptance.xlt.api.report.ReportCreator
meth public abstract java.lang.Object createReportFragment()

CLSS public abstract interface com.xceptance.xlt.api.report.ReportProvider
intf com.xceptance.xlt.api.report.ReportCreator
meth public abstract void processDataRecord(com.xceptance.xlt.api.engine.Data)
meth public abstract void setConfiguration(com.xceptance.xlt.api.report.ReportProviderConfiguration)

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
meth protected com.gargoylesoftware.htmlunit.WebResponse createWebResponse(com.gargoylesoftware.htmlunit.WebResponse,byte[])
meth protected com.gargoylesoftware.htmlunit.WebResponse createWebResponse(com.gargoylesoftware.htmlunit.WebResponse,java.lang.String)
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
meth public !varargs static com.gargoylesoftware.htmlunit.html.HtmlPage getFramePage(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String[])
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> java.util.List<{%%0}> findHtmlElements(com.gargoylesoftware.htmlunit.html.HtmlElement,java.lang.String)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> java.util.List<{%%0}> findHtmlElements(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> java.util.List<{%%0}> waitForHtmlElements(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String,long) throws java.lang.InterruptedException
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> {%%0} createHtmlElement(java.lang.String,com.gargoylesoftware.htmlunit.html.HtmlElement)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> {%%0} findHtmlElementsAndPickOne(com.gargoylesoftware.htmlunit.html.HtmlElement,java.lang.String)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> {%%0} findHtmlElementsAndPickOne(com.gargoylesoftware.htmlunit.html.HtmlElement,java.lang.String,boolean)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> {%%0} findHtmlElementsAndPickOne(com.gargoylesoftware.htmlunit.html.HtmlElement,java.lang.String,boolean,boolean)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> {%%0} findHtmlElementsAndPickOne(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String,boolean,boolean)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> {%%0} findSingleHtmlElementByID(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> {%%0} findSingleHtmlElementByXPath(com.gargoylesoftware.htmlunit.html.HtmlElement,java.lang.String)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlElement> {%%0} findSingleHtmlElementByXPath(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlInput> {%%0} getInputEndingWith(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String)
meth public static <%0 extends com.gargoylesoftware.htmlunit.html.HtmlInput> {%%0} getInputStartingWith(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String)
meth public static <%0 extends java.lang.Object> {%%0} findHtmlElementsAndPickOne(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String)
meth public static <%0 extends java.lang.Object> {%%0} findHtmlElementsAndPickOne(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String,boolean)
meth public static boolean isElementPresent(com.gargoylesoftware.htmlunit.html.HtmlElement,java.lang.String)
meth public static boolean isElementPresent(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String)
meth public static com.gargoylesoftware.htmlunit.html.HtmlAnchor getAnchorWithText(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String)
meth public static com.gargoylesoftware.htmlunit.html.HtmlInput createInput(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,java.lang.String,java.lang.String)
meth public static com.gargoylesoftware.htmlunit.html.HtmlSelect getSelectEndingWith(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String)
meth public static com.gargoylesoftware.htmlunit.html.HtmlSelect getSelectStartingWith(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String)
meth public static int countElementsByXPath(com.gargoylesoftware.htmlunit.html.HtmlPage,java.lang.String)
meth public static java.util.List<com.gargoylesoftware.htmlunit.html.HtmlForm> getFormsByIDRegExp(com.gargoylesoftware.htmlunit.html.HtmlPage,java.util.regex.Pattern)
meth public static java.util.List<com.gargoylesoftware.htmlunit.html.HtmlForm> getFormsByNameRegExp(com.gargoylesoftware.htmlunit.html.HtmlPage,java.util.regex.Pattern)
meth public static void checkRadioButton(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,int)
meth public static void checkRadioButton(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,java.lang.String)
meth public static void checkRadioButtonRandomly(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String)
meth public static void checkRadioButtonRandomly(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,boolean,boolean)
meth public static void select(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,java.lang.String)
meth public static void selectRandomly(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String)
meth public static void selectRandomly(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,boolean)
meth public static void selectRandomly(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,boolean,boolean)
meth public static void setCheckBoxValue(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,boolean)
meth public static void setInputValue(com.gargoylesoftware.htmlunit.html.HtmlForm,java.lang.String,java.lang.String)
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
meth public com.gargoylesoftware.htmlunit.WebResponse processResponse(com.gargoylesoftware.htmlunit.WebResponse)
supr com.xceptance.xlt.api.util.AbstractResponseProcessor
hfds contentPattern,replacement,urlPattern

CLSS public abstract interface com.xceptance.xlt.api.util.ResponseProcessor
meth public abstract com.gargoylesoftware.htmlunit.WebResponse processResponse(com.gargoylesoftware.htmlunit.WebResponse)

CLSS public com.xceptance.xlt.api.util.URLUtils
cons public init()
meth public static java.lang.String makeLinkAbsolute(java.lang.String,java.lang.String)
meth public static java.lang.String makeLinkAbsolute(java.net.URI,java.lang.String)
supr java.lang.Object

CLSS public com.xceptance.xlt.api.util.XltException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public final com.xceptance.xlt.api.util.XltLogger
cons public init()
fld public final static org.slf4j.Logger runTimeLogger
supr java.lang.Object
hfds RUNTIME

CLSS public abstract com.xceptance.xlt.api.util.XltProperties
cons public init()
meth public abstract boolean containsKey(java.lang.String)
meth public abstract boolean getProperty(java.lang.String,boolean)
meth public abstract int getProperty(java.lang.String,int)
meth public abstract java.lang.String getProperty(java.lang.String)
meth public abstract java.lang.String getProperty(java.lang.String,java.lang.String)
meth public abstract java.lang.String getPropertyRandomValue(java.lang.String,java.lang.String)
meth public abstract java.lang.String getVersion()
meth public abstract java.util.List<java.lang.String> getResolvedPropertyFiles()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.util.Map<java.lang.String,java.lang.String> getPropertiesForKey(java.lang.String)
meth public abstract java.util.Properties getProperties()
meth public abstract long getProperty(java.lang.String,long)
meth public abstract long getStartTime()
meth public abstract void removeProperty(java.lang.String)
meth public abstract void setProperties(java.io.File) throws java.io.IOException
meth public abstract void setProperties(java.util.Properties)
meth public abstract void setProperties(org.apache.commons.vfs2.FileObject) throws java.io.IOException
meth public abstract void setProperty(java.lang.String,java.lang.String)
meth public abstract void update()
meth public static com.xceptance.xlt.api.util.XltProperties getInstance()
meth public static void reset()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
meth public void validate(com.gargoylesoftware.htmlunit.html.HtmlPage)
meth public void validate(com.xceptance.xlt.api.htmlunit.LightWeightPage)
supr java.lang.Object

CLSS public com.xceptance.xlt.api.validators.HtmlEndTagValidator
meth public static com.xceptance.xlt.api.validators.HtmlEndTagValidator getInstance()
meth public void validate(com.gargoylesoftware.htmlunit.html.HtmlPage)
meth public void validate(com.xceptance.xlt.api.htmlunit.LightWeightPage)
meth public void validate(java.lang.String)
supr java.lang.Object
hfds CL_HTML_REGEX,REGULAR_TRAILING_CONTENT_REGEX,pattern,trailingContentPattern
hcls HtmlEndTagValidator_Singleton

CLSS public com.xceptance.xlt.api.validators.HttpResponseCodeValidator
cons public init()
cons public init(int)
meth public boolean equals(java.lang.Object)
meth public int getHttpResponseCode()
meth public int hashCode()
meth public static com.xceptance.xlt.api.validators.HttpResponseCodeValidator getInstance()
meth public void validate(com.gargoylesoftware.htmlunit.html.HtmlPage)
meth public void validate(com.xceptance.xlt.api.htmlunit.LightWeightPage)
supr java.lang.Object
hfds httpResponseCode
hcls HttpResponseCodeValidator_Singleton

CLSS public com.xceptance.xlt.api.validators.StandardValidator
cons public init()
meth public static com.xceptance.xlt.api.validators.StandardValidator getInstance()
meth public void validate(com.gargoylesoftware.htmlunit.html.HtmlPage) throws java.lang.Exception
supr java.lang.Object
hcls StandardValidator_Singleton

CLSS public com.xceptance.xlt.api.validators.XHTMLValidator
cons public init(boolean,boolean)
meth public static com.xceptance.xlt.api.validators.XHTMLValidator getInstance()
meth public void validate(com.gargoylesoftware.htmlunit.html.HtmlPage) throws java.lang.Exception
meth public void validate(com.xceptance.xlt.api.htmlunit.LightWeightPage) throws java.lang.Exception
meth public void validate(java.lang.String) throws java.lang.Exception
supr java.lang.Object
hfds breakOnErrors,breakOnWarnings,enabled,propertyName
hcls LocalEntityResolver,LocalErrorHandler,XHTMLValidator_Singleton

CLSS public com.xceptance.xlt.api.webdriver.XltChromeDriver
cons public init()
cons public init(org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.Capabilities,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.chrome.ChromeDriverService)
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.Capabilities,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.chrome.ChromeOptions)
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.chrome.ChromeOptions,boolean)
cons public init(org.openqa.selenium.chrome.ChromeOptions)
cons public init(org.openqa.selenium.chrome.ChromeOptions,boolean)
innr public final static Builder
meth public static com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder xltBuilder()
meth public void close()
meth public void quit()
supr org.openqa.selenium.chrome.ChromeDriver
hfds CONNECT_RETRY_BASE_TIMEOUT,CONNECT_RETRY_COUNT,CONNECT_RETRY_TIMEOUT_FACTOR,EXTENSION_FILE_ENDING,EXTENSION_FILE_NAME,FIELD_NAME_ENVIRONMENT,HEADLESS_ENABLED,IGNORE_MISSING_DATA,LOG,PROPERTY_DOMAIN,PROPERTY_HEADLESS,PROPERTY_IGNORE_MISSING_DATA,PROPERTY_RECORD_INCOMPLETE,RECORD_INCOMPLETE_ENABLED,connectionHandler,extensionFile

CLSS public final static com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder
 outer com.xceptance.xlt.api.webdriver.XltChromeDriver
cons public init()
meth public com.xceptance.xlt.api.webdriver.XltChromeDriver build()
meth public com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder setCapabilities(org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder setHeadless(boolean)
meth public com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder setOptions(org.openqa.selenium.chrome.ChromeOptions)
meth public com.xceptance.xlt.api.webdriver.XltChromeDriver$Builder setService(org.openqa.selenium.chrome.ChromeDriverService)
supr java.lang.Object
hfds capabilities,headless,options,service

CLSS public final com.xceptance.xlt.api.webdriver.XltDriver
cons public init()
cons public init(boolean)
cons public init(com.gargoylesoftware.htmlunit.BrowserVersion)
cons public init(com.gargoylesoftware.htmlunit.BrowserVersion,boolean)
meth protected com.gargoylesoftware.htmlunit.WebClient newWebClient(com.gargoylesoftware.htmlunit.BrowserVersion)
meth public com.gargoylesoftware.htmlunit.WebClient getWebClient()
meth public java.lang.String getCurrentUrl()
meth public java.lang.String getPageSource()
meth public java.lang.String getTitle()
meth public java.lang.String getWindowHandle()
meth public java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By)
meth public java.util.Set<java.lang.String> getWindowHandles()
meth public org.openqa.selenium.WebDriver$Navigation navigate()
meth public org.openqa.selenium.WebDriver$Options manage()
meth public org.openqa.selenium.WebElement findElement(org.openqa.selenium.By)
meth public void close()
meth public void get(java.lang.String)
meth public void quit()
supr com.xceptance.xlt.engine.xltdriver.HtmlUnitDriver

CLSS public final com.xceptance.xlt.api.webdriver.XltFirefoxDriver
cons public init()
cons public init(org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.Capabilities,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.Capabilities,org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.firefox.FirefoxBinary,org.openqa.selenium.firefox.FirefoxProfile)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.firefox.FirefoxBinary,org.openqa.selenium.firefox.FirefoxProfile,org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.firefox.FirefoxBinary,org.openqa.selenium.firefox.FirefoxProfile,org.openqa.selenium.Capabilities,org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.firefox.FirefoxBinary,org.openqa.selenium.firefox.FirefoxProfile,org.openqa.selenium.Capabilities,org.openqa.selenium.Capabilities,boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.firefox.FirefoxOptions)
cons public init(org.openqa.selenium.firefox.FirefoxOptions,boolean)
cons public init(org.openqa.selenium.firefox.FirefoxProfile)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
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
meth public com.xceptance.xlt.api.webdriver.XltFirefoxDriver$Builder setCapabilities(org.openqa.selenium.Capabilities)
meth public com.xceptance.xlt.api.webdriver.XltFirefoxDriver$Builder setHeadless(boolean)
meth public com.xceptance.xlt.api.webdriver.XltFirefoxDriver$Builder setProfile(org.openqa.selenium.firefox.FirefoxProfile)
meth public com.xceptance.xlt.api.webdriver.XltFirefoxDriver$Builder setRequiredCapabilities(org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
supr java.lang.Object
hfds binary,capabilities,headless,options,profile

CLSS public com.xceptance.xlt.engine.xltdriver.HtmlUnitDriver
cons public init()
cons public init(boolean)
cons public init(com.gargoylesoftware.htmlunit.BrowserVersion)
cons public init(com.gargoylesoftware.htmlunit.BrowserVersion,boolean)
cons public init(org.openqa.selenium.Capabilities)
cons public init(org.openqa.selenium.Capabilities,org.openqa.selenium.Capabilities)
fld public final static java.lang.String BROWSER_LANGUAGE_CAPABILITY = "browserLanguage"
fld public final static java.lang.String DOWNLOAD_IMAGES_CAPABILITY = "downloadImages"
fld public final static java.lang.String INVALIDSELECTIONERROR = "The xpath expression '%s' selected an object of type '%s' instead of a WebElement"
fld public final static java.lang.String INVALIDXPATHERROR = "The xpath expression '%s' cannot be evaluated"
fld public final static java.lang.String JAVASCRIPT_ENABLED = "javascriptEnabled"
innr protected abstract interface static JavaScriptResultsCollection
innr public HtmlUnitWindow
intf org.openqa.selenium.HasCapabilities
intf org.openqa.selenium.JavascriptExecutor
intf org.openqa.selenium.WebDriver
intf org.openqa.selenium.interactions.HasInputDevices
intf org.openqa.selenium.internal.FindsByClassName
intf org.openqa.selenium.internal.FindsByCssSelector
intf org.openqa.selenium.internal.FindsById
intf org.openqa.selenium.internal.FindsByLinkText
intf org.openqa.selenium.internal.FindsByName
intf org.openqa.selenium.internal.FindsByTagName
intf org.openqa.selenium.internal.FindsByXPath
meth protected <%0 extends java.lang.Object> {%%0} implicitlyWaitFor(java.util.concurrent.Callable<{%%0}>)
meth protected com.gargoylesoftware.htmlunit.Page lastPage()
meth protected com.gargoylesoftware.htmlunit.WebClient getWebClient()
meth protected com.gargoylesoftware.htmlunit.WebClient modifyWebClient(com.gargoylesoftware.htmlunit.WebClient)
meth protected com.gargoylesoftware.htmlunit.WebClient newWebClient(com.gargoylesoftware.htmlunit.BrowserVersion)
meth protected com.gargoylesoftware.htmlunit.WebWindow getCurrentWindow()
meth protected com.xceptance.xlt.engine.xltdriver.HtmlUnitWebElement toWebElement(com.gargoylesoftware.htmlunit.html.DomElement)
meth protected void assertElementNotStale(com.gargoylesoftware.htmlunit.html.DomElement)
meth protected void get(java.net.URL)
meth protected void pickWindow()
meth public !varargs java.lang.Object executeAsyncScript(java.lang.String,java.lang.Object[])
meth public !varargs java.lang.Object executeScript(java.lang.String,java.lang.Object[])
meth public boolean isAcceptSslCertificates()
meth public boolean isDownloadImages()
meth public boolean isJavascriptEnabled()
meth public com.gargoylesoftware.htmlunit.BrowserVersion getBrowserVersion()
meth public com.xceptance.xlt.engine.xltdriver.HtmlUnitWebElement getElementById(int)
meth public java.lang.String getCurrentUrl()
meth public java.lang.String getPageSource()
meth public java.lang.String getTitle()
meth public java.lang.String getWindowHandle()
meth public java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByClassName(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByCssSelector(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsById(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByLinkText(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByName(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByPartialLinkText(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByTagName(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByXPath(java.lang.String)
meth public java.util.Set<java.lang.String> getWindowHandles()
meth public org.openqa.selenium.Capabilities getCapabilities()
meth public org.openqa.selenium.WebDriver$Navigation navigate()
meth public org.openqa.selenium.WebDriver$Options manage()
meth public org.openqa.selenium.WebDriver$TargetLocator switchTo()
meth public org.openqa.selenium.WebElement findElement(org.openqa.selenium.By)
meth public org.openqa.selenium.WebElement findElementByClassName(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByCssSelector(java.lang.String)
meth public org.openqa.selenium.WebElement findElementById(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByLinkText(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByName(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByPartialLinkText(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByTagName(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByXPath(java.lang.String)
meth public org.openqa.selenium.interactions.Keyboard getKeyboard()
meth public org.openqa.selenium.interactions.Mouse getMouse()
meth public void close()
meth public void get(java.lang.String)
meth public void quit()
meth public void setAcceptSslCertificates(boolean)
meth public void setAutoProxy(java.lang.String)
meth public void setDownloadImages(boolean)
meth public void setExecutor(java.util.concurrent.Executor)
meth public void setHTTPProxy(java.lang.String,int,java.util.List<java.lang.String>)
meth public void setJavascriptEnabled(boolean)
meth public void setProxy(java.lang.String,int)
meth public void setProxySettings(org.openqa.selenium.Proxy)
meth public void setSocksProxy(java.lang.String,int)
meth public void setSocksProxy(java.lang.String,int,java.util.List<java.lang.String>)
supr java.lang.Object
hfds alert,asyncScriptExecutor,conditionLock,currentWindow,defaultExecutor,elementsCounter,elementsMap,enableJavascript,exception,executor,gotPage,implicitWait,initialWindowDimension,keyboard,mainCondition,mouse,options,pageLoadStrategy,proxyConfig,runAsyncRunning,scriptTimeout,sleepTime,targetLocator,unexpectedAlertBehaviour,webClient,windowPosition
hcls HtmlUnitNavigation,HtmlUnitOptions,HtmlUnitTargetLocator,HtmlUnitTimeouts,PageLoadStrategy

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface org.openqa.selenium.HasCapabilities
meth public abstract org.openqa.selenium.Capabilities getCapabilities()

CLSS public abstract interface org.openqa.selenium.JavascriptExecutor
meth public abstract !varargs java.lang.Object executeAsyncScript(java.lang.String,java.lang.Object[])
meth public abstract !varargs java.lang.Object executeScript(java.lang.String,java.lang.Object[])

CLSS public abstract interface org.openqa.selenium.SearchContext
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By)
meth public abstract org.openqa.selenium.WebElement findElement(org.openqa.selenium.By)

CLSS public abstract interface org.openqa.selenium.TakesScreenshot
meth public abstract <%0 extends java.lang.Object> {%%0} getScreenshotAs(org.openqa.selenium.OutputType<{%%0}>)

CLSS public abstract interface org.openqa.selenium.WebDriver
innr public abstract interface static ImeHandler
innr public abstract interface static Navigation
innr public abstract interface static Options
innr public abstract interface static TargetLocator
innr public abstract interface static Timeouts
innr public abstract interface static Window
intf org.openqa.selenium.SearchContext
meth public abstract java.lang.String getCurrentUrl()
meth public abstract java.lang.String getPageSource()
meth public abstract java.lang.String getTitle()
meth public abstract java.lang.String getWindowHandle()
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By)
meth public abstract java.util.Set<java.lang.String> getWindowHandles()
meth public abstract org.openqa.selenium.WebDriver$Navigation navigate()
meth public abstract org.openqa.selenium.WebDriver$Options manage()
meth public abstract org.openqa.selenium.WebDriver$TargetLocator switchTo()
meth public abstract org.openqa.selenium.WebElement findElement(org.openqa.selenium.By)
meth public abstract void close()
meth public abstract void get(java.lang.String)
meth public abstract void quit()

CLSS public org.openqa.selenium.chrome.ChromeDriver
cons public init()
cons public init(org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.chrome.ChromeDriverService)
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.chrome.ChromeDriverService,org.openqa.selenium.chrome.ChromeOptions)
cons public init(org.openqa.selenium.chrome.ChromeOptions)
intf org.openqa.selenium.html5.LocationContext
intf org.openqa.selenium.html5.WebStorage
intf org.openqa.selenium.interactions.HasTouchScreen
intf org.openqa.selenium.mobile.NetworkConnection
meth public org.openqa.selenium.html5.LocalStorage getLocalStorage()
meth public org.openqa.selenium.html5.Location location()
meth public org.openqa.selenium.html5.SessionStorage getSessionStorage()
meth public org.openqa.selenium.interactions.TouchScreen getTouch()
meth public org.openqa.selenium.mobile.NetworkConnection$ConnectionType getNetworkConnection()
meth public org.openqa.selenium.mobile.NetworkConnection$ConnectionType setNetworkConnection(org.openqa.selenium.mobile.NetworkConnection$ConnectionType)
meth public void launchApp(java.lang.String)
meth public void setFileDetector(org.openqa.selenium.remote.FileDetector)
meth public void setLocation(org.openqa.selenium.html5.Location)
supr org.openqa.selenium.remote.RemoteWebDriver
hfds locationContext,networkConnection,touchScreen,webStorage

CLSS public org.openqa.selenium.firefox.FirefoxDriver
cons public init()
cons public init(org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.firefox.FirefoxOptions)
cons public init(org.openqa.selenium.firefox.GeckoDriverService)
cons public init(org.openqa.selenium.firefox.GeckoDriverService,org.openqa.selenium.Capabilities)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(org.openqa.selenium.firefox.GeckoDriverService,org.openqa.selenium.firefox.FirefoxOptions)
cons public init(org.openqa.selenium.firefox.XpiDriverService)
cons public init(org.openqa.selenium.firefox.XpiDriverService,org.openqa.selenium.firefox.FirefoxOptions)
fld protected org.openqa.selenium.firefox.FirefoxBinary binary
fld public final static java.lang.String BINARY = "firefox_binary"
fld public final static java.lang.String MARIONETTE = "marionette"
fld public final static java.lang.String PROFILE = "firefox_profile"
innr public final static SystemProperty
intf org.openqa.selenium.firefox.HasExtensions
intf org.openqa.selenium.html5.WebStorage
meth public java.lang.String installExtension(java.nio.file.Path)
meth public org.openqa.selenium.html5.LocalStorage getLocalStorage()
meth public org.openqa.selenium.html5.SessionStorage getSessionStorage()
meth public void setFileDetector(org.openqa.selenium.remote.FileDetector)
meth public void uninstallExtension(java.lang.String)
supr org.openqa.selenium.remote.RemoteWebDriver
hfds EXTRA_COMMANDS,webStorage
hcls ExtraCommands,FirefoxDriverCommandExecutor

CLSS public abstract interface org.openqa.selenium.firefox.HasExtensions
 anno 0 org.openqa.selenium.Beta()
meth public abstract java.lang.String installExtension(java.nio.file.Path)
meth public abstract void uninstallExtension(java.lang.String)

CLSS public abstract interface org.openqa.selenium.html5.LocationContext
meth public abstract org.openqa.selenium.html5.Location location()
meth public abstract void setLocation(org.openqa.selenium.html5.Location)

CLSS public abstract interface org.openqa.selenium.html5.WebStorage
meth public abstract org.openqa.selenium.html5.LocalStorage getLocalStorage()
meth public abstract org.openqa.selenium.html5.SessionStorage getSessionStorage()

CLSS public abstract interface org.openqa.selenium.interactions.HasInputDevices
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract org.openqa.selenium.interactions.Keyboard getKeyboard()
meth public abstract org.openqa.selenium.interactions.Mouse getMouse()

CLSS public abstract interface org.openqa.selenium.interactions.HasTouchScreen
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract org.openqa.selenium.interactions.TouchScreen getTouch()

CLSS public abstract interface org.openqa.selenium.interactions.Interactive
meth public abstract void perform(java.util.Collection<org.openqa.selenium.interactions.Sequence>)
meth public abstract void resetInputState()

CLSS public abstract interface org.openqa.selenium.internal.FindsByClassName
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElementsByClassName(java.lang.String)
meth public abstract org.openqa.selenium.WebElement findElementByClassName(java.lang.String)

CLSS public abstract interface org.openqa.selenium.internal.FindsByCssSelector
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElementsByCssSelector(java.lang.String)
meth public abstract org.openqa.selenium.WebElement findElementByCssSelector(java.lang.String)

CLSS public abstract interface org.openqa.selenium.internal.FindsById
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElementsById(java.lang.String)
meth public abstract org.openqa.selenium.WebElement findElementById(java.lang.String)

CLSS public abstract interface org.openqa.selenium.internal.FindsByLinkText
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElementsByLinkText(java.lang.String)
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElementsByPartialLinkText(java.lang.String)
meth public abstract org.openqa.selenium.WebElement findElementByLinkText(java.lang.String)
meth public abstract org.openqa.selenium.WebElement findElementByPartialLinkText(java.lang.String)

CLSS public abstract interface org.openqa.selenium.internal.FindsByName
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElementsByName(java.lang.String)
meth public abstract org.openqa.selenium.WebElement findElementByName(java.lang.String)

CLSS public abstract interface org.openqa.selenium.internal.FindsByTagName
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElementsByTagName(java.lang.String)
meth public abstract org.openqa.selenium.WebElement findElementByTagName(java.lang.String)

CLSS public abstract interface org.openqa.selenium.internal.FindsByXPath
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract java.util.List<org.openqa.selenium.WebElement> findElementsByXPath(java.lang.String)
meth public abstract org.openqa.selenium.WebElement findElementByXPath(java.lang.String)

CLSS public abstract interface org.openqa.selenium.mobile.NetworkConnection
innr public static ConnectionType
meth public abstract org.openqa.selenium.mobile.NetworkConnection$ConnectionType getNetworkConnection()
meth public abstract org.openqa.selenium.mobile.NetworkConnection$ConnectionType setNetworkConnection(org.openqa.selenium.mobile.NetworkConnection$ConnectionType)

CLSS public abstract interface !annotation org.openqa.selenium.remote.Augmentable
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public org.openqa.selenium.remote.RemoteWebDriver
cons protected init()
cons public init(java.net.URL,org.openqa.selenium.Capabilities)
cons public init(org.openqa.selenium.Capabilities)
cons public init(org.openqa.selenium.remote.CommandExecutor,org.openqa.selenium.Capabilities)
innr protected RemoteTargetLocator
innr protected RemoteWebDriverOptions
innr public final static !enum When
intf org.openqa.selenium.HasCapabilities
intf org.openqa.selenium.JavascriptExecutor
intf org.openqa.selenium.TakesScreenshot
intf org.openqa.selenium.WebDriver
intf org.openqa.selenium.interactions.HasInputDevices
intf org.openqa.selenium.interactions.Interactive
intf org.openqa.selenium.internal.FindsByClassName
intf org.openqa.selenium.internal.FindsByCssSelector
intf org.openqa.selenium.internal.FindsById
intf org.openqa.selenium.internal.FindsByLinkText
intf org.openqa.selenium.internal.FindsByName
intf org.openqa.selenium.internal.FindsByTagName
intf org.openqa.selenium.internal.FindsByXPath
meth protected java.util.List<org.openqa.selenium.WebElement> findElements(java.lang.String,java.lang.String)
meth protected org.openqa.selenium.WebElement findElement(java.lang.String,java.lang.String)
meth protected org.openqa.selenium.remote.ExecuteMethod getExecuteMethod()
meth protected org.openqa.selenium.remote.Response execute(java.lang.String)
meth protected org.openqa.selenium.remote.Response execute(java.lang.String,java.util.Map<java.lang.String,?>)
meth protected org.openqa.selenium.remote.internal.JsonToWebElementConverter getElementConverter()
meth protected void log(org.openqa.selenium.remote.SessionId,java.lang.String,java.lang.Object,org.openqa.selenium.remote.RemoteWebDriver$When)
meth protected void setCommandExecutor(org.openqa.selenium.remote.CommandExecutor)
meth protected void setElementConverter(org.openqa.selenium.remote.internal.JsonToWebElementConverter)
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
meth public java.util.List<org.openqa.selenium.WebElement> findElements(org.openqa.selenium.By)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByClassName(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByCssSelector(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsById(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByLinkText(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByName(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByPartialLinkText(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByTagName(java.lang.String)
meth public java.util.List<org.openqa.selenium.WebElement> findElementsByXPath(java.lang.String)
meth public java.util.Set<java.lang.String> getWindowHandles()
meth public org.openqa.selenium.Capabilities getCapabilities()
meth public org.openqa.selenium.WebDriver$Navigation navigate()
meth public org.openqa.selenium.WebDriver$Options manage()
meth public org.openqa.selenium.WebDriver$TargetLocator switchTo()
meth public org.openqa.selenium.WebElement findElement(org.openqa.selenium.By)
meth public org.openqa.selenium.WebElement findElementByClassName(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByCssSelector(java.lang.String)
meth public org.openqa.selenium.WebElement findElementById(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByLinkText(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByName(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByPartialLinkText(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByTagName(java.lang.String)
meth public org.openqa.selenium.WebElement findElementByXPath(java.lang.String)
meth public org.openqa.selenium.interactions.Keyboard getKeyboard()
meth public org.openqa.selenium.interactions.Mouse getMouse()
meth public org.openqa.selenium.remote.CommandExecutor getCommandExecutor()
meth public org.openqa.selenium.remote.ErrorHandler getErrorHandler()
meth public org.openqa.selenium.remote.FileDetector getFileDetector()
meth public org.openqa.selenium.remote.SessionId getSessionId()
meth public static org.openqa.selenium.remote.RemoteWebDriverBuilder builder()
 anno 0 org.openqa.selenium.Beta()
meth public void close()
meth public void get(java.lang.String)
meth public void perform(java.util.Collection<org.openqa.selenium.interactions.Sequence>)
meth public void quit()
meth public void resetInputState()
meth public void setErrorHandler(org.openqa.selenium.remote.ErrorHandler)
meth public void setFileDetector(org.openqa.selenium.remote.FileDetector)
meth public void setLogLevel(java.util.logging.Level)
supr java.lang.Object
hfds capabilities,converter,errorHandler,executeMethod,executor,fileDetector,keyboard,level,localLogs,logger,mouse,remoteLogs,sessionId
hcls RemoteAlert,RemoteNavigation

