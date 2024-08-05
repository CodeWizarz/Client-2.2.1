function scriptCompleted(savefile) {

	var msg="\n##############################################\n";
   	msg=msg+"##### INJECTION COMPLETED SUCCESSFULLY #######\n";
   	msg=msg+"##############################################\n";
	var dateString = getDateString();
   	var textArea=window.document.getElementById('blog');
   	textArea.value = textArea.value+'\n'+dateString+': '+msg+'#'+savefile+'#';

	try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	} catch (e) {
		alert("Permission to save file was denied.");
		return true;
	}
	//alert( "Creating file: '"+savefile+"'");
	var file = Components.classes["@mozilla.org/file/local;1"]
		.createInstance(Components.interfaces.nsILocalFile);
	file.initWithPath( savefile );
	if ( file.exists() == false ) {
		file.create( Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 420 );
	}
	var outputStream = Components.classes["@mozilla.org/network/file-output-stream;1"]
		.createInstance( Components.interfaces.nsIFileOutputStream );
	/* Open flags
	#define PR_RDONLY       0x01
	#define PR_WRONLY       0x02
	#define PR_RDWR         0x04
	#define PR_CREATE_FILE  0x08
	#define PR_APPEND      0x10
	#define PR_TRUNCATE     0x20
	#define PR_SYNC         0x40
	#define PR_EXCL         0x80
	*/
	/*
	** File modes ....
	**
	** CAVEAT: 'mode' is currently only applicable on UNIX platforms.
	** The 'mode' argument may be ignored by PR_Open on other platforms.
	**
	**   00400   Read by owner.
	**   00200   Write by owner.
	**   00100   Execute (search if a directory) by owner.
	**   00040   Read by group.
	**   00020   Write by group.
	**   00010   Execute by group.
	**   00004   Read by others.
	**   00002   Write by others
	**   00001   Execute by others.
	**
	*/
	outputStream.init( file, 0x04 | 0x08 | 0x20, 420, 0 );
	var output = 'COMPLETED';
	var result = outputStream.write( output, output.length );
	outputStream.close();
	return true;
}

function logSectionToFile(line) {
   try {
      if (line.indexOf("# SECTION")!=-1 || line.indexOf("# ITERATION")!=-1 || 
    		  line.indexOf("*****************START OF RAPIDBUILD ITERATION NUMBER ") != -1 || 
    		  line.indexOf("*****************END OF RAPIDBUILD ITERATION NUMBER ") !=-1)	{
    	  writeToFile(logFileName,line+"\n");
      }
   }
   catch (e) {
	var errorMsg="logSectionToFile(). Cannot write to log file. Error: "+ e;
	alert(errorMsg);
   }
}
		
function writeToFile(fileName,line) {
   var lines=[];
   lines.push(line);
   writeLinesToFile(fileName,lines);
}

function writeLinesToFile(fileName,lines) {
   try {
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");

	var file = Components.classes["@mozilla.org/file/local;1"]
		.createInstance(Components.interfaces.nsILocalFile);
	file.initWithPath(fileName);
	if ( file.exists() == false ) {
		file.create( Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 420 );
	}
	var outputStream = Components.classes["@mozilla.org/network/file-output-stream;1"]
		.createInstance( Components.interfaces.nsIFileOutputStream );
	outputStream.init( file, 0x04 | 0x08 | 0x10, 420, 0 );
	
	for(var i=0;i<lines.length;i++) {
        var line=lines[i];
		outputStream.write( line,line.length );
	}
	lines=[];
	outputStream.close();
   }
   catch (e) {
	var errorMsg="Cannot write to file: "+fileName+". Error: "+ e;
	alert(errorMsg);
   }
}

function read() {
	try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	} catch (e) {
		alert("Permission to read file was denied.");
	}
	var file = Components.classes["@mozilla.org/file/local;1"]
		.createInstance(Components.interfaces.nsILocalFile);
	file.initWithPath( savefile );
	if ( file.exists() == false ) {
		alert("File does not exist");
	}
	var is = Components.classes["@mozilla.org/network/file-input-stream;1"]
		.createInstance( Components.interfaces.nsIFileInputStream );
	is.init( file,0x01, 00004, null);
	var sis = Components.classes["@mozilla.org/scriptableinputstream;1"]
		.createInstance( Components.interfaces.nsIScriptableInputStream );
	sis.init( is );
	var output = sis.read( sis.available() );
	document.getElementById('blog').value = output;
}

function setDocument(){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    if (isPopupWindow) 	{
		rscDocument=null;
		var wmc = Components.classes['@mozilla.org/appshell/window-mediator;1'];
    	var wm =wmc.getService(Components.interfaces.nsIWindowMediator);
		var browserEnumerator = wm.getEnumerator("navigator:browser");
		//addToLog('browserEnumerator:'+browserEnumerator);
		while (browserEnumerator.hasMoreElements()) {
			var win = browserEnumerator.getNext();
			var frameDoc=null;
			if (win.content && win.content.frames[0]) {
				frameDoc=win.content.frames[0].content.document.body.childNodes[0].contentDocument;
				//addToLog('frameDoc retrieved from an array of frame');
			}
			else
			if (win.content && win.content.document) {
				frameDoc=win.content.document;
				//addToLog('frameDoc retrieved from the content.document');
			}	
			//addToLog('frameDoc:'+frameDoc);
			if (frameDoc) {
					rscWindow=win;
					rscDocument=frameDoc;
		   
					rscWindow.removeEventListener("DOMContentLoaded", setDocumentLoadedPopup, false );	
					rscWindow.addEventListener("DOMContentLoaded", setDocumentLoadedPopup, false );
					rscWindow.removeEventListener("DOMWindowClose", setDOMWindowCloseEventReceived, false );
					rscWindow.addEventListener( "DOMWindowClose", setDOMWindowCloseEventReceived, false );
			}
		}	
    }
    else{
	 // if the popup is closed, then the main window will reload but the code below will assign
       // the old document before the new DOM and it will not fail, as the element will be found!!!
       // so we need to check if the main window is still old or not.
       // If the current DOM hasn't yet been switched to the orginal DOM, don't need to update the DOM
		if(tempRSCDocument === null) {
			rscDocument=rscBrowser.contentDocument;
			rscWindow=window;
		}
    }
}

function setInputValue(value){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    addToLog('setInputValue: '+value+' rscElt:'+rscElt);
    if (rscElt) {
		rscElt.focus();
		rscElt.value=value;
		return true;
    }
    else return false;
}

function setChecked(value){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    rscElt.checked=value;
    return true;
}

function getSequenceByTextNode(textNode){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();

    rscSequence=textNode;
	return true;
}

function getSequence(){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    return getSequenceByToken(null,null);
}

function getSequenceByToken(textBeforeSequence,textAfterSequence){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    if (!rscAttributeValue) throw "rscAttributeValue is not set!";

	var temp=rscAttributeValue;
	var hasBeforeOrAfterText=false;
    if (textBeforeSequence)
    {
		hasBeforeOrAfterText=true;
		var indexOf=temp.indexOf(textBeforeSequence);
		if (indexOf==-1) {
			return false;
		}
		temp=temp.substring(indexOf+textBeforeSequence.length);
    }
	if (textAfterSequence)
    {
		hasBeforeOrAfterText=true;
		var indexOf=temp.indexOf(textAfterSequence);
		if (indexOf==-1) {
			return false;
		}
		temp=temp.substring(0,indexOf);
    }
	if (hasBeforeOrAfterText) {
		rscSequence=temp;
	}
	else {
		var indexOf=rscAttributeValue.lastIndexOf(":");
		if (indexOf==-1) {
			rscSequence=temp;
		}
		else {
			rscSequence=rscAttributeValue.substring(indexOf+1);
		}
	}
    addToLog('rscSequence: '+rscSequence);
    return true;
}

function needToWaitForPopupIframe(popupIframeId, saveRSCDocument) {
    if (popupIframeId != null && popupIframeId.trim().length > 0) {

		if (rscDocument.getElementById(popupIframeId) != null 
				&& rscDocument.getElementById(popupIframeId).contentDocument != null 
				&& rscDocument.getElementById(popupIframeId).contentDocument.readyState == 'complete'
				&& rscDocument.getElementById(popupIframeId).contentDocument.body.getElementsByTagName('frame').length > 0
				&& rscDocument.getElementById(popupIframeId).contentDocument.body.getElementsByTagName('frame')[0].contentDocument != null
				&& rscDocument.getElementById(popupIframeId).contentDocument.body.getElementsByTagName('frame')[0].contentDocument.readyState == 'complete') {
			tempRSCDocument = saveRSCDocument ? rscDocument:null;
			rscDocument = rscDocument.getElementById(popupIframeId).contentDocument.body.getElementsByTagName('frame')[0].contentDocument;
			iframeDocument = rscDocument;
			return false;
		}
		
		return true;
    } else {
    	return false;
    }	
}

function getElementById(idValue, popupIframeId){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    addToLog('DEBUG  getElementById ID: '+idValue);
    setDocument();
    if (!rscDocument) return false;
    
    var originalRscDocument = rscDocument;
    
    if (needToWaitForPopupIframe(popupIframeId)) {
    	currentStep--;
		return false;
    }
    
    try {
        rscElt=rscDocument.getElementById(idValue);
        addToLog('DEBUG  getElementById: idValue = ' + idValue + ', rscElt: ' + rscElt);
        if (rscElt) return true;
        else return false;    	
    } finally {
    	rscDocument = originalRscDocument;
    }
}

function getAttributeValue(attributeName){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    rscAttributeValue=rscElt.getAttribute(attributeName);
    //addToLog('rscAttributeValue: '+rscAttributeValue);

    if (rscAttributeValue) return true;
    else return false;
}


function setCheckBox(checked){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    hasCheckBoxStatusChanged=false;
    if ( currentStep !=  steps.length) {
      var tmp=steps[currentStep+1];
      if (tmp=='ensureDOMisLoaded();') {
          isCheckBoxMode=true;
      }
    }
    if (rscElt.defaultChecked) {
	    if (checked) {
              // already checked, so do nothing
          }
          else {
              // must uncheck it:
              addToLog('generate a CLICK on the checkbox to uncheck it');
              hasCheckBoxStatusChanged=true;
              rscElt.click();
          }
    }
    else {
    	    if (checked) {
              // must check it:
              addToLog('generate a CLICK on the checkbox to check it');
              hasCheckBoxStatusChanged=true;
              rscElt.click();
          }
          else {
              // already unchecked, so do nothing
              addToLog('Checkbox already unchecked, do nothing');
          }
    }
    return true;
}

function setSelectOption(value,isSelectOptionByText){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    var length=rscElt.length;
    var index=-1;
	
    if ( value == '' || value == '\"\"' )
	{
		index = 0;
	}
	else
	{	
		for (i=0;i<length;i++) {
			 var option=rscElt.options[i];
			 var optionText;
			 if (isSelectOptionByText) {
				optionText=option.text;
			 }
			 else {
				optionText=option.value;
			 }
			 
			 //addToLog("optionText: '"+optionText+"'");
			 if (optionText!='') {
				if (trim(value).indexOf('##SUBSTRING##')==-1 )
				{
					if ( trim(optionText)==trim(value) )
					{
						index=i;
						break;
					}
				}
				else
				{
					var indexTmp=trim(value).indexOf('##SUBSTRING##');
					var tmp=trim(value).substring(0,indexTmp);
					if (trim(optionText).indexOf(tmp)!=-1)
					{
						index=i;
						break;
					}
				}
			 }
		  }
	}
      if (index!=-1  ) {
          var alreadySelectedIndex=rscElt.selectedIndex;
          //if ( index!=alreadySelectedIndex ) {
            rscElt.focus();
            rscElt.selectedIndex=index;
            // generate an event in case some javascript functions are defined in
            // the "onchange" attribute of the element.
            generateHTMLEventOnCurrentElement('change');
          //}
          //else
          //{
          //  addToLog("VALUE: '"+value+"' ALREADY SELECTED: NO ACTION");
          //}
      }
      else throw "Cannot select the value: '"+value+"' from the drop down list.";
	return true;
}

function generateClickEventOnCurrentElement(){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) {
		return false;
	}
    
    dispatchClickEventBrowser(2);
	
    return true;
}

function generateClickEventOnCurrentElementForceUseMouseClickEvent(){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) {
		return false;
	}
    
	addToLog('Dispatching click event from document...');
	var eventRSC = rscDocument.createEvent('MouseEvents');
	eventRSC.initEvent("click", true, true);
	rscElt.dispatchEvent(eventRSC);
	
    return true;
}

function generateClickEventOnParentOfCurrentElement(){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;
	
	var onclickAttrOnCurrentElement = rscElt.getAttribute('onclick');
    if (onclickAttrOnCurrentElement) {
		addToLog('found onclick attribute for the current element and is going to dispatch Click Event');
		if (generateClickEventOnCurrentElement()) {
			return true;
		}
	} 
	
    var parentEltOfCurrentElt=rscElt.parentNode;
    if (!parentEltOfCurrentElt) {
		addToLog('not generating event because no parent element found for the current element');
		return true;
    }

    var onclickAttr=parentEltOfCurrentElt.getAttribute('onclick');
    if (!onclickAttr) {
		// look at the next level:
		parentEltOfCurrentElt=parentEltOfCurrentElt.parentNode;
		if (!parentEltOfCurrentElt) {
			return true;
		}
		onclickAttr=parentEltOfCurrentElt.getAttribute('onclick');
		if (!onclickAttr) {
			addToLog('not generating event because the parent elements does not have the "onclick" attribute');
			return true;
		}
    }
    var rscEvent =  rscBrowser.contentDocument.createEvent('MouseEvents');
    rscEvent.initMouseEvent( 'click', true, true, rscWindow, 1, 12, 345, 7, 220, false, false, true, false, 0, null );
    rscElt.dispatchEvent(rscEvent);
    addToLog('Generating event on current element: '+rscElt+' ; '+rscEvent+' ms ...');
    return true;
}

function getPreviousElementNode(n)
{
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	var x=n.previousSibling;
	while (x && x.nodeType!=1)
	{
		x=x.previousSibling;
	}
	return x;
}

function getPreviousSibling(n)
{
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	var x=getPreviousElementNode(n);
	addToLog('previous sibling node:'+x);
	if (!x) {
		x=n.parentNode;
		addToLog('parent node:'+x);
		if (x) {
			x=getPreviousElementNode(x);
			addToLog('sibling from parent node:'+x);
		}
	}
	addToLog('final node:'+x);
	return x;
}

function getPreviousSiblingOfCurrentElement(tagName){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) {
		return false;
	}
    var siblingNode=rscElt;
	while (true)
	{
		siblingNode=getPreviousSibling(siblingNode);
		if (!siblingNode) {
			addToLog('Cannot find the sibling element for the current element');
			return false;
		}
		if (siblingNode.tagName.toUpperCase()==tagName.toUpperCase())
		{
			addToLog('Found sibling element: '+siblingNode.tagName);
			rscElt=siblingNode;
			return true;
		}
		else {
			if (siblingNode.firstChild && siblingNode.firstChild.tagName
				&& siblingNode.firstChild.tagName==tagName.toUpperCase() )
			{
				addToLog('Found element in the first child node of sibling: '+siblingNode.firstChild.tagName);
				rscElt=siblingNode.firstChild;
				return true;
			}
		}
	}
}

function getParentNodeUntilFound(tagNameToFound,node)
{
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	var x=node.parentNode;
	while (x && x.tagName && x.tagName.toLowerCase()!=tagNameToFound.toLowerCase() )
	{
		x=x.parentNode;
	}
	return x;
}

function getSiblingsCount(siblingsTagName,node)
{
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	var x=node.previousSibling;
	//addToLog('x: '+x+' x.tagName:'+x.tagName+' x.previousSibling='+x.previousSibling);
	var count=0;
	while (x)
	{
		if (x.tagName && x.tagName.toLowerCase()==siblingsTagName.toLowerCase() )
		{
			// only keep TRs where there is at least an input field otherwise this is not related to data and can be ignored
			// see Mantis ticket: 2386
			var countTemp=x.getElementsByTagName('INPUT').length;
			addToLog('getSiblingsCount INPUT count='+countTemp);
			if (countTemp > 0 ) {
				count++;
			}
		}
		x=x.previousSibling;
	}
	return count;
}

function getRowSequence()
{
	return getRowSequenceByType('INTERNAL');
}

function getVisualSequence()
{
	return getRowSequenceByType('VISUAL');
}

function getRowSequenceByType(type)
{
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

	if (rscElt) {
		var trNode=getParentNodeUntilFound('tr',rscElt);
		var siblingsCount=getSiblingsCount('tr',trNode);
		addToLog('siblingsCount: '+siblingsCount);
		if (type=='VISUAL') {
			rscSequence=''+(siblingsCount);
		}
		else {
			// remove the header row:
			rscSequence=''+(siblingsCount-1);
		}
		addToLog('found row sequence: '+rscSequence);
	}
	return true;
}

function setSiblingsFlag(isFlagOn)
{
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	siblingsFlag=isFlagOn;
	if (!isFlagOn) {
		rscRootElt=null;
	}
	else {
		// stop at the row level
		rscRootElt=getParentNodeUntilFound('tr',rscElt);
		addToLog('rscRootElt: '+rscRootElt);
	}
	return true;
}

function modifyText() {
    alert('event received');
}

function generateHTMLEventOnCurrentElement(eventType){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    var tmpAttr=rscElt.getAttribute('on'+eventType);

    if (tmpAttr) {
    	//rscElt.addEventListener(eventType, modifyText, false);
    	var rscEvent =  rscBrowser.contentDocument.createEvent('HTMLEvents');
    	addToLog('generating event: '+rscEvent+' ms ...');

    	rscEvent.initEvent(eventType, true, false);
    	rscElt.dispatchEvent(rscEvent);
    }
    return true;
}

function generateKeysEventOnCurrentElement(eventType){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    var tmpAttr=rscElt.getAttribute('on'+eventType);

    if (tmpAttr) {
    	//rscElt.addEventListener(eventType, modifyText, false);
    	var rscEvent =  rscBrowser.contentDocument.createEvent('KeyEvents');
    	addToLog('## GENERATING event: '+rscEvent+' ms ...');

    	rscEvent.initEvent(eventType, true, false);
    	rscElt.dispatchEvent(rscEvent);
    }
    return true;
}

function getElementByTextNodeSelectiveAttributeValue(tagName,text,attributeNameRequired,attributeValueRequired){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	return getElementByTextNodeSelectiveGeneric(tagName,text,attributeNameRequired,attributeValueRequired);
}

function getElementByTextNodeSelective(tagName,text,attributeNameRequired){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    return getElementByTextNodeSelectiveGeneric(tagName,text,attributeNameRequired,':');
}

function getElementByTextNodeSelectiveGeneric(tagName,text,attributeNameRequired,attributeValueRequired){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    var elts=rscDocument.getElementsByTagName(tagName);
    rscElt=null;
    for(var i=0;i<elts.length;i++) {
		var tempEltRSC=elts[i];
		var attributeValue=tempEltRSC.getAttribute(attributeNameRequired);
		if (attributeValue && attributeValue.indexOf(attributeValueRequired)!=-1) {
			var isFound=getElementByTextNodeGeneric(tempEltRSC,tagName,text,true);
	   		if (isFound) {
				return true;
			}
		}
    }
    return false;
}

function getElementByTextNode(tagName,text,popupIframeId){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;
    
    var originalRscDocument = rscDocument; 
    
    if (needToWaitForPopupIframe(popupIframeId)) {
    	currentStep--;
		return false;
    }    
	
	if ( text.indexOf("LOOKUP_VALUE") != -1 ) {
		text=text.replace("LOOKUP_VALUE",""+rscSequence);
	}

    try {
        var elts=rscDocument.getElementsByTagName(tagName);
        rscElt=null;
        for(var i=0;i<elts.length;i++) {
    		var tempEltRSC=elts[i];
    		//var isFound=getElementByTextNodeGeneric(tempEltRSC,tagName,text,false);
			var isFound=getElementByConcatenatedTextNodeGeneric(tempEltRSC,tagName,text);
    		if (isFound) {
    			return true;
    		}
        }
        return false;    	
    } finally {
    	rscDocument = originalRscDocument;
    }
}

function getElementByConcatenatedTextNodeGeneric(tempEltRSC,tagName,text){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
   
    if (tagName.toLowerCase()=='select') {
		var textNodeValue =tempEltRSC.options[tempEltRSC.selectedIndex].text;
		//addToLogNoCommand('select textNodeValue:'+textNodeValue+'#texttofind:'+text+'#');
		if (trim(text)==trim(textNodeValue) ){
    		rscElt=tempEltRSC;
    		return true;
		}
	}
	else {
		var textNodeValue=getConcatenatedTextNodeValues(tempEltRSC);
	 	if (textNodeValue){
			//addToLogNoCommand('textNodeValue:'+textNodeValue+'#texttofind:'+text+'#');
       		if (trim(text)==trim(textNodeValue) ){
           		rscElt=tempEltRSC;
				addToLog('textNodeValue found: "'+textNodeValue+'" text: "'+text+'" element: '+rscElt);
           		return true;
       		}
		}
    }
    return false;
}

function getConcatenatedTextNodeValues(node){
  var all = textNodesUnder(node);
  var x = all.length; 
  var toReturn="";
  for (var i=0; i< x; i++) {
	toReturn=toReturn+all[i];
  }
  return toReturn;
}

function textNodesUnder(node){
  var all = [];
  for (node=node.firstChild;node;node=node.nextSibling){
    if (node.nodeType==3) all.push(node.nodeValue);
    else all = all.concat(textNodesUnder(node));
  }
  return all;
}

function getElementByTextNodeGeneric(tempEltRSC,tagName,text,isSelectedInitialTagByAttribute){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
   
    if (tagName.toLowerCase()=='select') {
		var textNodeValue =tempEltRSC.options[tempEltRSC.selectedIndex].text;
		//addToLogNoCommand('select textNodeValue:'+textNodeValue+'#texttofind:'+text+'#');
		if (trim(text)==trim(textNodeValue) ){
    		rscElt=tempEltRSC;
    		return true;
		}
	}
	else {
		var textNodeValue=getFirstTextNodeValue(tempEltRSC,isSelectedInitialTagByAttribute);
	 	if (textNodeValue){
			//addToLogNoCommand('textNodeValue:'+textNodeValue+'#texttofind:'+text+'#');
       		if (trim(text)==trim(textNodeValue) ){
           		rscElt=tempEltRSC;
				addToLog('text node found: '+textNodeValue+' element: '+rscElt);
           		return true;
       		}
		}
    }
    return false;
}

function getFirstTextNodeValue(node,isSelectedInitialTagByAttribute){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    var res="";
    var children=node.childNodes;
    var length=children.length;
    if (length==0) {
		return node.nodeValue;
	}
    for (var i=0; i< length; i++) {
        var childNode = children[i];
		if ( !isSelectedInitialTagByAttribute && childNode != node && childNode.tagName == node.tagName )
		{	//For text being sought for a select-tag-by-attribute, we retain our selected tag as correct (don't fail text match) even though we found
			//another tag of the same type (but undetermined attribute value) inside.
			//For text NOT being sought by a select-tag-by-attribute, we want to ensure we get the innermost matching tag - the tag closest to the text being sought.
			return null;
		}
		else if (childNode.nodeType == 3 /*Node.TEXT_NODE*/ 
		) {
			return childNode.nodeValue;
		}
		else {
			var rec=getFirstTextNodeValue(childNode,isSelectedInitialTagByAttribute);
			if (rec && rec!=""){
				return rec;
			}
		}
    }
    return res;
}

function getButtonElementByTextNode(tagName,text){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    var elts=rscDocument.getElementsByTagName(tagName);
    rscElt=null;
	
    for(var i=0;i<elts.length;i++) {
		var tempEltRSC=elts[i];
		var textNodeValue=getTextNodeValue(tempEltRSC);
		if (textNodeValue){
       		if (trim(text)==trim(textNodeValue) ){
              rscElt=tempEltRSC;
              return true;
       		}
		}
    };
    return false;
}

function trim(stringToTrim) {
	return stringToTrim.replace(/^\s+|\s+$/g,"");
}


function getTextNodeValue(node){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    var res="";
    var children=node.childNodes;
    var length=children.length;
    if (length==0) {
		addToLog('node.nodeValue:'+node.nodeValue);
		return node.nodeValue;
	}
    for (var i=0; i< length; i++) {
        var childNode = children[i];
        res=res+getTextNodeValue(childNode);
    }
    return res;
}

function URLDecode(psEncodeString)
{
  return unescape(psEncodeString);
}

function isFirefoxOrGreater(version) {
	if (/Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent)){ 
		//test for Firefox/x.x or Firefox x.x (ignoring remaining digits);
		var ffversion=new Number(RegExp.$1) // capture x.x portion and store as a number
		if (ffversion>=version)
			 return true;
		else return false;
	}
	return false;
}

function generateClickAnchorOnCurrentElement(){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if(tempRSCDocument === null) {
		rscDocument=rscBrowser.contentDocument;
	}
    if (!rscDocument) return false;

    resetDocumentLoaded();
    var hrefAttr=rscElt.href; // this code forces the browser to translate into a full URL!

    var isJavascript=false;
    if (hrefAttr && hrefAttr.indexOf('javascript')!=-1) {
		isJavascript=true;
	}
    var onclickAttr=rscElt.getAttribute('onclick');
    if (onclickAttr || isJavascript) {
		addToLog('onclick Attribute or Javascript in HREF (js in href:'+isJavascript+')');
		if (isJavascript) {
			if (onclickAttr && onclickAttr!='') {
				addToLog('Not overriding what is already in the onclick attribute.');
			}
			else {
				addToLog('javascript found in href attr, setting onclick attribute: '+URLDecode(hrefAttr));
				rscElt.setAttribute("onclick","eval(unescape(this.href));");
			}
		}
		dispatchClickEventBrowser(3);
    }
    else {
		if (hrefAttr) {
			addToLog('hrefAttr found: '+hrefAttr);
			rscDocument.location.href=hrefAttr;
		}
    }
    return true;
}

function dispatchClickEventFirefoxVersionGreaterThan(version){
	if ( isFirefoxOrGreater(version) ) {
		try{	
			addToLog('Firefox version: '+version+' or greater detected, trying dispatching event from ONCLICK attribute...');
			rscElt["onclick"]();
			return true;
		}
		catch(e) {
			var msg="error dispatching event, using fallback position. error: "+e;
			addToLog(msg);
			return false;
		}
	}
	else {
		return false;
	}
}

function dispatchClickEventBrowser(version){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	var isDispatchClickEvent=dispatchClickEventFirefoxVersionGreaterThan(version);
	if (!isDispatchClickEvent) {
		addToLog('Dispatching click event from document...');
		var eventRSC = rscDocument.createEvent('MouseEvents');
		eventRSC.initEvent("click", true, true);
		rscElt.dispatchEvent(eventRSC);
	}
}

function replaceAllRegex(txt, replace, with_this) {
  return txt.replace(new RegExp(replace, 'g'),with_this);
}

function replaceAllLoop(source,stringToFind,stringToReplace){
  var temp = source;
  var index = temp.indexOf(stringToFind);
  var res="";
  if (index == -1)
  {
	  res=source;
  }
  while(index != -1){
        res += temp.substring(0,index)+stringToReplace;
		temp=temp.substring(index+1);
        index = temp.indexOf(stringToFind);
		if (index == -1)
		{
			res += temp;
		}
  }
  return res;
}

function getElementByAttributeValue(tagName,attrName,attrVal,isMatchExactValue,popupIframeId){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) {
		return false;
	}
    
    var originalRscDocument = rscDocument;
    
    if (needToWaitForPopupIframe(popupIframeId)) {
    	currentStep--;
		return false;
    }
    
    try {
    	var elts=null;
    	if (siblingsFlag) {
    		elts=rscRootElt.getElementsByTagName(tagName);
    		addToLog('retrieving elements by siblings: elts:'+elts);
    	}
    	else {
    		elts=rscDocument.getElementsByTagName(tagName);
    	}
        
        rscElt=null;
    	var tmp=replaceAllLoop(attrVal,"(","\\(");
    	tmp=replaceAllLoop(tmp,")","\\)")
    	tmp=replaceAllRegex(tmp,"SUB_STRING",".*");
		
		if ( tmp.indexOf("LOOKUP_VALUE") != -1 ) {
			tmp=tmp.replace("LOOKUP_VALUE",""+rscSequence);
		}
		
    	if (tagName.toLowerCase()=='a') {
    		tmp=replaceAllRegex(tmp," ",".*");
    	}
    	
    	var regExpr=new RegExp(tmp, 'i');
    	if (!isMatchExactValue) {
    		addToLog('looking for regExpr: @'+regExpr+'@ tmp: @'+tmp+'@ rscSequence:'+rscSequence);
    	}
        for(var i=0;i<elts.length;i++) {
            var tempElt=elts[i];
            var tempEltAttrVal=tempElt.getAttribute(attrName);
            if (tempEltAttrVal) {
				//addToLog('attrVal: @'+attrVal+'@ ; tempEltAttrVal: @'+tempEltAttrVal+'@');

              if (isMatchExactValue) {
                  if (tempEltAttrVal==attrVal) {
     					addToLog('Found the elt, exact match');
    	                rscElt=tempElt;
    					return true;
                  }
              }
              else {
                  if (regExpr.test(tempEltAttrVal) ) {
    					addToLog('Found the elt, regex: "'+tempEltAttrVal+'"');
    	                rscElt=tempElt;
    					return true;
                  }
              }
            }
        }
        return false;        	
    } finally {
    	rscDocument = originalRscDocument;
    }
	

}

function getElementByMatchingSearchAttributeValue(tagName,attrName,searchAttrVal, targetValue){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) {
		return false;
	}
    
    var originalRscDocument = rscDocument;
    
    try {
    	var elts=null;
    
    	elts=rscDocument.getElementsByTagName(tagName);

        rscElt=null;
        for(var i=0;i<elts.length;i++) {
            var tempElt=elts[i];
            var tempEltAttrVal=tempElt.getAttribute(attrName);
            console.log('attrName:'+attrName);
            console.log('tempEltAttrVal:'+tempEltAttrVal);
	          if (tempEltAttrVal && tempEltAttrVal.indexOf(searchAttrVal) > -1) {
					addToLog('Found the elt by matching Search Attribute Value');
	                rscElt=tempElt;
	                console.log('Found the elt by matching Search Attribute Value');
					return true;
	          }
        }
        addToLog('Not Found the elt by matching Search Attribute Value !!');
        return false;        	
    } finally {
    	rscDocument = originalRscDocument;
    }
}

function ensureNoErrors(){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    var elts=rscDocument.getElementsByTagName('img');
    for(var i=0;i<elts.length;i++) {
        var tempElt=elts[i];
        var tempEltAttrVal=tempElt.getAttribute('src');
        if (tempEltAttrVal) {
            if (
              tempEltAttrVal=='/OA_HTML/cabo/images/swan/errorl.gif'
              || tempEltAttrVal=='/OA_HTML/cabo/images/cache/cerr.gif'
              || tempEltAttrVal=='/OA_HTML/cabo/images/errorl.gif'
            ) {
               isError=true;
               alert('ORACLE APPS ERROR detected, please review the Oracle error on the second tab.\n'+
               ' You must close Firefox, fix your data and/ or restart the injection from another record.'); 
               break;
            }
        }
    }
    return true;
}

function getXElementByAttributeValue(tagName,attrName,attrVal,matchExactValue,position){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    var elts=rscDocument.getElementsByTagName(tagName);
    rscElt=null;
    //addToLog('attrVal:'+attrVal);
    counter=0;
    for(var i=0;i<elts.length;i++) {
		var tempElt=elts[i];
        var tempEltAttrVal=tempElt.getAttribute(attrName);
        if (tempEltAttrVal) {
          if (matchExactValue) {
              if (tempEltAttrVal==attrVal) {
				counter++;
 				addToLog('Found the elt, exact match, checking position:'+counter);
				if (counter==position) {
                  	rscElt=tempElt;
					return true;
				}
              }
          }
          else {
              if (tempEltAttrVal.indexOf(attrVal)!=-1) {
                counter++;
				addToLog('Found the elt, sub string, checking position:'+counter);
                if (counter==position) {
					rscElt=tempElt;
					return true;
				}
              }
          }
        }
    }
    return false;
}

function getElementByAttributeBlankValue(tagName,attrName,attrVal,matchExactValue){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    var elts=rscDocument.getElementsByTagName(tagName);
    rscElt=null;
    for(var i=0;i<elts.length;i++) {
        var tempElt=elts[i];
        var tempEltAttrVal=tempElt.getAttribute(attrName);
        if (tempEltAttrVal) {
          //alert('tempEltAttrVal:'+tempEltAttrVal);
	    //addToLog('tempEltAttrVal:'+tempEltAttrVal);

          if (matchExactValue) {
              if (tempEltAttrVal==attrVal) {
				  if (tagName=="SELECT")
				  {
					  if (hasSelectTagNoSelectedValue(tempElt))
					  {
						  rscElt=tempElt;
						  return true;
					  }
				  }
				  else {
						var hasBlankAttrValue=hasBlankAttributeValue(tempElt,'value');
 						if (hasBlankAttrValue) {
  		  					addToLog('Found the elt, exact match');
							rscElt=tempElt;
							return true;
						}
				  }
              }
          }
          else {
              if (tempEltAttrVal.indexOf(attrVal)!=-1) {
				  if (tagName=="SELECT")
				  {
					  if (hasSelectTagNoSelectedValue(tempElt))
					  {
						  rscElt=tempElt;
						  return true;
					  }
				  }
				  else {
						var hasBlankAttrValue=hasBlankAttributeValue(tempElt,'value');
 						if (hasBlankAttrValue) {
							addToLog('Found the elt, sub string');
							rscElt=tempElt;
							return true;
						}
				  }
              }
          }
        }
    }
    return false;
};

function hasSelectTagNoSelectedValue(eltParam){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	while ( eltParam.selectedIndex < 0 )
	{
		alert('eltParam.selectedIndex < 0; please wait a moment then click OK');
	}
    var selectedValue=eltParam.options[eltParam.selectedIndex].value;
	addToLog('SELECT detected, checking for selected index or not :'+selectedValue+'###');
	if (trim(selectedValue)=='')
	{
		addToLog('Found the elt for SELECT');
		return true;
	}
	return false;
};


function hasBlankAttributeValue(eltParam,attrName){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    if (eltParam) {
		var tempEltBlankVal;
		if (eltParam.tagName=='INPUT') {
			var inputType=eltParam.getAttribute("type");
			if (inputType && inputType.toLowerCase()=='hidden')
			{
				return false;
			}
			tempEltBlankVal=eltParam.value;
		}
    	else {
			tempEltBlankVal=eltParam.getAttribute(attrName);
		}

		//special case for textarea and their ilk.  textarea.value returns a result, but textarea.getAttribute('value') does not, presumably as it's not a real attribute.
		if (!tempEltBlankVal) {
			tempEltBlankVal = eval('eltParam' + '.' + attrName);
		}		

		if (tempEltBlankVal) {
			if (tempEltBlankVal=='') return true;
			else return false;
		}
		else {
			return true;
		}
    }
	else {
		return false;
    }
};



function getParentElementByAttributeValue(tagName,attrName,attrVal,matchExactValue){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    var hasElement=getElementByAttributeValue(tagName,attrName,attrVal,matchExactValue);
    if (hasElement) {
	// get the parent
	rscElt=rscElt.parentNode;
	return true;
    }
    else {
	return false;
    }
};

function getElementByAttributeNameValue(tagName,attrName,attrVal,valueAttributeValue,matchExactValue){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) return false;

    var elts=rscDocument.getElementsByTagName(tagName);
    rscElt=null;
    for(var i=0;i<elts.length;i++) {
        var tempElt=elts[i];
        var tempEltAttrVal=tempElt.getAttribute(attrName);
        if (tempEltAttrVal) {
          //alert('tempEltAttrVal:'+tempEltAttrVal);
          if (matchExactValue) {
              if (tempEltAttrVal==attrVal) {
			var valueAtt=tempElt.getAttribute("value");
          		if (valueAtt.indexOf(valueAttributeValue)!=-1) {
                 	 	//alert('found it - exact match');
 				addToLog('Found the elt, exact match');
			      rscElt=tempElt;
				return true;
			}
              }
          }
          else {
              if (tempEltAttrVal.indexOf(attrVal)!=-1) {
			var valueAtt=tempElt.getAttribute("value");
          		if (valueAtt && valueAtt.indexOf(valueAttributeValue)!=-1) {
	                  //alert('found it - contained: '+tempElt.innerHTML);
				addToLog('Found the elt, sub string');
	                  rscElt=tempElt;
				return true;
			}
              }
          }
        }
    }
    return false;
};

function switchDOM(iframeId) {
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	setDocument();
	addToLog();
	if (!rscDocument) {
		return false;
	}
	if (needToWaitForPopupIframe(iframeId, true)) {
		currentStep--;
		return false;
	}
	return true;


}

function switchBackDOM() {
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	addToLog();
	if(tempRSCDocument===null) {
		return false;
		//throw new Error("Error: expect to get tempRSCDocument, but it's NULL.");
	} else {
		rscDocument = tempRSCDocument;
		tempRSCDocument = null;
		iframeDocument = null;
		return true;
	}
}
function addSleep(value){
   isSleep=true;
   sleepTime=value;
   addToLog('Sleeping '+sleepTime+' ms ...');
   return true;
}

function ensureDOMisLoaded(){
   addToLog('isDOMLoaded: '+isDocumentLoaded);
   setSiblingsFlag(false); 
   if (isDocumentLoaded) {
      isCheckBoxMode=false;
      return true;
   }
   else {
      if (isCheckBoxMode) {
          if (hasCheckBoxStatusChanged ){
              return false;
          }
          else {
              addToLog('checkbox status did not change so no need to wait.');
              isCheckBoxMode=false;
              return true;
          }
      }   
      else {
          return false;
      }
   }
}

function ensureWindowOpened(){
	addToLog('isWindowOpened: '+isWindowOpened);

	if ( isFirefoxOrGreater(3.6) ) {
		addToLogNoCommand('DEBUG Firefox 3.6 or greater detected... checking for popup window.');
		setWindowsCount();
	}

	if (isWindowOpened) {
		isPopupWindow=true;

		rscSequenceBeforeWindowOpened=rscSequence;

	   	return true;
	}
	else return false;
}


function resetWindowOpened(){
   addToLog('Resetting the window opened status.');
   isPopupWindow=false;
   isWindowOpened=false;
   counterWindows=1;
   rscSequence=rscSequenceBeforeWindowOpened;
   if(iframeDocument !== null) {
	   rscDocument = iframeDocument;
   }
   return true;
}

function getDateString(){
   var d=new Date();
   var ds=d.getHours()+':'+d.getMinutes()+':'+d.getSeconds()+' '+d.getMilliseconds();
   return ds;
}

function addToLog(message){
	var buf = new StringBuffer();
	buf.append(': step: ');
	buf.append(currentStep);
	buf.append('; command: ');
	buf.append(steps[currentStep]);
	buf.append(' ; ');
	buf.append(message);
	
	addToDebugFile(buf);
}

function logMsg(message){
   addToDebugFile(message);
   return true;
}

function addToLogNoCommand(message){
   addToDebugFile(message);
}

function addToDebugFile(message){
   if (isDebug) {
	var dateString = getDateString();
	var buf = new StringBuffer();
	debuggingBufferCounter++;
	buf.append(dateString);
	buf.append(': ');
	buf.append(message);
	buf.append('\n');
	
   	debuggingArray.push(buf.toString());
	buf = null;
	
	if (debuggingBufferCounter==DEBUGGING_FLUSH_SIZE) {
		debuggingBufferCounter=0;
		writeLinesToFile(deguggingFileName,debuggingArray);
	}
   }
}

function addEndToDebugFile(){
   if (isDebug) {
		writeLinesToFile(deguggingFileName,debuggingArray);
   }
}

function setDocumentLoaded(e){
try{
 netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
 if (e.target.documentURI.indexOf("/OA_HTML/cabo/images/swan/t.htm") == -1 &&
      e.target.documentURI.indexOf("/OA_HTML/blank.html") == -1 ) {
   if (e.target=='[object HTMLDocument]' ) {
	isDocumentLoaded=true;
	var targ=e.target;
	addToLogNoCommand('## DOM has loaded. Event type: '+e.type);
	//addToLogNoCommand('DocumentURI:'+targ.documentURI);
   }
   else addToLogNoCommand('Event:'+e+' . Event target is instance of: '+e.target+' URI:'+e.target.documentURI);
 }
}
catch (e) {
	var errorMsg="setDocumentLoaded(). Error: "+ e;
	alert(errorMsg);
}
}

function setDocumentLoadedPopup(e){
try{
  if (!isPopupWindow) {
	addToLogNoCommand('## DOMCONTENTLOADED FOR POPUP RECEIVED BUT POPUP CLOSED SO DISCARDING EVENT.');
  } 
  else { 	 
   netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
   if (e.target.documentURI.indexOf("/OA_HTML/cabo/images/swan/t.htm") == -1 &&
       e.target.documentURI.indexOf("/OA_HTML/blank.html") == -1 ) {
     if (e.target=='[object HTMLDocument]' ) {
	isDocumentLoaded=true;
      var targ=e.target;
	addToLogNoCommand('##DOMCONTENTLOADED FOR POPUP RECEIVED. Event type: '+e.type);
	//addToLogNoCommand('DocumentURI:'+targ.documentURI);
     }
     else addToLogNoCommand('POPUP Event:'+e+' . Event target is instance of: '+e.target);
   }
  }
}
catch (e) {
	var errorMsg="setDocumentLoadedPopup(). Error: "+ e;
	alert(errorMsg);
}
}

function setDOMFrameContentLoaded(){
   addToLogNoCommand('DOMFrameContentLoaded has loaded.');
}

function setDOMWindowCloseEventReceived(){
   addToLogNoCommand('########## POPUP WINDOW HAS CLOSED ###########');
   isPopupWindow=false;
}

function setPopupWindowEventReceived(){
   addToLogNoCommand('PopupWindow Event was received.');
   isWindowOpened=true;
}


function resetDocumentLoaded(){
   addToLog('Resetting the document loaded status.');
   isDocumentLoaded=false;
   return true;
}

function resetSequence(){
   addToLog('Resetting the sequence number.');
   rscSequence='';
   return true;
}

function stopInjection(){
   isManualStop=true;
   var message="\n##############################################\n";
   message=message+"######### MANUAL STOP INJECTION ##############\n";
   message=message+"##############################################\n";
   var dateString = getDateString();
   var textArea=window.document.getElementById('blog');
   textArea.value = textArea.value+'\n'+dateString+': '+message;
   addEndToDebugFile();
}

function closeWindows(){
   netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
   var mainWindow = window.QueryInterface(Components.interfaces.nsIInterfaceRequestor)
                   .getInterface(Components.interfaces.nsIWebNavigation)
                   .QueryInterface(Components.interfaces.nsIDocShellTreeItem)
                   .rootTreeItem
                   .QueryInterface(Components.interfaces.nsIInterfaceRequestor)
                   .getInterface(Components.interfaces.nsIDOMWindow);
   mainWindow.getBrowser().removeCurrentTab();
   this.close();
   return true;
}

function executeCommandRealTime(){
     var inputElt=window.document.getElementById('inputCmd');
     addToLog('REALTIME CMD: '+inputElt.value);
     var res= executeCommand(inputElt.value);
     addToLog('REALTIME RESULT: '+res);
}

function setRepeat(isRepeatSet){
   addToLog('isRepeatSet: '+isRepeatSet);
   isRepeat=isRepeatSet;
   isRepeatInit=true;
   return true;
}

function StringBuffer() { 
   this.buffer = []; 
 } 

 StringBuffer.prototype.append = function append(string) { 
   this.buffer.push(string); 
   return this; 
 }; 

 StringBuffer.prototype.toString = function toString() { 
   return this.buffer.join(""); 
 }; 

  function getLookupSequenceManyColumns() {
	  netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	  var tagNames= new Array();
      var values= new Array();
      var targetAttributes= new Array();
	  for(var i = 0; i < arguments.length; i=i+3) {
        var tagName=arguments[i];
		tagNames.push(tagName);
		var value=arguments[i+1];
		values.push(value);
		var targetAttributeName=arguments[i+2];
		targetAttributes.push(targetAttributeName);
	  }
	  
	  rscSequence=get(0,tagNames,values,targetAttributes);
	  addToLog('#### rscSequence: '+rscSequence);

	  if (rscSequence==-1)
	  {
		return false;
	  }
	  return true;
 }

 function get(index,tagNames,values,targetAttributes) {
	  netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	  var res=-1;
	  var seqsCol=getLookupSequences(tagNames[index],values[index],targetAttributes[index]);
	  //addToLog('first index:'+index+' seqsCol.length: '+seqsCol.length);
	  for(var i = 0; i <seqsCol.length ; i++) {
		var seqCol=seqsCol[i];
		if ( (index+1)==tagNames.length)
		{
			//addToLog('first break seqCol:'+seqCol);
			return seqCol;
		}

		var seqsColNext=getLookupSequences(tagNames[index+1],values[index+1],targetAttributes[index+1]);
		//addToLog('second index:'+(index+1)+' seqsColNext.length: '+seqsColNext.length);
		for(var j = 0; j <seqsColNext.length ; j++) {
			var seqColNext=seqsColNext[j];
			if (seqCol==seqColNext)
			{
				//addToLog('index+2::'+(index+2)+' tagNames.length:'+tagNames.length);
				if ( (index+2)>=tagNames.length)
				{
					//addToLog('sec break seqCol:'+seqCol);
					return seqCol;
				}
				//addToLog('calling get() with index:'+(index+1));
				res=get(index+1,tagNames,values,targetAttributes);
				if (res==-1) {
					// other cols not matching, so trying next element	
				}
				else {
					//addToLog('after get res:'+res);
					return res;
				}
			}
		}
	  }
	  //addToLog('end res:'+res);
	  return res;
 }

function getLookupSequences(tagName,nodeValue,targetAttributeName) {
	  netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	  var elts= getElementsByTextNode(tagName,nodeValue);
	  //addToLog('getLookupSequences() tagName:'+tagName+' nodeValue:'+nodeValue+' elts.length: '+elts.length);
	  var res=new Array();
	  for(var i=0;i<elts.length;i++) {
			rscElt=elts[i];
			
			var indexOfColumnSeparator=targetAttributeName.indexOf(RSC_LOOKUP_SEPARATOR);
			if (indexOfColumnSeparator!=-1) {
				var isSuccess=getSequenceByRSCLookupSeparator(targetAttributeName);
				if (isSuccess) {
					res.push(rscSequence);
				}
			}
			else {
				var hasAtt=getAttributeValue(targetAttributeName);
				if (hasAtt) {
					getSequence();
					//addToLogNoCommand('pushing seq:'+rscSequence);
					res.push(rscSequence);
				}
			}
	  }
	  return res;
 }
 
function getSequenceByRSCLookupSeparator(targetAttributeName) {
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	var indexOfColumn=targetAttributeName.indexOf(RSC_LOOKUP_SEPARATOR);
	var prefix=getPrefix(targetAttributeName,indexOfColumn);
	var suffix=getSuffix(targetAttributeName,indexOfColumn);
	var isSuccess=getAttributeValue(targetAttributeName.substring(0,indexOfColumn));
	if (!isSuccess){
		return false;
	}
	return getSequenceByToken(prefix,suffix);
}
	
function getPrefix(targetAttributeName,indexOfColumn) {

	var temp=targetAttributeName.substring(indexOfColumn+RSC_LOOKUP_SEPARATOR.length);
	var indexOfColumnPrefix=temp.indexOf(RSC_LOOKUP_SEPARATOR);
	var prefix="";
	if (indexOfColumnPrefix!=-1) {
		prefix=temp.substring(0,indexOfColumnPrefix);
	}
	else {
		prefix=temp;
	}
	return prefix;
}
 
function getSuffix(targetAttributeName,indexOfColumn) {
	var temp=targetAttributeName.substring(indexOfColumn+RSC_LOOKUP_SEPARATOR.length);
	var indexOfColumnPrefix=temp.indexOf(RSC_LOOKUP_SEPARATOR);
	var suffix="";
	if (indexOfColumnPrefix!=-1) {
		suffix=temp.substring(indexOfColumnPrefix+RSC_LOOKUP_SEPARATOR.length);
	}
	return suffix;
}

 function getElementsByTextNode(tagName,text){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	var res=new Array();
    setDocument();
    if (!rscDocument) {
		return res;
	}
    var elts=rscDocument.getElementsByTagName(tagName);
	
    for(var i=0;i<elts.length;i++) {
		var tempEltRSC=elts[i];
		var value;
		
		if (tempEltRSC.tagName=='INPUT') {
			value=tempEltRSC.value;
		}
		else
		if (tempEltRSC.tagName=='SELECT') {
			value=tempEltRSC.options[tempEltRSC.selectedIndex].text;
		}		
		else {
			var fc=tempEltRSC.firstChild;
			//addToLogNoCommand('getElementsByTextNode, element: '+tempEltRSC+' eltid: '+tempEltRSC.getAttribute("id")+' ** child: '+fc);
			if (fc) {
				value=fc.nodeValue;
			}
			else {
				value="\"\"";
			}
		}
		
		if (value){
			//addToLogNoCommand('value: #'+value+'#');
			if (trim(text)==trim(value) ){
				//addToLogNoCommand('comparing: $'+text+'$ with $'+value+'$ SUCCESS pushing elt: value:'+value);
				res.push(tempEltRSC);
			}
		}
    }
    return res;
}

function setWindowsCount(){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
	var wmc = Components.classes['@mozilla.org/appshell/window-mediator;1'];
    var wm =wmc.getService(Components.interfaces.nsIWindowMediator);
    var browserEnumerator = wm.getEnumerator("navigator:browser");
	var tempCounter=0;	
    while (browserEnumerator.hasMoreElements()) {
         var win = browserEnumerator.getNext();
 	     tempCounter++;
	}
	if (tempCounter>counterWindows)
	{
		isWindowOpened=true;
		addToLog('################# NEW WINDOW, counter:'+tempCounter);
	}
	counterWindows=tempCounter;
}

/*********************************************
 *********************************************
 		ENGINE VARIABLES

 *********************************************
 ********************************************
 */

var rscAttributeValue=null;
var rscSequence=null;
var rscSequenceBeforeWindowOpened=null;
var rscElt=null;
var rscBrowser=null;
var rscDocument=null;
var tempRSCDocument=null;
var iframeDocument=null;
var isPopupWindow=false;
var isDocumentLoaded=false;
var isWindowOpened=false;
var isManualStop=false;
var isDebug=false;
var rscNewTab=null;
var forceCloseWindows=true;
var rscWindow=null;
var hasCheckBoxStatusChanged=false;
var isCheckBoxMode=false;

var steps = new Array();
var currentStep = 0;

var repeat_steps = new Array();
var repeat_steps_indexes = new Array();
var currentRepeatStepIndex=0;
var repeatStepIndexEnd=0;
var isRepeat=false;
var currentRepeatStep = 0;
var isRepeatExecuteNormalCommand=false;

//var maxTimeoutCounter=1*15*2; //30 secs
var maxTimeoutCounter=30*60*2; //30 mins
var logFileName;

var debuggingArray = new Array();
var debuggingBufferCounter=0;
var DEBUGGING_FLUSH_SIZE=20; // flush to disk every X lines
var deguggingFileName;

var currentTimeoutCounter=0;
var retryTime=1000; // in ms  IF RETRY TIME IS 200 MS THEN THE SETDOCUMENT WILL BLOCK ON WIN.CONTENT FOR POPUP???????
var isSleep=false;
var sleepTime=2000;
var isError=false;
var counterWindows=1;

var rscRootElt=null;
var siblingsFlag=false;
var RSC_LOOKUP_SEPARATOR="###";

try{
netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");

const windowMediatorIID = Components.interfaces.nsIWindowMediator;
addToLogNoCommand('DEBUG windowMediatorIID: '+windowMediatorIID);

const windowMediator = Components.classes["@mozilla.org/appshell/window-mediator;1"].getService(windowMediatorIID);
addToLogNoCommand('DEBUG windowMediator: '+windowMediator);

const topWin = windowMediator.getMostRecentWindow("navigator:browser");
addToLogNoCommand('DEBUG topWin: '+topWin);

var currentBrowser=topWin.getBrowser();
addToLogNoCommand('DEBUG currentBrowser: '+currentBrowser);

rscNewTab=currentBrowser.addTab("about:blank");
addToLogNoCommand('DEBUG rscNewTab: '+rscNewTab);

currentBrowser.selectedTab = rscNewTab;
//currentBrowser.mTabContainer.advanceSelectedTab(1, true);
rscBrowser = currentBrowser.getBrowserAtIndex(currentBrowser.mTabContainer.selectedIndex);
addToLogNoCommand('DEBUG rscBrowser: '+rscBrowser);

currentBrowser.addEventListener("DOMContentLoaded", setDocumentLoaded, false );
currentBrowser.addEventListener("PopupWindow", setPopupWindowEventReceived, false );

}
catch (e) {
	alert("INJECTION ERROR: An exception occurred in the file: 'rsc.js' . e:"+e+" Error name: " + e.name+ ". Error message: " + e.message);
}


function execute(){
   try{

	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");

	if ( currentStep ==  steps.length) {
		addEndToDebugFile();
		return;
	}
	if (isManualStop || isError) return;

	// write to log file only if starting with the section id comment:
	logSectionToFile(steps[currentStep]);
	var res=executeCommand(steps[currentStep]);

 	//alert('currentStep: '+currentStep+' / res:'+res);
	if (isRepeat ) {
		currentStep++;
		while ( isCommandToIgnore( steps[currentStep] ) ) {
			currentStep++;
		}
		isRepeatExecuteNormalCommand=true;
		executeRepeat();
	}
	else
	if (res) {

		//addToLog('increasing current step');
		currentStep++;
		if ( currentStep ==  steps.length) return;

		currentTimeoutCounter=0;
 		if (isSleep) {
			isSleep=false;
			window.setTimeout('execute()', sleepTime);
		}
		else {
			if (isDebug) addToLog(' executing no timeout...');
			execute();
		}
	}
	else {
		executeTimeOut('execute()');
	}
   }
   catch (e) {
	var errorMsg="INJECTION ERROR, execute(): An exception occurred in the script. Error: "+ e+"\n"+" Error name: " + e.name+ ". Error message: " + e.message;
	addToLog(errorMsg);
	addEndToDebugFile();
	alert(errorMsg);
   }
}

function executeCommand(commandToExecute){
	var res=window.eval(commandToExecute);
	return res;
}

function executeTimeOut(functionToExecute){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");

	currentTimeoutCounter++;
	if (currentTimeoutCounter==maxTimeoutCounter) throw "Timeout executing the command: ('"+
	currentStep+"') '"+steps[currentStep]+"'";

	if (isDebug) addToLog(' executing with timeout ('+currentTimeoutCounter+')...');
	window.setTimeout(functionToExecute,retryTime);
}

function isCommandToIgnore(command) {
	if (command.indexOf("logMsg(")!=-1)
	{
		return true;
	}
	return false;
}

function executeRepeat(){
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");

     	if (isManualStop) return;

	var res=false;
	if (isRepeatExecuteNormalCommand) {
		isRepeatExecuteNormalCommand=false;
		addToLog('REPEAT normal command: '+steps[currentStep]);
		res= executeCommand(steps[currentStep]);

		if (res) {
			// we found the element, so we return to normal steps list
			addToLog('REPEAT we found the element, so we return to normal steps list.');
			currentStep++;
			isRepeat=false;
			currentRepeatStepIndex++;
			execute();
		}
		else {
			// ready to execute the repeating commands from the first one.
			addToLog('REPEAT element not found, ready to execute the repeating commands from the first one.');
			currentRepeatStep=repeat_steps_indexes[currentRepeatStepIndex];
			if ( (currentRepeatStepIndex+1) ==  repeat_steps_indexes.length) {
				repeatStepIndexEnd=repeat_steps.length;
			}
			else {
				repeatStepIndexEnd=repeat_steps_indexes[currentRepeatStepIndex+1];
			}
			addToLog('REPEAT currentRepeatStep:'+currentRepeatStep+' repeatStepIndexEnd:'+
			repeatStepIndexEnd+' currentRepeatStepIndex:'+currentRepeatStepIndex);

			isRepeatExecuteNormalCommand=false;
			executeRepeat();
		}
      }
	else {
		addToLog('REPEAT alternate command: '+repeat_steps[currentRepeatStep]);
		res= executeCommand(repeat_steps[currentRepeatStep]);

		if (res) {
			// The alternate command succeeded, so we execute the next one.
			addToLog('REPEAT The alternate command succeeded, so we execute the next one.');

			currentRepeatStep++;
			if ( currentRepeatStep ==  repeatStepIndexEnd) {
				// no next alternate command, re-executing the normal command
				addToLog('REPEAT no next alternate command, re-executing the normal command');
				isRepeatExecuteNormalCommand=true;
			}
			executeRepeat();
		}
		else {
			// The alternate command did not succeed, so we re-execute it.
			addToLog('REPEAT The alternate command did not succeed, so we re-execute it.');
			executeTimeOut('executeRepeat()');
		}
      }
}