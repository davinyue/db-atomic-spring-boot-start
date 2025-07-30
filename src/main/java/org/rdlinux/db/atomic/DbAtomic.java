package org.rdlinux.db.atomic;

public interface DbAtomic<Vt> {
    /**
     * 获取当前值
     */
    Vt get();

    /**
     * 设置新值
     */
    void set(Vt newValue);

    /**
     * 设置新值并返回老值
     */
    Vt getAndSet(Vt newValue);

    /**
     * 对比设置
     *
     * @param expectedValue 对比值
     * @param newValue      新值
     */
    boolean compareAndSet(Vt expectedValue, Vt newValue);

    /**
     * 对比删除
     *
     * @param expectedValue 对比值
     */
    boolean compareAndDelete(Vt expectedValue);

    /**
     * 增加指定值
     */
    void add(Vt addedValue);

    /**
     * 获取老值并增加指定值
     */
    Vt getAndAdd(Vt addValue);

    /**
     * 增加指定值并返回新值
     */
    Vt addAndGet(Vt addValue);

    /**
     * 删除
     */
    void delete();
}
