package lk.petcare;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
/** Service layer owns business rules and atomic treatment and payment operations. */
public final class ClinicService {
    public void book(long pet, long service, long vet, LocalDateTime slot) throws SQLException, ValidationException {
        Validation.appointment(slot.toString());
        try (Connection c = Database.open()) {
            c.setAutoCommit(false);
            try {
                // Lock a stable parent row so concurrent bookings for this vet serialize.
                try (PreparedStatement p = c.prepareStatement("SELECT id FROM vets WHERE id=? FOR UPDATE")) {
                    p.setLong(1, vet);
                    try (ResultSet r = p.executeQuery()) { if (!r.next()) throw new ValidationException("Select an existing vet."); }
                }
                try (PreparedStatement p = c.prepareStatement("SELECT id FROM appointments WHERE vet_id=? AND slot=? AND status<>'CANCELLED'")) {
                    ClinicDao.bind(p, vet, Timestamp.valueOf(slot));
                    try (ResultSet r = p.executeQuery()) { if (r.next()) throw new ValidationException("This vet already has an appointment in that slot."); }
                }
                try (PreparedStatement p = c.prepareStatement("INSERT INTO appointments(pet_id,customer_id,service_id,vet_id,slot,fee) SELECT p.id,p.customer_id,s.id,?,?,s.price FROM services s JOIN pets p ON p.id=? WHERE s.id=? AND s.active=1")) {
                    ClinicDao.bind(p, vet, Timestamp.valueOf(slot), pet, service);
                    if (p.executeUpdate() != 1) throw new ValidationException("Select an active service.");
                }
                c.commit();
            } catch (SQLException | ValidationException ex) { c.rollback(); throw ex; }
        }
    }
    public void complete(long appointment, String diagnosis, String medication, BigDecimal paid) throws SQLException, ValidationException {
        diagnosis = Validation.required(diagnosis, "Diagnosis", 1000);
        if (medication.length() > 1000) throw new ValidationException("Medication must be at most 1000 characters.");
        try (Connection c = Database.open()) {
            c.setAutoCommit(false);
            try {
                BigDecimal fee;
                try (PreparedStatement p = c.prepareStatement("SELECT fee,status FROM appointments WHERE id=? FOR UPDATE")) {
                    p.setLong(1, appointment);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || !r.getString("status").equals("BOOKED"))
                            throw new ValidationException("Only a booked appointment can be completed.");
                        fee = r.getBigDecimal("fee");
                    }
                }
                if (paid.signum() < 0 || paid.compareTo(fee) > 0)
                    throw new ValidationException("Payment must be between zero and the appointment fee.");
                try (PreparedStatement p = c.prepareStatement("INSERT INTO treatments(appointment_id,diagnosis,medication) VALUES(?,?,?)")) {
                    ClinicDao.bind(p, appointment, diagnosis, medication.trim()); p.executeUpdate();
                }
                if (paid.signum() > 0) {
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO payments(appointment_id,amount) VALUES(?,?)")) {
                        ClinicDao.bind(p, appointment, paid); p.executeUpdate();
                    }
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE appointments SET status='COMPLETED' WHERE id=?")) {
                    p.setLong(1, appointment); p.executeUpdate();
                }
                c.commit();
            } catch (SQLException | ValidationException ex) { c.rollback(); throw ex; }
        }
    }
    public void payBalance(long appointment, BigDecimal amount) throws SQLException, ValidationException {
        try (Connection c = Database.open()) {
            c.setAutoCommit(false);
            try {
                BigDecimal fee;
                try (PreparedStatement p = c.prepareStatement("SELECT fee,status FROM appointments WHERE id=? FOR UPDATE")) {
                    p.setLong(1, appointment);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || !r.getString("status").equals("COMPLETED")) throw new ValidationException("Complete treatment before recording a balance payment.");
                        fee = r.getBigDecimal("fee");
                    }
                }
                BigDecimal already;
                try (PreparedStatement p = c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM payments WHERE appointment_id=?")) {
                    p.setLong(1, appointment);
                    try (ResultSet r = p.executeQuery()) { r.next(); already = r.getBigDecimal(1); }
                }
                if (amount.signum() <= 0 || amount.compareTo(fee.subtract(already)) > 0)
                    throw new ValidationException("Payment must be positive and must not exceed the remaining balance.");
                try (PreparedStatement p = c.prepareStatement("INSERT INTO payments(appointment_id,amount) VALUES(?,?)")) {
                    ClinicDao.bind(p, appointment, amount); p.executeUpdate();
                }
                c.commit();
            } catch (SQLException | ValidationException ex) { c.rollback(); throw ex; }
        }
    }
}
