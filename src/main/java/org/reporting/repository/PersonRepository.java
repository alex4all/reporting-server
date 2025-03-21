package org.reporting.repository;

import org.reporting.model.Person;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PersonRepository {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/yourdb";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public List<Person> findAll() {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email FROM persons";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                persons.add(mapResultSetToPerson(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching persons", e);
        }
        return persons;
    }

    public Optional<Person> findById(Long id) {
        String sql = "SELECT id, first_name, last_name, email FROM persons WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToPerson(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching person with id: " + id, e);
        }
        return Optional.empty();
    }

    private Person mapResultSetToPerson(ResultSet rs) throws SQLException {
        return new Person(
            rs.getLong("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email")
        );
    }
} 