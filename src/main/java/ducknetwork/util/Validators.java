package ducknetwork.util;

import ducknetwork.exceptions.DomainExceptions;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Simple validation utilities using Strategy-like lambdas.
 */
public final class Validators {
    private Validators() {}

    /**
     * Validate email contains '@' and a '.' after it (simple check).
     */
    public static final Consumer<String> EMAIL_VALIDATOR = (email) -> {
        if (email == null || email.trim().isEmpty()) throw new DomainExceptions.ValidationException("Email required");
        String e = email.trim();
        int at = e.indexOf('@');
        if (at <= 0 || at == e.length()-1 || e.indexOf('.', at) < 0)
            throw new DomainExceptions.ValidationException("Invalid email format");
    };

    /**
     * Validate username length >= 3 and no spaces-only.
     */
    public static final Consumer<String> USERNAME_VALIDATOR = (username) -> {
        if (username == null || username.trim().length() < 3)
            throw new DomainExceptions.ValidationException("Username must be at least 3 characters");
    };

    /**
     * Helper to run a validator and rethrow as ValidationException if needed.
     */
    public static void validate(Consumer<String> validator, String value) {
        validator.accept(value);
    }
}
