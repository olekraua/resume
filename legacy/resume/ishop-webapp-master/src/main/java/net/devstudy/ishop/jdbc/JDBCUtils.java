package net.devstudy.ishop.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public final class JDBCUtils {

	public static <T> T select(Connection c, String sql, ResultSetHandler<T> resultSetHandler, Object... parameters) throws SQLException {
		try (PreparedStatement ps = c.prepareStatement(sql)) {
			populatePreparedStatement(ps, parameters);
			ResultSet rs = ps.executeQuery();
			return resultSetHandler.handle(rs);
		}
	}
	
	public static <T> T insert(Connection c, String sql, ResultSetHandler<T> resultSetHandler, Object... parameters) throws SQLException {
		try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			populatePreparedStatement(ps, parameters);
			int result = ps.executeUpdate();
			if (result != 1) {
				throw new SQLException("Can't insert row to database. Result=" + result);
			}
			ResultSet rs = ps.getGeneratedKeys();
			return resultSetHandler.handle(rs);
		}
	}
	
	public static void insertBatch(Connection c, String sql, List<Object[]> parametersList) throws SQLException {
		try (PreparedStatement ps = c.prepareStatement(sql)) {
			for (Object[] parameters : parametersList) {
				populatePreparedStatement(ps, parameters);
				ps.addBatch();
			}
			ps.executeBatch();
		}
	}
	
	public static void populateSqlAndParams(StringBuilder sql, List<Object> params, List<Integer> list, String expression) {
		if (list != null && !list.isEmpty()) {
			sql.append(" and (");
			for (int i = 0; i < list.size(); i++) {
				sql.append(expression);
				params.add(list.get(i));
				if (i != list.size() - 1) {
					sql.append(" or ");
				}
			}
			sql.append(")");
		}
	}
	
	private static void populatePreparedStatement(PreparedStatement ps, Object... parameters) throws SQLException {
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				ps.setObject(i + 1, parameters[i]);
			}
		}
	}

	private JDBCUtils() {
	}
}
