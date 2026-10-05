package lk.petcare;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
class ValidationTest {
 @Test void moneyUsesExactDecimalValues() throws Exception { assertEquals(new BigDecimal("2500.10"),Validation.money("2500.10")); }
 @Test void rejectsInvalidMoney() { for(String x:new String[]{"-1","NaN","1.001","100000000",""}) assertThrows(ValidationException.class,()->Validation.money(x)); }
 @Test void rejectsBlankNames() { assertThrows(ValidationException.class,()->Validation.required("  ","Name",100)); }
 @Test void phoneAcceptsLocalAndInternationalFormats() throws Exception { assertEquals("0771234567",Validation.phone("0771234567")); assertEquals("+94 77 1234567",Validation.phone("+94 77 1234567")); }
 @Test void phoneRejectsLettersAndTooFewDigits() { assertThrows(ValidationException.class,()->Validation.phone("phone123")); assertThrows(ValidationException.class,()->Validation.phone("123----")); }
 @Test void rejectsPastAndOffGridSlots() { assertThrows(ValidationException.class,()->Validation.appointment("2000-01-01T09:00")); assertThrows(ValidationException.class,()->Validation.appointment(LocalDate.now().plusDays(1)+"T09:15")); }
 @Test void acceptsFutureHalfHourSlot() throws Exception { assertEquals(30,Validation.appointment(LocalDate.now().plusDays(1)+"T09:30").getMinute()); }
}
