function doPost(url, formId, reRenderList) {

    input = $("#"+formId).serialize()

    $.each(reRenderList, function() {
        input += "&reRenderList="+this
    })

    $.post(url, input,
        function(page){
            $.each(page, function(id, html){
                $("#"+id).html(html);
            });
        }, "json");

    return false;
}