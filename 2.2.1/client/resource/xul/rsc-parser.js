var outputKBFile="NOT SET";
var outputControlFile="NOT SET";

var KB_TYPE="KB";
var CONTROL_TYPE="CONTROL";

function writeKBRowHeader() {
	writeToFile(outputKBFile,getRowHeader()+"\n");
}

function writeControlRowHeader() {
	writeToFile(outputControlFile,getRowHeader()+"\n");
}

function getRowHeader() {
	var line="";
	line+="Column Name *";
	line+=",Column Type *";
	line+=",Max Length";
	line+=",HTML Template";
	line+=",HTML Tag Name";
	line+=",HTML ID Attribute";
	line+=",HTML Name Attribute";
	line+=",HTML Title Attribute";	
	line+=",HTML Text Node";
	line+=",Foreign Key";
	line+=",Table Name Map";
	line+=",Field Name Map";
	line+=",Substitution";
	line+=",Mandatory";
	return line;
}

function writeRow(columnName,columnType,maxLength,HTMLTemplate,HTMLTagName,HTMLIDAttribute,
	HTMLNameAttribute,HTMLTitleAttribute,HTMLTextNode,ForeignKey,TableNameMap,FieldNameMap,Substitution,Mandatory) {
	var line="";
	line+=columnName;
	line+=","+columnType;
	line+=","+maxLength;
	line+=","+HTMLTemplate;
	line+=","+HTMLTagName;
	line+=","+HTMLIDAttribute;
	line+=","+HTMLNameAttribute;
	line+=","+HTMLTitleAttribute;	
	line+=","+HTMLTextNode;
	line+=","+ForeignKey;
	line+=","+TableNameMap;
	line+=","+FieldNameMap;
	line+=","+Substitution;
	line+=","+Mandatory;

	if (columnType.toUpperCase()==KB_TYPE) {
		writeToFile(outputKBFile,line+"\n");
	}
	else {
		writeToFile(outputControlFile,line+"\n");
	}
}

function getAttributeValue(element,attributeName){
	var attributeValue=element.getAttribute(attributeName);
	if (attributeValue)	{
		return attributeValue;
	}
	else {
		return "";
	}
}

function parseElements(tagName,columnType) {
	netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();
    if (!rscDocument) {
		return false;
	}

	var elts=rscDocument.getElementsByTagName(tagName);
    for(var i=0;i<elts.length;i++) {
		var elt=elts[i];
		if (tagName.toLowerCase()=='input')
		{
			 parseInputElement(elt);
		}
		else 
		if (tagName.toLowerCase()=='a')
		{
			 parseAnchorElement(elt);
		}
		else 
		{
			 parseGenericElement(elt,columnType);
		}
    }
	return true;
}

function parseInputElement(elt) {
	var inputType=elt.getAttribute("type");
	if (inputType && inputType.toLowerCase()!="hidden") {
		var columnType=KB_TYPE;
		var HTMLTemplate=inputType.toUpperCase();

		if (inputType.toLowerCase()=="button" || inputType.toLowerCase()=="submit"){
			HTMLTemplate="BUTTON";
			columnType=CONTROL_TYPE;
		}
		else
		if (inputType.toLowerCase()=="text" || inputType.toLowerCase()=="password"){
			HTMLTemplate="INPUT";
		}
			
		var titleAttribute=getAttributeValue(elt,"title");
		var HTMLIDAttribute=getAttributeValue(elt,"id");
		var HTMLNameAttribute=getAttributeValue(elt,"Name");

		writeRow(
				titleAttribute,
				columnType,
				getAttributeValue(elt,"maxLength"),
				HTMLTemplate,
				"INPUT",
				HTMLIDAttribute,
				HTMLNameAttribute,
				titleAttribute,
				"","","","","","No");
    }
	return true;
}

function parseAnchorElement(elt) {
	var titleAttribute=getAttributeValue(elt,"title");
	var HTMLIDAttribute=getAttributeValue(elt,"id");
	var HTMLNameAttribute=getAttributeValue(elt,"Name");
	var HTMLTextNodeAttribute=getFirstTextNodeValue(elt);

	writeRow(
				titleAttribute,
				CONTROL_TYPE,
				"",
				"ANCHOR",
				"A",
				HTMLIDAttribute,
				HTMLNameAttribute,
				titleAttribute,
				HTMLTextNodeAttribute,"","","","","No");
   
	return true;
}

function parseGenericElement(elt,columnType) {
	var titleAttribute=getAttributeValue(elt,"title");
	var HTMLTemplate=elt.tagName.toUpperCase();
	var HTMLIDAttribute=getAttributeValue(elt,"id");
	var HTMLNameAttribute=getAttributeValue(elt,"Name");

	writeRow(
				titleAttribute,
				columnType,
				getAttributeValue(elt,"maxLength"),
				HTMLTemplate,
				HTMLTemplate,
				HTMLIDAttribute,
				HTMLNameAttribute,
				titleAttribute,
				"","","","","","No");

	return true;
}

function parseDocument() {
	writeKBRowHeader();
	writeControlRowHeader();
	parseElements("input",KB_TYPE);
	parseElements("select",KB_TYPE);
	parseElements("textarea",KB_TYPE);
	parseElements("a",CONTROL_TYPE);
	parseElements("button",CONTROL_TYPE);

	return true;
}
