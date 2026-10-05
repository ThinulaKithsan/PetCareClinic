package lk.petcare;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public final class Validation {
    private Validation() {}
    public static String required(String value, String label, int max) throws ValidationException {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty() || text.length() > max)
            throw new ValidationException(label + " is required and must be at most " + max + " characters.");
        return text;
    }
    public static String phone(String value) throws ValidationException {
        String text = required(value, "Phone", 20);
        if (!text.matches("\\+?[0-9 ()-]{7,20}") || text.replaceAll("\\D", "").length() < 7)
            throw new ValidationException("Enter a valid phone number with at least 7 digits.");
        return text;
    }
    public static BigDecimal money(String value) throws ValidationException {
        try {
            BigDecimal amount = new BigDecimal(value.trim());
            if (amount.signum() < 0 || amount.scale() > 2 || amount.compareTo(new BigDecimal("99999999.99")) > 0)
                throw new NumberFormatException();
            return amount.setScale(2);
        } catch (RuntimeException ex) { throw new ValidationException("Enter a non-negative amount with at most two decimal places."); }
    }
    public static LocalDateTime appointment(String value) throws ValidationException {
        try {
            LocalDateTime time = LocalDateTime.parse(value.trim());
            if (time.isBefore(LocalDateTime.now())) throw new ValidationException("New appointments must be in the future.");
            if (time.getMinute() % 30 != 0 || time.getSecond() != 0 || time.getNano() != 0)
                throw new ValidationException("Use a 30-minute slot, for example 2026-10-05T09:30.");
            return time;
        } catch (java.time.format.DateTimeParseException ex) {
            throw new ValidationException("Use date and time in YYYY-MM-DDTHH:MM format.");
        }
    }
}
