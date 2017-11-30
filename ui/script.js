$(document).ready(function(){
    $('#submete').click(function(){

        
        
        $.ajax({
            url: "http://localhost:8071/ws?wsdl/print",
            data: "", //ur data to be sent to server
            contentType: "application/json; charset=utf-8", 
            type: "GET",
            success: function (data) {
               alert(data);
            },
            error: function (x, y, z) {
               alert(x.responseText +"  " +x.status);
            }
        });
    });
    
});