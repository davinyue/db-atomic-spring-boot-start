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

    @Override
    public void add(Long addValue) {
        Assert.notNull(addValue, "addValue must not be null");
        this.doWithMandatoryTransaction(() -> {
            this.lock();
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            Formula lvFm = Formula.build(f ->
                    f.with(table.field(DbAtomicTable.Filed.longValue))
                            .add(addValue));
            EzUpdate update = EzUpdate.update(table)
                    .set(s ->
                            s.add(table.field(DbAtomicTable.Filed.longValue).set(lvFm)))
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                    )
                    .build();
            this.ezMapper.ezUpdate(update);
        });
    }


    @Override
    public Long get() {
        EntityTable table = EntityTable.of(DbAtomicTable.class);
        EzQuery<Long> query = EzQuery.builder(Long.class)
                .from(table)
                .select(s -> s.add(table.field(DbAtomicTable.Filed.longValue)))
                .where(w ->
                        w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                )
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

            EntityTable table = EntityTable.of(DbAtomicTable.class);
            EzUpdate update = EzUpdate.update(table)
                    .set(s -> s.add(table.field(DbAtomicTable.Filed.longValue).set(newValue)))
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                    )
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
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            EzUpdate update = EzUpdate.update(table)
                    .set(s -> s.add(table.field(DbAtomicTable.Filed.longValue).set(newValue)))
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                                    .add(table.field(DbAtomicTable.Filed.longValue).eq(expectedValue))
                    )
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
            EntityTable table = EntityTable.of(DbAtomicTable.class);
            EzDelete delete = EzDelete.delete(table)
                    .where(w ->
                            w.add(table.field(DbAtomicTable.Filed.id).eq(this.name))
                                    .add(table.field(DbAtomicTable.Filed.longValue).eq(expectedValue))
                    )
                    .build();
            ret.set(this.ezMapper.ezDelete(delete) > 0);
        });
        return ret.get();
    }


    @Override
    public Long getAndAdd(Long addValue) {
        Long ret = this.get();
        this.add(addValue);
        return ret;
    }

    @Override
    public Long addAndGet(Long addValue) {
        this.add(addValue);
        return this.get();
    }
}
