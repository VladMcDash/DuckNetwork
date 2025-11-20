package ducknetwork.exceptions;

/**
 * Custom domain exceptions for DuckSocialNetwork.
 * Used to signal validation errors, missing users, etc.
 */
public class DomainExceptions {

    /**  Eroare generala de validare (username/email invalid etc.) */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    /**  Cand un utilizator nu exista in rețea */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    /**  Cand se incearca o acțiune interzisa */
    public static class IllegalActionException extends RuntimeException {
        public IllegalActionException(String message) {
            super(message);
        }
    }

    /**  Cand ceva legat de evenimente e invalid */
    public static class EventException extends RuntimeException {
        public EventException(String message) {
            super(message);
        }
    }

    /**  Cand un card nu poate fi procesat*/
    public static class CardException extends RuntimeException {
        public CardException(String message) {
            super(message);
        }
    }
}
