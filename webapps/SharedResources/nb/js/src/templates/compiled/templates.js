this["nb"] = this["nb"] || {};
this["nb"]["templates"] = this["nb"]["templates"] || {};
this["nb"]["templates"]["attachments"] = Handlebars.template({"1":function(container,depth0,helpers,partials,data,blockParams) {
    var alias1=container.lambda, alias2=container.escapeExpression;

  return "    <div class=\"attachments-file uploading\">\n        <span class=\"file-name\" data-file=\""
    + alias2(alias1(blockParams[0][0], depth0))
    + "\">\n            "
    + alias2(alias1(blockParams[0][0], depth0))
    + "\n            <div class=\"upload-progress\"></div>\n        </span>\n        <span class=\"btn btn-sm btn-link btn-remove-file\">\n            <i class=\"fa fa-times\"></i>\n        </span>\n        <input type=\"hidden\" name=\"fileid\" value=\""
    + alias2(alias1(blockParams[0][0], depth0))
    + "\" disabled/>\n    </div>\n";
},"3":function(container,depth0,helpers,partials,data) {
    return "    <div class=\"blink-anim\">files empty</div>\n";
},"compiler":[7,">= 4.0.0"],"main":function(container,depth0,helpers,partials,data,blockParams) {
    var stack1;

  return ((stack1 = helpers.each.call(depth0 != null ? depth0 : {},(depth0 != null ? depth0.files : depth0),{"name":"each","hash":{},"fn":container.program(1, data, 1, blockParams),"inverse":container.program(3, data, 0, blockParams),"data":data,"blockParams":blockParams})) != null ? stack1 : "");
},"useData":true,"useBlockParams":true});
this["nb"]["templates"]["dialog-list"] = Handlebars.template({"1":function(container,depth0,helpers,partials,data,blockParams,depths) {
    var stack1, alias1=depth0 != null ? depth0 : {};

  return "    <li class=nb-dialog-list-it>\n        <label data-role=\"dialog-list-item\">\n"
    + ((stack1 = helpers["if"].call(alias1,(depths[1] != null ? depths[1].isMulti : depths[1]),{"name":"if","hash":{},"fn":container.program(2, data, 0, blockParams, depths),"inverse":container.program(4, data, 0, blockParams, depths),"data":data,"blockParams":blockParams})) != null ? stack1 : "")
    + "            <span>"
    + container.escapeExpression(container.lambda(((stack1 = blockParams[0][0]) != null ? stack1.name : stack1), depth0))
    + "</span>\n"
    + ((stack1 = helpers.each.call(alias1,(depths[1] != null ? depths[1].fields : depths[1]),{"name":"each","hash":{},"fn":container.program(6, data, 1, blockParams, depths),"inverse":container.noop,"data":data,"blockParams":blockParams})) != null ? stack1 : "")
    + "        </label>\n    </li>\n";
},"2":function(container,depth0,helpers,partials,data,blockParams,depths) {
    var stack1, alias1=container.lambda, alias2=container.escapeExpression;

  return "                <input data-type=\"select\" type=\"checkbox\" name=\"select_"
    + alias2(alias1((depths[1] != null ? depths[1].dialogId : depths[1]), depth0))
    + "\" value=\""
    + alias2(alias1(((stack1 = blockParams[1][0]) != null ? stack1.id : stack1), depth0))
    + "\"/>\n";
},"4":function(container,depth0,helpers,partials,data,blockParams,depths) {
    var stack1, alias1=container.lambda, alias2=container.escapeExpression;

  return "                <input data-type=\"select\" type=\"radio\" name=\"select_"
    + alias2(alias1((depths[1] != null ? depths[1].dialogId : depths[1]), depth0))
    + "\" value=\""
    + alias2(alias1(((stack1 = blockParams[1][0]) != null ? stack1.id : stack1), depth0))
    + "\"/>\n";
},"6":function(container,depth0,helpers,partials,data,blockParams) {
    var stack1;

  return ((stack1 = helpers["if"].call(depth0 != null ? depth0 : {},((stack1 = blockParams[0][0]) != null ? stack1["1"] : stack1),{"name":"if","hash":{},"fn":container.program(7, data, 0, blockParams),"inverse":container.program(9, data, 0, blockParams),"data":data,"blockParams":blockParams})) != null ? stack1 : "");
},"7":function(container,depth0,helpers,partials,data,blockParams) {
    var stack1, alias1=container.lambda, alias2=container.escapeExpression, alias3=depth0 != null ? depth0 : {}, alias4=helpers.helperMissing;

  return "                    <input data-id=\""
    + alias2(alias1(((stack1 = blockParams[2][0]) != null ? stack1.id : stack1), depth0))
    + "\" name=\""
    + alias2(alias1(((stack1 = blockParams[1][0]) != null ? stack1["0"] : stack1), depth0))
    + "\" value=\""
    + alias2((helpers.mapValue || (depth0 && depth0.mapValue) || alias4).call(alias3,blockParams[2][0],((stack1 = blockParams[1][0]) != null ? stack1["0"] : stack1),{"name":"mapValue","hash":{},"data":data,"blockParams":blockParams}))
    + "\" data-text=\""
    + alias2((helpers.mapValue || (depth0 && depth0.mapValue) || alias4).call(alias3,blockParams[2][0],((stack1 = blockParams[1][0]) != null ? stack1["1"] : stack1),{"name":"mapValue","hash":{},"data":data,"blockParams":blockParams}))
    + "\" type=\"hidden\"/>\n";
},"9":function(container,depth0,helpers,partials,data,blockParams) {
    var stack1, alias1=container.lambda, alias2=container.escapeExpression;

  return "                    <input data-id=\""
    + alias2(alias1(((stack1 = blockParams[2][0]) != null ? stack1.id : stack1), depth0))
    + "\" name=\""
    + alias2(alias1(((stack1 = blockParams[1][0]) != null ? stack1["0"] : stack1), depth0))
    + "\" value=\""
    + alias2((helpers.mapValue || (depth0 && depth0.mapValue) || helpers.helperMissing).call(depth0 != null ? depth0 : {},blockParams[2][0],((stack1 = blockParams[1][0]) != null ? stack1["0"] : stack1),{"name":"mapValue","hash":{},"data":data,"blockParams":blockParams}))
    + "\" type=\"hidden\"/>\n";
},"compiler":[7,">= 4.0.0"],"main":function(container,depth0,helpers,partials,data,blockParams,depths) {
    var stack1;

  return ((stack1 = container.invokePartial(partials.pagination,depth0,{"name":"pagination","data":data,"blockParams":blockParams,"helpers":helpers,"partials":partials,"decorators":container.decorators})) != null ? stack1 : "")
    + "<ul class=nb-dialog-list>\n"
    + ((stack1 = helpers.each.call(depth0 != null ? depth0 : {},(depth0 != null ? depth0.models : depth0),{"name":"each","hash":{},"fn":container.program(1, data, 1, blockParams, depths),"inverse":container.noop,"data":data,"blockParams":blockParams})) != null ? stack1 : "")
    + "</ul>\n";
},"usePartial":true,"useData":true,"useDepths":true,"useBlockParams":true});