package de.learnlib.benchmarking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.persistence.queries.DataModifyQuery;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionCustomizer;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;

/**
 * EclipseLink blindly adds FK constraints on relations. This session customizer intercepts the booting processes,
 * checks for existing constraints, and skips whatever command tries to add an existing constraint.
 */
public class ExistingConstraintSkipper implements SessionCustomizer {

    private static final Pattern ADD_CONSTRAINT_PATTERN =
            Pattern.compile("ALTER TABLE\\s+\"?(\\w+)\"?\\s+ADD CONSTRAINT\\s+\"?(\\w+)\"?", Pattern.CASE_INSENSITIVE);

    @Override
    public void customize(Session session) {
        Set<String> existingConstraints = loadExistingConstraints(session);

        session.getEventManager().addListener(new SessionEventAdapter() {

            @Override
            public void preExecuteQuery(SessionEvent event) {
                DatabaseQuery query = event.getQuery();
                if (query instanceof DataModifyQuery) {
                    String sql = query.getSQLString();
                    if (sql != null) {
                        Matcher matcher = ADD_CONSTRAINT_PATTERN.matcher(sql);
                        if (matcher.find()) {
                            String tableName = matcher.group(1).toUpperCase();
                            String constraintName = matcher.group(2).toUpperCase();
                            String key = tableName + "." + constraintName;

                            if (existingConstraints.contains(key)) {
                                // replace with a fast, valid HSQLDB no-op statement
                                query.setSQLString("SET SCHEMA PUBLIC");
                            } else {
                                // record newly created constraint so duplicates in the same run are also skipped
                                existingConstraints.add(key);
                            }
                        }
                    }
                }
            }
        });
    }

    private Set<String> loadExistingConstraints(Session session) {
        Set<String> constraints = new HashSet<>();
        if (!(session.getDatasourceLogin() instanceof DatabaseLogin login)) {
            return constraints;
        }

        String url = login.getConnectionString();
        String user = login.getUserName();
        String password = login.getPassword();

        String metadataSql =
                "SELECT TABLE_NAME, CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = 'PUBLIC'";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(metadataSql)) {

            while (rs.next()) {
                String table = rs.getString("TABLE_NAME").toUpperCase();
                String constraint = rs.getString("CONSTRAINT_NAME").toUpperCase();
                constraints.add(table + "." + constraint);
            }
        } catch (Exception e) {
            session.getSessionLog().warning("Could not preload HSQLDB constraints: " + e.getMessage());
        }
        return constraints;
    }
}
