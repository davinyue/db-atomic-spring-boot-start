package org.rdlinux.db.atomic;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "rd_db_atomic")
public class DbAtomicTable {
    @Id
    private String id;
    private Date createTime;
    private Date updateTime;
    private Integer intValue;
    private Long longValue;
    private Double doubleValue;

    public String getId() {
        return this.id;
    }

    public DbAtomicTable setId(String id) {
        this.id = id;
        return this;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public DbAtomicTable setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public DbAtomicTable setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Integer getIntValue() {
        return this.intValue;
    }

    public DbAtomicTable setIntValue(Integer intValue) {
        this.intValue = intValue;
        return this;
    }

    public Long getLongValue() {
        return this.longValue;
    }

    public DbAtomicTable setLongValue(Long longValue) {
        this.longValue = longValue;
        return this;
    }

    public Double getDoubleValue() {
        return this.doubleValue;
    }

    public DbAtomicTable setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
        return this;
    }

    public static final class Filed {
        public static final String id = "id";
        public static final String createTime = "createTime";
        public static final String updateTime = "updateTime";
        public static final String intValue = "intValue";
        public static final String longValue = "longValue";
        public static final String doubleValue = "doubleValue";
    }
}
