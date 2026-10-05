package lk.petcare;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.*;
public final class ClinicFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final ClinicDao dao = new ClinicDao();
    private final ClinicService service = new ClinicService();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel status = new JLabel("Ready — prices and payments in LKR");
    private final DashboardPanel dashboard = new DashboardPanel(this::refreshDashboard,i -> tabs.setSelectedIndex(i),this::book);
    private final java.util.List<JButton> navigation = new java.util.ArrayList<>();
    private final JTable appointments = table();
    private final JTextField search = new JTextField(24);
    private boolean busy;
    public ClinicFrame(AuthService.User user, Runnable logout) {
        super("PetCare Clinic | Clinic workspace");
        java.util.Objects.requireNonNull(user,"Authenticated user required");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320,840); setMinimumSize(new Dimension(1180,760)); setLocationRelativeTo(null);
        tabs.addTab("Overview",dashboard);
        tabs.addTab("Clients",master("Customers")); tabs.addTab("Pets",master("Pets"));
        tabs.addTab("Services",master("Services")); tabs.addTab("Vets",master("Vets")); tabs.addTab("Appointments",appointmentsPanel()); tabs.addTab("Reports",reportsPanel());
        // JTabbedPane retains page-change events while the sidebar supplies navigation.
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            protected int calculateTabAreaHeight(int placement,int runs,int maxHeight) { return 0; }
            protected void paintTabArea(Graphics g,int placement,int selected) { }
            protected void paintContentBorder(Graphics g,int placement,int selected) { }
        });
        JPanel shell=new JPanel(new BorderLayout()); shell.setBackground(Theme.BG);
        shell.add(sidebar(user),BorderLayout.WEST);
        JPanel workspace=new JPanel(new BorderLayout()); workspace.setOpaque(false);
        JPanel header=new JPanel(new BorderLayout(16,0)); header.setBackground(Color.WHITE); header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,Theme.LINE),BorderFactory.createEmptyBorder(18,24,18,24)));
        JPanel greet=new JPanel(new GridLayout(0,1,0,5)); greet.setOpaque(false);
        greet.add(Theme.label("Welcome, "+user.name(),19,true,Theme.INK)); greet.add(Theme.label("PetCare Clinic  /  Team workspace",12,false,Theme.MUTED)); header.add(greet);
        JPanel tools=new JPanel(new FlowLayout(FlowLayout.RIGHT,14,0)); tools.setOpaque(false);
        JCheckBox motion=new JCheckBox("Animations",PetArtwork.isAnimationsEnabled()); motion.setOpaque(false); motion.setForeground(Theme.MUTED); motion.addActionListener(e -> PetArtwork.setAnimationsEnabled(motion.isSelected())); tools.add(motion);
        tools.add(Theme.label(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")),12,false,Theme.MUTED));
        tools.add(Theme.button("Log out",() -> {
            if(busy) { status.setText("Wait for the current operation before logging out."); return; }
            if(JOptionPane.showConfirmDialog(this,"Log out of your clinic workspace?","Log out",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) { dispose(); logout.run(); }
        })); header.add(tools,BorderLayout.EAST); workspace.add(header,BorderLayout.NORTH); workspace.add(tabs);
        status.setForeground(Theme.MUTED); status.setBorder(BorderFactory.createEmptyBorder(10,24,10,24)); workspace.add(status,BorderLayout.SOUTH);
        shell.add(workspace); setContentPane(shell);
        tabs.addChangeListener(e -> { updateNavigation(); if(tabs.getSelectedIndex()==0) refreshDashboard(); });
        updateNavigation(); SwingUtilities.invokeLater(this::refreshDashboard);
    }
    private JPanel sidebar(AuthService.User user) {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(Theme.NAVY); p.setPreferredSize(new Dimension(218,0)); p.setBorder(BorderFactory.createEmptyBorder(30,18,24,18));
        JPanel brand=new JPanel(new GridLayout(0,1,0,7)); brand.setOpaque(false); brand.add(Theme.label("PETCARE",26,true,Color.WHITE)); brand.add(Theme.label("VETERINARY CLINIC",10,true,new Color(190,215,171))); brand.setBorder(BorderFactory.createEmptyBorder(0,8,32,0)); p.add(brand,BorderLayout.NORTH);
        JPanel nav=new JPanel(); nav.setOpaque(false); nav.setLayout(new BoxLayout(nav,BoxLayout.Y_AXIS));
        String[] names={"Overview","Clients","Pets","Services","Veterinarians","Appointments","Reports"};
        for(int i=0;i<names.length;i++) { final int page=i; JButton b=Theme.button(names[i],() -> tabs.setSelectedIndex(page)); b.putClientProperty("navigation",Boolean.TRUE); b.setHorizontalAlignment(SwingConstants.LEFT); b.setAlignmentX(LEFT_ALIGNMENT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE,46)); navigation.add(b); nav.add(b); nav.add(Box.createVerticalStrut(9)); }
        p.add(nav); JPanel profile=new JPanel(new GridLayout(0,1,0,6)); profile.setOpaque(false); profile.add(Theme.label("SIGNED IN AS",10,true,new Color(167,195,156))); profile.add(Theme.label(user.username(),14,true,Color.WHITE)); profile.add(Theme.label("Clinic team member",11,false,new Color(183,205,172))); p.add(profile,BorderLayout.SOUTH); return p;
    }
    private void updateNavigation() {
        for(int i=0;i<navigation.size();i++) { JButton b=navigation.get(i); boolean selected=i==tabs.getSelectedIndex(); b.setBackground(selected?Theme.TEAL:Theme.NAVY); b.setForeground(selected?Color.WHITE:new Color(207,221,198)); b.setBorder(BorderFactory.createEmptyBorder(13,16,13,10)); }
    }
    private JPanel master(String entity) {
        JTable grid = table(); JTextField filter = new JTextField(22);
        JPanel panel = new JPanel(new BorderLayout(8,8)); panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        Runnable refresh = () -> task(() -> masterRows(entity, filter.getText().trim()), rows -> load(grid, rows));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT)); top.add(new JLabel("Search name")); top.add(filter);
        top.add(button("Search / Refresh", refresh)); filter.addActionListener(e -> refresh.run());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(button("Add", () -> editMaster(entity, grid, false, refresh)));
        actions.add(button("Edit selected", () -> editMaster(entity, grid, true, refresh)));
        actions.add(button("Delete selected", () -> {
            Long id = selectedId(grid); if (id == null) return;
            if (JOptionPane.showConfirmDialog(this, "Delete selected " + entity.toLowerCase() + " record? Related history prevents deletion.", "Confirm deletion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            String name = switch(entity) { case "Customers" -> "customers"; case "Pets" -> "pets"; case "Vets" -> "vets"; default -> "services"; };
            task(() -> { dao.execute("DELETE FROM " + name + " WHERE id=?", id); return true; }, ok -> refresh.run());
        }));
        JPanel heading=new JPanel(new BorderLayout(0,16)); heading.setOpaque(false);
        String title=entity.equals("Customers")?"Client directory":entity.equals("Pets")?"Pets in our care":entity.equals("Vets")?"Veterinary team":"Clinic services";
        heading.add(Theme.label(title,25,true,Theme.INK),BorderLayout.NORTH); heading.add(top);
        panel.setBorder(BorderFactory.createEmptyBorder(22,24,24,24)); panel.add(heading,BorderLayout.NORTH); panel.add(new JScrollPane(grid)); panel.add(actions,BorderLayout.SOUTH);
        tabs.addChangeListener(e -> { if (tabs.getSelectedComponent() == panel) refresh.run(); }); return panel;
    }
    private ClinicDao.Rows masterRows(String entity, String filter) throws SQLException {
        String like = "%" + filter + "%";
        return switch(entity) {
            case "Customers" -> dao.query("SELECT id AS ID,name AS Name,phone AS Phone,email AS Email,address AS Address FROM customers WHERE name LIKE ? ORDER BY id DESC", like);
            case "Pets" -> dao.query("SELECT p.id AS ID,p.name AS Name,p.species AS Species,p.breed AS Breed,p.birth_date AS Birth,c.name AS Owner,p.customer_id AS Owner_ID FROM pets p JOIN customers c ON c.id=p.customer_id WHERE p.name LIKE ? ORDER BY p.id DESC", like);
            case "Vets" -> dao.query("SELECT id AS ID,name AS Name FROM vets WHERE name LIKE ? ORDER BY name",like);
            default -> dao.query("SELECT id AS ID,name AS Name,price AS Price_LKR,active AS Active FROM services WHERE name LIKE ? ORDER BY id DESC", like);
        };
    }
    private void editMaster(String entity, JTable grid, boolean edit, Runnable refresh) {
        Long id = edit ? selectedId(grid) : null; if (edit && id == null) return;
        Object[] row = edit ? row(grid) : null;
        if(entity.equals("Vets")) {
            JTextField name=field(row,1); JPanel f=form(); addField(f,"Veterinarian name",name);
            if(!confirm(f,edit?"Edit veterinarian":"Add veterinarian")) return;
            try { String n=Validation.required(name.getText(),"Veterinarian name",100);
                task(() -> { if(edit) dao.execute("UPDATE vets SET name=? WHERE id=?",n,id); else dao.execute("INSERT INTO vets(name) VALUES(?)",n); return true; },ok -> refresh.run());
            } catch(ValidationException ex) { error(ex); } return;
        }
        if (entity.equals("Pets")) {
            task(() -> dao.choices("SELECT id,CONCAT(name,' (#',id,')') FROM customers ORDER BY name"), owners -> petDialog(id, row, owners, refresh)); return;
        }
        JTextField name = field(row, 1), second = field(row, 2), third = field(row, 3), fourth = field(row, 4);
        JCheckBox active = new JCheckBox("Available for new appointments", row == null || Boolean.TRUE.equals(row[3]) || "1".equals(String.valueOf(row[3])));
        JPanel form = form(); addField(form, "Name", name);
        if (entity.equals("Customers")) { addField(form,"Phone",second); addField(form,"Email optional",third); addField(form,"Address optional",fourth); }
        else { addField(form,"Price in LKR",second); addField(form,"Active",active); }
        if (!confirm(form, (edit ? "Edit " : "Add ") + entity)) return;
        try {
            String n = Validation.required(name.getText(), "Name",100);
            if (entity.equals("Customers")) {
                String phone = Validation.phone(second.getText()), email = third.getText().trim(), address = fourth.getText().trim();
                if (email.length() > 120 || (!email.isEmpty() && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))) throw new ValidationException("Enter a valid email, or leave it blank.");
                if (address.length() > 255) throw new ValidationException("Address must be at most 255 characters.");
                task(() -> { if (edit) dao.execute("UPDATE customers SET name=?,phone=?,email=?,address=? WHERE id=?",n,phone,email,address,id);
                             else dao.execute("INSERT INTO customers(name,phone,email,address) VALUES(?,?,?,?)",n,phone,email,address); return true; }, ok -> refresh.run());
            } else {
                java.math.BigDecimal price = Validation.money(second.getText());
                task(() -> { if (edit) dao.execute("UPDATE services SET name=?,price=?,active=? WHERE id=?",n,price,active.isSelected(),id);
                             else dao.execute("INSERT INTO services(name,price,active) VALUES(?,?,?)",n,price,active.isSelected()); return true; }, ok -> refresh.run());
            }
        } catch (ValidationException ex) { error(ex); }
    }
    private void petDialog(Long id, Object[] row, List<ClinicDao.Choice> owners, Runnable refresh) {
        if (owners.isEmpty()) { error(new ValidationException("Register a customer before registering a pet.")); return; }
        JTextField name=field(row,1), species=field(row,2), breed=field(row,3), birth=field(row,4);
        JComboBox<ClinicDao.Choice> owner=new JComboBox<>(owners.toArray(ClinicDao.Choice[]::new));
        if (row != null) for (ClinicDao.Choice choice:owners) if (choice.id()==((Number)row[6]).longValue()) owner.setSelectedItem(choice);
        JPanel f=form(); addField(f,"Pet name",name); addField(f,"Species",species); addField(f,"Breed optional",breed); addField(f,"Birth date YYYY-MM-DD optional",birth); addField(f,"Owner",owner);
        if (!confirm(f,id==null?"Register pet":"Edit pet")) return;
        try {
            String n=Validation.required(name.getText(),"Pet name",100), s=Validation.required(species.getText(),"Species",50), b=breed.getText().trim();
            if (b.length()>100) throw new ValidationException("Breed must be at most 100 characters.");
            LocalDate date=null;
            if (!birth.getText().isBlank()) { try { date=LocalDate.parse(birth.getText().trim()); } catch (RuntimeException ex) { throw new ValidationException("Use YYYY-MM-DD for birth date."); } }
            if (date!=null && date.isAfter(LocalDate.now())) throw new ValidationException("Birth date cannot be in the future.");
            LocalDate d=date; long ownerId=((ClinicDao.Choice)owner.getSelectedItem()).id();
            task(() -> { if(id==null) dao.execute("INSERT INTO pets(customer_id,name,species,breed,birth_date) VALUES(?,?,?,?,?)",ownerId,n,s,b,d);
                         else dao.execute("UPDATE pets SET customer_id=?,name=?,species=?,breed=?,birth_date=? WHERE id=?",ownerId,n,s,b,d,id); return true; }, ok -> refresh.run());
        } catch(ValidationException ex) { error(ex); }
    }
    private JPanel appointmentsPanel() {
        JPanel panel=new JPanel(new BorderLayout(8,8)); panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT)); top.add(new JLabel("Customer or pet")); top.add(search); top.add(button("Search / Refresh",this::refreshAppointments)); search.addActionListener(e -> refreshAppointments());
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.add(button("Book appointment",this::book));
        actions.add(button("Complete treatment",this::complete)); actions.add(button("Pay balance",this::pay));
        actions.add(button("View treatment",this::viewTreatment));
        actions.add(button("Cancel selected",() -> {
            Long id=selectedId(appointments); if(id==null) return;
            if(JOptionPane.showConfirmDialog(this,"Cancel this booked appointment?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            task(() -> { dao.execute("UPDATE appointments SET status='CANCELLED' WHERE id=? AND status='BOOKED'",id); return true; },ok -> refreshAppointments());
        }));
        JPanel heading=new JPanel(new BorderLayout(0,16)); heading.setOpaque(false); heading.add(Theme.label("Appointments and treatment",25,true,Theme.INK),BorderLayout.NORTH); heading.add(top);
        panel.setBorder(BorderFactory.createEmptyBorder(22,24,24,24)); panel.add(heading,BorderLayout.NORTH); panel.add(new JScrollPane(appointments)); panel.add(actions,BorderLayout.SOUTH);
        tabs.addChangeListener(e -> { if(tabs.getSelectedComponent()==panel) refreshAppointments(); }); return panel;
    }
    private void refreshAppointments() {
        String like="%"+search.getText().trim()+"%";
        task(() -> dao.query("SELECT a.id AS ID,a.slot AS Appointment,p.name AS Pet,c.name AS Customer,s.name AS Service,v.name AS Vet,a.status AS Status,a.fee AS Fee_LKR,COALESCE(x.paid,0) AS Paid_LKR,CASE WHEN a.status='CANCELLED' THEN 0 ELSE a.fee-COALESCE(x.paid,0) END AS Balance_LKR FROM appointments a JOIN pets p ON p.id=a.pet_id JOIN customers c ON c.id=a.customer_id JOIN services s ON s.id=a.service_id JOIN vets v ON v.id=a.vet_id LEFT JOIN (SELECT appointment_id,SUM(amount) paid FROM payments GROUP BY appointment_id) x ON x.appointment_id=a.id WHERE p.name LIKE ? OR c.name LIKE ? ORDER BY a.slot DESC",like,like),rows -> load(appointments,rows));
    }
    private void book() {
        task(() -> List.of(dao.choices("SELECT p.id,CONCAT(p.name,' — ',c.name,' (#',p.id,')') FROM pets p JOIN customers c ON c.id=p.customer_id ORDER BY p.name"),
            dao.choices("SELECT id,CONCAT(name,' — LKR ',price) FROM services WHERE active=1 ORDER BY name"),dao.choices("SELECT id,name FROM vets ORDER BY name")), lists -> {
            if(lists.stream().anyMatch(List::isEmpty)) { error(new ValidationException("Register a pet and ensure active services and vets exist.")); return; }
            JComboBox<ClinicDao.Choice> pet=new JComboBox<>(lists.get(0).toArray(ClinicDao.Choice[]::new)), svc=new JComboBox<>(lists.get(1).toArray(ClinicDao.Choice[]::new)), vet=new JComboBox<>(lists.get(2).toArray(ClinicDao.Choice[]::new));
            JTextField slot=new JTextField(LocalDate.now().plusDays(1)+"T09:00"); JPanel f=form(); addField(f,"Pet",pet); addField(f,"Service",svc); addField(f,"Vet",vet); addField(f,"30-minute slot YYYY-MM-DDTHH:MM",slot);
            if(!confirm(f,"Book veterinary appointment")) return;
            try {
                LocalDateTime time=Validation.appointment(slot.getText()); long p=((ClinicDao.Choice)pet.getSelectedItem()).id(),s=((ClinicDao.Choice)svc.getSelectedItem()).id(),v=((ClinicDao.Choice)vet.getSelectedItem()).id();
                task(() -> { service.book(p,s,v,time); return true; },ok -> refreshAppointments());
            } catch(ValidationException ex) { error(ex); }
        });
    }
    private void complete() {
        Long id=selectedId(appointments); if(id==null) return;
        Object[] row=row(appointments); if(!"BOOKED".equals(row[6])) { error(new ValidationException("Select a booked appointment.")); return; }
        JTextArea diagnosis=new JTextArea(4,28),medication=new JTextArea(4,28); diagnosis.setLineWrap(true); medication.setLineWrap(true);
        JTextField payment=new JTextField(row[7].toString()); JPanel f=form(); addField(f,"Diagnosis",new JScrollPane(diagnosis)); addField(f,"Medication and instructions optional",new JScrollPane(medication)); addField(f,"Payment now in LKR",payment);
        if(!confirm(f,"Complete treatment — appointment "+id)) return;
        try { var paid=Validation.money(payment.getText()); String d=Validation.required(diagnosis.getText(),"Diagnosis",1000),m=medication.getText();
            task(() -> { service.complete(id,d,m,paid); return true; },ok -> refreshAppointments());
        } catch(ValidationException ex) { error(ex); }
    }
    private void pay() {
        Long id=selectedId(appointments); if(id==null) return;
        String value=JOptionPane.showInputDialog(this,"Payment in LKR. Current balance: "+row(appointments)[9]); if(value==null) return;
        try { var amount=Validation.money(value); task(() -> { service.payBalance(id,amount); return true; },ok -> refreshAppointments()); }
        catch(ValidationException ex) { error(ex); }
    }
    private void viewTreatment() {
        Long id=selectedId(appointments); if(id==null) return;
        task(() -> dao.query("SELECT diagnosis,medication,recorded_at FROM treatments WHERE appointment_id=?",id),rows -> {
            if(rows.values().isEmpty()) { error(new ValidationException("No treatment recorded for this appointment.")); return; }
            Object[] r=rows.values().get(0); JTextArea text=new JTextArea("Recorded: "+r[2]+"\n\nDiagnosis\n"+r[0]+"\n\nMedication and instructions\n"+r[1],16,48);
            text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true); JOptionPane.showMessageDialog(this,new JScrollPane(text),"Treatment "+id,JOptionPane.INFORMATION_MESSAGE);
        });
    }
    private JPanel reportsPanel() {
        JPanel panel=new JPanel(new BorderLayout(12,12)); panel.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JTextField from=new JTextField(LocalDate.now().withDayOfMonth(1).toString()),to=new JTextField(LocalDate.now().toString());
        JPanel f=form(); addField(f,"Start date YYYY-MM-DD",from); addField(f,"End date YYYY-MM-DD inclusive",to); JPanel heading=new JPanel(new BorderLayout(0,20)); heading.setOpaque(false); heading.add(Theme.label("Visit and revenue reports",25,true,Theme.INK),BorderLayout.NORTH); heading.add(f); panel.add(heading,BorderLayout.NORTH);
        panel.add(new JLabel("Visit and revenue report joins appointments, pets, customers, services, vets, treatments and payment totals."));
        panel.add(button("Export Jasper report as PDF",() -> {
            try {
                LocalDate start=LocalDate.parse(from.getText().trim()),end=LocalDate.parse(to.getText().trim());
                if(end.isBefore(start)) throw new ValidationException("End date must not be before start date.");
                JFileChooser picker=new JFileChooser(); picker.setSelectedFile(new java.io.File("petcare-visits.pdf"));
                if(picker.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
                Path file=picker.getSelectedFile().toPath(); if(!file.toString().toLowerCase().endsWith(".pdf")) file=Path.of(file+".pdf");
                if(java.nio.file.Files.exists(file) && JOptionPane.showConfirmDialog(this,"Replace existing PDF?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
                Path output=file; task(() -> { new ReportService().export(start,end,output); return output; },p -> JOptionPane.showMessageDialog(this,"Report saved to "+p));
            } catch(Exception ex) { if(ex instanceof java.time.format.DateTimeParseException) error(new ValidationException("Use YYYY-MM-DD for both report dates.")); else error(ex); }
        }),BorderLayout.SOUTH); return panel;
    }
    private void refreshDashboard() {
        task(() -> new Object[]{dao.query("SELECT (SELECT COUNT(*) FROM customers) customers,(SELECT COUNT(*) FROM pets) pets,(SELECT COUNT(*) FROM appointments WHERE DATE(slot)=CURRENT_DATE AND status='BOOKED') today,(SELECT COALESCE(SUM(amount),0) FROM payments WHERE DATE(paid_at)=CURRENT_DATE) revenue"),
            dao.query("SELECT DATE_FORMAT(a.slot,'%H:%i') AS Time,p.name AS Pet,c.name AS Client,s.name AS Service,v.name AS Vet,a.status AS Status FROM appointments a JOIN pets p ON p.id=a.pet_id JOIN customers c ON c.id=a.customer_id JOIN services s ON s.id=a.service_id JOIN vets v ON v.id=a.vet_id WHERE DATE(a.slot)=CURRENT_DATE ORDER BY a.slot")}, data -> {
                dashboard.setMetrics(((ClinicDao.Rows)data[0]).values().get(0)); load(dashboard.today,(ClinicDao.Rows)data[1]);
            });
    }
    private <T> void task(Callable<T> work, Consumer<T> done) {
        if(busy) { status.setText("Please wait for the current operation to finish."); return; }
        busy=true; status.setText("Working…"); setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T,Void>() {
            protected T doInBackground() throws Exception { return work.call(); }
            protected void done() {
                busy=false; setCursor(Cursor.getDefaultCursor());
                try { T result=get(); status.setText("Ready — operation completed"); done.accept(result); }
                catch(Exception ex) { Throwable cause=ex instanceof java.util.concurrent.ExecutionException ? ex.getCause():ex; error(cause); }
            }
        }.execute();
    }
    private void error(Throwable ex) {
        String message;
        if(ex instanceof ValidationException) message=ex.getMessage();
        else if(ex instanceof SQLException sql) {
            Logger.getLogger("petcare").log(Level.WARNING,"Database operation failed",sql);
            message=switch(sql.getErrorCode()) {
                case 1062 -> "Duplicate record or occupied appointment slot. Refresh and choose another value.";
                case 1451 -> "This record has related history and cannot be deleted.";
                case 1452 -> "A selected record was removed. Refresh and try again.";
                default -> "Database operation failed. Check MySQL, credentials and the application log. "+(sql.getMessage().startsWith("Set PETCARE")?sql.getMessage():"");
            };
        } else { Logger.getLogger("petcare").log(Level.WARNING,"Operation failed",ex); message="Operation failed. Check the application log for details."; }
        status.setText("Operation was not completed"); JOptionPane.showMessageDialog(this,message,"Please check",JOptionPane.WARNING_MESSAGE);
    }
    private static JTable table() { JTable t=new JTable(); Theme.styleTable(t); t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); t.setAutoCreateRowSorter(true); t.setFillsViewportHeight(true); return t; }
    private static void load(JTable t,ClinicDao.Rows rows) { DefaultTableModel m=new DefaultTableModel(rows.columns(),0) { public boolean isCellEditable(int r,int c) { return false; } }; rows.values().forEach(m::addRow); t.setModel(m); }
    private Long selectedId(JTable t) { if(t.getSelectedRow()<0) { error(new ValidationException("Select a row first.")); return null; } return ((Number)t.getValueAt(t.getSelectedRow(),0)).longValue(); }
    private static Object[] row(JTable t) { Object[] r=new Object[t.getColumnCount()]; for(int i=0;i<r.length;i++) r[i]=t.getValueAt(t.getSelectedRow(),i); return r; }
    private static JTextField field(Object[] row,int i) { return new JTextField(row!=null && i<row.length && row[i]!=null?row[i].toString():"",26); }
    private static JPanel form() { return new JPanel(new GridLayout(0,2,12,12)); }
    private static void addField(JPanel f,String label,Component input) { f.add(new JLabel(label)); f.add(input); }
    private boolean confirm(JPanel form,String title) { return JOptionPane.showConfirmDialog(this,form,title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION; }
    private static JButton button(String label,Runnable action) {
        return label.startsWith("Add") || label.startsWith("Book") || label.startsWith("Complete") || label.startsWith("Export") ? Theme.primary(label,action) : Theme.button(label,action);
    }
}
