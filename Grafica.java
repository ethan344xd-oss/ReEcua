import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.JPanel;

public class Grafica extends JPanel {

    private static final Color[] PALETA = {
        new Color(37, 99, 235), new Color(220, 38, 38), new Color(22, 163, 74),
        new Color(217, 119, 6), new Color(147, 51, 234), new Color(8, 145, 178)
    };

    private static final class Curva {
        double[] cf;
        int[] ex;
        int[] ey;
        String texto;
        Color color;

        double f(double x, double y) {
            double s = 0;
            for (int i = 0; i < cf.length; i++) {
                double t = cf[i];
                for (int k = 0; k < ex[i]; k++) {
                    t *= x;
                }
                for (int k = 0; k < ey[i]; k++) {
                    t *= y;
                }
                s += t;
            }
            return s;
        }
    }

    private final List<Curva> curvas = new ArrayList<Curva>();
    private final List<double[]> puntos = new ArrayList<double[]>();
    private List<List<double[]>> tablas = new ArrayList<List<double[]>>();
    private int nivel = -1;
    private char vx = 'x';
    private char vy = 'y';
    private boolean activo = false;
    private boolean ajustar = true;
    private boolean oscuro = false;
    private double cx = 0;
    private double cy = 0;
    private double esc = 30;
    private int arrastreX;
    private int arrastreY;
    private int ratonX = -1;
    private int ratonY = -1;

    public Grafica() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                arrastreX = e.getX();
                arrastreY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                cx -= (e.getX() - arrastreX) / esc;
                cy += (e.getY() - arrastreY) / esc;
                arrastreX = e.getX();
                arrastreY = e.getY();
                ratonX = e.getX();
                ratonY = e.getY();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                ratonX = e.getX();
                ratonY = e.getY();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ratonX = -1;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ajustar = true;
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double f = Math.pow(1.15, -e.getPreciseWheelRotation());
                double dx = e.getX() - getWidth() / 2.0;
                double dy = e.getY() - getHeight() / 2.0;
                double wx = cx + dx / esc;
                double wy = cy - dy / esc;
                esc = Math.max(0.5, Math.min(100000, esc * f));
                cx = wx - dx / esc;
                cy = wy + dy / esc;
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    public void setOscuro(boolean o) {
        oscuro = o;
        repaint();
    }

    public void setNivel(int n) {
        nivel = n;
        repaint();
    }

    public void establecer(List<Motor.Poly> ecs, TreeSet<Character> vars, List<Map<Character, Double>> sols,
            List<String> textos) {
        establecer(ecs, vars, sols, textos, null);
    }

    public void establecer(List<Motor.Poly> ecs, TreeSet<Character> vars, List<Map<Character, Double>> sols,
            List<String> textos, List<List<double[]>> tabla) {
        tablas = tabla == null ? new ArrayList<List<double[]>>() : tabla;
        curvas.clear();
        puntos.clear();
        activo = vars.size() == 2;
        if (activo) {
            vx = vars.first();
            vy = vars.last();
            for (int i = 0; i < ecs.size(); i++) {
                Motor.Poly p = ecs.get(i);
                int n = p.t.size();
                Curva c = new Curva();
                c.cf = new double[n];
                c.ex = new int[n];
                c.ey = new int[n];
                int j = 0;
                for (Map.Entry<Motor.Mono, Motor.Frac> en : p.t.entrySet()) {
                    c.cf[j] = en.getValue().dbl();
                    c.ex[j] = en.getKey().exp(vx);
                    c.ey[j] = en.getKey().exp(vy);
                    j++;
                }
                c.texto = textos.get(i);
                c.color = PALETA[i % PALETA.length];
                curvas.add(c);
            }
            for (Map<Character, Double> m : sols) {
                puntos.add(new double[]{m.get(vx), m.get(vy)});
            }
        }
        ajustar = true;
        repaint();
    }

    private void ajusteAutomatico(int w, int h) {
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
        for (double[] p : puntos) {
            minX = Math.min(minX, p[0]);
            maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]);
            maxY = Math.max(maxY, p[1]);
        }
        double sx = Math.max(maxX - minX, 6);
        double sy = Math.max(maxY - minY, 6);
        double pad = Math.max(sx, sy) * 0.25;
        cx = (minX + maxX) / 2;
        cy = (minY + maxY) / 2;
        esc = Math.min(w / (sx + 2 * pad), h / (sy + 2 * pad));
        ajustar = false;
    }

    private static String num(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9 * Math.max(1, Math.abs(v))) {
            return String.valueOf((long) Math.rint(v));
        }
        return new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.US)).format(v);
    }

    private static double pasoBonito(double crudo) {
        double mag = Math.pow(10, Math.floor(Math.log10(crudo)));
        double[] m = {1, 2, 5, 10};
        for (double k : m) {
            if (k * mag >= crudo) {
                return k * mag;
            }
        }
        return 10 * mag;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        Color fondo = oscuro ? new Color(27, 29, 33) : Color.WHITE;
        Color rejilla = oscuro ? new Color(52, 55, 62) : new Color(232, 234, 238);
        Color eje = oscuro ? new Color(150, 156, 166) : new Color(90, 96, 106);
        Color texto = oscuro ? new Color(205, 209, 216) : new Color(60, 66, 76);
        g.setColor(fondo);
        g.fillRect(0, 0, w, h);
        Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        g.setFont(base);

        if (!activo) {
            g.setColor(texto);
            String[] ls = {"La gráfica se muestra cuando el sistema tiene 2 incógnitas",
                "(por ejemplo x e y)."};
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < ls.length; i++) {
                g.drawString(ls[i], (w - fm.stringWidth(ls[i])) / 2, h / 2 + i * 18 - 6);
            }
            g.dispose();
            return;
        }
        if (ajustar && w > 0 && h > 0) {
            ajusteAutomatico(w, h);
        }

        double xmin = cx - w / 2.0 / esc;
        double xmax = cx + w / 2.0 / esc;
        double ymin = cy - h / 2.0 / esc;
        double ymax = cy + h / 2.0 / esc;
        double paso = pasoBonito(64.0 / esc);
        FontMetrics fm = g.getFontMetrics();

        int ejeY = (int) Math.round(h / 2.0 + cy * esc);
        int ejeX = (int) Math.round(w / 2.0 - cx * esc);
        int yEtq = Math.max(14, Math.min(h - 6, ejeY + 15));
        int xEtq = Math.max(4, Math.min(w - 30, ejeX + 5));

        g.setStroke(new BasicStroke(1f));
        for (double gx = Math.ceil(xmin / paso) * paso; gx <= xmax; gx += paso) {
            int px = (int) Math.round(w / 2.0 + (gx - cx) * esc);
            g.setColor(rejilla);
            g.drawLine(px, 0, px, h);
            if (Math.abs(gx) > paso / 2) {
                g.setColor(texto);
                String t = num(gx);
                g.drawString(t, px - fm.stringWidth(t) / 2, yEtq);
            }
        }
        for (double gy = Math.ceil(ymin / paso) * paso; gy <= ymax; gy += paso) {
            int py = (int) Math.round(h / 2.0 - (gy - cy) * esc);
            g.setColor(rejilla);
            g.drawLine(0, py, w, py);
            if (Math.abs(gy) > paso / 2) {
                g.setColor(texto);
                g.drawString(num(gy), xEtq, py - 3);
            }
        }
        g.setColor(eje);
        g.setStroke(new BasicStroke(1.6f));
        g.drawLine(0, ejeY, w, ejeY);
        g.drawLine(ejeX, 0, ejeX, h);
        g.setColor(texto);
        g.setFont(base.deriveFont(Font.BOLD, 13f));
        g.drawString(String.valueOf(vx), w - 16, Math.max(14, Math.min(h - 6, ejeY - 6)));
        g.drawString(String.valueOf(vy), Math.max(4, Math.min(w - 14, ejeX + 8)), 14);
        g.setFont(base);

        int cel = 3;
        int nx = w / cel + 2;
        int ny = h / cel + 2;
        double[] fila = new double[nx];
        double[] sig = new double[nx];
        double[] qx = new double[4];
        double[] qy = new double[4];
        g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int ci = 0; ci < curvas.size(); ci++) {
            if (nivel >= 0 && ci >= nivel) {
                break;
            }
            Curva c = curvas.get(ci);
            g.setColor(c.color);
            for (int i = 0; i < nx; i++) {
                fila[i] = c.f(cx + (i * cel - w / 2.0) / esc, cy + (h / 2.0) / esc);
            }
            for (int j = 0; j < ny - 1; j++) {
                double y1 = cy - ((j + 1) * cel - h / 2.0) / esc;
                for (int i = 0; i < nx; i++) {
                    sig[i] = c.f(cx + (i * cel - w / 2.0) / esc, y1);
                }
                for (int i = 0; i < nx - 1; i++) {
                    double a = fila[i];
                    double b = fila[i + 1];
                    double d = sig[i];
                    double e = sig[i + 1];
                    int np = 0;
                    if ((a < 0) != (b < 0)) {
                        double t = a / (a - b);
                        qx[np] = (i + t) * cel;
                        qy[np++] = j * cel;
                    }
                    if ((b < 0) != (e < 0)) {
                        double t = b / (b - e);
                        qx[np] = (i + 1) * cel;
                        qy[np++] = (j + t) * cel;
                    }
                    if ((d < 0) != (e < 0)) {
                        double t = d / (d - e);
                        qx[np] = (i + t) * cel;
                        qy[np++] = (j + 1) * cel;
                    }
                    if ((a < 0) != (d < 0)) {
                        double t = a / (a - d);
                        qx[np] = i * cel;
                        qy[np++] = (j + t) * cel;
                    }
                    if (np >= 2) {
                        g.draw(new Line2D.Double(qx[0], qy[0], qx[1], qy[1]));
                    }
                    if (np == 4) {
                        g.draw(new Line2D.Double(qx[2], qy[2], qx[3], qy[3]));
                    }
                }
                double[] tmp = fila;
                fila = sig;
                sig = tmp;
            }
        }

        if (nivel >= 0) {
            for (int ci = 0; ci < curvas.size() && ci < nivel && ci < tablas.size(); ci++) {
                for (double[] tp : tablas.get(ci)) {
                    int tx0 = (int) Math.round(w / 2.0 + (tp[0] - cx) * esc);
                    int ty0 = (int) Math.round(h / 2.0 - (tp[1] - cy) * esc);
                    g.setColor(Color.WHITE);
                    g.fillOval(tx0 - 4, ty0 - 4, 8, 8);
                    g.setColor(curvas.get(ci).color);
                    g.setStroke(new BasicStroke(2f));
                    g.drawOval(tx0 - 4, ty0 - 4, 8, 8);
                }
            }
        }

        g.setStroke(new BasicStroke(2f));
        g.setFont(base.deriveFont(Font.BOLD, 12f));
        fm = g.getFontMetrics();
        boolean verPuntos = nivel < 0 || nivel > curvas.size();
        for (double[] p : verPuntos ? puntos : new ArrayList<double[]>()) {
            int px = (int) Math.round(w / 2.0 + (p[0] - cx) * esc);
            int py = (int) Math.round(h / 2.0 - (p[1] - cy) * esc);
            g.setColor(Color.WHITE);
            g.fillOval(px - 5, py - 5, 10, 10);
            g.setColor(new Color(30, 30, 30));
            g.drawOval(px - 5, py - 5, 10, 10);
            String t = "(" + num(p[0]) + ", " + num(p[1]) + ")";
            int tw = fm.stringWidth(t);
            int tx = Math.min(px + 9, w - tw - 8);
            int ty = Math.max(py - 9, 30);
            g.setColor(oscuro ? new Color(20, 22, 26, 215) : new Color(255, 255, 255, 215));
            g.fillRoundRect(tx - 3, ty - 13, tw + 6, 18, 6, 6);
            g.setColor(oscuro ? new Color(240, 242, 246) : new Color(20, 22, 26));
            g.drawString(t, tx, ty);
        }

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        fm = g.getFontMetrics();
        int dib = nivel < 0 ? curvas.size() : Math.min(nivel, curvas.size());
        if (dib > 0) {
            int anchoLey = 0;
            for (int i = 0; i < dib; i++) {
                anchoLey = Math.max(anchoLey, fm.stringWidth(curvas.get(i).texto));
            }
            int altoLey = dib * 18 + 8;
            g.setColor(oscuro ? new Color(20, 22, 26, 215) : new Color(255, 255, 255, 220));
            g.fillRoundRect(8, 8, anchoLey + 36, altoLey, 8, 8);
            g.setColor(rejilla);
            g.drawRoundRect(8, 8, anchoLey + 36, altoLey, 8, 8);
            for (int i = 0; i < dib; i++) {
                Curva c = curvas.get(i);
                g.setColor(c.color);
                g.fillRect(16, 17 + i * 18, 10, 10);
                g.setColor(texto);
                g.drawString(c.texto, 32, 26 + i * 18);
            }
        }

        g.setFont(base);
        g.setColor(texto);
        String ayuda = "Rueda: zoom \u00b7 Arrastrar: mover \u00b7 Doble clic: ajustar";
        g.drawString(ayuda, 10, h - 8);
        if (ratonX >= 0) {
            double mx = cx + (ratonX - w / 2.0) / esc;
            double my = cy - (ratonY - h / 2.0) / esc;
            String t = vx + " = " + num(Math.round(mx * 100) / 100.0) + ",  " + vy + " = "
                    + num(Math.round(my * 100) / 100.0);
            g.drawString(t, w - g.getFontMetrics().stringWidth(t) - 10, h - 8);
        }
        g.dispose();
    }
}
