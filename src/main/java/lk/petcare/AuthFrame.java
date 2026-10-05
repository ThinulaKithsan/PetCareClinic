package lk.petcare;
import javax.swing.*;
import java.awt.*;
public final class AuthFrame extends JFrame {
    private static final long serialVersionUID=1L;
    public AuthFrame() {
        super("PetCare Clinic | Sign in"); setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new AuthPanel(user -> {
            dispose(); new ClinicFrame(user,() -> new AuthFrame().setVisible(true)).setVisible(true);
        }));
        setSize(1060,760); setMinimumSize(new Dimension(1000,740)); setLocationRelativeTo(null);
    }
}
