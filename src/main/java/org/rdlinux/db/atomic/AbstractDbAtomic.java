package org.rdlinux.db.atomic;

import org.rdlinux.ezmybatis.core.EzUpdate;
import org.rdlinux.ezmybatis.core.mapper.EzMapper;
import org.rdlinux.ezmybatis.core.sqlstruct.table.EntityTable;
import org.rdlinux.ezmybatis.utils.Assert;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

public abstract class AbstractDbAtomic<Vt> implements DbAtomic<Vt> {
    protected final String name;
    protected final EzMapper ezMapper;
    protected final PlatformTransactionManager transactionManager;

    protected AbstractDbAtomic(String name, EzMapper ezMapper, PlatformTransactionManager transactionManager) {
        Assert.notEmpty(name, "name must not be empty");
        Assert.notNull(ezMapper, "ezMapper must not be null");
        Assert.notNull(transactionManager, "transactionManager must not be null");
        this.name = name;
        this.ezMapper = ezMapper;
        this.transactionManager = transactionManager;
        this.init();
    }

    /**
     * 使用事务执行
     */
    protected void doWithTransaction(Runnable runnable) {
        TransactionTemplate template = new TransactionTemplate(this.transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.executeWithoutResult(status -> {
            try {
                runnable.run();
            } catch (Exception e) {
                //标记事务回滚
                status.setRollbackOnly();
                throw e;
            }
        });
    }

    /**
     * 必须使用存在事务执行
     */
    protected void doWithMandatoryTransaction(Runnable runnable) {
        TransactionTemplate template = new TransactionTemplate(this.transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
        template.executeWithoutResult(status -> {
            try {
                runnable.run();
            } catch (Exception e) {
                //标记事务回滚
                status.setRollbackOnly();
                throw e;
            }
        });
    }

    /**
     * 初始化
     */
    protected void init() {
        this.doWithTransaction(() -> {
            Date now = new Date();
            DbAtomicTable dbAtomicTable = new DbAtomicTable().setId(this.name).setCreateTime(now)
                    .setUpdateTime(now)
                    .setDoubleValue(0.0)
                    .setIntValue(0)
                    .setLongValue(0L);
            try {
                this.ezMapper.insert(dbAtomicTable);
            } catch (DuplicateKeyException ignore) {
            }
        });
    }

    /**
     * 加锁
     */
    protected void lock() {
        EntityTable table = EntityTable.of(DbAtomicTable.class);
        boolean isExist = false;
        while (!isExist) {
            EzUpdate update = EzUpdate.update(table)
                    .set()
                    .setField(DbAtomicTable.Filed.updateTime, new Date())
                    .done()
                    .where()
                    .addFieldCondition(DbAtomicTable.Filed.id, this.name)
                    .done()
                    .build();
            isExist = this.ezMapper.ezUpdate(update) > 0;
            if (!isExist) {
                this.init();
            }
        }
    }

    @Override
    public void delete() {
        this.doWithMandatoryTransaction(() -> this.ezMapper.deleteById(DbAtomicTable.class, this.name));
    }
}
