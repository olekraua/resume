package net.devstudy.ishop.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.devstudy.ishop.entity.Account;
import net.devstudy.ishop.entity.Category;
import net.devstudy.ishop.entity.Order;
import net.devstudy.ishop.entity.OrderItem;
import net.devstudy.ishop.entity.Producer;
import net.devstudy.ishop.entity.Product;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public final class ResultSetHandlerFactory {

	public static ResultSetHandler<Product> PRODUCT_RESULT_SET_HANDLER = new ResultSetHandler<Product>() {
		@Override
		public Product handle(ResultSet rs) throws SQLException {
			Product p = new Product();
			p.setCategory(rs.getString("category"));
			p.setDescription(rs.getString("description"));
			p.setId(rs.getInt("id"));
			p.setImageLink(rs.getString("image_link"));
			p.setName(rs.getString("name"));
			p.setPrice(rs.getBigDecimal("price"));
			p.setProducer(rs.getString("producer"));
			return p;
		}
	};
	
	public static ResultSetHandler<Category> CATEGORY_RESULT_SET_HANDLER = new ResultSetHandler<Category>() {
		@Override
		public Category handle(ResultSet rs) throws SQLException {
			Category c = new Category();
			c.setId(rs.getInt("id"));
			c.setName(rs.getString("name"));
			c.setUrl(rs.getString("url"));
			c.setProductCount(rs.getInt("product_count"));
			return c;
		}
	};

	public static ResultSetHandler<Producer> PRODUCER_RESULT_SET_HANDLER = new ResultSetHandler<Producer>() {
		@Override
		public Producer handle(ResultSet rs) throws SQLException {
			Producer p = new Producer();
			p.setId(rs.getInt("id"));
			p.setName(rs.getString("name"));
			p.setProductCount(rs.getInt("product_count"));
			return p;
		}
	};
	
	public static ResultSetHandler<Account> ACCOUNT_RESULT_SET_HANDLER = new ResultSetHandler<Account>() {
		@Override
		public Account handle(ResultSet rs) throws SQLException {
			Account a = new Account();
			a.setId(rs.getInt("id"));
			a.setEmail(rs.getString("email"));
			a.setName(rs.getString("name"));
			return a;
		}
	};
	
	public static ResultSetHandler<OrderItem> ORDER_ITEM_RESULT_SET_HANDLER = new ResultSetHandler<OrderItem>() {
		@Override
		public OrderItem handle(ResultSet rs) throws SQLException {
			OrderItem orderItem = new OrderItem();
			orderItem.setId(rs.getLong("oid"));
			orderItem.setCount(rs.getInt("count"));
			orderItem.setIdOrder(rs.getLong("id_order"));
			Product p = PRODUCT_RESULT_SET_HANDLER.handle(rs);
			orderItem.setProduct(p);
			return orderItem;
		}
	};

	public static ResultSetHandler<Order> ORDER_RESULT_SET_HANDLER = new ResultSetHandler<Order>() {
		@Override
		public Order handle(ResultSet rs) throws SQLException {
			Order o = new Order();
			o.setId(rs.getLong("id"));
			o.setCreated(rs.getTimestamp("created"));
			o.setIdAccount(rs.getInt("id_account"));
			return o;
		}
	};
	
	public static ResultSetHandler<Integer> getCountResultSetHandler() {
		return new ResultSetHandler<Integer>() {
			@Override
			public Integer handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return rs.getInt(1);
				} else {
					return 0;
				}
			}
		};
	}

	public static <T> ResultSetHandler<T> getSingleResultSetHandler(final ResultSetHandler<T> oneRowResultSetHandler) {
		return new ResultSetHandler<T>() {
			@Override
			public T handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return oneRowResultSetHandler.handle(rs);
				} else {
					return null;
				}
			}
		};
	}

	public static <T> ResultSetHandler<List<T>> getListResultSetHandler(final ResultSetHandler<T> oneRowResultSetHandler) {
		return new ResultSetHandler<List<T>>() {
			@Override
			public List<T> handle(ResultSet rs) throws SQLException {
				List<T> list = new ArrayList<>();
				while (rs.next()) {
					list.add(oneRowResultSetHandler.handle(rs));
				}
				return list;
			}
		};
	}

	private ResultSetHandlerFactory() {
	}
}
