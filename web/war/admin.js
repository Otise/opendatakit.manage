function updateAggregateFormSelect() {
  var aggregateFormSel = document.getElementById('aggregateFormSelect')
  var otherUrl = document.getElementById('otherUrl');
  if (aggregateFormSel && otherUrl && aggregateFormSel.value == 'other') {
    otherUrl.style.display = 'block';
  } else {
    otherUrl.style.display = 'none';
  }
}

function updateActionType() {
  var actionTypeSel = document.getElementById('actionTypeSel');
  var type = actionTypeSel.value;
  document.getElementById('actionSubmit').disabled = (type == '');
  document.getElementById('ADD_FORM_INPUTS').style.display = 'none';
  document.getElementById('INSTALL_PACKAGE_INPUTS').style.display = 'none';
  document.getElementById('NEW_TASKS_SMS_INPUTS').style.display = 'none';
  switch(type){
  case 'ADD_FORM':
    document.getElementById('ADD_FORM_INPUTS').style.display = 'block';
    break;
  case 'INSTALL_PACKAGE':
    document.getElementById('INSTALL_PACKAGE_INPUTS').style.display = 'block';
    break;
  case 'NEW_TASKS_SMS':
    document.getElementById('NEW_TASKS_SMS_INPUTS').style.display = 'block';
    break;
  }
}

function updateSelectAllDevices(selectAll) {
  var checked = selectAll.checked;
  var checkboxes = document.getElementsByName('imei');
  for (i = 0; i < checkboxes.length; i++) {
    checkboxes[i].checked = checked;
  }
}

function updateSelectedDevice() {
  var selectAll = document.getElementById('selectAllCheckbox');
  selectAll.checked = false;
}

function updateSelectAllTasks(selectAll, status) {
  var checked = selectAll.checked;
  var checkboxes = document.getElementsByName('taskId');
  for (i = 0; i < checkboxes.length; i++) {
	var cb_attr = checkboxes[i].attributes;
	var cb_status = attributes.getNamedItem('status');
	if (cb_status == status) {
	  checkboxes[i].checked = checked;
	}
  }
}

function updateSelectedTask(status) {
  var selectAll = document.getElementById('selectAllCheckbox-' + status);
  selectAll.checked = false;
}

function onSubmitAction() {
  var deviceChecks = document.getElementsByName('deviceCheck');
  for(i=0; i<deviceChecks.length; i++) {
    var deviceCheck = deviceChecks[i];
    if (deviceCheck.checked){
      var attributes = deviceCheck.attributes;
      var imei = attributes.getNamedItem("imei");
      alert(imei.value);
    }
  }
  return true;
}