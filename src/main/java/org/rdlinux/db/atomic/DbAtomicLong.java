package org.rdlinux.db.atomic;

import org.rdlinux.ezmybatis.core.EzDelete;
import org.rdlinux.ezmybatis.core.EzQuery;
import org.rdlinux.ezmybatis.core.EzUpdate;
import org.rdlinux.ezmybatis.core.mapper.EzMapper;
import org.rdlinux.ezmybatis.core.sqlstruct.formula.Formula;
import org.rdlinux.ezmybatis.core.sqlstruct.table.EntityTable;
import org.rdlinux.ezmybatis.utils.Assert;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class DbAtomicLong extends AbstractDbAtomic<Long> {
    public DbAtomicLong(String name, EzMapper ezMapper, PlatformTransactionManager transactionManager) {
        super(name, ezMapper, transactionManager);
    }

    private void addValue(Long addValue) {
        Assert.notNull(addValue, "addValue must not be null");
        EntityTable table = EntityTable.of(DbAtomicTable.class);
        EzUpdate update = EzUpdate.update(table)
                .set()
                .setField(DbAtomicTable.Filed.longValue, Formula.builder(table).withField(DbAtomicTable.Filed.longValue)
                        .addValue(addValue).done().build())
                .done()
                .where()
                .addFieldCondition(DbAtomicTable.Filed.id, this.name)
                .done()
                .build();
        this.ezMapper.ezUpdate(update);
    }


    @Override
    public Long get() {
        EzQuery<Long> query = EzQuery.builder(Long.class)
                .from(EntityTable.of(DbAtomicTable.class))
                .select()
                .addField(DbAtomicTable.Filed.longValue)
                .done()
                .where()
                .addFieldCondition(DbAtomicTable.Filed.id, this.name)
                .done()
                .build();
        Long ret = this.ezMapper.queryOne(query);
        if (ret == null) {
            ret = 0L;
        }
        return ret;
    }

    @Override
    public void set(Long newValue) {
        Assert.notNull(newValue, "newValue must not be null");
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            EzUpdate update = EzUpdate.update(EntityTable.of(DbAtomicTable.class))
                    .set()
                    .setField(DbAtomicTable.Filed.longValue, newValue)
                    .done()
                    .where()
                    .addFieldCondition(DbAtomicTable.Filed.id, this.name)
                    .done()
                    .build();
            this.ezMapper.ezUpdate(update);
        });
    }

    @Override
    public Long getAndSet(Long newValue) {
        Assert.notNull(newValue, "newValue must not be null");
        Long ret = this.get();
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            this.set(newValue);
        });
        return ret;
    }

    @Override
    public boolean compareAndSet(Long expectedValue, Long newValue) {
        Assert.notNull(expectedValue, "expectedValue must not be null");
        Assert.notNull(newValue, "newValue must not be null");
        AtomicBoolean ret = new AtomicBoolean(Boolean.FALSE);
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            EzUpdate update = EzUpdate.update(EntityTable.of(DbAtomicTable.class))
                    .set()
                    .setField(DbAtomicTable.Filed.longValue, newValue)
                    .done()
                    .where()
                    .addFieldCondition(DbAtomicTable.Filed.id, this.name)
                    .addFieldCondition(DbAtomicTable.Filed.longValue, expectedValue)
                    .done()
                    .build();
            ret.set(this.ezMapper.ezUpdate(update) > 0);
        });
        return ret.get();
    }

    @Override
    public boolean compareAndDelete(Long expectedValue) {
        Assert.notNull(expectedValue, "expectedValue must not be null");
        AtomicBoolean ret = new AtomicBoolean(Boolean.FALSE);
        this.doWithMandatoryTransaction(() -> {
            EzDelete delete = EzDelete.delete(EntityTable.of(DbAtomicTable.class))
                    .where()
                    .addFieldCondition(DbAtomicTable.Filed.id, this.name)
                    .addFieldCondition(DbAtomicTable.Filed.longValue, expectedValue)
                    .done()
                    .build();
            ret.set(this.ezMapper.ezDelete(delete) > 0);
        });
        return ret.get();
    }


    @Override
    public Long getAndAdd(Long addValue) {
        Long ret = this.get();
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            this.addValue(addValue);
        });
        return ret;
    }

    @Override
    public Long addAndGet(Long addValue) {
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            this.addValue(addValue);
        });
        return this.get();
    }
}
