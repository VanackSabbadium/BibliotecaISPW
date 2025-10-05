package it.biblioteca.dao.jdbc;

import it.biblioteca.dao.ConnectionProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class JdbcSupport {
    protected final ConnectionProvider cp;

    protected JdbcSupport(ConnectionProvider cp) {
        this.cp = cp;
    }

    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    protected interface PSBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    protected <T> List<T> query(String sql, RowMapper<T> mapper) throws SQLException {
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<T> out = new ArrayList<>();
            while (rs.next()) out.add(mapper.map(rs));
            return out;
        }
    }

    protected <T> List<T> query(String sql, PSBinder binder, RowMapper<T> mapper) throws SQLException {
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) out.add(mapper.map(rs));
                return out;
            }
        }
    }

    protected <T> T queryOne(String sql, PSBinder binder, RowMapper<T> mapper) throws SQLException {
        List<T> list = query(sql, binder, mapper);
        return list.isEmpty() ? null : list.getFirst();
    }

    protected int update(String sql, PSBinder binder) throws SQLException {
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (binder != null) binder.bind(ps);
            return ps.executeUpdate();
        }
    }

    protected long insertAndReturnKey(String sql, PSBinder binder) throws SQLException {
        try (Connection c = cp.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (binder != null) binder.bind(ps);
            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
            return -1L;
        }
    }
}