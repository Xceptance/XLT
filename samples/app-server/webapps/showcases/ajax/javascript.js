function ajax()
{
    var xmlHttp = null;

    // Mozilla, Opera, Safari sowie Internet Explorer 7
    if (typeof XMLHttpRequest != 'undefined') 
    {
        xmlHttp = new XMLHttpRequest();
    }

    if (!xmlHttp) 
    {
        // Internet Explorer 6 und älter
        try 
        {
            xmlHttp  = new ActiveXObject("Msxml2.XMLHTTP");
        } 
        catch(e) 
        {
            try 
            {
                xmlHttp  = new ActiveXObject("Microsoft.XMLHTTP");
            } 
            catch(e) 
            {
                xmlHttp  = null;
            }
        }
    }
    
    if (xmlHttp) 
    {
        xmlHttp.open('GET', 'foo.txt', true);
        
        xmlHttp.onreadystatechange = function () 
        {
            if (xmlHttp.readyState == 4) 
            {
                document.getElementById("content").firstChild.data = xmlHttp.responseText;
            }
        };
        
        xmlHttp.send(null);
    }
}
