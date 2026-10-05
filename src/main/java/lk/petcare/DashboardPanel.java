package lk.petcare;
import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;
public final class DashboardPanel extends JPanel {
    private static final long serialVersionUID=1L;
    private final JLabel[] metrics=new JLabel[4];
    public final JTable today=new JTable();
    public DashboardPanel(Runnable refresh,IntConsumer navigate,Runnable book) {
        super(new BorderLayout(0,22)); setBorder(BorderFactory.createEmptyBorder(20,22,22,22)); setBackground(Theme.BG);
        JPanel upper=new JPanel(new BorderLayout(0,20)); upper.setOpaque(false);
        JPanel heading=new JPanel(new BorderLayout()); heading.setOpaque(false);
        JPanel title=new JPanel(new GridLayout(0,1,0,6)); title.setOpaque(false); title.add(Theme.label("Clinic overview",25,true,Theme.INK)); title.add(Theme.label("Your appointments, clients and daily receipts at a glance.",13,false,Theme.MUTED));
        heading.add(title); heading.add(Theme.button("Refresh overview",refresh),BorderLayout.EAST); upper.add(heading,BorderLayout.NORTH);
        JPanel cards=new JPanel(new GridLayout(1,4,16,0)); cards.setOpaque(false);
        String[] names={"REGISTERED CLIENTS","PETS IN OUR CARE","TODAY'S BOOKINGS","RECEIVED TODAY · LKR"};
        for(int i=0;i<4;i++) { JPanel card=Theme.card(new BorderLayout(0,12)); card.add(Theme.label(names[i],10,true,Theme.MUTED),BorderLayout.NORTH); metrics[i]=Theme.label("—",i==3?24:30,true,Theme.INK); card.add(metrics[i]); cards.add(card); }
        JPanel overview=new JPanel(new BorderLayout(0,18)); overview.setOpaque(false);
        JPanel welcome=Theme.card(new BorderLayout(20,0)); welcome.setBackground(new Color(235,242,226));
        JPanel copy=new JPanel(new GridLayout(0,1,0,8)); copy.setOpaque(false); copy.add(Theme.label("A thoughtful start to every visit",19,true,Theme.NAVY)); copy.add(Theme.label("Keep your patients, appointments and care records organised.",12,false,Theme.MUTED)); welcome.add(copy); welcome.add(new PetArtwork(150,64),BorderLayout.EAST);
        overview.add(welcome,BorderLayout.NORTH); overview.add(cards); upper.add(overview); add(upper,BorderLayout.NORTH);
        JPanel visits=Theme.card(new BorderLayout(0,16)); JPanel vh=new JPanel(new BorderLayout()); vh.setOpaque(false); vh.add(Theme.label("Today's appointments",18,true,Theme.INK)); vh.add(Theme.primary("+ Book appointment",book),BorderLayout.EAST); visits.add(vh,BorderLayout.NORTH);
        Theme.styleTable(today); today.setFillsViewportHeight(true); today.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); JScrollPane scroll=new JScrollPane(today); scroll.setColumnHeaderView(today.getTableHeader()); visits.add(scroll); visits.add(Theme.label("Select Appointments to record treatment, payments or cancellations.",12,false,Theme.MUTED),BorderLayout.SOUTH); add(visits);
        JPanel quick=Theme.card(new BorderLayout(0,12)); JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); buttons.setOpaque(false);
        buttons.add(Theme.button("Manage clients",() -> navigate.accept(1))); buttons.add(Theme.button("Register pets",() -> navigate.accept(2))); buttons.add(Theme.button("Visit reports",() -> navigate.accept(6))); quick.add(buttons); add(quick,BorderLayout.SOUTH);
    }
    public void setMetrics(Object[] values) {
        for(int i=0;i<metrics.length;i++) metrics[i].setText(i==3?String.format(java.util.Locale.US,"%,.2f",new java.math.BigDecimal(values[i].toString())):values[i].toString());
    }
}
