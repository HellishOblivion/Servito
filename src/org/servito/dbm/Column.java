package org.servito.dbm;

import java.io.Serializable;
import java.util.List;

public class Column implements Serializable {

    private static final long SerialVersionUID = 20002L;

    private ColumnLabel label;
    private List<String> data;

    public Column(ColumnLabel label, List<String> data) {
        this.label = label;
        this.data = data;
    }

    public ColumnLabel getLabel() {
        return label;
    }

    public List<String> getData() {
        return data;
    }

}
