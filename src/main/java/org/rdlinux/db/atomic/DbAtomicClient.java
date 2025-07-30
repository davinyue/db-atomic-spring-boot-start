package org.rdlinux.db.atomic;

import org.rdlinux.ezmybatis.core.mapper.EzMapper;
import org.rdlinux.ezmybatis.utils.Assert;
import org.springframework.transaction.PlatformTransactionManager;

public class DbAtomicClient {
    protected final EzMapper ezMapper;
    protected final PlatformTransactionManager transactionManager;

    public DbAtomicClient(EzMapper ezMapper, PlatformTransactionManager transactionManager) {
        Assert.notNull(ezMapper, "ezMapper must not be null");
        Assert.notNull(transactionManager, "transactionManager must not be null");
        this.ezMapper = ezMapper;
        this.transactionManager = transactionManager;
    }

    /**
     * 获取原子int
     */
    public DbAtomicInteger getAtomicInteger(String name) {
        return new DbAtomicInteger(name, this.ezMapper, this.transactionManager);
    }

    /**
     * 获取原子long
     */
    public DbAtomicLong getAtomicLong(String name) {
        return new DbAtomicLong(name, this.ezMapper, this.transactionManager);
    }

    /**
     * 获取原子double
     */
    public DbAtomicDouble getAtomicDouble(String name) {
        return new DbAtomicDouble(name, this.ezMapper, this.transactionManager);
    }
}
