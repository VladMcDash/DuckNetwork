package ducknetwork.domain;

/**
 * Definește tipurile posibile de Duck, folosite pentru a popula ComboBox-urile
 * și pentru a asigura consistența datelor.
 */
public enum DuckType {
    SWIMMING,
    FLYING,
    FLYING_AND_SWIMMING;

    @Override
    public String toString() {
        return name();
    }
}