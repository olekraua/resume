<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="ishop" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div id="shoppingCart">
	<c:if test="${CURRENT_ACCOUNT == null }">
		<div class="alert alert-warning hidden-print" role="alert">To make order, please sign in</div>
	</c:if>
	<ishop:product-table items="${CURRENT_SHOPPING_CART.items }" totalCost="${CURRENT_SHOPPING_CART.totalCost }" showActionColumn="true" />
	<div class="row hidden-print">
		<div class="col-md-4 col-md-offset-4 col-lg-2 col-lg-offset-5">
			<c:choose>
				<c:when test="${CURRENT_ACCOUNT != null }">
					<a href="javascript:void(0);" class="post-request btn btn-primary btn-block" data-url="/order">Make order</a>
				</c:when>
				<c:otherwise>
					<ishop:sign-in classes="btn-block" />
				</c:otherwise>
			</c:choose>
		</div>
	</div>
</div>