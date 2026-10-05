package lk.petcare;
import java.sql.*;
import java.util.*;
/** DAO pattern: parameterized SQL and resource management stay out of Swing. */
public final class ClinicDao {
    public record Rows(String[] columns, List<Object[]> values) {}
    public record Choice(long id, String label) { @Override public String toString() { return label; } }
    public Rows query(String sql, Object... args) throws SQLException {
        try (Connection c = Database.open(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, args);
            try (ResultSet r = p.executeQuery()) {
                int count = r.getMetaData().getColumnCount();
                String[] labels = new String[count];
                for (int i = 0; i < count; i++) labels[i] = r.getMetaData().getColumnLabel(i + 1);
                List<Object[]> data = new ArrayList<>();
                while (r.next()) {
                    Object[] row = new Object[count];
                    for (int i = 0; i < count; i++) row[i] = r.getObject(i + 1);
                    data.add(row);
                }
                return new Rows(labels, data);
            }
        }
    }
    public void execute(String sql, Object... args) throws SQLException {
        try (Connection c = Database.open(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, args);
            if (p.executeUpdate() == 0) throw new SQLException("Record no longer exists. Refresh and retry.");
        }
    }
    public List<Choice> choices(String sql) throws SQLException {
        List<Choice> result = new ArrayList<>();
        for (Object[] row : query(sql).values()) result.add(new Choice(((Number)row[0]).longValue(), row[1].toString()));
        return result;
    }
    static void bind(PreparedStatement p, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
    }
}
