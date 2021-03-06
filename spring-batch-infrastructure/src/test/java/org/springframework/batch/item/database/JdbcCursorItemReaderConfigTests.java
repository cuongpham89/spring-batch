package org.springframework.batch.item.database;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runners.JUnit4;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(JUnit4.class)
public class JdbcCursorItemReaderConfigTests {

	/*
	 * Should fail if trying to call getConnection() twice
	 */
	@Test
	public void testUsesCurrentTransaction() throws Exception {
		DataSource ds = mock(DataSource.class);
		Connection con = mock(Connection.class);
		when(con.getAutoCommit()).thenReturn(false);
		PreparedStatement ps = mock(PreparedStatement.class);
		when(con.prepareStatement("select foo from bar", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
				ResultSet.HOLD_CURSORS_OVER_COMMIT)).thenReturn(ps);
		when(ds.getConnection()).thenReturn(con);
		when(ds.getConnection()).thenReturn(con);
		con.commit();
		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		final JdbcCursorItemReader<String> reader = new JdbcCursorItemReader<String>();
		reader.setDataSource(new ExtendedConnectionDataSourceProxy(ds));
		reader.setUseSharedExtendedConnection(true);
		reader.setSql("select foo from bar");
		final ExecutionContext ec = new ExecutionContext();
		tt.execute(
				new TransactionCallback() {
                    @Override
					public Object doInTransaction(TransactionStatus status) {
						reader.open(ec);
						reader.close();
						return null;
					}
				});
	}
	
	/*
	 * Should fail if trying to call getConnection() twice
	 */
	@Test
	public void testUsesItsOwnTransaction() throws Exception {
		
		DataSource ds = mock(DataSource.class);
		Connection con = mock(Connection.class);
		when(con.getAutoCommit()).thenReturn(false);
		PreparedStatement ps = mock(PreparedStatement.class);
		when(con.prepareStatement("select foo from bar", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)).thenReturn(ps);
		when(ds.getConnection()).thenReturn(con);
		when(ds.getConnection()).thenReturn(con);
		con.commit();
		PlatformTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		final JdbcCursorItemReader<String> reader = new JdbcCursorItemReader<String>();
		reader.setDataSource(ds);
		reader.setSql("select foo from bar");
		final ExecutionContext ec = new ExecutionContext();
		tt.execute(
				new TransactionCallback() {
                    @Override
					public Object doInTransaction(TransactionStatus status) {
						reader.open(ec);
						reader.close();
						return null;
					}
				});
	}

}
