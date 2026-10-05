package lk.petcare;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.HexFormat;
public final class PasswordHasher {
    public static final int ITERATIONS=600_000;
    public record Stored(String salt,String hash,int iterations) {}
    private PasswordHasher() {}
    public static Stored hash(char[] password) throws GeneralSecurityException {
        byte[] salt=new byte[16]; new SecureRandom().nextBytes(salt);
        return new Stored(HexFormat.of().formatHex(salt),HexFormat.of().formatHex(derive(password,salt,ITERATIONS)),ITERATIONS);
    }
    public static boolean verify(char[] password,Stored stored) throws GeneralSecurityException {
        if(stored.iterations()<100_000 || stored.iterations()>2_000_000) throw new GeneralSecurityException("Invalid password work factor.");
        try {
            byte[] salt=HexFormat.of().parseHex(stored.salt()),expected=HexFormat.of().parseHex(stored.hash());
            if(salt.length!=16 || expected.length!=32) throw new GeneralSecurityException("Invalid stored password.");
            return MessageDigest.isEqual(expected,derive(password,salt,stored.iterations()));
        } catch(IllegalArgumentException ex) { throw new GeneralSecurityException("Invalid stored password.",ex); }
    }
    private static byte[] derive(char[] password,byte[] salt,int iterations) throws GeneralSecurityException {
        PBEKeySpec spec=new PBEKeySpec(password,salt,iterations,256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }
}
