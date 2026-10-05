package lk.petcare;
import java.sql.*;
import java.security.GeneralSecurityException;
import java.util.Locale;
public final class AuthService {
    public record User(long id,String name,String username) {}
    public static String username(String value) throws ValidationException {
        String name=Validation.required(value,"Username",40).toLowerCase(Locale.ROOT);
        if(!name.matches("[a-z0-9._]{3,40}")) throw new ValidationException("Username must contain 3–40 letters, numbers, dots or underscores.");
        return name;
    }
    public User register(String fullName,String username,char[] password,char[] confirm) throws SQLException,ValidationException,GeneralSecurityException {
        String name=Validation.required(fullName,"Full name",100),login=username(username);
        if(password.length<15 || password.length>128) throw new ValidationException("Use a password or passphrase of 15–128 characters.");
        if(!java.util.Arrays.equals(password,confirm)) throw new ValidationException("Passwords do not match.");
        PasswordHasher.Stored stored=PasswordHasher.hash(password);
        try(Connection c=Database.open(); PreparedStatement p=c.prepareStatement("INSERT INTO users(full_name,username,password_salt,password_hash,password_iterations) VALUES(?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            ClinicDao.bind(p,name,login,stored.salt(),stored.hash(),stored.iterations()); p.executeUpdate();
            try(ResultSet keys=p.getGeneratedKeys()) { if(!keys.next()) throw new SQLException("Account ID not returned."); return new User(keys.getLong(1),name,login); }
        }
    }
    public User login(String username,char[] password) throws SQLException,ValidationException,GeneralSecurityException {
        String login=username(username);
        if(password.length==0 || password.length>128) throw new ValidationException("Enter your username and password.");
        try(Connection c=Database.open();PreparedStatement p=c.prepareStatement("SELECT id,full_name,username,password_salt,password_hash,password_iterations FROM users WHERE username=?")) {
            p.setString(1,login);
            try(ResultSet rows=p.executeQuery()) {
                if(!rows.next()) throw new ValidationException("Username or password is incorrect.");
                PasswordHasher.Stored stored=new PasswordHasher.Stored(rows.getString("password_salt"),rows.getString("password_hash"),rows.getInt("password_iterations"));
                if(!PasswordHasher.verify(password,stored)) throw new ValidationException("Username or password is incorrect.");
                return new User(rows.getLong("id"),rows.getString("full_name"),rows.getString("username"));
            }
        }
    }
}
