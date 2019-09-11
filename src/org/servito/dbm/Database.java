package org.servito.dbm;


public interface Database {

    boolean exists();
    void create() throws Exception;
    void delete() throws Exception;
    void open() throws Exception;
    void close() throws Exception;
    Object executeQuery(String query) throws Exception;
    void executeUpdate(String update) throws Exception;
    void createTable(String name, ColumnLabel[] labels) throws Exception;
    void deleteTable(String name) throws Exception;
    void renameTable(String oldName, String newName) throws Exception;
    String[] getTableNames() throws Exception;
    ColumnLabel[] getColumnLabels(String table) throws Exception;
    Column[] getColumns(String table, String javaCondition, String...colNames) throws Exception;
    void addColumn(String table, ColumnLabel label) throws Exception;
    void removeColumn(String name) throws Exception;
    void insertRecord(String table, String[] record) throws Exception;
    void deleteRecords(String table, String javaCondition) throws Exception;
    void updateRecords(String table, String javaCondition, String[] updates) throws Exception;

}
