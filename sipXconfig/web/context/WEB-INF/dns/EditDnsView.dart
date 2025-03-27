import 'dart:html';
import 'dart:convert';
import './packages/sipxconfig/sipxconfig.dart';

var api = new Api(test : false);

main() {
  new DnsViewEditor();
}

class DnsViewEditor {
  var msg = new UserMessage(querySelector("#message")!);
  late DataLoader loader;
  int? dnsViewId;
  
  DnsViewEditor() {
    querySelector("#ok")!.onClick.listen(ok);
    querySelector("#apply")!.onClick.listen(apply);
    querySelector("#cancel")!.onClick.listen(cancel);
    
    for (var elemId in ["#regionId", "#planId", "#customRecordsIdsRow", "input[name=preview-show]", 
          "input[name=excluded]"]) {
      querySelectorAll(elemId)!.onClick.listen(loadPreview);      
    }
    Location l = document.window!.location! as Location;
    var params = Uri.parse(l.href).queryParameters;
    if (params['dnsViewId'] != null) {
      dnsViewId = int.parse(params['dnsViewId']!);
    }
    loader = new DataLoader(this.msg, loadForm);    
    load();
  }
      
  ok(e) {
    save(close);
  }

  apply(e) {
    save();
  }
  
  cancel(e) {
    close();
  }
  
  loadPreview([e]) {
    var elem = querySelector("#preview")!;
    if (api.test) {
      elem.text = 'preview in test mode';
      return;
    }
    var meta = getFormData();
    String show = 'ALL';
    for (InputElement e in querySelectorAll("input[name=preview-show]")!) {
      if (e.checked!) {
        show = e.value!;
        break;
      }
    }
    HttpRequest req = new HttpRequest();
    req.open('POST', api.url("rest/dnsPreview/${show}"));
    req.setRequestHeader("Content-Type", "application/json"); 
    req.send(jsonEncode(meta));
    req.onLoad.listen((e) {
      if (DataLoader.checkResponse(msg, req)) {
        elem.text = req.responseText;
      }
    });      
  }
  
  close() {
    window.location.href = 'EditDns.html';      
  }

  save([onOk]) {
    var meta = getFormData();
    HttpRequest req = new HttpRequest();
    var method;
    var id = '';        
    if (dnsViewId == null) {
      method = 'POST';            
    } else {
      id = "${dnsViewId}/";
      meta['id'] = dnsViewId!;
      method = 'PUT';            
    }
    req.open(method, api.url("rest/dnsView/${id}"));
    req.setRequestHeader("Content-Type", "application/json"); 
    req.send(jsonEncode(meta));
    req.onLoad.listen((e) {
      if (DataLoader.checkResponse(msg, req)) {
        if (dnsViewId == null && req.responseText != null) {
          dnsViewId = int.parse(req.responseText!);
        }
        if (onOk != null) {
          onOk();
        }
        msg.success(getString("msg.actionSuccess"));
      }
    });      
  }  
  
  Map<String, Object> getFormData() {
    var meta = new Map<String,Object>();
    meta['planId'] = safeInt((querySelector("#planId")! as SelectElement).value!);
    meta['name'] = (querySelector("#name")! as InputElement).value!;
    meta['regionId'] = safeInt((querySelector("#regionId")! as SelectElement).value!);
    List<int> customRecordsIds = [];
    meta['customRecordsIds'] = customRecordsIds;
    SelectElement customs = querySelector("#customRecordsIds")! as SelectElement;
    for (var option in customs.selectedOptions) {
      customRecordsIds.add(int.parse(option.value!));
    }
    List<String> excludes = [];
    for (InputElement e in querySelectorAll("input[name=excluded]")!) {      
      if (e.checked!) {
        excludes.add(e.value!);
      }
    }
    meta['excluded'] = excludes;
    
    return meta;
  }
  
  int safeInt(String value) {
    return value == null || value == "" ? 0 : int.parse(value);
  }
  
  load() {
    var id = (dnsViewId != null ? '${dnsViewId}' : 'blank');
    var url = api.url("rest/dnsView/${id}", "edit-view-test.json");
    loader.load(url);
  }
  
  loadRegionCandidates(Map<String, String> regionOptions, int regionId) {
    SelectElement select = querySelector("#regionId")! as SelectElement;
    regionOptions.forEach((id, value) {
      bool selected = (int.parse(id) == regionId);
      select.append(new OptionElement(data: value, value: id, selected : selected));        
    });
  }
  
  loadPlanOptions(Map<String, String> planOptions, int planId) {
    SelectElement select = querySelector("#planId")! as SelectElement;
    planOptions.forEach((id, value) {
      bool selected = (int.parse(id) == planId);
      select.append(new OptionElement(data: value, value: id, selected : selected));
    });
  }
  
  loadCustomRecords(Map<String, String> customRecordsOptions, List<int> customRecordsIds) {
    if (customRecordsOptions.length == 0) {
      // If there are no custom record sets defined, don't bother showing list. It's disconcerting
      // to show select box where there's nothing to select
      querySelector("#customRecordsIdsRow")!.style.display = "none";
      return;
    }
    SelectElement select = querySelector("#customRecordsIds")! as SelectElement;
    customRecordsOptions.forEach((id, value) {
      bool selected = (customRecordsIds != null && customRecordsIds.contains(int.parse(id)));
      select.append(new OptionElement(data: value, value: id, selected : selected));
    });    
  }
  
  loadExcluded(List<String> excluded) {
    if (excluded == null || excluded.length == 0) {
      return;
    }
    
    for (InputElement e in querySelectorAll("input[name=excluded]")!) {
      e.checked = excluded.contains(e.value); 
    }    
  }
  
  loadForm(json) {
    var data = jsonDecode(json);
    Map<String, Object> view = data['view'];
    if (view != null) {
      (querySelector("#name")! as InputElement).value = view!['name'] as String;
      int regionId = view['regionId']! as int;
      int planId = view['planId']! as int;
      List<int> customRecordsIds = view['customRecordsIds']! as List<int>;
      List<String> excluded = view['excluded']! as List<String>;

      loadRegionCandidates(data['regionCandidates'], regionId);
      loadPlanOptions(data['planCandidates'], planId);
      loadCustomRecords(data['customRecordsCandidates'], customRecordsIds);
      loadExcluded(excluded);
      loadPreview();
    }
  }  
}
