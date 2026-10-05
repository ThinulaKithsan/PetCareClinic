package lk.petcare;
import javax.swing.SwingUtilities;
public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { Theme.install(); new AuthFrame().setVisible(true); });
    }
}
