package lk.petcare;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.*;
/** The same panel is used by the live login window and headless UI previews. */
public final class AuthPanel extends JPanel {
    private static final long serialVersionUID=1L;
    private final JTextField fullName=new JTextField(),username=new JTextField();
    private final JPasswordField password=new JPasswordField(),confirmPassword=new JPasswordField();
    private final JLabel title=Theme.label("Welcome back",28,true,Theme.INK),subtitle=Theme.label("Sign in to manage your clinic.",13,false,Theme.MUTED);
    private final JLabel feedback=Theme.label(" ",12,false,new Color(165,54,54));
    private final JPanel nameRow,confirmRow,form;
    private final JButton submit,switchMode;
    private final Consumer<AuthService.User> signedIn;
    private boolean register,busy;
    private int failedAttempts;
    private long retryAfter;
    public AuthPanel(Consumer<AuthService.User> signedIn) {
        super(new GridLayout(1,2)); this.signedIn=signedIn;
        JPanel brand=new JPanel(new BorderLayout(0,18)); brand.setBackground(new Color(231,239,221)); brand.setBorder(BorderFactory.createEmptyBorder(40,42,30,42));
        JPanel top=new JPanel(new GridLayout(0,1,0,7)); top.setOpaque(false);
        top.add(Theme.label("PETCARE",27,true,Theme.NAVY)); top.add(Theme.label("VETERINARY CLINIC",11,true,Theme.MUTED)); brand.add(top,BorderLayout.NORTH);
        JPanel story=new JPanel(new BorderLayout(0,10)); story.setOpaque(false);
        JPanel caption=new JPanel(new GridLayout(0,1,0,8)); caption.setOpaque(false); caption.add(Theme.label("Care for every companion",25,true,Theme.INK));
        JLabel description=new JLabel("<html><div style='width:290px'>Appointments, treatment and payments in one organised clinic workspace.</div></html>"); description.setForeground(Theme.MUTED); description.setFont(new Font("Dialog",Font.PLAIN,14)); caption.add(description); story.add(caption,BorderLayout.NORTH);
        story.add(new PetArtwork(420,330));
        JPanel features=new JPanel(new GridLayout(0,1,0,10)); features.setOpaque(false); features.add(Theme.label("Appointments  /  Treatment  /  Reports",12,true,Theme.NAVY)); features.add(Theme.label("Built for your veterinary team",12,false,Theme.MUTED)); story.add(features,BorderLayout.SOUTH); brand.add(story);
        JCheckBox motion=new JCheckBox("Enable animations",PetArtwork.isAnimationsEnabled()); motion.setOpaque(false); motion.setForeground(Theme.MUTED); motion.addActionListener(e -> PetArtwork.setAnimationsEnabled(motion.isSelected())); brand.add(motion,BorderLayout.SOUTH); add(brand);
        JPanel right=new JPanel(new GridBagLayout()); right.setBackground(Theme.BG);
        form=Theme.card(new BorderLayout(0,20)); form.setPreferredSize(new Dimension(420,480));
        JPanel heading=new JPanel(new GridLayout(0,1,0,8)); heading.setOpaque(false); heading.add(title); heading.add(subtitle); form.add(heading,BorderLayout.NORTH);
        JPanel fields=new JPanel(); fields.setOpaque(false); fields.setLayout(new BoxLayout(fields,BoxLayout.Y_AXIS));
        nameRow=input("Full name",fullName); fields.add(nameRow); fields.add(input("Username",username)); fields.add(input("Password",password));
        confirmRow=input("Confirm password",confirmPassword); fields.add(confirmRow);
        JCheckBox show=new JCheckBox("Show password"); show.setOpaque(false); char echo=password.getEchoChar(); show.addActionListener(e -> { password.setEchoChar(show.isSelected()?(char)0:echo); confirmPassword.setEchoChar(show.isSelected()?(char)0:echo); });
        show.setAlignmentX(LEFT_ALIGNMENT); fields.add(show); fields.add(Box.createVerticalStrut(10));
        feedback.setAlignmentX(LEFT_ALIGNMENT); fields.add(feedback); fields.add(Box.createVerticalStrut(16));
        submit=Theme.primary("Sign in",this::submit); submit.setAlignmentX(LEFT_ALIGNMENT); submit.setMaximumSize(new Dimension(Integer.MAX_VALUE,44)); fields.add(submit);
        form.add(fields); switchMode=Theme.button("New here? Create an account",() -> setRegistration(!register)); form.add(switchMode,BorderLayout.SOUTH);
        right.add(form); add(right); password.addActionListener(e -> submit()); confirmPassword.addActionListener(e -> submit()); setRegistration(false);
    }
    private static JPanel input(String label,JTextField field) {
        JPanel p=new JPanel(new BorderLayout(0,7)); p.setOpaque(false); p.setAlignmentX(LEFT_ALIGNMENT); p.setMaximumSize(new Dimension(Integer.MAX_VALUE,78));
        p.add(Theme.label(label,12,true,Theme.INK),BorderLayout.NORTH); field.setPreferredSize(new Dimension(300,40)); p.add(field); p.setBorder(BorderFactory.createEmptyBorder(0,0,14,0)); return p;
    }
    public void setRegistration(boolean value) {
        if(busy) return; register=value; form.setPreferredSize(new Dimension(420,value?620:480)); nameRow.setVisible(value); confirmRow.setVisible(value);
        title.setText(value?"Create your account":"Welcome back"); subtitle.setText(value?"Choose a passphrase of at least 15 characters.":"Sign in to manage your clinic.");
        submit.setText(value?"Create account":"Sign in"); switchMode.setText(value?"Already registered? Sign in":"New here? Create an account"); feedback.setText(" ");
        password.setText(""); confirmPassword.setText(""); revalidate(); repaint();
    }
    private void submit() {
        if(busy) return;
        if(!register && System.currentTimeMillis()<retryAfter) { feedback.setText("Please wait a moment before trying again."); return; }
        final boolean create=register; final String name=fullName.getText(),login=username.getText();
        final char[] pass=password.getPassword(),confirmation=confirmPassword.getPassword();
        busy=true; submit.setEnabled(false); switchMode.setEnabled(false); feedback.setForeground(Theme.MUTED); feedback.setText(create?"Creating your account…":"Signing in…");
        new SwingWorker<AuthService.User,Void>() {
            protected AuthService.User doInBackground() throws Exception {
                try { AuthService auth=new AuthService(); return create?auth.register(name,login,pass,confirmation):auth.login(login,pass); }
                finally { Arrays.fill(pass,'\0'); Arrays.fill(confirmation,'\0'); }
            }
            protected void done() {
                busy=false; submit.setEnabled(true); switchMode.setEnabled(true); password.setText(""); confirmPassword.setText("");
                try { AuthService.User user=get(); failedAttempts=0; signedIn.accept(user); }
                catch(Exception ex) {
                    Throwable cause=ex instanceof java.util.concurrent.ExecutionException?ex.getCause():ex;
                    String text;
                    if(cause instanceof ValidationException) { text=cause.getMessage(); if(!create && ++failedAttempts>=5) { retryAfter=System.currentTimeMillis()+30_000; failedAttempts=0; text="Too many attempts. Try again in 30 seconds."; } }
                    else if(cause instanceof SQLException sql) {
                        Logger.getLogger("petcare").log(Level.WARNING,"Authentication database failure",sql);
                        text=switch(sql.getErrorCode()) { case 1062 -> "Username already exists. Choose another."; case 1146 -> "Run the users table migration, then retry."; default -> "Cannot connect. Check MySQL and database settings."; };
                        if(sql.getMessage().startsWith("Set PETCARE")) text="Set PETCARE_DB_PASSWORD and restart NetBeans.";
                    } else { Logger.getLogger("petcare").log(Level.WARNING,"Authentication failed",cause); text="Sign-in failed. Check the application log."; }
                    feedback.setForeground(new Color(165,54,54)); feedback.setText("<html><div style='width:330px'>"+html(text)+"</div></html>");
                }
            }
        }.execute();
    }
    private static String html(String text) { return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
}
