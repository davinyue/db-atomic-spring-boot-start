# db-atomic

![Java](https://img.shields.io/badge/Java-8+-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x-green)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

`db-atomic` 是一个基于数据库实现的分布式原子类工具库，提供线程安全、事务一致的 `AtomicInteger`、`AtomicLong` 和
`AtomicDouble` 实现。

## 🎯 核心特性

- **🌐 分布式原子性** - 跨 JVM 保证原子操作，支持集群环境
- **💾 事务一致性** - 与 Spring 事务强一致，避免数据不一致问题
- **🔢 多数据类型** - 支持整数、长整数、双精度浮点数
- **⚡ 轻量无侵入** - 基于 [ez-mybatis](https://github.com/rdlinux/ez-mybatis)，集成简单
- **🛡️ 线程安全** - 基于数据库锁机制，天然支持并发访问

## 🚀 适用场景

- 分布式系统中的计数器管理
- 业务流水号生成
- 库存扣减等需要强一致性的场景
- 需要与数据库事务保持一致的原子操作

## 💡 为什么选择 db-atomic？

### 传统方案的局限性：

| 方案                      | 问题                |
|-------------------------|-------------------|
| Java 原生 `AtomicInteger` | 仅在单 JVM 内有效，无法跨进程 |
| Redis 原子操作              | 无法与数据库事务保持一致性     |
| 数据库乐观锁                  | 实现复杂，需要大量样板代码     |

### db-atomic 的优势：

✅ **分布式支持** - 基于数据库实现，天然支持多实例  
✅ **事务一致性** - 操作在事务提交前不会生效，确保数据一致  
✅ **使用简单** - API 设计类似 Java 原生原子类，学习成本低  
✅ **性能可靠** - 基于数据库优化，支持高并发访问

## 📦 快速开始

### 1. 添加依赖

**Spring Boot 2.x**

```xml

<dependency>
    <groupId>org.rdlinux</groupId>
    <artifactId>db-atomic-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 基础配置

确保你的项目已配置数据源，`db-atomic` 会自动创建所需的数据表。

### 3. 使用示例

#### 注入客户端

```java

@Autowired
private DbAtomicClient dbAtomicClient;
```

## 📚 API 使用指南

> ⚠️ **重要提示**：所有更新操作必须在事务中执行，否则会抛出异常。

### AtomicInteger 使用示例

```java

@Test
public void atomicIntegerExample() {
    // 获取原子整数实例
    DbAtomicInteger counter = dbAtomicClient.getAtomicInteger("user_counter");

    // 读取当前值（无需事务）
    Integer currentValue = counter.get();
    log.info("当前计数值: {}", currentValue);

    // 原子自增操作（需要事务）
    transactionalService.required(() -> {
        int oldValue = counter.getAndAdd(1);
        int newValue = counter.addAndGet(2);
        log.info("自增前: {}, 自增后: {}", oldValue, newValue);
    });

    // CAS 操作
    transactionalService.required(() -> {
        boolean success = counter.compareAndSet(5, 100);
        log.info("CAS 更新结果: {}", success);
    });

    // 设置值
    transactionalService.required(() -> {
        counter.set(50);
        log.info("设置新值: {}", counter.get());
    });

    // 删除操作
    transactionalService.required(() -> {
        counter.delete(); // 强制删除
        // 或使用 CAS 删除
        // boolean deleted = counter.compareAndDelete(expectedValue);
    });
}
```

### AtomicLong 使用示例

```java

@Test
public void atomicLongExample() {
    DbAtomicLong sequence = dbAtomicClient.getAtomicLong("order_sequence");

    transactionalService.required(() -> {
        // 生成订单号
        long orderNumber = sequence.incrementAndGet();
        log.info("生成订单号: {}", orderNumber);

        // 批量操作
        long batchResult = sequence.addAndGet(100L);
        log.info("批量增加后: {}", batchResult);
    });
}
```

### AtomicDouble 使用示例

```java

@Test
public void atomicDoubleExample() {
    DbAtomicDouble balance = dbAtomicClient.getAtomicDouble("account_balance");

    transactionalService.required(() -> {
        // 账户余额操作
        double currentBalance = balance.get();

        // 扣款操作
        if (currentBalance >= 99.99) {
            boolean success = balance.compareAndSet(currentBalance, currentBalance - 99.99);
            log.info("扣款操作结果: {}", success);
        }

        // 充值操作
        balance.addAndGet(50.0);
        log.info("充值后余额: {}", balance.get());
    });
}
```

## 🔧 API 参考

| 方法                              | 说明     | 是否需要事务 |
|---------------------------------|--------|--------|
| `get()`                         | 获取当前值  | ❌      |
| `set(value)`                    | 设置值    | ✅      |
| `compareAndSet(expect, update)` | CAS 更新 | ✅      |
| `compareAndDelete(expect)`      | CAS 删除 | ✅      |
| `addAndGet(delta)`              | 先加后取   | ✅      |
| `getAndAdd(delta)`              | 先取后加   | ✅      |
| `delete()`                      | 强制删除   | ✅      |

## ⚡ 性能考虑

- **连接池配置**：建议配置足够的数据库连接池大小
- **索引优化**：`db-atomic` 会自动为键名创建索引
- **事务粒度**：建议将多个原子操作放在同一个事务中执行

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 Apache 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🔗 相关项目

- [ez-mybatis](https://github.com/rdlinux/ez-mybatis) - 轻量级 MyBatis 增强工具

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐ Star 支持一下！**

</div>