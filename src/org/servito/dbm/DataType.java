package org.servito.dbm;


import java.io.Serializable;

/**
 * This enum provides a list of all SQL types which can be used in a table.
 *
 * @author Samuele Atzori
 * @since 1.0
 * @see org.servito.dbm.Database
 */
public enum DataType implements Serializable {

    /**
     * Numeric SQL data type.
     * From 0 to 1.
     */
    BOOLEAN("boolean",16),

    /**
     * Numeric SQL data type.
     * From 0 to 255.
     */
    TINY_INT("tinyint",-6),

    /**
     * Numeric SQL data type.
     * From -32,768 to +32,767.
     */
    SMALL_INT("smallint",5),

    /**
     * Numeric SQL data type.
     * From -2,147,483,648 to +2,147,483,647.
     */
    INT("int",4),

    /**
     * Numeric SQL data type.
     * From -9,223,372,036,854,775,808 to +9,223,372,036,854,775,807.
     */
    BIG_INT("bigint",-5),

    /**
     * Numeric SQL data type.
     * Holds a decimal number.
     */
    FLOAT("float",6),

    /**
     * Numeric SQL data type.
     * Holds a big decimal number.
     */
    DOUBLE("double",8),

    /**
     * String SQL data type.
     * Holds a string with a maximum length of 65,535 characters.
     */
    VARCHAR("varchar",12),

    /**
     * A date.
     * Format: YYYY-MM-DD. The supported range is from '1000-01-01' to '9999-12-31'.
     */
    DATE("date",91),

    /**
     * A time.
     * Format: hh:mm:ss. The supported range is from '-838:59:59' to '838:59:59'.
     */
    TIME("time",92),

    /**
     * There are other types not specified in here.
     * You must not use this type when you create a table, because it isn't supported by sql language.
     * This is just a placeholder for other types like blob, timestamp, varchar, etc...
     */
    OTHER(null,10000);

    /**
     * Each type has got a string value used to make the SQL query.
     * This field is private and unmodifiable.
     */
    private String strVal;

    /**
     * There is another list of values which represent sql types in {@link java.sql.Types}, but it hasn't got methods to
     * parse each value to the corresponding String value.
     * All ids match the corresponding int constant.
     * Moreover, there are other types in JDBC Types class those aren't supported in here.
     */
    private int id;

    /**
     * Simple enum constructor.
     */
    DataType(String strVal, int id) {
        this.strVal = strVal;
        this.id = id;
    }

    /**
     * {@code strVal}'s simple getter.
     *
     * @return Theq String value of each data type.
     */
    public String toString() {
        return strVal;
    }

    /**
     * {@code id}'s simple getter.
     *
     * @return The corresponding int constant of each type.
     */
    public int getId() {
        return id;
    }

    private static final long SerialVersionUID = 20001L;

}
