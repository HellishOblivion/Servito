package org.servito.dbm;

import java.io.Serializable;

public class ColumnLabel implements Serializable {

    private static final long serialVersionUID = 20000L;

    private String name;
    private DataType type;

    public ColumnLabel(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

}
