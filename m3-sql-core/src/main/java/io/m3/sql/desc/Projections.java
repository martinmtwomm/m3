package io.m3.sql.desc;

/**
 * @author <a href="mailto:jacques.militello@olky.eu">Jacques Militello</a>
 */
public final class Projections {

    private Projections() {
    }

    public static SqlColumn count(SqlColumn column) {
        return new SqlFunctionColumn("COUNT", column);
    }


}
