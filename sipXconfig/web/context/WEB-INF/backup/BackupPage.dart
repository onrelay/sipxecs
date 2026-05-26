import 'dart:html';
import 'dart:convert'; 
import 'dart:async';
import 'package:intl/intl.dart';
import './packages/sipxconfig/sipxconfig.dart';

var api = new Api(test : true);
bool isInProgress = false;

main() {
  var backup = new BackupPage();
  var tabs = new BackupTabs(querySelector("#leftNavAbsolute")!, ["local", "ftp"], backup.showContent);  
  tabs.setPersistentStateId("backup");
  var persistedTabId = window.sessionStorage["backup"];
  backup.showContent(tabs, persistedTabId == null ? "local" : persistedTabId);
}

class BackupTabs extends Tabs {
  BackupTabs(Element root, Iterable<String> ids, tabChangeListener) : super(root, ids, tabChangeListener);
  changeTab(String selectedId) {
    if (!isInProgress) {
      super.changeTab(selectedId);
    }
  }
}

class BackupPage {
  var msg = new UserMessage(querySelector("#message")!);
  late DataLoader loader;
  String type = 'local';
  late SettingEditor dbSettings;
  late SettingEditor generalSettings;
  late SettingEditor ftpSettings;
  var timeOfDayFormat = DateFormat("jm");
  late Timer refresh;

  BackupPage() {
    querySelector("#backup-now")!.onClick.listen(backupNow);
    querySelector("#apply")!.onClick.listen(apply);
    loader = new DataLoader(this.msg, loadForm);
    dbSettings = new SettingEditor(querySelector("#db-settings")! as TableSectionElement);
    generalSettings = new SettingEditor(querySelector("#general-settings")! as TableSectionElement);
    ftpSettings = new SettingEditor(querySelector("#ftp-settings")! as TableSectionElement);
    inProgress(false);
    refresh = new Timer.periodic(new Duration(seconds: 30), (e) {
      if (isInProgress) {                                                                            
        load();
      }
    });
  }
  
  load() {
    inProgress(true);
    var url = api.url("rest/backup/${type}", "backup-test.json");
    UListElement listElem = querySelector("#backups")! as UListElement;
    listElem.children.clear();
    loader.load(url);
  }
  
  loadForm(json) {
    var data = jsonDecode(json);
    Map<String, dynamic> generalBackupSettings = getSetting(data['settings']! as Map<String, dynamic>, "general");
    generalSettings.render(generalBackupSettings);
    Map<String, dynamic> backupSettings = getSetting(data['dbSettings']! as Map<String, dynamic>, "db");
    dbSettings.render(backupSettings);
    Map<String, dynamic> ftpSettings = getSetting(data['settings']! as Map<String, dynamic>, "ftp");
    this.ftpSettings.render(ftpSettings);
    List<dynamic> archiveIds = [];
    Map<String, dynamic> plan = data['backup']! as Map<String, dynamic>;
    if (plan != null) {    
      SelectElement limit = querySelector("#backup-limit")! as SelectElement;
      int count = plan['limitedCount'] as int;
      if (count == null) {
        limit.value = null;
      } else {
        limit.value = count.toString();
      }
            
      int dow = 0;
      bool enabled = false;
      String timeOfDay = "";
      archiveIds = plan['definitionIds']! as List<dynamic>;
      // backend uses array even though we only support a single schedule ATM
      List<dynamic> schedules = plan['schedules']! as List<dynamic>;
      if (schedules != null && schedules.length > 0) {
        dow = schedules[0]['scheduledDay']['dayOfWeek'];
        enabled = schedules[0]['enabled'];
        
        Map<String, dynamic> t = schedules[0]['timeOfDay'] as Map<String, dynamic>;
        if (t != null) {
          var dt = new DateTime(0, 1, 1, t['hrs'] as int, t['min'] as int);
          timeOfDay = timeOfDayFormat.format(dt);
        }
      }
      (querySelector("#dailyScheduledTime")! as InputElement).value = timeOfDay;
      (querySelector("#dailyScheduledDay")! as SelectElement).selectedIndex = dow;
      (querySelector("#dailyScheduleEnabled")! as InputElement).checked = enabled;
    }

    UListElement archives = querySelector("#archives")! as UListElement;
    archives.children.clear();
    Map<String, dynamic> defs = data['definitions'] as Map<String, dynamic>;
    if (defs != null) {
      var i=0;
      defs.forEach((defId, label) {
        i++;
        var checked = archiveIds.contains(defId) ? "checked" : "";
        archives.appendHtml('''
<li>
  <input id="backupFiles${i}" type="checkbox" name="definitionIds" value="${defId}" ${checked}/>
  ${label}
</li>
''');              
      });
    }  
    
    Map<String, dynamic> backups = data['backups']! as Map<String, dynamic>;
    UListElement listElem = querySelector("#backups")! as UListElement;
    listElem.children.clear();
    if (backups != null) {
      var backupIdFmt = DateFormat("yyyy-MM-dd-HH-mm");
      var tstampFmt = DateFormat.yMd().add_Hm();
      backups.forEach((backupId, backup) {
        var backupFiles = backup as List<dynamic>;
        var tstamp = backupIdFmt.parse(fixDateDartBug(backupId));
        var tstampStr = tstampFmt.format(tstamp);
        var html = "<li>${tstampStr}<br/>";
        for (String f in backupFiles) {
          var decode = f.split('|');
          html += '''
<a href="${decode[1]}">${decode[0]}</a>
''';
        }
        listElem.appendHtml(html + "</li>");
      });
    }
    else {
      listElem.appendHtml("<li>No backup files in backup folder</li>");
    }

    inProgress(false);
  }
  
  // Dart's DateFormat cannot seem to handle date strings that do not have a delimiter
  // between date numbers.  e.g.  01/01 is ok but 0101 is not. So here we inject delims
  // as workaround
  String fixDateDartBug(String s) {
    return "${s.substring(0, 4)}-${s.substring(4, 6)}-${s.substring(6, 8)}-${s.substring(8, 10)}-${s.substring(10, 12)}";    
  }
  
  inProgress(bool inProg) {
    isInProgress = inProg;
    querySelector('#inprogress')!.hidden = ! isInProgress;
    for (var e in querySelectorAll('.action')!) {
      if (e is ButtonElement) {   
        (e as ButtonElement).disabled = isInProgress;
      }
    }
    if( isInProgress ) {
      UListElement listElem = querySelector("#backups")! as UListElement;
      listElem.children.clear();
    }
  }

  Map<String, dynamic> getSetting(Map<String, dynamic> settings, String path) {
    var selected = settings;
    for (var segment in path.split("/")) {
      Map<String, dynamic> value = selected['value'] as Map<String, dynamic>;
      selected = value[segment] as Map<String, dynamic>;  
    }
    return selected;    
  }
  
  parseForm() {
    var form = new Map<String, dynamic>();
    form['limitedCount'] = int.parse((querySelector("#backup-limit")! as SelectElement)!.value!);
    List<String> definitionIds = [];
    for (CheckboxInputElement c in querySelectorAll("input[name=definitionIds]")!) {
      if (c!.checked!) {
        definitionIds.add(c.value!);
      }
    }
    form['definitionIds'] = definitionIds;
    var timeStr = (querySelector("#dailyScheduledTime")! as InputElement).value!;
    DateTime timeOfDay = timeOfDayFormat.parse(timeStr);
    int dayOfWeek = int.parse((querySelector("#dailyScheduledDay")! as SelectElement).value!);
    var enabled = (querySelector("#dailyScheduleEnabled")! as CheckboxInputElement).checked;
    form['schedules'] = [{
      "timeOfDay" : {
          "hrs" : timeOfDay.hour,
          "min" : timeOfDay.minute 
      },
      "scheduledDay" : {
        "dayOfWeek" : dayOfWeek
      },
      "enabled" : enabled
    }];

    return form;
  }
  
  backupNow(e) {
    postOrPut('POST', 'message.backupCompleted');
  }
  
  apply(e) {
    postOrPut('PUT', 'msg.actionSuccess');
  }
  
  postOrPut(String method, String successMessage) {
    inProgress(true);
    msg.success("");
    msg.error("");
    var meta = new Map<String, dynamic>();
    meta['dbSettings'] = dbSettings.parseForm();
    meta['generalSettings'] = generalSettings.parseForm();
    meta['ftpSettings'] = ftpSettings.parseForm();
    meta['backup'] = parseForm();    
    HttpRequest req = new HttpRequest();
    req.open(method, api.url("rest/backup/${type}"));
    req.setRequestHeader("Content-Type", "application/json"); 
    req.send(jsonEncode(meta));
    req.onLoad.listen((e) {
      if( req.status! >= 200 && req.status! <= 299) {
        msg.success(getString(successMessage));
      } else if(req.status! == 408) {
        msg.success(getString('message.backupTimeout'));
        inProgress(false);
      } else {
        msg.error(getString('message.backupError'));
        inProgress(false);
      }
      load();
    });      
  }
  
  showContent(Tabs tabs, String selectedId) {
    // custom tab listener because we can to show portions of a single div
    // for both types of backups
    msg.success("");
    msg.error("");
    type = selectedId;    
    var backupElem = querySelector("#backup")!;
    var display = (selectedId == 'local' || selectedId == 'ftp' ? '' : 'none');
    backupElem.style.display = display;
    tabs.showTabContent(selectedId);    
    load();
  }
}