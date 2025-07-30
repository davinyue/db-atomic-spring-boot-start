package org.rdlinux.db.atomic.spring.boot.start;

import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.rdlinux.db.atomic.DbAtomicClient;
import org.rdlinux.ezmybatis.constant.DbType;
import org.rdlinux.ezmybatis.core.EzMybatisContent;
import org.rdlinux.ezmybatis.core.mapper.EzMapper;
import org.rdlinux.ezmybatis.spring.boot.start.EzMybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
@ConditionalOnClass({EzMapper.class, DbAtomicProperties.class})
@EnableConfigurationProperties(DbAtomicProperties.class)
@AutoConfigureAfter({EzMybatisAutoConfiguration.class})
public class DbAtomicAutoConfig {
    @Resource
    private EzMapper ezMapper;
    @Resource
    private PlatformTransactionManager transactionManager;
    @Resource
    private DbAtomicProperties dbAtomicProperties;
    @Resource
    private DataSource dataSource;
    @Resource
    private MybatisProperties mybatisProperties;

    @PostConstruct
    public void init() {
        if (!this.dbAtomicProperties.isAutoCreateTable()) {
            return;
        }
        DbType dbType = EzMybatisContent.getDbType(this.mybatisProperties.getConfiguration());
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("sql/" + dbType.name() + ".sql");
        if (inputStream == null) {
            return;
        }
        try (Connection connection = this.dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String sqlTest = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            // 去掉注释与空行（可选）
            sqlTest = sqlTest.replaceAll("(?m)--.*?$", ""); // 去除行注释
            sqlTest = sqlTest.replaceAll("/\\*.*?\\*/", ""); // 去除块注释（非贪婪匹配）
            // 按分号拆分语句
            String[] sqlStatements = sqlTest.split(";(\\s*\\r?\\n|\\s*$)");
            for (String sql : sqlStatements) {
                try {
                    statement.execute(sql);
                } catch (Exception ignore) {
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    @Bean
    public DbAtomicClient dbAtomicClient() {
        return new DbAtomicClient(this.ezMapper, this.transactionManager);
    }
}

