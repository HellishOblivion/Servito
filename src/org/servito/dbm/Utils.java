package org.servito.dbm;

public class Utils {

    public static String parseCondition(String java) {
        String sql = java;
        String[][] replaces = new String[][]{
                {"==", "="},
                {"!=", "<>"},
                {"\"", "'"},
                {"&&", " AND "},
                {"||", " OR "},
        };
        sql = sql.replaceAll("== +[nN][uU][lL][lL]"," IS NULL");
        sql = sql.replaceAll("!= +[nN][uU][lL][lL]"," IS NOT NULL");
        for (String[] replace : replaces) {
            sql = sql.replace(replace[0], replace[1]);
        }
        return sql;
    }

}
