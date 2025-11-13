package net.devstudy.ishop.entity;
/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class Producer extends AbstractEntity<Integer> {
	private static final long serialVersionUID = -4967160259057526492L;
	private String name;
	private int productCount;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getProductCount() {
		return productCount;
	}
	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}
}
