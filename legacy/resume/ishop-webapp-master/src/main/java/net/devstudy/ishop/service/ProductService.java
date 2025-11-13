package net.devstudy.ishop.service;

import java.util.List;

import net.devstudy.ishop.entity.Category;
import net.devstudy.ishop.entity.Producer;
import net.devstudy.ishop.entity.Product;
import net.devstudy.ishop.form.SearchForm;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public interface ProductService {

	List<Product> listAllProducts(int page, int limit);
	
	int countAllProducts();
	
	List<Product> listProductsByCategory(String categoryUrl, int page, int limit);
	
	int countProductsByCategory(String categoryUrl);
	
	List<Category> listAllCategories();
	
	List<Producer> listAllProducers();
	
	List<Product> listProductsBySearchForm(SearchForm searchForm, int page, int limit);
	
	int countProductsBySearchForm(SearchForm searchForm);
}
