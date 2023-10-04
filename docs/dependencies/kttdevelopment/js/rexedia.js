const importBtn     = $("#import");
const importFile    = $("#import_file");
const exportBtn     = $("#export");

const coverRegex    = $("#cover-regex");
const coverFormat   = $("#cover-format");
const outputRegex   = $("#output-regex");
const outputFormat  = $("#output-format");
const addRowBtn     = $("#addRow");
const delRowBtn     = $("#deleteRow");

const fileName      = $("#file-name");
const result        = $("#result");

/* regex validation // */

const invalid = "is-invalid";
const valid   = "is-valid";

$(document).ready(function(){
    importBtn.click(function(){importFile.click();});
    importFile.change(importPreset);
    exportBtn.click(exportPreset);

    coverRegex.on('input', validateRegex);
    coverFormat.on('input', test);
    outputRegex.on('input', validateRegex);
    outputFormat.on('input', test);

    addRowBtn.click(addRow);
    delRowBtn.click(deleteRow);

    fileName.on('input', test);

    window.onbeforeunload = function(){
        if(yml)
            return true;
    };

    addRow();

    $(document).on('click', 'button', function(e) {
        e.preventDefault();
    });

    test();
});

function validateRegex(e) {
    const target = e.target ? e.target : $(e)[0];
    const regex = target.value;
    let className = target.className;

    if(!regex){
        className = className.replace(' ' + valid, "");
        className = className.replace(' ' + invalid, "");
    }else if(isValidRegex(regex)){
        className = className.replace(' ' + invalid, "");
        if(!className.includes(' ' + valid))
            className += ' ' + valid;
    }else{
        className = className.replace(' ' + valid, "");
        if(!className.includes(' ' + invalid))
            className += ' ' + invalid;
    }

    target.className = className;

    test();
}

function isValidRegex(string){
    try{
        regex = new RegExp(string);
        return true;
    }catch(e){
        return false;
    }
}

/* row handle // */

/* replace = '#' */
const row = `
<div id="#-meta" class="border-bottom mt-2">
    <input type="text" class="form-control form-control-sm mb-2" id="#-meta-name" placeholder="Name">
    <input type="text" class="form-control form-control-sm mb-2" id="#-meta-regex" placeholder="Regex">
    <input type="text" class="form-control form-control-sm mb-2" id="#-meta-format" placeholder="Replacement String">
</div>
`;

var rows = 0;

function addRow(){
    rows++;
    $("#rows").append(row.replaceAll('#', rows));
}

function deleteRow(){
    if(rows <= 1)
        return;
    $('#' + rows + "-meta")[0].remove();
    rows--;

    test();
}

/* tester // */

function test(){
    let fn = $(fileName)[0].value;
    let name = fn.includes(".")
            ? fn.substring(0, fn.lastIndexOf('.'))
            : fn;;
    let ext = fn.includes('.') ? fn.substring(fn.lastIndexOf('.') + 1) : "";

    let OUT =
    `<table class="table table-sm">
        <thead>
            <tr>
                <th scope="col">Tag</th>
                <th scope="col">Value</th>
            </tr>
        </thead>
        <tbody>`;

    /* cover */
    OUT += "<tr><th scope=\"row\">Cover</th>";
    try{
        OUT += "<td>" + name.replace(new RegExp(coverRegex[0].value), coverFormat[0].value) + "</td>";
    }catch(e){}
    OUT += "</tr>";

    /* rows */
    let size = rows + 1;
    for(let i = 1; i < size; i++)
        if($('#' + i + "-meta-name")[0].value || $('#' + i + "-meta-regex")[0].value || $('#' + i + "-meta-format")[0].value)
            OUT += "<tr><th scope=\"col\">" + $('#' + i + "-meta-name")[0].value + "</th><td>" + name.replace(new RegExp( $('#' + i + "-meta-regex")[0].value),  $('#' + i + "-meta-format")[0].value) + "</td></tr>";

    /* output */
    OUT += "<tr><th scope=\"row\">Output</th>";
    try{
        let output = name.replace(new RegExp(outputRegex[0].value), outputFormat[0].value);
        OUT += "<td>" + output + (ext && !output.includes('.') ? '.' + ext : "") + "</td>";
    }catch(e){}
    OUT += "</tr>";

    /* change */
    OUT +=
    `   </tbody>
    </table>`;

    result[0].innerHTML = OUT;
}

/* import */
var yml;

function importPreset(){
    const file = importFile[0].files[0];
    if(file){
        const IN = new FileReader();
        IN.readAsText(file, "UTF-8");
        IN.onload = function(e){
            yml = jsyaml.load(e.target.result);
            /* cover */
            if(yml.cover){
                coverRegex[0].value = yml.cover.regex ? yml.cover.regex : "";
                validateRegex('#' + coverRegex[0].id);
                coverFormat[0].value = yml.cover.format ? yml.cover.format : "";
            }
            /* output */
            if(yml.output){
                outputRegex[0].value = yml.output.regex ? yml.output.regex : "";
                validateRegex('#' + outputRegex[0].id);
                outputFormat[0].value = yml.output.format ? yml.output.format : "";
            }

            /* rows */
            let size = rows-1;
            for(var i = 0; i < size; i++)
                deleteRow();

            size = yml.metadata.length + 1;
            for(var i = 1; i < size; i++){
                if(i != 1)
                    addRow();
                var row = yml.metadata[i-1];
                $('#' + i + "-meta-name")[0].value   = row.meta ? row.meta : "";
                $('#' + i + "-meta-regex")[0].value  = row.regex ? row.regex : "";
                validateRegex('#' + i + "-meta-regex");
                $('#' + i + "-meta-format")[0].value = row.format ? row.format : "";
            }

            /* tester */
            test();
        }
    }
    importFile[0].value = "";
}

/* export // */

function exportPreset(){
    let OUT = "";
    /* cover */
    if(coverRegex[0].value || coverFormat[0].value)
        OUT += "cover:\n  regex: '" + coverRegex[0].value + "'\n  format: '" + coverFormat[0].value + "'\n";
    /* meta */
    let rowstr = "";
    let size = rows + 1;
    for(let i = 1; i < size; i++)
        if($('#' + i + "-meta-name")[0].value || $('#' + i + "-meta-regex")[0].value || $('#' + i + "-meta-format")[0].value)
            rowstr += "  - meta: '" + $('#' + i + "-meta-name")[0].value + "'\n    regex: '" + $('#' + i + "-meta-regex")[0].value + "'\n    format: '" + $('#' + i + "-meta-format")[0].value + "'\n";
    if(rowstr)
        OUT += "metadata:\n" + rowstr;

    /* output */
    if(outputRegex[0].value || outputFormat[0].value)
        OUT += "output:\n  regex: '" + outputRegex[0].value + "'\n  format: '" + outputFormat[0].value + "'\n";

    /* download */
    download(OUT, "preset.yml", "text/yaml");
}