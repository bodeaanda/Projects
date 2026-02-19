package BusinessLogic.Validators;

/**
 * Generic interface for validating objects.
 *
 * @param <T> the type of object to validate
 */
public interface Validator<T> {

    /**
     * Validates the given object.
     *
     * @param t the object to validate
     * @throws IllegalArgumentException if the object is not valid
     */
    void validate(T t);
}

