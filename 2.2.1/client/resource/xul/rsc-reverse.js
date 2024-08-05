/**************************************************
 * $Revision: 8857 $:
 * $Author: john.snell $:
 * $Date: 2009-10-27 11:05:50 +0700 (Tue, 27 Oct 2009) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/resource/xul/rsc-reverse.js $:
 * $Id: rsc-reverse.js 8857 2009-10-27 04:05:50Z john.snell $:
 *
 * Functions and data members used to support Reverse functionality in SwiftBuild.
 *
*/

var GLOBAL_primaryKeys = new Array();
var GLOBAL_dataFilenameMatrix = new Array();


function getSpanTextByID(id)
{
    getElementById(id);
    if ( null == rscElt || typeof(rscElt) == 'undefined' )
    {
        rscElt = null;
    }
    else
    {
        rscElt = getTextNodeValue(rscElt);
    }
    return true;
}


function getInputFieldTextByID(id)
{
    getElementById(id);
    if ( null == rscElt || typeof(rscElt) == 'undefined' )
    {
        rscElt = null;
    }
    else
    {
        rscElt = rscElt.value;
    }
    return true;
}

function getCheckBoxValueByID(id)
{
    getElementById(id);

    if ( null == rscElt || typeof(rscElt) == 'undefined' )
    {
        rscElt = null;
    }
    else
    {
        rscElt = rscElt.checked;
    }
    return true;
}

function getDropDownSelectedValueByID(id)
{
    getElementById(id);
    return getDropDownSelectedValueFromCurrentElement();
}


function getDropDownSelectedValueFromCurrentElement()
{
    if ( null == rscElt || typeof(rscElt) == 'undefined' )
    {
        rscElt = null;
    }
    else
    {
        var text = null;
        for ( var i = 0; i < rscElt.childNodes.length; i++ )
        {
            if ( rscElt.childNodes[i].selected )
            {
                //always get the text, and not the 'value=' that would be submitted in the form.
                // values are often random/unique numbers used as IDs, but not reusable across sessions.
                text = getTextNodeValue(rscElt.childNodes[i]);

                break;
            }
        }
        rscElt = text;
    }

    return true;
}






/*
Example:
<a class="x41" href="#" onclick="_navBarSubmit('DefaultFormName', 'goto','PmtMthdSpecifyTable',1,'21IF23B98w', '4RAVWSSpM','PmtMthdSpecifyTable');return false">Next 4</a>

Given this, extract the <a> element by the embedded name 'PmtMthdSpecifyTable'.
*/
function getNextXLinkByOnclickID(embeddedOnclickID)
{
    return getElementByAttributeFragmentAndTextFragment('a', 'onclick', embeddedOnclickID, "Next");
}
function getNextXDropDownSelectedByOnchangeID(embeddedOnchangeID)
{
    getElementByAttributeValue('select', 'onchange', embeddedOnchangeID, false);
    return getDropDownSelectedValueFromCurrentElement();
}




function getElementByAttributeFragmentAndTextFragment(tagName,attrName,attrVal,text)
{
    var elts=rscDocument.getElementsByTagName(tagName);
    rscElt=null;
    var elts_length = elts.length;
    for ( var i=0;i<elts_length;i++ )
    {
        var tempElt=elts[i];
        var tempEltAttrVal=tempElt.getAttribute(attrName);
        if ( tempEltAttrVal )
        {
            if ( tempEltAttrVal.indexOf(attrVal)!=-1 &&
                 getTextNodeValue(tempElt).indexOf(text) != -1 )
            {
                rscElt=tempElt;
                return true;
            }
        }
    }
    return false;
}

var GLOBAL_iterationStateMatrix = new Array();


function list_of_links(id)
{
    netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();

    //confirm our location in the result pagination sequence
    var currentActualStartIndex = 1;
    var currentDesiredStartIndex = GLOBAL_iterationStateMatrix[id]['paginationSize'] * GLOBAL_iterationStateMatrix[id]['desiredNextLinkClickDepth'] + 1;
    getNextXDropDownSelectedByOnchangeID(GLOBAL_iterationStateMatrix[id]['selectNextOnchangeFunction']);
    if ( null != rscElt )
    {
        var indexOfHyphen = rscElt.toString().indexOf("-");
        currentActualStartIndex =  parseInt(rscElt.toString().substring(0, indexOfHyphen));
    }

    //click us out to the correct page NEXT/NEXT/NEXT page depth
    if ( GLOBAL_iterationStateMatrix[id]['numberOfResultsRetrieved'] >= GLOBAL_iterationStateMatrix[id]['maximumResultsToRetrieve'] )
    {
        //retrieve no more results.  Terminate.
        GLOBAL_isAsynchronousExecutionFinished = true;
    }
    else if ( currentActualStartIndex < currentDesiredStartIndex )
    {
        getNextXLinkByOnclickID(GLOBAL_iterationStateMatrix[id]['nextOnclickFunction']);
        if ( null != rscElt )
        {
            generateClickEventOnCurrentElement();
            setTimeoutWrapper(curry(list_of_links, id));
        }
        else
        {
            //--Nothing more to iterate on this page, no next links--
            //Instead of forcing the user to re-initialize the state when reusing this ID
            //(as with a nested list-of-links, a sublist of a primary list), we will instead
            //clear out the state here:
            GLOBAL_iterationStateMatrix[id]['actualNextLinkClickDepth'] = 0;
            GLOBAL_iterationStateMatrix[id]['counter'] = 0;
            GLOBAL_iterationStateMatrix[id]['desiredNextLinkClickDepth'] = 0;

            //redirect based on the presence or non-presence of a nextFunction
            if ( GLOBAL_iterationStateMatrix[id]['nextFunctionName'] != null &&
                 typeof(GLOBAL_iterationStateMatrix[id]['nextFunctionName']) != 'undefined' )
            {
                setTimeoutWrapper(GLOBAL_iterationStateMatrix[id]['nextFunctionName']);
            }
            else
            {
                GLOBAL_isAsynchronousExecutionFinished = true;
            }
        }
    }
    else
    {
        //Originally, we tested to see if the currently indexed list entry was null - if null, then
        //the list must be finished, right?  Except when Oracle mixes list entry types, such as with
        //Legal Entity Administator : Jurisdictions, where they list both Jurisdictions, and occasionally,
        //Tax Regimes.  Unfortunately, Tax Regimes have a completely different page - AND a different link
        //id.  So we iterate through the entire pagination size looking for usable entries.
        rscElt = null;
        while ( GLOBAL_iterationStateMatrix[id]['counter'] < GLOBAL_iterationStateMatrix[id]['paginationSize'] )
        {
            var linkName = GLOBAL_iterationStateMatrix[id]['prefix'] + GLOBAL_iterationStateMatrix[id]['counter'];
            getElementById(linkName);
            GLOBAL_iterationStateMatrix[id]['counter']++;
            if ( null != rscElt )
            {
                break;
            }
        }
        if ( null != rscElt )
        {
            resetDocumentLoaded();
            generateClickAnchorOnCurrentElement();
            GLOBAL_iterationStateMatrix[id]['numberOfResultsRetrieved']++;
            setTimeoutWrapper(GLOBAL_iterationStateMatrix[id]['postClickFunction']);
        }
        else
        {
            //increment the desired depth and "recurse" to the code that clicks next for us
            GLOBAL_iterationStateMatrix[id]['counter'] = 0;
            GLOBAL_iterationStateMatrix[id]['desiredNextLinkClickDepth']++;

            setTimeoutWrapper(curry(list_of_links, id));
        }
    }

    return true;
}


function openRowInDataXMLOutputFile(fileID)
{
    isHeaderRow = 0 == GLOBAL_dataRow_fileIDTo_rowCounterMap[fileID];
    GLOBAL_dataRowBuffer_fileIDTo_columnHeaderToValueMap[fileID] = new Array();
    toWrite = "<r>\n";
    if ( isHeaderRow )
    {
        toWrite = "<h>\n";
    }
    writeToDataFile(fileID, toWrite);

    return true;
}
function closeRowInDataXMLOutputFile(fileID)
{
    isHeaderRow = 0 == GLOBAL_dataRow_fileIDTo_rowCounterMap[fileID];
    GLOBAL_dataRow_fileIDTo_rowCounterMap[fileID] = GLOBAL_dataRow_fileIDTo_rowCounterMap[fileID] + 1;
    flushAndCloseCurrentDataRow(fileID);
    toWrite = "</r>\n";
    if ( isHeaderRow )
    {
        toWrite = "</h>\n";
    }
    writeToDataFile(fileID, toWrite);
    return true;
}

function writeCellToDataXMLOutputFile(fileID, cellData)
{
    if ( null == cellData || typeof(cellData) == 'undefined' || '' == cellData )
    {
        writeToDataFile(fileID, "<c/>\n");
    }
    else
    {
        writeToDataFile(fileID, "<c>" + cellData + "</c>\n");
    }

    return true;
}


//TODO:  Fix scoping issue.  Data added to this array from execute() and data added from
//other methods exist in different scopes; if the data is buffered, this causes the data
//to be written out in the wrong order.  Setting buffering to 0 forces no buffering,
//but decreases disk performance (not clear if I should care about that)
var GLOBAL_MAX_LENGTH_OF_DATA_DATA_BUFFER_BEFORE_FLUSHING = 0;
var GLOBAL_DataDataBuffer_Table = new Array();


var GLOBAL_dataRowBuffer_fileIDTo_columnHeaderListMap = new Array();
var GLOBAL_dataRowBuffer_fileIDTo_columnHeaderToValueMap = new Array();
var GLOBAL_dataRow_fileIDTo_rowCounterMap = new Array();

function storeColumnValue(fileID, rscColumnName, value)
{
    GLOBAL_dataRowBuffer_fileIDTo_columnHeaderToValueMap[fileID][rscColumnName] = value;
    return true;
}



function writeColumnHeaders(fileID)
{
    var orderedListOfColumnHeaders = GLOBAL_dataRowBuffer_fileIDTo_columnHeaderListMap[fileID];
    for ( i = 0; i < orderedListOfColumnHeaders.length; i++ )
    {
        var rscColumnName = orderedListOfColumnHeaders[i];
        storeColumnValue(fileID, rscColumnName, rscColumnName);
    }
    return true;
}


/**
 * Writes out all the columns to the data file, including
 * those for which no data has been retrieved (set as blank)
 *
 * @param fileID
 */
function flushAndCloseCurrentDataRow(fileID)
{
    var orderedListOfColumnHeaders = GLOBAL_dataRowBuffer_fileIDTo_columnHeaderListMap[fileID];
    for ( i = 0; i < orderedListOfColumnHeaders.length; i++ )
    {
        var rscColumnName = orderedListOfColumnHeaders[i];
        var value = GLOBAL_dataRowBuffer_fileIDTo_columnHeaderToValueMap[fileID][rscColumnName];
        writeCellToDataXMLOutputFile(fileID, value);
    }
    return true;
}


function writeToDataFile(fileID, text)
{
    writeToDataFileForceFlush(fileID, text, false);
}

function writeToDataFileForceFlush(fileID, text, forceFlushToDisk)
{
    var fileName = GLOBAL_dataFilenameMatrix[fileID];
    if ( GLOBAL_DataDataBuffer_Table[fileName] == null )
    {
        GLOBAL_DataDataBuffer_Table[fileName] = "";
    }
    GLOBAL_DataDataBuffer_Table[fileName] = GLOBAL_DataDataBuffer_Table[fileName] + text;
    if ( forceFlushToDisk ||
         GLOBAL_DataDataBuffer_Table[fileName].length > GLOBAL_MAX_LENGTH_OF_DATA_DATA_BUFFER_BEFORE_FLUSHING )
    {
        flushTextToDataFile(fileName, GLOBAL_DataDataBuffer_Table[fileName]);
        GLOBAL_DataDataBuffer_Table[fileName] = "";
    }
}


function flushTextToDataFile(fileName, text)
{
    try
    {
        netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
        var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
        file.initWithPath(fileName);
        if ( file.exists() == false )
        {
            file.create( Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 420 );
        }
        var outputFileStream = Components.classes["@mozilla.org/network/file-output-stream;1"].createInstance( Components.interfaces.nsIFileOutputStream );
        outputFileStream.init( file, 0x04 | 0x08 | 0x10, 420, 0 );

        var converterFileStream = Components.classes['@mozilla.org/intl/converter-output-stream;1'].createInstance(Components.interfaces.nsIConverterOutputStream);
        converterFileStream.init(outputFileStream, 'UTF-8', text.length, Components.interfaces.nsIConverterInputStream.DEFAULT_REPLACEMENT_CHARACTER);

        converterFileStream.writeString(text);
        converterFileStream.flush();

        converterFileStream.close();
        outputFileStream.close();
    }
    catch (e)
    {
        var errorMsg="cannot write to Data file: " + fileName + " :: Error: "+ e;
        myAlert(errorMsg);
    }

    return true;
}



function writeHeaderToDataXMLOutputFile(fileID, tableName)
{
    var headerXML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>\n" +
    "<data name=\"" + tableName + "\" xmlns=\"http://data0000.configurator.erapidsuite.com\">\n"
    "<m/>\n";

    writeToDataFile(fileID, headerXML);

    GLOBAL_dataRow_fileIDTo_rowCounterMap[fileID] = 0;

    return true;
}

function writeFooterToDataXMLOutputFile(fileID)
{
    var footerXML =
    "</data>\n";

    writeToDataFileForceFlush(fileID, footerXML, true);
    return true;
}


var GLOBAL_isAsynchronousExecutionFinished = false;

function setUntilGlobalFinishedFlagSet()
{
    GLOBAL_isAsynchronousExecutionFinished = true;
    return true;
}

function waitUntilGlobalFinishedFlagSet()
{
    //addToLog("GLOBAL_isAsynchronousExecutionFinished = " + GLOBAL_isAsynchronousExecutionFinished);
    if ( GLOBAL_isAsynchronousExecutionFinished )
    {
        GLOBAL_isAsynchronousExecutionFinished = false;
        return true;
    }
    else
    {
        return false;
    }
}


var GLOBAL_waitFunction_delayFactorMS = 200;

function setTimeoutWrapper(functionPointer)
{
    var f = curry(waitForDOMLoadEvent, functionPointer);
    setTimeout(f, GLOBAL_waitFunction_delayFactorMS);
}

function waitForDOMLoadEvent(functionPointer)
{
    if ( !ensureDOMisLoaded() )
    {
        //addToLog("waitForDOMLoadEvent: waiting GLOBAL_waitFunction_delayFactorMS = " + GLOBAL_waitFunction_delayFactorMS);
        setTimeout(curry(waitForDOMLoadEvent, functionPointer), GLOBAL_waitFunction_delayFactorMS);
    }
    else
    {
        //setTimeout(functionPointer, 1500);
        //myAlert('dom is loaded, about to call functionPointer = ' + functionPointer);
        functionPointer();
    }
}


function alphabetic_iterator()
{
    netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
    setDocument();

    if ( GLOBAL_al_currentRangeIndex >= GLOBAL_al_letterRanges_current_offset.length )
    {
        //then the list of configured character ranges has been exceeded, and we're finished
        GLOBAL_isAsynchronousExecutionFinished = true;
        return true;
    }

    if (  GLOBAL_al_letterRanges_current_offset[GLOBAL_al_currentRangeIndex] >=
          GLOBAL_al_letterRanges_length[GLOBAL_al_currentRangeIndex] )
    {
        //then the current character range has been exceeded - move to the next range.
        GLOBAL_al_currentRangeIndex = GLOBAL_al_currentRangeIndex + 1;
        return alphabetic_iterator();
    }

    //current range has not been exceeded - search on current character, then increment
    getElementById(GLOBAL_al_inputField);

    //currentChar = start char (decimal value) + counter to increment to the current char, converted back to character data
    var currentChar_decimal = GLOBAL_al_letterRanges_start[GLOBAL_al_currentRangeIndex] +
                              GLOBAL_al_letterRanges_current_offset[GLOBAL_al_currentRangeIndex];
    var currentChar = String.fromCharCode(currentChar_decimal);
    setInputValue(currentChar + '%');

    //increment for the next round
    GLOBAL_al_letterRanges_current_offset[GLOBAL_al_currentRangeIndex] = GLOBAL_al_letterRanges_current_offset[GLOBAL_al_currentRangeIndex] + 1;


    //TODO:  not have this be hard coded to a Go button.
    getButtonElementByTextNode('button','Go');
    resetDocumentLoaded();
    generateClickEventOnCurrentElement();
    setTimeoutWrapper(curry(GLOBAL_al_nextFunction, GLOBAL_al_nextFunctionArgument));

    return true;
}

