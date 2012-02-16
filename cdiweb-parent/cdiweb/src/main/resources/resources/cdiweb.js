function doAjaxPost(url, elementIds, reRenderList) {

    input = "";

    // elementIds an be either array or a sinle element
    if (elementIds instanceof Array) {
        $.each(elementIds, function() {
            if (input.length > 0) {
                input += "&"
            }
            input += $("#"+this).serialize()
        })
    }
    else {
        input = $("#"+elementIds).serialize()
    }

    // reRenderList an be either array or a sinle element
    if (reRenderList instanceof Array) {
        $.each(reRenderList, function() {
            if (input.length > 0) {
                input += "&"
            }
            input += "reRenderList="+this
        })
    }
    else {
        if (input.length == 0) {
            input += "&"
        }

        input += "reRenderList="+reRenderList
    }

    // finally make the post
    $.post(url, input,
        function(page){
            $.each(page, function(id, html){
                $("#"+id).html(html);
            });
        }, "json");

    return false;
}