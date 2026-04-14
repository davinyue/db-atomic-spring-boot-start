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

/**
 * 基于数据库的原子int
 */
public class DbAtomicInteger extends AbstractDbAtomic<Integer> {
    public DbAtomicInteger(String name, EzMapper ezMapper, PlatformTransactionManager transactionManager) {
        super(name, ezMapper, transactionManager);
    }

    @Override
    public void add(Integer addValue) {
        Assert.notNull(addValue, "addValue must not be null");
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            Formula ivFm = Formula.build(f ->
                    f.with(table.field(DbAtomicTable.Filed.intValue))
                            .add(addValue));
            EzUpdate update = EzUpdate.update(table)
                    .set(s ->
                            s.add(table.field(DbAtomicTable.Filed.intValue).set(ivFm)))
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                    )
                    .build();
            this.ezMapper.ezUpdate(update);
        });
    }


    @Override
    public Integer get() {
        EntityTable table = EntityTable.of(DbAtomicTable.class);
        EzQuery<Integer> query = EzQuery.builder(Integer.class)
                .from(table)
                .select(s -> s.add(table.field(DbAtomicTable.Filed.intValue)))
                .where(w ->
                        w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                )
                .build();
        Integer ret = this.ezMapper.queryOne(query);
        if (ret == null) {
            ret = 0;
        }
        return ret;
    }

    @Override
    public void set(Integer newValue) {
        Assert.notNull(newValue, "newValue must not be null");
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            EzUpdate update = EzUpdate.update(table)
                    .set(s -> s.add(table.field(DbAtomicTable.Filed.intValue).set(newValue)))
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                    )
                    .build();
            this.ezMapper.ezUpdate(update);
        });
    }

    @Override
    public Integer getAndSet(Integer newValue) {
        Assert.notNull(newValue, "newValue must not be null");
        Integer ret = this.get();
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            this.set(newValue);
        });
        return ret;
    }

    @Override
    public boolean compareAndSet(Integer expectedValue, Integer newValue) {
        Assert.notNull(expectedValue, "expectedValue must not be null");
        Assert.notNull(newValue, "newValue must not be null");
        AtomicBoolean ret = new AtomicBoolean(Boolean.FALSE);
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            EzUpdate update = EzUpdate.update(table)
                    .set(s -> s.add(table.field(DbAtomicTable.Filed.intValue).set(newValue)))
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                                    .add(table.field(DbAtomicTable.Filed.intValue).eq(expectedValue))
                    )
                    .build();
            ret.set(this.ezMapper.ezUpdate(update) > 0);
        });
        return ret.get();
    }

    @Override
    public boolean compareAndDelete(Integer expectedValue) {
        Assert.notNull(expectedValue, "expectedValue must not be null");
        AtomicBoolean ret = new AtomicBoolean(Boolean.FALSE);
        this.doWithMandatoryTransaction(() -> {
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            EzDelete delete = EzDelete.delete(table)
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                                    .add(table.field(DbAtomicTable.Filed.intValue).eq(expectedValue))
                    )
                    .build();
            ret.set(this.ezMapper.ezDelete(delete) > 0);
        });
        return ret.get();
    }


    @Override
    public Integer getAndAdd(Integer addValue) {
        Integer ret = this.get();
        this.add(addValue);
        return ret;
    }

    @Override
    public Integer addAndGet(Integer addValue) {
        this.add(addValue);
        return this.get();
    }
}
