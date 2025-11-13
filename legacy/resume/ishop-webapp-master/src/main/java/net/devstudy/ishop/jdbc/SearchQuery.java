package net.devstudy.ishop.jdbc;

import java.util.List;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class SearchQuery {
	private StringBuilder sql;
	private List<Object> params;

	public SearchQuery() {
		super();
	}

	public SearchQuery(StringBuilder sql, List<Object> params) {
		super();
		this.sql = sql;
		this.params = params;
	}

	public StringBuilder getSql() {
		return sql;
	}

	public void setSql(StringBuilder sql) {
		this.sql = sql;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}
}
