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

public class DbAtomicDouble extends AbstractDbAtomic<Double> {
    public DbAtomicDouble(String name, EzMapper ezMapper, PlatformTransactionManager transactionManager) {
        super(name, ezMapper, transactionManager);
    }

    @Override
    public void add(Double addValue) {
        Assert.notNull(addValue, "addValue must not be null");
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            Formula dvFm = Formula.build(f ->
                    f.with(table.field(DbAtomicTable.Filed.doubleValue))
                            .add(addValue));
            EzUpdate update = EzUpdate.update(table)
                    .set(s ->
                            s.add(table.field(DbAtomicTable.Filed.doubleValue).set(dvFm)))
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                    )
                    .build();
            this.ezMapper.ezUpdate(update);
        });
    }


    @Override
    public Double get() {
        EntityTable table = EntityTable.of(DbAtomicTable.class);
        EzQuery<Double> query = EzQuery.builder(Double.class)
                .from(table)
                .select(s -> s.add(table.field(DbAtomicTable.Filed.doubleValue)))
                .where(w ->
                        w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                )
                .build();
        Double ret = this.ezMapper.queryOne(query);
        if (ret == null) {
            ret = 0.0;
        }
        return ret;
    }

    @Override
    public void set(Double newValue) {
        Assert.notNull(newValue, "newValue must not be null");
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            EzUpdate update = EzUpdate.update(table)
                    .set(s -> s.add(table.field(DbAtomicTable.Filed.doubleValue).set(newValue)))
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                    )
                    .build();
            this.ezMapper.ezUpdate(update);
        });
    }

    @Override
    public Double getAndSet(Double newValue) {
        Assert.notNull(newValue, "newValue must not be null");
        Double ret = this.get();
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            this.set(newValue);
        });
        return ret;
    }

    @Override
    public boolean compareAndSet(Double expectedValue, Double newValue) {
        Assert.notNull(expectedValue, "expectedValue must not be null");
        Assert.notNull(newValue, "newValue must not be null");
        AtomicBoolean ret = new AtomicBoolean(Boolean.FALSE);
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            EzUpdate update = EzUpdate.update(table)
                    .set(s -> s.add(table.field(DbAtomicTable.Filed.doubleValue).set(newValue)))
                    .where(w -> w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                            .add(table.field(DbAtomicTable.Filed.doubleValue).eq(expectedValue)))
                    .build();
            ret.set(this.ezMapper.ezUpdate(update) > 0);
        });
        return ret.get();
    }

    @Override
    public boolean compareAndDelete(Double expectedValue) {
        Assert.notNull(expectedValue, "expectedValue must not be null");
        AtomicBoolean ret = new AtomicBoolean(Boolean.FALSE);
        this.doWithMandatoryTransaction(() -> {
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            EzDelete delete = EzDelete.delete(table)
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                                    .add(table.field(DbAtomicTable.Filed.doubleValue).eq(expectedValue))
                    )
                    .build();
            ret.set(this.ezMapper.ezDelete(delete) > 0);
        });
        return ret.get();
    }


    @Override
    public Double getAndAdd(Double addValue) {
        Double ret = this.get();
        this.add(addValue);
        return ret;
    }

    @Override
    public Double addAndGet(Double addValue) {
        this.add(addValue);
        return this.get();
    }
}
