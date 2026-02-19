package DataAccess;

import Connection.ConnectionFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generic DAO class providing basic CRUD operations (Create, Read, Update, Delete)
 * for any entity type {@code T} using reflection.
 * <p>
 * Assumes the database table name is the plural of the class name, and that the class
 * has an {@code id} field as its primary key.
 *
 * @param <T> The type of the entity.
 */
public class GenericDAO<T> {
    private static final Logger LOGGER = Logger.getLogger(GenericDAO.class.getName());
    private final Class<T> type;
    private final String tableName;

    public GenericDAO(Class<T> type) {
        this.type = type;
        this.tableName = type.getSimpleName() + "s";
    }

    /**
     * Retrieves all records from the database table corresponding to the type {@code T}.
     *
     * @return A list containing all entities of type {@code T} from the table.
     */
    public List<T> findAll() {
        List<T> resultList = new ArrayList<>();
        String query = "SELECT * FROM " + tableName;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                T instance = mapResultSetToEntity(result);
                resultList.add(instance);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in findAll", e);
        }
        return resultList;
    }

    /**
     * Retrieves a single entity from the database by its ID.
     *
     * @param id The ID of the entity to retrieve.
     * @return The entity of type {@code T} with the specified ID, or {@code null} if not found.
     */
    public T findById(int id) {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return mapResultSetToEntity(result);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in findById", e);
        }
        return null;
    }

    /**
     * Inserts a new entity into the corresponding database table.
     * The method automatically excludes the "id" field from the INSERT statement and
     * sets the generated ID back into the entity.
     *
     * @param entity The entity to insert.
     * @return The inserted entity with its generated ID, or {@code null} if the operation failed.
     */
    public T insert(T entity) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            Field[] fields = type.getDeclaredFields();
            List<String> columns = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            List<Field> filteredFields = Arrays.stream(fields)
                    .filter(f -> !f.getName().equalsIgnoreCase("id"))
                    .peek(f -> f.setAccessible(true))
                    .toList();

            columns = filteredFields.stream()
                    .map(Field::getName)
                    .toList();

            values = filteredFields.stream()
                    .map(f -> {
                        try {
                            return f.get(entity);
                        } catch (IllegalAccessException e) {
                            LOGGER.log(Level.WARNING, "Error accessing field value", e);
                            return null;
                        }
                    })
                    .toList();


            String placeholders = String.join(", ", columns.stream().map(c -> "?").toList());
            String query = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, String.join(", ", columns), placeholders);

            try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < values.size(); i++) {
                    statement.setObject(i + 1, values.get(i));
                }
                statement.executeUpdate();

                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    Field idField = type.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(entity, keys.getInt(1));
                }
                return entity;
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in insert", e);
        }
        return null;
    }

    /**
     * Updates an existing record in the database that matches the ID of the given entity.
     * All fields (except "id") are updated based on the current state of the entity object.
     *
     * @param entity The entity containing updated values.
     * @return The updated entity if the operation was successful, otherwise {@code null}.
     * @throws IllegalStateException if the entity class does not contain an "id" field.
     */
    public T update(T entity) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            Field[] fields = type.getDeclaredFields();

            for (Field f : fields) {
                f.setAccessible(true);
            }

            Field idField = Arrays.stream(fields)
                    .filter(f -> f.getName().equalsIgnoreCase("id"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No ID field found"));

            Object idValue = idField.get(entity);

            List<Field> nonIdFields = Arrays.stream(fields)
                    .filter(f -> !f.getName().equalsIgnoreCase("id"))
                    .toList();

            List<String> assignments = nonIdFields.stream()
                    .map(f -> f.getName() + " = ?")
                    .toList();

            List<Object> values = nonIdFields.stream()
                    .map(f -> {
                        try {
                            return f.get(entity);
                        } catch (IllegalAccessException e) {
                            LOGGER.log(Level.WARNING, "Error accessing field value", e);
                            return null;
                        }
                    })
                    .toList();

            String query = String.format("UPDATE %s SET %s WHERE id = ?", tableName, String.join(", ", assignments));

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < values.size(); i++) {
                    statement.setObject(i + 1, values.get(i));
                }
                statement.setObject(values.size() + 1, idValue);

                int affected = statement.executeUpdate();
                return affected > 0 ? entity : null;
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in update", e);
        }
        return null;
    }

    /**
     * Deletes the record with the specified ID from the database.
     *
     * @param id The ID of the record to delete.
     * @return {@code true} if a record was successfully deleted; {@code false} otherwise.
     */
    public boolean delete(int id) {
        String query = "DELETE FROM " + tableName + " WHERE id = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            int affected = statement.executeUpdate();
            return affected > 0;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in delete", e);
        }
        return false;
    }

    /**
     * Maps the current row of the given {@link ResultSet} to an instance of type {@code T}.
     * It uses Java Reflection to set each field's value based on the column name.
     * Handles common type conversions, such as {@link java.math.BigDecimal} to {@code double} and
     * {@link java.sql.Date} to {@code java.time.LocalDate}.
     *
     * @param result The {@link ResultSet} positioned at the row to map.
     * @return An instance of type {@code T} with fields populated from the result set.
     * @throws Exception If instantiation or reflection access fails.
     */
    private T mapResultSetToEntity(ResultSet result) throws Exception {
        T instance = type.getDeclaredConstructor().newInstance();

        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            String columnName = field.getName();

            try {
                Object value;
                try {
                    value = result.getObject(columnName);
                } catch (SQLException e) {
                    String snakeCase = columnName.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
                    value = result.getObject(snakeCase);
                }

                if (value != null) {
                    if (field.getType().equals(double.class) && value instanceof BigDecimal) {
                        value = ((BigDecimal) value).doubleValue();
                    }
                    if (field.getType().equals(int.class) && value instanceof Number) {
                        value = ((Number) value).intValue();
                    }
                    if (field.getType().getName().equals("java.time.LocalDate") && value instanceof Date) {
                        value = ((Date) value).toLocalDate();
                    }
                    field.set(instance, value);
                }

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error setting field '" + columnName + "'", e);
            }
        }

        return instance;
    }
}
