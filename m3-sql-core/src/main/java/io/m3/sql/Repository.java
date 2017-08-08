package io.m3.sql;

import io.m3.sql.builder.InsertBuilder;
import io.m3.sql.builder.SelectBuilder;
import io.m3.sql.builder.UpdateBuilder;
import io.m3.sql.desc.SqlColumn;
import io.m3.sql.desc.SqlPrimaryKey;
import io.m3.sql.desc.SqlSingleColumn;
import io.m3.sql.desc.SqlTable;
import io.m3.sql.jdbc.*;
import io.m3.util.ImmutableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repository pattern, subclasses will be generated by apt.
 *
 * @author <a href="mailto:jacques.militello@gmail.com">Jacques Militello</a>
 */
public abstract class Repository {

    private final Database database;

    public Repository(Database database) {
        this.database = database;
    }

    protected Database database() {
        return this.database;
    }

    protected final SelectBuilder select(ImmutableList<SqlColumn> columns) {
        return new SelectBuilder(this.database, columns);
    }

    protected final InsertBuilder insert(SqlTable table, ImmutableList<SqlPrimaryKey> keys, ImmutableList<SqlSingleColumn> columns) {
        return new InsertBuilder(this.database, table, keys, columns);
    }

    protected final UpdateBuilder update(SqlTable table, ImmutableList<SqlSingleColumn> columns, ImmutableList<SqlPrimaryKey> keys) {
        return new UpdateBuilder(this.database, table, columns, keys);
    }

    protected final <T> T executeSelect(String sql, PreparedStatementSetter pss, ResultSetMapper<T> mapper) {

        PreparedStatement ps = database.transactionManager().current().select(sql);

        try {
            pss.set(ps);
        } catch (SQLException cause) {
            throw new PreparedStatementSetterException(sql, pss, cause);
        }

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapper.map(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected final <E> void executeInsert(String sql, InsertMapper<E> im, E pojo) {

        PreparedStatement ps = database.transactionManager().current().insert(sql);

        try {
            im.insert(ps, pojo);
        } catch (SQLException cause) {
            throw new InsertMapperException(sql, im, pojo, cause);
        }

        int val;

        try {
            val = ps.executeUpdate();
        } catch (SQLException cause) {
            //throw CoreSqlAnalyser.analyse(cause);
            cause.printStackTrace();
            return;
        }

        if (val != 1) {
            throw new RuntimeException("");
        }
    }

    protected final <E> void executeInsertAutoIncrement(String sql, InsertMapperWithAutoIncrement<E> im, E pojo) {

        PreparedStatement ps = database.transactionManager().current().insert(sql);

        try {
            im.insert(ps, pojo);
        } catch (SQLException cause) {
            throw new InsertMapperException(sql, im, pojo, cause);
        }

        int val;

        try {
            val = ps.executeUpdate();
        } catch (SQLException cause) {
            //throw CoreSqlAnalyser.analyse(cause);
            cause.printStackTrace();
            return;
        }

        if (val != 1) {
            throw new RuntimeException("");
        }

        try (ResultSet rs = ps.getGeneratedKeys()) {

            if (rs.next()) {
                im.setId(pojo, rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected final <E> void executeUpdate(String sql, UpdateMapper<E> um, E pojo) {

        PreparedStatement ps = database.transactionManager().current().update(sql);

        try {
            um.update(ps, pojo);
        } catch (SQLException cause) {
            //throw new PreparedStatementSetterException(sql, pss, cause);
        }

        int val;

        try {
            val = ps.executeUpdate();
        } catch (SQLException cause) {
            cause.printStackTrace();
            //throw CoreSqlAnalyser.analyse(cause);
            return;
        }

        if (val != 1) {
            throw new RuntimeException("");
        }

    }

    protected final <E> void executeDelete(String sql, DeleteMapper<E> dm, E pojo) {

        PreparedStatement ps = database.transactionManager().current().update(sql);

       // try {
            dm.delete(ps, pojo);
        //} catch (SQLException cause) {
            //throw new PreparedStatementSetterException(sql, pss, cause);
        //}

        int val;

        try {
            val = ps.executeUpdate();
        } catch (SQLException cause) {
            cause.printStackTrace();
            //throw CoreSqlAnalyser.analyse(cause);
            return;
        }

        if (val != 1) {
            throw new RuntimeException("");
        }

    }

    protected final <E> void addBatch(String sql, InsertMapper<E> im, E pojo) {

        PreparedStatement ps = database.transactionManager().current().batch(sql);

        try {
            im.insert(ps, pojo);
        } catch (SQLException cause) {
            throw new InsertMapperException(sql, im, pojo, cause);
        }

        try {
            ps.addBatch();
        } catch (SQLException cause) {
            cause.printStackTrace();
            return;
        }
    }

    public final void executeBatch() {

        Iterable<PreparedStatement> statements = database.transactionManager().current().getBatchs();

        for (PreparedStatement ps : statements) {
            try {
                ps.executeBatch();
            } catch (SQLException cause) {
                cause.printStackTrace();
                // throw CoreSqlAnalyser.analyse(cause);
            }
        }
    }
}
