/**
 * 
 * @param tag
 * @param id
 * @param name
 * @returns new tag
 */
function createTag(tag, id, name)
{
	var newTag = document.createElement(tag);
	
	if(id!=null && id.length>0)
	{
		newTag.setAttribute('id', id);
	}

	if(name!=null && name.length>0)
	{
		newTag.setAttribute('name', name);
	}

	return newTag;
}

/**
 * 
 * @param atID
 * @param tag
 * @param id
 * @param name
 * @returns inserted tag
 */
function insertTag(atID, tag, id, name)
{
	var parent = document.getElementById(atID);
	return insertElementTag(parent, tag, id, name);
}

/**
 * 
 * @param parentElement
 * @param tag
 * @param id
 * @param name
 * @returns inserted tag
 */
function insertElementTag(parentElement, tag, id, name)
{
	var element = createTag(tag, id, name)
	parentElement.appendChild( element );
				
	return element;
}

/**
 * 
 * @param atID
 * @param element
 * @returns element with atID
 */
function removeTag(atID, element)
{
	var parent = document.getElementById(atID);
	parent.removeChild(element);
	
	return parent;
}

/**
 * 
 * @param atID
 * @param id
 * @returns element with atID
 */
function removeTagById(atID, id)
{
	var parent = document.getElementById(atID);
	var element = document.getElementById(id);
	parent.removeChild(element);
	
	return parent;
}
/**
 * 
 * @param id
 * @param text
 * @returns the modified element with given atID
 */
function insertText(atID, text)
{
	var element = document.getElementById(atID);
	insertElementText(element, text);
	return element; 
}

/**
 * 
 * @param element
 * @param text
 * @returns the modified element
 */
function insertElementText(element, text)
{
	var textNode = document.createTextNode(text);
	element.appendChild(textNode);

	return element;
}

/**
 * 
 * @param atID
 * @param text
 */
function setText(atID, text)
{
	var element = document.getElementById(atID);
	setElementText(element, text);
}

/**
 * 
 * @param element
 * @param text
 */
function setElementText(element, text)
{
	element.innerHTML = text;
}

/**
 * 
 * @param at
 * @param id
 * @param name
 * @returns inserted div tag
 */
function insertDiv(atID, id, name)
{
	return insertTag(atID, 'div', id, name);
}

/**
 * 
 * @param at
 * @param id
 * @param name
 * @param type
 * @param value
 * @returns inserted input tag
 */
function insertInput(at, id, name, type, value)
{
	var element = insertTag(at, 'input', id, name);
	element.setAttribute('type', type);
	
	if(value!=null && value.length>0)
	{
		element.setAttribute('value', value);
	}
	
	return element;
}

/**
 * 
 * @param at
 * @param id
 * @param name
 * @param href
 * @param value
 * @returns inserted anchor tag
 */
function insertAnchor(at, id, name, href, value)
{
	var anchor = insertTag(at, 'a', id, name);

	if(href!=null && href.length>0)
	{
		anchor.setAttribute('href', href);
	}
	else
	{
		anchor.setAttribute('href', '#');
	}

	if(value!=null && value.length>0)
	{
		anchor.innerHTML = value;
		//anchor.appendChild( document.createTextNode(value) );
	}

	return anchor;
}

function readCookie(cookieName)
{
	var result = "";
	
	if(cookieName!=null && cookieName!="")
	{
		var allCookie = "" + document.cookie;
		var startIndex = allCookie.indexOf( cookieName + "=" );
		 
		if ( startIndex>=0)
		{
			var endIndex = allCookie.indexOf( ';', startIndex );
		 
			if ( endIndex==-1 ) 
				endIndex = allCookie.length; 
			
			//result = unescape( allCookie.substring( startIndex + cookieName.length + 1, endIndex ) );
			var cookieValue = allCookie.substring( startIndex + cookieName.length + 1, endIndex );
			cookieValue = cookieValue.replace(/"/g, "&quot;");
			return cookieValue;
			
		}
	}
	 
	return result;
}

function setCookie(cookieName, cookieValue, nDays)
{
	 var today = new Date();
	 var expire = new Date();
	 
	 if ( nDays==null || nDays==0 ) 
		 nDays=1;
	 
	 expire.setTime( today.getTime() + 3600000 * 24 * nDays );
	 
	 //document.cookie = cookieName + "=" + escape(cookieValue) + ";expires=0;path=/testpages/examplePage_1.html";// + expire.toGMTString();
	 document.cookie = cookieName + "=" + cookieValue + ";expires=0;path=/testpages/examplePage_1.html";// + expire.toGMTString();
}