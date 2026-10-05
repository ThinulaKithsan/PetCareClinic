package lk.petcare;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
/** Bundled illustration with gentle motion; no network calls or extra animation library. */
public final class PetArtwork extends JPanel {
    private static final long serialVersionUID=1L;
    private static final Map<PetArtwork,Boolean> INSTANCES=new WeakHashMap<>();
    private static boolean animationsEnabled=true;
    private static final BufferedImage IMAGE=load();
    private final Timer timer;
    private double phase;
    public PetArtwork(int width,int height) {
        setOpaque(false); setPreferredSize(new Dimension(width,height));
        timer=new Timer(50,e -> { if(isShowing()) { phase+=0.055; repaint(); } });
        INSTANCES.put(this,Boolean.TRUE);
        getAccessibleContext().setAccessibleName("Illustration of a puppy and kitten");
    }
    public static boolean isAnimationsEnabled() { return animationsEnabled; }
    public static void setAnimationsEnabled(boolean value) {
        animationsEnabled=value;
        for(PetArtwork p:INSTANCES.keySet()) { if(value && p.isDisplayable()) p.timer.start(); else p.timer.stop(); p.repaint(); }
    }
    public void addNotify() { super.addNotify(); if(animationsEnabled) timer.start(); }
    public void removeNotify() { timer.stop(); super.removeNotify(); }
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); if(IMAGE==null) return;
        Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double scale=Math.min((getWidth()-16.0)/IMAGE.getWidth(),(getHeight()-18.0)/IMAGE.getHeight());
        if(scale>0) { int w=(int)(IMAGE.getWidth()*scale),h=(int)(IMAGE.getHeight()*scale); int y=(getHeight()-h)/2+(animationsEnabled?(int)(Math.sin(phase)*3):0); g2.drawImage(IMAGE,(getWidth()-w)/2,y,w,h,null); }
        g2.dispose();
    }
    private static BufferedImage load() {
        try(var input=PetArtwork.class.getResourceAsStream("/images/pet-companions.png")) { return input==null?null:ImageIO.read(input); }
        catch(IOException ex) { java.util.logging.Logger.getLogger("petcare").log(java.util.logging.Level.WARNING,"Pet illustration could not be loaded",ex); return null; }
    }
}
