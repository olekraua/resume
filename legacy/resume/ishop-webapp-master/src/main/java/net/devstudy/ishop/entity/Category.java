package net.devstudy.ishop.entity;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class Category extends AbstractEntity<Integer> {
	private static final long serialVersionUID = 5709707193151272208L;
	private String name;
	private String url;
	private Integer productCount;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getProductCount() {
		return productCount;
	}

	public void setProductCount(Integer productCount) {
		this.productCount = productCount;
	}

	@Override
	public String toString() {
		return String.format("Category [id=%s, name=%s, url=%s, productCount=%s]", getId(), name, url, productCount);
	}
}
