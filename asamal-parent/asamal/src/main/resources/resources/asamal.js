function doAjaxPost(url, elementIds, reRenderList, fromController, fromView) {

    input = "asamalFromController="+fromController+"&asamalFromView="+fromView;

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

    // finally make the post
    $.post(url, input,
        function(page){
            $.each(page, function(id, html){
                $("#"+id).html(html);
            });
        }, "json");

    return false;
}