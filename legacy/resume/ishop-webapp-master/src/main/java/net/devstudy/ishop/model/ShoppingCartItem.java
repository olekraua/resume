package net.devstudy.ishop.model;

import java.io.Serializable;

import net.devstudy.ishop.entity.Product;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class ShoppingCartItem implements Serializable {
	private static final long serialVersionUID = 6436798264138502851L;
	private Product product;
	private int count;
	public ShoppingCartItem() {
		super();
	}
	public ShoppingCartItem(Product product, int count) {
		super();
		this.product = product;
		this.count = count;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	@Override
	public String toString() {
		return String.format("ShoppingCartItem [product=%s, count=%s]", product, count);
	}
}
