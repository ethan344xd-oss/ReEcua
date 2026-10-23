import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

public class Main {

    private static final String TITULO_VENTANA = "Calculadora de ecuaciones \u00b7 Métodos de resolución";

    private static final String ACERCA_DE =
            "Calculadora de ecuaciones\n"
            + "Uso para el docente:David Batun.\n\n"
            + "Código abierto (MIT License): Hecho por:Uc Lobato Ethan Rafael";

    private JFrame ventana;
    private JTextArea entrada;
    private final JTextArea[] areas = new JTextArea[4];
    private final JScrollPane[] scrolls = new JScrollPane[4];
    private final Motor.Res[] vistas = new Motor.Res[4];
    private final boolean[] modo = new boolean[4];
    private final int[] pasoV = new int[4];
    private JSplitPane splitGrafico;
    private JLabel estado;
    private JLabel tEntrada;
    private JLabel tSalida;
    private JLabel ayuda;
    private JLabel lPaso;
    private JLabel lDespejar;
    private JLabel lEnEc;
    private JPanel izq;
    private JPanel der;
    private JPanel barra;
    private JPanel botones;
    private JPanel cabIzq;
    private JPanel cabDer;
    private JPanel navegacion;
    private JPanel despeje;
    private JPanel abajoIzq;
    private JScrollPane scEntrada;
    private JSplitPane split;
    private JTabbedPane pestanas;
    private Grafica grafica;
    private JButton bInicio;
    private JButton bAnterior;
    private JButton bSiguiente;
    private JButton bTodo;
    private JComboBox<String> cVar;
    private JComboBox<String> cEc;
    private JCheckBoxMenuItem itemOscuro;
    private JCheckBoxMenuItem itemVerificar;
    private JCheckBoxMenuItem itemPasos;
    private boolean actualizando = false;
    private int tam = 15;
    private boolean oscuro = false;
    private DefaultHighlighter.DefaultHighlightPainter resaltador;
    private int aciertos = 0;
    private int intentos = 0;
    private String sistemaPractica = "";
    private Map<Character, Motor.Frac> solPractica = new TreeMap<Character, Motor.Frac>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
            }
            new Main().iniciar();
        });
    }

    private static Font fuente(int size, boolean negrita) {
        int estilo = negrita ? Font.BOLD : Font.PLAIN;
        Font f = new Font("Consolas", estilo, size);
        if (!"Consolas".equalsIgnoreCase(f.getFamily())) {
            f = new Font(Font.MONOSPACED, estilo, size);
        }
        return f;
    }

    private JLabel titulo(String t) {
        JLabel l = new JLabel(t);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 16f));
        return l;
    }

    private String bienvenida() {
        return "Calculadora de ecuaciones\n"
                + Motor.rep("\u2500", 58) + "\n\n"
                + "  1. Escribe tus ecuaciones a la izquierda (una por línea).\n"
                + "  2. Pulsa Resolver o usa Ctrl + Enter.\n"
                + "  3. Aquí aparecerá la resolución paso a paso.\n\n"
                + "  Cada pestaña resuelve con un método distinto:\n"
                + "  sustitución, igualación, reducción y gráfico.\n"
                + "  Usa Siguiente y Anterior para avanzar de a un paso.\n\n"
                + "  Acepta lineales y también xy, x^2, x\u00b2, paréntesis, fracciones...\n"
                + "  Prueba el menú Ejemplos o el modo Práctica.\n";
    }

    private void iniciar() {
        ventana = new JFrame(TITULO_VENTANA);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        construir();
        ventana.setJMenuBar(menuBar);
        ventana.add(split, BorderLayout.CENTER);
        ventana.add(barra, BorderLayout.SOUTH);
        aplicarTema();
        ventana.setSize(1120, 700);
        ventana.setMinimumSize(new Dimension(820, 520));
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
        resolverAccion();
    }

    private JMenuBar menuBar;

    void construir() {
        entrada = new JTextArea("2x + y = 5\nx - y = 1");
        entrada.setMargin(new Insets(8, 10, 8, 10));
        for (int i = 0; i < 4; i++) {
            areas[i] = new JTextArea();
            areas[i].setEditable(false);
            areas[i].setMargin(new Insets(10, 12, 10, 12));
            areas[i].setText(bienvenida());
            scrolls[i] = new JScrollPane(areas[i]);
        }

        tEntrada = titulo("Ecuaciones");
        tSalida = titulo("Resolución");
        ayuda = new JLabel("<html>Una por línea o separadas con ;<br>Acepta x, X, \u00d7, xy, x^2, 2(x+1), x/2 ...</html>");
        ayuda.setFont(ayuda.getFont().deriveFont(12f));

        JButton bResolver = new JButton("Resolver");
        JButton bLimpiar = new JButton("Limpiar");
        JButton bPractica = new JButton("Práctica...");
        JButton bPaso = new JButton("Paso a paso");
        bResolver.setFont(bResolver.getFont().deriveFont(Font.BOLD, 14f));
        bLimpiar.setFont(bLimpiar.getFont().deriveFont(14f));
        bPractica.setFont(bPractica.getFont().deriveFont(14f));
        bPaso.setFont(bPaso.getFont().deriveFont(14f));
        bResolver.addActionListener(e -> resolverAccion());
        bLimpiar.addActionListener(e -> limpiarTodo());
        bPractica.addActionListener(e -> practica());
        bPaso.addActionListener(e -> pasoAPaso());
        botones = new JPanel(new GridLayout(2, 2, 8, 8));
        botones.setOpaque(false);
        botones.add(bResolver);
        botones.add(bLimpiar);
        botones.add(bPaso);
        botones.add(bPractica);

        cVar = new JComboBox<String>();
        cEc = new JComboBox<String>();
        cVar.addItem("Automático");
        cEc.addItem("Automática");
        cVar.addActionListener(e -> {
            if (!actualizando) {
                resolverAccion();
            }
        });
        cEc.addActionListener(e -> {
            if (!actualizando) {
                resolverAccion();
            }
        });
        lDespejar = new JLabel("Despejar:");
        lEnEc = new JLabel("En la ecuación:");
        despeje = new JPanel(new GridLayout(2, 2, 8, 6));
        despeje.setOpaque(false);
        despeje.setBorder(BorderFactory.createTitledBorder("Elegir el despeje"));
        despeje.add(lDespejar);
        despeje.add(cVar);
        despeje.add(lEnEc);
        despeje.add(cEc);

        abajoIzq = new JPanel(new BorderLayout(0, 8));
        abajoIzq.setOpaque(false);
        abajoIzq.add(despeje, BorderLayout.NORTH);
        abajoIzq.add(botones, BorderLayout.SOUTH);

        cabIzq = new JPanel(new BorderLayout(0, 4));
        cabIzq.setOpaque(false);
        cabIzq.add(tEntrada, BorderLayout.NORTH);
        cabIzq.add(ayuda, BorderLayout.CENTER);

        scEntrada = new JScrollPane(entrada);
        grafica = new Grafica();
        splitGrafico = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrolls[3], grafica);
        splitGrafico.setResizeWeight(0.4);
        splitGrafico.setDividerLocation(250);
        splitGrafico.setContinuousLayout(true);
        splitGrafico.setBorder(null);

        pestanas = new JTabbedPane();
        pestanas.addTab("Sustitución", scrolls[0]);
        pestanas.addTab("Igualación", scrolls[1]);
        pestanas.addTab("Reducción", scrolls[2]);
        pestanas.addTab("Gráfico", splitGrafico);
        pestanas.addChangeListener(e -> refrescarActual());

        bInicio = new JButton("Inicio");
        bAnterior = new JButton("\u25c0 Anterior");
        bSiguiente = new JButton("Siguiente \u25b6");
        bTodo = new JButton("Todo");
        lPaso = new JLabel(" ");
        bInicio.addActionListener(e -> inicio());
        bAnterior.addActionListener(e -> anterior());
        bSiguiente.addActionListener(e -> siguiente());
        bTodo.addActionListener(e -> todo());
        navegacion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        navegacion.setOpaque(false);
        navegacion.add(lPaso);
        navegacion.add(bInicio);
        navegacion.add(bAnterior);
        navegacion.add(bSiguiente);
        navegacion.add(bTodo);

        cabDer = new JPanel(new BorderLayout());
        cabDer.setOpaque(false);
        cabDer.add(tSalida, BorderLayout.WEST);
        cabDer.add(navegacion, BorderLayout.EAST);

        izq = new JPanel(new BorderLayout(0, 8));
        izq.setBorder(new EmptyBorder(12, 12, 12, 6));
        izq.add(cabIzq, BorderLayout.NORTH);
        izq.add(scEntrada, BorderLayout.CENTER);
        izq.add(abajoIzq, BorderLayout.SOUTH);

        der = new JPanel(new BorderLayout(0, 8));
        der.setBorder(new EmptyBorder(12, 6, 12, 12));
        der.add(cabDer, BorderLayout.NORTH);
        der.add(pestanas, BorderLayout.CENTER);

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izq, der);
        split.setDividerLocation(360);
        split.setResizeWeight(0.3);
        split.setContinuousLayout(true);
        split.setBorder(null);

        estado = new JLabel("  Listo");
        estado.setBorder(new EmptyBorder(6, 10, 6, 10));
        barra = new JPanel(new BorderLayout());
        barra.add(estado, BorderLayout.WEST);

        entrada.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "resolver");
        entrada.getActionMap().put("resolver", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resolverAccion();
            }
        });

        menuBar = crearMenu();
        resaltador = new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 244, 179));
        aplicarFuentes();
        actualizarNavegacion();
    }

    @SuppressWarnings("deprecation")
    private int mascara() {
        try {
            return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (java.awt.HeadlessException ex) {
            return InputEvent.CTRL_DOWN_MASK;
        }
    }

    private JMenuItem item(String texto, int tecla, int extra, ActionListener a) {
        JMenuItem it = new JMenuItem(texto);
        if (tecla != 0) {
            it.setAccelerator(KeyStroke.getKeyStroke(tecla, extra));
        }
        it.addActionListener(a);
        return it;
    }

    private JMenuBar crearMenu() {
        JMenuBar mb = new JMenuBar();
        int ctrl = mascara();

        JMenu archivo = new JMenu("Archivo");
        archivo.setMnemonic(KeyEvent.VK_A);
        archivo.add(item("Nueva", KeyEvent.VK_N, ctrl, e -> limpiarTodo()));
        archivo.add(item("Abrir ecuaciones...", KeyEvent.VK_O, ctrl, e -> abrir()));
        archivo.add(item("Guardar resolución...", KeyEvent.VK_S, ctrl, e -> guardar()));
        archivo.addSeparator();
        archivo.add(item("Copiar resolución", KeyEvent.VK_C, ctrl | InputEvent.SHIFT_DOWN_MASK, e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textoCompleto()), null);
            estado.setText("  Resolución copiada al portapapeles");
        }));
        archivo.addSeparator();
        archivo.add(item("Salir", KeyEvent.VK_Q, ctrl, e -> System.exit(0)));

        JMenu ejemplos = new JMenu("Ejemplos");
        ejemplos.setMnemonic(KeyEvent.VK_E);
        String[][] ej = {
            {"Sistema 2\u00d72", "2x + y = 5\nx - y = 1"},
            {"Sistema 3\u00d73", "x + y + z = 6\n2x - y + z = 3\nx + 2y - z = 2"},
            {"Paréntesis y fracciones", "2(x + 1) - y/2 = 5\n0.5x + 3y = 4"},
            {"Notación mezclada (x, X, \u00d7)", "3X + 2y = 12\n4\u00d7 - Y = 5"},
            {"Sin solución", "x + y = 2\n2x + 2y = 5"},
            {"Infinitas soluciones", "x + y = 2\n2x + 2y = 4"},
            {"Producto: xy = 6", "xy = 6\nx + y = 5"},
            {"Círculo y recta", "x^2 + y^2 = 25\nx - y = 1"},
            {"Parábola y recta", "y = x\u00b2 - 3\ny = 2x"},
            {"Raíces irracionales", "xy = 1\nx + y = 3"}
        };
        for (String[] par : ej) {
            final String cuerpo = par[1];
            ejemplos.add(item(par[0], 0, 0, e -> {
                entrada.setText(cuerpo);
                cVar.setSelectedIndex(0);
                cEc.setSelectedIndex(0);
                resolverAccion();
            }));
        }

        JMenu pasos = new JMenu("Pasos");
        pasos.setMnemonic(KeyEvent.VK_P);
        pasos.add(item("Siguiente paso", KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK, e -> siguiente()));
        pasos.add(item("Paso anterior", KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK, e -> anterior()));
        pasos.add(item("Ir al inicio", KeyEvent.VK_HOME, InputEvent.ALT_DOWN_MASK, e -> inicio()));
        pasos.add(item("Mostrar todo", KeyEvent.VK_END, InputEvent.ALT_DOWN_MASK, e -> todo()));
        pasos.addSeparator();
        itemPasos = new JCheckBoxMenuItem("Empezar siempre paso a paso");
        pasos.add(itemPasos);

        JMenu practica = new JMenu("Práctica");
        practica.setMnemonic(KeyEvent.VK_R);
        practica.add(item("Modo práctica...", KeyEvent.VK_P, ctrl, e -> practica()));

        JMenu ver = new JMenu("Ver");
        ver.setMnemonic(KeyEvent.VK_V);
        itemOscuro = new JCheckBoxMenuItem("Modo oscuro");
        itemOscuro.addActionListener(e -> {
            oscuro = itemOscuro.isSelected();
            aplicarTema();
        });
        itemVerificar = new JCheckBoxMenuItem("Mostrar verificación", true);
        itemVerificar.addActionListener(e -> resolverAccion());
        ver.add(itemOscuro);
        ver.add(itemVerificar);
        ver.addSeparator();
        ver.add(item("Aumentar letra", KeyEvent.VK_EQUALS, ctrl, e -> cambiarTam(1)));
        ver.add(item("Reducir letra", KeyEvent.VK_MINUS, ctrl, e -> cambiarTam(-1)));

        JMenu ayudaM = new JMenu("Ayuda");
        ayudaM.setMnemonic(KeyEvent.VK_Y);
        ayudaM.add(item("Formato aceptado", KeyEvent.VK_F1, 0, e -> formato()));
        ayudaM.add(item("Acerca de", 0, 0, e -> JOptionPane.showMessageDialog(ventana,
                ACERCA_DE, "Acerca de", JOptionPane.INFORMATION_MESSAGE)));

        mb.add(archivo);
        mb.add(ejemplos);
        mb.add(pasos);
        mb.add(practica);
        mb.add(ver);
        mb.add(ayudaM);
        return mb;
    }

    private void formato() {
        JOptionPane.showMessageDialog(ventana,
                "\u2022 Una ecuación por línea (o separadas con ; o coma).\n"
                + "\u2022 Incógnitas: cualquier letra. x y X son la misma.\n"
                + "\u2022 Multiplicar: 2x, 2*x, 2\u00b7x, 2\u00d7x, 2(x+1), xy.\n"
                + "   Un \u00d7 sin nada que multiplicar después se toma como x.\n"
                + "\u2022 Potencias: x^2, x**2 o x\u00b2 (exponente entero de 1 a 9).\n"
                + "\u2022 Dividir: x/2, (x+1)/3, x\u00f72 (solo entre números).\n"
                + "\u2022 Decimales con punto o coma: 0.5 o 0,5.\n"
                + "\u2022 Paréntesis: ( ), [ ] y { }.\n"
                + "\u2022 Sistemas no lineales: hace falta que alguna ecuación sea lineal\n"
                + "   en alguna incógnita (se despeja y se sustituye).",
                "Formato aceptado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cambiarTam(int d) {
        tam = Math.max(10, Math.min(32, tam + d));
        aplicarFuentes();
    }

    private void aplicarFuentes() {
        entrada.setFont(fuente(tam, false));
        for (JTextArea t : areas) {
            t.setFont(fuente(tam, false));
        }
    }

    void aplicarTema() {
        Color fondo = oscuro ? new Color(37, 39, 44) : new Color(240, 241, 243);
        Color caja = oscuro ? new Color(27, 29, 33) : Color.WHITE;
        Color texto = oscuro ? new Color(225, 228, 233) : new Color(30, 34, 40);
        Color suave = oscuro ? new Color(150, 156, 166) : new Color(100, 106, 116);
        Color borde = oscuro ? new Color(70, 73, 80) : new Color(205, 208, 214);
        for (JComponent c : new JComponent[]{izq, der, barra, split, pestanas, splitGrafico}) {
            c.setBackground(fondo);
        }
        for (JLabel l : new JLabel[]{tEntrada, tSalida, lDespejar, lEnEc}) {
            l.setForeground(texto);
        }
        lPaso.setForeground(suave);
        ayuda.setForeground(suave);
        estado.setForeground(suave);
        TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(borde), "Elegir el despeje");
        tb.setTitleColor(texto);
        despeje.setBorder(tb);
        List<JTextArea> todasAreas = new ArrayList<JTextArea>();
        todasAreas.add(entrada);
        for (JTextArea t : areas) {
            todasAreas.add(t);
        }
        for (JTextArea t : todasAreas) {
            t.setBackground(caja);
            t.setForeground(texto);
            t.setCaretColor(texto);
            t.setSelectionColor(oscuro ? new Color(65, 105, 170) : new Color(180, 205, 240));
            t.setSelectedTextColor(oscuro ? Color.WHITE : Color.BLACK);
        }
        List<JScrollPane> todosScroll = new ArrayList<JScrollPane>();
        todosScroll.add(scEntrada);
        for (JScrollPane sc : scrolls) {
            todosScroll.add(sc);
        }
        for (JScrollPane sc : todosScroll) {
            sc.getViewport().setBackground(caja);
            sc.setBorder(BorderFactory.createLineBorder(borde));
        }
        resaltador = new DefaultHighlighter.DefaultHighlightPainter(
                oscuro ? new Color(70, 90, 40) : new Color(255, 244, 179));
        grafica.setOscuro(oscuro);
        for (int i = 0; i < 4; i++) {
            mostrar(i);
        }
        if (ventana != null) {
            ventana.repaint();
        }
    }

    private int act() {
        return pestanas.getSelectedIndex();
    }

    private String textoCompleto() {
        Motor.Res r = vistas[act()];
        return r != null ? r.texto : areas[act()].getText();
    }

    void resolverAccion() {
        Character pv = null;
        if (cVar.getSelectedIndex() > 0) {
            pv = ((String) cVar.getSelectedItem()).charAt(0);
        }
        Integer pe = cEc.getSelectedIndex() > 0 ? Integer.valueOf(cEc.getSelectedIndex()) : null;
        boolean verif = itemVerificar.isSelected();
        String texto = entrada.getText();
        vistas[0] = Motor.resolver(texto, verif, pv, pe, 0);
        vistas[1] = Motor.resolver(texto, verif, pv, pe, 1);
        vistas[2] = Motor.resolver(texto, verif, pv, pe, 2);
        vistas[3] = Motor.grafico(vistas[0], verif);
        actualizarCombos();
        for (int i = 0; i < 4; i++) {
            modo[i] = itemPasos.isSelected();
            pasoV[i] = modo[i] ? 0 : vistas[i].bloques.size() - 1;
        }
        List<String> textos = new ArrayList<String>();
        for (int i = 0; i < vistas[0].ecs.size(); i++) {
            textos.add("(" + (i + 1) + ") " + Motor.ec(vistas[0].ecs.get(i)));
        }
        grafica.establecer(vistas[0].ecs, vistas[0].vars, vistas[0].numericas, textos, vistas[3].tablas);
        for (int i = 0; i < 4; i++) {
            mostrar(i);
        }
        refrescarActual();
    }

    private void actualizarCombos() {
        Motor.Res base = vistas[0];
        actualizando = true;
        String selV = (String) cVar.getSelectedItem();
        int selE = cEc.getSelectedIndex();
        cVar.removeAllItems();
        cVar.addItem("Automático");
        for (Character v : base.vars) {
            cVar.addItem(String.valueOf(v));
        }
        if (selV != null) {
            cVar.setSelectedItem(selV);
        }
        cEc.removeAllItems();
        cEc.addItem("Automática");
        for (int i = 1; i <= base.ecs.size(); i++) {
            cEc.addItem("(" + i + ")");
        }
        if (selE > 0 && selE <= base.ecs.size()) {
            cEc.setSelectedIndex(selE);
        }
        actualizando = false;
    }

    void mostrar(int i) {
        Motor.Res r = vistas[i];
        if (r == null) {
            return;
        }
        List<String> b = r.bloques;
        int n = b.size();
        int hasta = modo[i] ? Math.min(pasoV[i], n - 1) : n - 1;
        StringBuilder sb = new StringBuilder();
        int ini = 0;
        int fin = 0;
        for (int k = 0; k <= hasta; k++) {
            if (k > 0) {
                sb.append("\n");
            }
            if (k == hasta) {
                ini = sb.length();
            }
            sb.append(b.get(k));
            if (k == hasta) {
                fin = sb.length();
            }
        }
        JTextArea area = areas[i];
        area.setText(sb.toString());
        area.getHighlighter().removeAllHighlights();
        if (modo[i]) {
            try {
                area.getHighlighter().addHighlight(ini, fin, resaltador);
            } catch (BadLocationException ex) {
            }
            area.setCaretPosition(fin);
        } else {
            area.setCaretPosition(0);
        }
        if (i == 3) {
            int nivel = -1;
            if (modo[i] && r.niveles != null && hasta < r.niveles.length) {
                nivel = r.niveles[hasta];
            }
            grafica.setNivel(nivel);
        }
        if (i == act()) {
            refrescarActual();
        }
    }

    private void refrescarActual() {
        int i = act();
        Motor.Res r = vistas[i];
        if (r == null) {
            lPaso.setText(" ");
            actualizarNavegacion();
            return;
        }
        int n = r.bloques.size();
        if (modo[i]) {
            lPaso.setText("Paso " + (Math.min(pasoV[i], n - 1) + 1) + " de " + n);
        } else {
            lPaso.setText(n + (n == 1 ? " bloque" : " bloques"));
        }
        estado.setText("  " + r.resumen);
        actualizarNavegacion();
    }

    private void actualizarNavegacion() {
        int i = act();
        Motor.Res r = vistas[i];
        boolean hay = r != null && r.bloques.size() > 1;
        int n = r == null ? 0 : r.bloques.size();
        bInicio.setEnabled(hay);
        bTodo.setEnabled(hay && modo[i]);
        bSiguiente.setEnabled(hay && (!modo[i] || pasoV[i] < n - 1));
        bAnterior.setEnabled(hay && (!modo[i] || pasoV[i] > 0));
    }

    void inicio() {
        int i = act();
        if (vistas[i] == null) {
            return;
        }
        modo[i] = true;
        pasoV[i] = 0;
        mostrar(i);
    }

    void siguiente() {
        int i = act();
        if (vistas[i] == null) {
            return;
        }
        if (!modo[i]) {
            modo[i] = true;
            pasoV[i] = 0;
        } else if (pasoV[i] < vistas[i].bloques.size() - 1) {
            pasoV[i]++;
        }
        mostrar(i);
    }

    void anterior() {
        int i = act();
        if (vistas[i] == null) {
            return;
        }
        if (!modo[i]) {
            modo[i] = true;
            pasoV[i] = Math.max(0, vistas[i].bloques.size() - 2);
        } else if (pasoV[i] > 0) {
            pasoV[i]--;
        }
        mostrar(i);
    }

    void todo() {
        int i = act();
        modo[i] = false;
        mostrar(i);
    }

    private void pasoAPaso() {
        boolean antes = itemPasos.isSelected();
        itemPasos.setSelected(true);
        resolverAccion();
        itemPasos.setSelected(antes);
    }

    private void limpiarTodo() {
        entrada.setText("");
        for (int i = 0; i < 4; i++) {
            vistas[i] = null;
            modo[i] = false;
            areas[i].setText(bienvenida());
            areas[i].getHighlighter().removeAllHighlights();
            areas[i].setCaretPosition(0);
        }
        estado.setText("  Listo");
        grafica.setNivel(-1);
        grafica.establecer(new ArrayList<Motor.Poly>(), new TreeSet<Character>(),
                new ArrayList<Map<Character, Double>>(), new ArrayList<String>());
        refrescarActual();
        entrada.requestFocusInWindow();
    }

    private void abrir() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(ventana) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            byte[] b = Files.readAllBytes(fc.getSelectedFile().toPath());
            entrada.setText(new String(b, StandardCharsets.UTF_8));
            estado.setText("  Archivo cargado: " + fc.getSelectedFile().getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana, "No se pudo leer el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardar() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("resolucion.txt"));
        if (fc.showSaveDialog(ventana) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = fc.getSelectedFile();
        if (!f.getName().contains(".")) {
            f = new File(f.getParentFile(), f.getName() + ".txt");
        }
        if (f.exists() && JOptionPane.showConfirmDialog(ventana, "El archivo ya existe. ¿Reemplazarlo?",
                "Guardar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Files.write(f.toPath(), textoCompleto().getBytes(StandardCharsets.UTF_8));
            estado.setText("  Guardado: " + f.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(ventana, "No se pudo guardar el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void generarPractica(int n) {
        char[] vs = {'x', 'y', 'z'};
        Random r = new Random();
        for (int intento = 0; intento < 300; intento++) {
            int[] sol = new int[n];
            for (int i = 0; i < n; i++) {
                sol[i] = r.nextInt(13) - 6;
            }
            StringBuilder texto = new StringBuilder();
            for (int i = 0; i < n; i++) {
                Motor.Poly p = new Motor.Poly();
                int rhs = 0;
                for (int j = 0; j < n; j++) {
                    int c = r.nextInt(11) - 5;
                    if (c == 0) {
                        continue;
                    }
                    p = p.mas(Motor.Poly.var(vs[j]).esc(Motor.Frac.de(c)));
                    rhs += c * sol[j];
                }
                p = p.mas(Motor.Poly.cte(Motor.Frac.de(-rhs)));
                if (i > 0) {
                    texto.append("\n");
                }
                texto.append(Motor.ec(p));
            }
            Motor.Res res = Motor.resolver(texto.toString(), false, null, null);
            if (res.resumen.startsWith("Solución única")) {
                sistemaPractica = texto.toString();
                solPractica = new TreeMap<Character, Motor.Frac>();
                for (int i = 0; i < n; i++) {
                    solPractica.put(vs[i], Motor.Frac.de(sol[i]));
                }
                return;
            }
        }
    }

    private void practica() {
        final JDialog d = new JDialog(ventana, "Modo práctica", true);
        final JTextArea sis = new JTextArea(5, 26);
        sis.setEditable(false);
        sis.setFont(fuente(18, false));
        sis.setMargin(new Insets(10, 14, 10, 14));
        final JTextField resp = new JTextField();
        resp.setFont(fuente(16, false));
        final JLabel msg = new JLabel(" ");
        msg.setFont(msg.getFont().deriveFont(Font.BOLD, 13f));
        final JLabel marcador = new JLabel();
        final Runnable pintarMarcador = () -> marcador.setText("Aciertos: " + aciertos + " de " + intentos);

        final Runnable[] nuevo = new Runnable[2];
        for (int k = 0; k < 2; k++) {
            final int n = k + 2;
            nuevo[k] = () -> {
                generarPractica(n);
                sis.setText(sistemaPractica);
                resp.setText("");
                msg.setText("Resuelve y escribe la respuesta, por ejemplo: x=2, y=3");
                msg.setForeground(UIManager.getColor("Label.foreground"));
                resp.requestFocusInWindow();
            };
        }
        JButton b22 = new JButton("Nuevo 2\u00d72");
        JButton b33 = new JButton("Nuevo 3\u00d73");
        JButton bComp = new JButton("Comprobar");
        JButton bVer = new JButton("Ver resolución");
        JButton bCerrar = new JButton("Cerrar");
        b22.addActionListener(e -> nuevo[0].run());
        b33.addActionListener(e -> nuevo[1].run());
        final ActionListener comprobar = e -> {
            Map<Character, Motor.Frac> dado = Motor.leerRespuesta(resp.getText());
            if (dado == null) {
                msg.setForeground(new Color(200, 120, 0));
                msg.setText("No entendí la respuesta. Escribe algo como x=2, y=3");
                return;
            }
            intentos++;
            if (dado.equals(solPractica)) {
                aciertos++;
                msg.setForeground(new Color(22, 140, 60));
                msg.setText("¡Correcto! Pulsa Nuevo para otro sistema.");
            } else {
                msg.setForeground(new Color(200, 40, 40));
                msg.setText("Todavía no. Revisa tus cálculos e inténtalo otra vez.");
            }
            pintarMarcador.run();
        };
        bComp.addActionListener(comprobar);
        resp.addActionListener(comprobar);
        bVer.addActionListener(e -> {
            entrada.setText(sistemaPractica);
            cVar.setSelectedIndex(0);
            cEc.setSelectedIndex(0);
            resolverAccion();
            d.dispose();
        });
        bCerrar.addActionListener(e -> d.dispose());

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        arriba.add(b22);
        arriba.add(b33);
        arriba.add(marcador);
        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.add(new JScrollPane(sis), BorderLayout.CENTER);
        JPanel resPanel = new JPanel(new BorderLayout(0, 6));
        resPanel.add(new JLabel("Tu respuesta:"), BorderLayout.NORTH);
        resPanel.add(resp, BorderLayout.CENTER);
        resPanel.add(msg, BorderLayout.SOUTH);
        centro.add(resPanel, BorderLayout.SOUTH);
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        abajo.add(bComp);
        abajo.add(bVer);
        abajo.add(bCerrar);
        JPanel raiz = new JPanel(new BorderLayout(0, 10));
        raiz.setBorder(new EmptyBorder(14, 14, 14, 14));
        raiz.add(arriba, BorderLayout.NORTH);
        raiz.add(centro, BorderLayout.CENTER);
        raiz.add(abajo, BorderLayout.SOUTH);
        d.setContentPane(raiz);
        pintarMarcador.run();
        nuevo[0].run();
        d.pack();
        d.setMinimumSize(new Dimension(520, 400));
        d.setLocationRelativeTo(ventana);
        d.setVisible(true);
    }
}
