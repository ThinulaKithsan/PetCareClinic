package lk.petcare;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
public final class Theme {
    public static final Color NAVY = new Color(37,69,58), TEAL = new Color(53,105,81), BG = new Color(248,248,241),
        INK = new Color(36,52,44), MUTED = new Color(102,116,105), LINE = new Color(222,228,216);
    private Theme() {}
    public static void install() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch(Exception ignored) { /* Standard Swing fallback. */ }
        Font font = new Font("Dialog",Font.PLAIN,13);
        for(Object key : java.util.Collections.list(UIManager.getDefaults().keys()))
            if(UIManager.get(key) instanceof Font) UIManager.put(key,font);
        UIManager.put("Panel.background",BG); UIManager.put("Label.foreground",INK);
        UIManager.put("TextField.background",Color.WHITE); UIManager.put("PasswordField.background",Color.WHITE);
        UIManager.put("TextField.border",new CompoundBorder(new LineBorder(LINE),new EmptyBorder(9,10,9,10)));
        UIManager.put("PasswordField.border",new CompoundBorder(new LineBorder(LINE),new EmptyBorder(9,10,9,10)));
        UIManager.put("ComboBox.background",Color.WHITE); UIManager.put("Table.gridColor",LINE);
        UIManager.put("Table.selectionBackground",new Color(227,239,220)); UIManager.put("Table.selectionForeground",INK);
        UIManager.put("ScrollPane.border",new LineBorder(LINE)); UIManager.put("OptionPane.background",BG);
    }
    public static JLabel label(String text,int size,boolean bold,Color color) {
        JLabel l = new JLabel(text); l.setFont(new Font("Dialog",bold?Font.BOLD:Font.PLAIN,size)); l.setForeground(color); return l;
    }
    public static JButton button(String text,Runnable action) {
        JButton b=new JButton(text); b.setFont(new Font("SansSerif",Font.BOLD,12)); b.setBackground(Color.WHITE); b.setForeground(INK);
        b.setFocusPainted(false); b.setBorder(new CompoundBorder(new LineBorder(LINE),new EmptyBorder(10,14,10,14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            private Color normal;
            public void mouseEntered(java.awt.event.MouseEvent e) { if(b.isEnabled() && !Boolean.TRUE.equals(b.getClientProperty("navigation"))) { normal=b.getBackground(); b.setBackground(normal.equals(Color.WHITE)?new Color(239,244,231):normal.brighter()); } }
            public void mouseExited(java.awt.event.MouseEvent e) { if(normal!=null) { b.setBackground(normal); normal=null; } }
        }); b.addActionListener(e -> action.run()); return b;
    }
    public static JButton primary(String text,Runnable action) { JButton b=button(text,action); b.setBackground(TEAL); b.setForeground(Color.WHITE); b.setBorder(new EmptyBorder(11,18,11,18)); return b; }
    public static JPanel card(LayoutManager layout) {
        JPanel p=new JPanel(layout) {
            private static final long serialVersionUID=1L;
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,18,18);
                g2.setColor(LINE); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,18,18); g2.dispose();
            }
        }; p.setOpaque(false); p.setBorder(new EmptyBorder(20,20,20,20)); return p;
    }
    public static void styleTable(JTable t) {
        t.setRowHeight(38); t.setShowVerticalLines(false); t.setIntercellSpacing(new Dimension(0,1));
        t.setFont(new Font("SansSerif",Font.PLAIN,12)); t.setBackground(Color.WHITE);
        t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12)); t.getTableHeader().setBackground(new Color(237,241,229));
        t.getTableHeader().setForeground(INK); t.getTableHeader().setPreferredSize(new Dimension(0,38)); t.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer renderer=new DefaultTableCellRenderer() {
            private static final long serialVersionUID=1L;
            public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int column) {
                super.getTableCellRendererComponent(table,value,selected,focus,row,column);
                setBorder(new EmptyBorder(7,10,7,10));
                setBackground(selected?new Color(227,239,220):(row%2==0?Color.WHITE:new Color(250,251,246)));
                setForeground(INK);
                if(value instanceof java.math.BigDecimal amount) { setText(String.format(java.util.Locale.US,"%,.2f",amount)); setHorizontalAlignment(RIGHT); }
                else { setHorizontalAlignment(LEFT); }
                if("BOOKED".equals(value)) setForeground(TEAL);
                if("CANCELLED".equals(value)) setForeground(new Color(156,74,53));
                if("COMPLETED".equals(value)) setForeground(new Color(48,104,164));
                return this;
            }
        };
        t.setDefaultRenderer(Object.class,renderer); t.setDefaultRenderer(Number.class,renderer);
    }
}
