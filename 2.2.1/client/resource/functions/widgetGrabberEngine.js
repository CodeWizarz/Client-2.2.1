var prevBackground;
var prevElement;
var htmlcode;
var wx;
var wy;
var ww;
var wh;

var clientX=0;

document.addEventListener('mouseup', function(e) {
	processEvent(e);
});

function processEvent(e) {
	//alert('new mouse up event e.clientX:'+e.clientX+' clientX:'+window.clientX);
	if (window.clientX==e.clientX) {
		//alert('duplicate mouse up event');
		return;
	}
	window.clientX=e.clientX;
	selectText();
	//alert("text: "+text);
	e.stopPropagation();
}

function selectText() {
    if (window.getSelection) {
		var selObj = window.getSelection();
		var range  = selObj.getRangeAt(0);
		if (range==''){
		   htmlcode='';
		   return;
		}
		var rootNode=range.commonAncestorContainer;
		//alert('selObj:'+selObj+' range:'+range+' rootNode:'+rootNode);
		
		if (prevElement){
		   prevElement.style.background = prevBackground;
		}
		
		if (rootNode.style) {
			prevBackground = rootNode.style.background;
			rootNode.style.background = '#94E2AB';	
			htmlcode = rootNode.innerHTML;
			var el = rootNode.getBoundingClientRect();
			wx = el.left  ;//target.offsetLeft;
			wy = el.top + window.pageYOffset;//target.offsetTop;
			ww = rootNode.offsetWidth;
			wh = rootNode.offsetHeight;
			//alert('selectText, htmlcode: #'+htmlcode+'#');
			prevElement = rootNode; 
		}
    }
	else {
		htmlcode='';
	}
}

function rightclickNoSelection(e) {
    var x = event.clientX, y = event.clientY;
    var elementMouseIsOver = document.elementFromPoint(x, y);
	// this is IE specific "outerHTML"
	htmlcode = elementMouseIsOver.parentElement.outerHTML ;
	//alert('rightclickNoSelection, htmlcode: '+htmlcode);
}

document.addEventListener('contextmenu', function(ev) {
    ev.preventDefault();
	//alert('htmlcode: #'+htmlcode+'#');
	if (htmlcode=='') {
		rightclickNoSelection(ev);
	}
	javaCallback("GRAB", htmlcode, wx, wy, ww, wh);
}, false);

var elemDiv = document.createElement('div');
elemDiv.innerHTML = "GRAB MODE ENABLED";
elemDiv.style.cssText = 'cursor: pointer;color: white;z-index: 999999;display: block; position: absolute; left: 0px; top: 0px; border: solid black 2px; padding: 5px; background-color: #94E2AB; text-align: justify; font-size: 16px; width: 240px;';
document.body.appendChild(elemDiv);