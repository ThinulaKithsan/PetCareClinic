package lk.petcare;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PasswordHasherTest {
 @Test void correctPasswordVerifiesAndWrongPasswordFails() throws Exception {
  var stored=PasswordHasher.hash("My clinic passphrase 2026".toCharArray());
  assertTrue(PasswordHasher.verify("My clinic passphrase 2026".toCharArray(),stored));
  assertFalse(PasswordHasher.verify("Wrong clinic passphrase".toCharArray(),stored));
 }
 @Test void identicalPasswordsReceiveDifferentSalts() throws Exception {
  var first=PasswordHasher.hash("My clinic passphrase 2026".toCharArray());
  var second=PasswordHasher.hash("My clinic passphrase 2026".toCharArray());
  assertNotEquals(first.salt(),second.salt()); assertNotEquals(first.hash(),second.hash());
 }
 @Test void usernameIsNormalizedAndValidated() throws Exception {
  assertEquals("rasika",AuthService.username(" Rasika "));
  assertThrows(ValidationException.class,()->AuthService.username("r"));
  assertThrows(ValidationException.class,()->AuthService.username("name with spaces"));
 }
}
