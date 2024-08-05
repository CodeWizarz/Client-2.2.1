function clickGoToTask()    {
	var elts = document.getElementsByTagName("a");
	for (var i = 0; i < elts.length; i++)       {
		if (elts[i].getAttribute("title") == "Go to Task") { 
			elts[i].click();
		}
	}
}

function process() {
	var  taskNameInput= document.getElementById("pt1:USma:0:MAnt1:0:AP2:r3:0:qryId1:value10::content");
	taskNameInput.value = "TASK-NAME-PLACEHOLDER";
	var  taskNameSearchButton= document.getElementById("pt1:USma:0:MAnt1:0:AP2:r3:0:qryId1::search");
	taskNameSearchButton.click();
	window.setTimeout(function() {
		clickGoToTask();
	}, 1000);
}

return process();