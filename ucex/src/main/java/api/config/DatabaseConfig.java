package api.config;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan(basePackages = "api.dao")
public class DatabaseConfig {

	@Value("${ucex.mybatis.driverName}")
	private String driverName;
	@Value("${ucex.mybatis.userName}")
	private String userName;
	@Value("${ucex.mybatis.password}")
	private String password;
	@Value("${ucex.mybatis.url}")
	private String url;

	@Value("${ucex.mybatis.poolPingQuery}")
	private String poolPingQuery;
	@Value("${ucex.mybatis.poolPingEnabled}")
	private String poolPingEnabled;
	@Value("${ucex.mybatis.poolPingConnectionsNotUsedFor}")
	private int poolPingConnectionsNotUsedFor;
	@Value("${ucex.mybatis.mapperLocation}")
	private Resource[] mapperLocation;

	@Bean
	public DataSource getDataSource() {

		PooledDataSource ds = new PooledDataSource();
		ds.setDriver(driverName);
		ds.setUsername(userName);
		ds.setPassword(password);
		ds.setUrl(url);
		ds.setPoolPingQuery(poolPingQuery);
		ds.setPoolPingEnabled(Boolean.valueOf(poolPingEnabled));
		ds.setPoolPingConnectionsNotUsedFor(poolPingConnectionsNotUsedFor);

		return ds;
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {

		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(getDataSource());
		sqlSessionFactoryBean.setMapperLocations(mapperLocation);
		sqlSessionFactoryBean.setTypeAliasesPackage("api.domain");

		return sqlSessionFactoryBean.getObject();

	}

	@Bean
	public DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager(getDataSource());
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
