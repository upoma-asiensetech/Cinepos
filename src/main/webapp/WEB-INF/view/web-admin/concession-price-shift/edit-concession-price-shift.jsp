<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://java.sun.com/jsp/jstl/core"  %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <jsp:directive.include file="../layouts/header.jsp" />
</head>
<body>

<div id="wrapper">

    <!-- Navigation -->
    <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
        <jsp:directive.include file="../layouts/navbar-top.jsp" />
    </nav>

    <!-- Page Content -->
    <div id="page-wrapper">
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-12">
                    <h1 class="page-header">Edit Concession Price Shift</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <div class="row">
                <div class="col-lg-6">
                    <form id="editConcessionPriceShiftForm">
                        <input type="hidden" id="concessionProductPriceShiftId" value="${concessionPriceShift.id}">
                        <div class="form-group">
                            <label>Product</label>
                            <select class="form-control" id="productId" >
                                <d:forEach var="product" items="${concessionProducts}" >
                                    <option
                                            <d:if test="${product.id == concessionPriceShift.concessionProduct.id}" >
                                                selected
                                            </d:if>
                                            value="${product.id}">${product.name}</option>
                                </d:forEach>
                            </select>
                            <p class="help-block error" id="errorMsg_productId"></p>
                        </div>

                        <div class="form-group">
                            <label>Price</label>
                            <input id="price" type="number" min="0" class="form-control" value="${concessionPriceShift.price}">
                            <p class="help-block error" id="errorMsg_price"></p>
                        </div>

                        <div class="form-group">
                            <label>Start Date</label>
                            <div class='input-group date'>
                                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span>
                                    </span>
                                <input id="startDate" type='text'
                                       value="<fmt:formatDate  value="${concessionPriceShift.startDate}" pattern="MM/dd/yyy" />"
                                       class="form-control" name="startDate" placeholder="MM/DD/YYY" />

                            </div>
                            <p class="help-block error" id="errorMsg_startDate"></p>
                        </div>
                        <div class="form-group">
                            <label>End Date</label>
                            <div class='input-group date'>
                                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span>
                                    </span>
                                <input type='text' class="form-control" id="endDate"
                                       value="<fmt:formatDate  value="${concessionPriceShift.endDate}" pattern="MM/dd/yyy" />"
                                       name="endDate" placeholder="MM/DD/YYY" />

                            </div>
                            <p class="help-block error" id="errorMsg_endDate"></p>
                        </div>

                        <br>
                        <p class="help-block" id="statusMsg"></p>
                        <p class="help-block error" id="errorMsg_id"></p>
                        <button class="btn btn-primary" onclick="return submitPriceShiftData()">Update Product Price Shift</button>
                    </form>
                </div>
            </div>
        </div>
        <!-- /.container-fluid -->
    </div>
    <!-- /#page-wrapper -->

</div>

<jsp:directive.include file="../layouts/footer.jsp" />

<script>

    $(document).ready(function(){
        var date_input=$('#startDate,#endDate'); //our date input has the name "date"
        var container=$('.bootstrap-iso form').length>0 ? $('.bootstrap-iso form').parent() : "body";
        date_input.datepicker({
            format: 'mm/dd/yyyy',
            container: container,
            todayHighlight: true,
            autoclose: true,
            startDate: new Date()
        });
    });

    function submitPriceShiftData(){

        $("#statusMsg").html("").hide();


        var concessionProductPriceShiftId =$("#concessionProductPriceShiftId").val();
        var productId =$("#productId").val();
        var startDate =$("#startDate").val();
        var endDate =$("#endDate").val();
        var price =$("#price").val();
        enableDisableFormElement("editConcessionPriceShiftForm",["input","button","select"],false);
        var postData={
            id:concessionProductPriceShiftId,
            concessionProductId:productId,
//            concessionProductId:1,
            price:price
        };

        if(startDate)
            postData['startDate'] = startDate;
        if(endDate)
            postData['endDate'] = endDate;

        $.ajax({
            url: BASEURL+'api/admin/concession-price-shift/update',
            type: 'POST',
            data: postData ,
            statusCode: {
                401: function (response) {
                    console.log("unauthorized");
                    console.log(response);
                    showLoginModal();
                    enableDisableFormElement("editConcessionPriceShiftForm",["input","button","select"],true);
                },
                422: function (response) {
                    console.log(response);
                    $("#statusMsg").html("Error found").show();
                    BindErrorsWithHtml("errorMsg_",response.responseJSON);
                    enableDisableFormElement("editConcessionPriceShiftForm",["input","button","select"],true);
                }
            },success: function(data){
                $("#statusMsg").html("Price Shift created successfully").show();
                setTimeout(function(){
                    window.location = BASEURL+"admin/concession-price-shift/all";
                },2000);


            }
        });
        return false;
    }
</script>
</body>

</html>


