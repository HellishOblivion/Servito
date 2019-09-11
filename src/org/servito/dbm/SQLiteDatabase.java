package org.servito.dbm;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;


public class SQLiteDatabase implements Database {

    private Connection conn;
    private String name;
    private String path;
    private File file;

    public SQLiteDatabase(String path) {
        String[] pathToStringArray = path.split("/");
        this.name = pathToStringArray[pathToStringArray.length - 1].split("\\.")[0];
        this.path = path;
        this.conn = null;
        file = new File(path);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public void create() throws SQLException {
        try {
            file.createNewFile();
        } catch(IOException e) {
            throw new SQLException("Impossible to create database, try to check your path.");
        }
    }

    @Override
    public void delete() throws SQLException {
        close();
        file.delete();
    }

    @Override
    public void open() throws SQLException {
        DriverManager.registerDriver(new org.sqlite.JDBC());
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    @Override
    public void close() throws SQLException {
        if(this.conn != null) this.conn.close();
    }

    @Override
    public synchronized Object executeQuery(String query) throws SQLException {
        Statement st = conn.createStatement();
        return st.executeQuery(query);
    }

    @Override
    public synchronized void executeUpdate(String update) throws SQLException {
        Statement st = conn.createStatement();
        st.executeUpdate(update);
    }

    @Override
    public void createTable(String name, ColumnLabel... labels) throws SQLException {
        StringBuilder update = new StringBuilder("CREATE TABLE " + name + "(");
        for(ColumnLabel label : labels) {
            DataType type = label.getType();
            String str = label.getName() + " " + type.toString();
            if(type == DataType.FLOAT) str += "(24)";
            else if(type == DataType.DOUBLE) str += "(53)";
            str += ",";
            update.append(str);
        }
        update.setCharAt(update.length() - 1, ')');
        executeUpdate(update.toString());
    }

    @Override
    public void deleteTable(String name) throws SQLException {
        executeUpdate("DROP TABLE " + name);
    }

    @Override
    public void renameTable(String oldName, String newName) throws SQLException {
        executeUpdate("ALTER TABLE " + oldName + " RENAME TO " + newName);
    }

    @Override
    public String[] getTableNames() throws SQLException {
        ResultSet result = (ResultSet) executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
        StringBuilder reply = new StringBuilder();
        while(result.next()) {
            String str = result.getString(1) + "|";
            reply.append(str);
        }
        return reply.toString().split("[|]");
    }

    @Override
    public Column[] getColumns(String table, String condition, String... names) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT ");
        for (String name : names) {
            if(name.equals("*")) {
                query.append("* ");
                break;
            }
            String str = name + ",";
            query.append(str);
        }
        query.setCharAt(query.length() - 1, ' ');
        String toAppend = "FROM " + table;
        query.append(toAppend);
        if(condition != null) {
            toAppend = " WHERE " + Utils.parseCondition(condition);
            query.append(toAppend);
        }
        ResultSet result = (ResultSet) executeQuery(query.toString());
        ColumnLabel[] labels = getColumnLabels(result.getMetaData());
        Column[] columns = new Column[labels.length];
        int i = 0;
        for(ColumnLabel label : labels) {
            Column column = new Column(label, new ArrayList<>());
            columns[i] = column;
            i++;
        }
        i = 0;
        while(result.next()) {
            for (Column column : columns) {
                column.getData().add(i, result.getString(column.getLabel().getName()));
            }
            i++;
        }
        return columns;
    }

    @Override
    public ColumnLabel[] getColumnLabels(String table) throws SQLException {
        ResultSet result = (ResultSet) executeQuery("SELECT * FROM " + table + " LIMIT 0");
        return getColumnLabels(result.getMetaData());
    }

    private ColumnLabel[] getColumnLabels(ResultSetMetaData metaData) throws SQLException {
        List<ColumnLabel> labels = new ArrayList<>();
        for(int i = 1; i <= metaData.getColumnCount(); i++) {
            final int currentId = metaData.getColumnType(i);
            DataType type = DataType.OTHER;
            for(DataType t : EnumSet.allOf(DataType.class)) {
                if(t.getId() == currentId) {
                    type = t;
                    break;
                }
            }
            labels.add(new ColumnLabel(metaData.getColumnName(i), type));
        }
        return Arrays.copyOf(labels.toArray(), labels.size(), ColumnLabel[].class);
    }

    @Override
    public void addColumn(String table, ColumnLabel label) throws SQLException {
        executeUpdate("ALTER TABLE " + table + " ADD " + label.getName() + " " + label.getType().toString());
    }

    @Override
    public void removeColumn(String name) throws SQLException {
        throw new SQLException("Unsupported operation.");
    }

    @Override
    public void insertRecord(String table, String... data) throws SQLException {
        StringBuilder update = new StringBuilder("INSERT INTO " + table + " VALUES (");
        String str;
        for (String d : data) {
            str = d + ",";
            update.append(str);
        }
        update.setCharAt(update.length() - 1, ')');
        executeUpdate(update.toString());
    }

    @Override
    public void deleteRecords(String table, String condition) throws SQLException {
        executeUpdate("DELETE FROM " + table + " WHERE " + Utils.parseCondition(condition));
    }

    @Override
    public void updateRecords(String table, String condition, String... updates) throws SQLException {
        StringBuilder update = new StringBuilder("UPDATE " + table + " SET ");
        for (String u : updates) {
            String toAppend = u.replace("\"", "'") + ",";
            update.append(toAppend);
        }
        update.setCharAt(update.length() - 1, ' ');
        if(condition != null) {
            String str = "WHERE " + Utils.parseCondition(condition);
            update.append(str);
        }
        executeUpdate(update.toString());
    }

}
