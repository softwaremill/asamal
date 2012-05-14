
function ajaxPost(controller, view, elementIds, reRenderList) {
    return doAjaxPost(asamalContextPath+"/rerender/"+controller+"/"+view,
                elementIds, reRenderList, asamalViewHash);
}

function doAjaxPost(url, elementIds, reRenderList, viewHash) {

    input = "";

    // elementIds an be either array or a sinle element
    if (elementIds instanceof Array) {
        $.each(elementIds, function() {
            input += "&" + $("#"+this).serialize()
        })
    }
    else {
        input += "&" + $("#"+elementIds).serialize()
    }

    // reRenderList an be either array or a sinle element
    if (reRenderList instanceof Array) {
        $.each(reRenderList, function() {
            input += "&reRenderList="+this
        })
    }
    else {
        input += "&reRenderList="+reRenderList
    }

    if (input.search("&asamalViewHash=") < 0) {
        input += "&asamalViewHash=" + viewHash
    }

    // finally make the post (substring input, to get rid of leading &)
    $.post(url, input.substring(1),
        function(page){
            $.each(page, function(id, html){
                $("#"+id).empty().html(html);
            });
        })
        .error(function(xhr, status, error) {
                try {
                    console.log("An AJAX error occured: " + status + "\nError: " + error);
                } catch (e) {
                    // console log not available, ignore
                }
            });

    return false;
}