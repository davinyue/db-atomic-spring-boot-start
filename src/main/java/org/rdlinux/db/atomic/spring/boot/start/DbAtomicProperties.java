package org.rdlinux.db.atomic.spring.boot.start;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = DbAtomicProperties.PREFIX)
public class DbAtomicProperties {
    public static final String PREFIX = "db-atomic";
    /**
     * 自动创建表
     */
    private boolean autoCreateTable = true;

    public boolean isAutoCreateTable() {
        return this.autoCreateTable;
    }

    public void setAutoCreateTable(boolean autoCreateTable) {
        this.autoCreateTable = autoCreateTable;
    }
}
