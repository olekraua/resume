package net.devstudy.ishop.servlet.ajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import net.devstudy.ishop.form.ProductForm;
import net.devstudy.ishop.model.ShoppingCart;
import net.devstudy.ishop.servlet.AbstractController;
import net.devstudy.ishop.util.RoutingUtils;
import net.devstudy.ishop.util.SessionUtils;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public abstract class AbstractProductController extends AbstractController {
	private static final long serialVersionUID = 5096979151346608146L;

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ProductForm form = createProductForm(req);
		ShoppingCart shoppingCart = getCurrentShoppingCart(req);
		processProductForm(form, shoppingCart, req, resp);
		if(!SessionUtils.isCurrentShoppingCartCreated(req)) {
			SessionUtils.setCurrentShoppingCart(req, shoppingCart);
		}
		sendResponse(shoppingCart, req, resp);
	}
	
	private ShoppingCart getCurrentShoppingCart(HttpServletRequest req) {
		ShoppingCart shoppingCart = SessionUtils.getCurrentShoppingCart(req);
		if(shoppingCart == null) {
			shoppingCart = new ShoppingCart();
		}
		return shoppingCart;
	}

	protected abstract void processProductForm(ProductForm form, ShoppingCart shoppingCart, HttpServletRequest req, HttpServletResponse resp) 
				throws ServletException, IOException;

	protected void sendResponse(ShoppingCart shoppingCart, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		JSONObject cardStatistics = new JSONObject();
		cardStatistics.put("totalCount", shoppingCart.getTotalCount());
		cardStatistics.put("totalCost", shoppingCart.getTotalCost());
		RoutingUtils.sendJSON(cardStatistics, req, resp);
	}
}
