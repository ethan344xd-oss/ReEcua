import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

final class Motor {

    static final char S = '\u221a';
    static BigInteger sD = null;

    private Motor() {
    }

    static BigInteger isqrt(BigInteger n) {
        if (n.signum() <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger x = BigInteger.ONE.shiftLeft((n.bitLength() + 1) / 2);
        while (true) {
            BigInteger y = x.add(n.divide(x)).shiftRight(1);
            if (y.compareTo(x) >= 0) {
                return x;
            }
            x = y;
        }
    }

    static BigInteger[] extraerCuadrado(BigInteger n) {
        BigInteger k = BigInteger.ONE;
        BigInteger m = n;
        for (long i = 2; i <= 100000; i++) {
            BigInteger bi = BigInteger.valueOf(i);
            BigInteger b2 = bi.multiply(bi);
            if (b2.compareTo(m) > 0) {
                break;
            }
            while (m.mod(b2).signum() == 0) {
                m = m.divide(b2);
                k = k.multiply(bi);
            }
        }
        return new BigInteger[]{k, m};
    }

    static List<BigInteger> divisores(BigInteger n) {
        if (n.bitLength() > 44) {
            throw new IllegalStateException("los coeficientes son demasiado grandes para buscar raíces racionales");
        }
        long v = n.longValue();
        List<BigInteger> r = new ArrayList<BigInteger>();
        for (long i = 1; i * i <= v; i++) {
            if (v % i == 0) {
                r.add(BigInteger.valueOf(i));
                if (i != v / i) {
                    r.add(BigInteger.valueOf(v / i));
                }
            }
        }
        return r;
    }

    static final class Frac {
        static final Frac CERO = new Frac(BigInteger.ZERO, BigInteger.ONE);
        static final Frac UNO = new Frac(BigInteger.ONE, BigInteger.ONE);
        final BigInteger n;
        final BigInteger d;

        Frac(BigInteger n, BigInteger d) {
            if (d.signum() == 0) {
                throw new ArithmeticException("hay una división entre cero");
            }
            if (d.signum() < 0) {
                n = n.negate();
                d = d.negate();
            }
            BigInteger g = n.gcd(d);
            if (g.compareTo(BigInteger.ONE) > 0) {
                n = n.divide(g);
                d = d.divide(g);
            }
            this.n = n;
            this.d = d;
        }

        static Frac de(long v) {
            return new Frac(BigInteger.valueOf(v), BigInteger.ONE);
        }

        static Frac decimal(String s) {
            BigDecimal b = new BigDecimal(s);
            BigInteger num = b.unscaledValue();
            int sc = b.scale();
            if (sc >= 0) {
                return new Frac(num, BigInteger.TEN.pow(sc));
            }
            return new Frac(num.multiply(BigInteger.TEN.pow(-sc)), BigInteger.ONE);
        }

        Frac mas(Frac o) {
            return new Frac(n.multiply(o.d).add(o.n.multiply(d)), d.multiply(o.d));
        }

        Frac menos(Frac o) {
            return mas(o.neg());
        }

        Frac por(Frac o) {
            return new Frac(n.multiply(o.n), d.multiply(o.d));
        }

        Frac entre(Frac o) {
            return new Frac(n.multiply(o.d), d.multiply(o.n));
        }

        Frac neg() {
            return new Frac(n.negate(), d);
        }

        Frac abs() {
            return new Frac(n.abs(), d);
        }

        Frac pot(int k) {
            Frac r = UNO;
            for (int i = 0; i < k; i++) {
                r = r.por(this);
            }
            return r;
        }

        int signo() {
            return n.signum();
        }

        boolean esCero() {
            return n.signum() == 0;
        }

        boolean esEntero() {
            return d.equals(BigInteger.ONE);
        }

        boolean esUno() {
            return n.equals(BigInteger.ONE) && d.equals(BigInteger.ONE);
        }

        double dbl() {
            return new BigDecimal(n).divide(new BigDecimal(d), MathContext.DECIMAL64).doubleValue();
        }

        String aprox() {
            BigDecimal q = new BigDecimal(n).divide(new BigDecimal(d), 6, RoundingMode.HALF_UP);
            boolean exacto = q.multiply(new BigDecimal(d)).compareTo(new BigDecimal(n)) == 0;
            return (exacto ? "= " : "\u2248 ") + q.stripTrailingZeros().toPlainString();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Frac)) {
                return false;
            }
            Frac f = (Frac) o;
            return n.equals(f.n) && d.equals(f.d);
        }

        @Override
        public int hashCode() {
            return n.hashCode() * 31 + d.hashCode();
        }

        @Override
        public String toString() {
            return esEntero() ? n.toString() : n + "/" + d;
        }
    }

    static final class Mono implements Comparable<Mono> {
        static final Mono UNO = new Mono(new TreeMap<Character, Integer>());
        final TreeMap<Character, Integer> e;

        Mono(TreeMap<Character, Integer> e) {
            this.e = e;
        }

        static Mono de(char v, int ex) {
            TreeMap<Character, Integer> m = new TreeMap<Character, Integer>();
            m.put(v, ex);
            return new Mono(m);
        }

        boolean esCte() {
            return e.isEmpty();
        }

        int exp(char v) {
            Integer x = e.get(v);
            return x == null ? 0 : x;
        }

        int grado() {
            int g = 0;
            for (Map.Entry<Character, Integer> en : e.entrySet()) {
                if (en.getKey() != S) {
                    g += en.getValue();
                }
            }
            return g;
        }

        Mono por(Mono o) {
            TreeMap<Character, Integer> m = new TreeMap<Character, Integer>(e);
            for (Map.Entry<Character, Integer> en : o.e.entrySet()) {
                m.put(en.getKey(), exp(en.getKey()) + en.getValue());
            }
            return new Mono(m);
        }

        Mono sin(char v) {
            TreeMap<Character, Integer> m = new TreeMap<Character, Integer>(e);
            m.remove(v);
            return new Mono(m);
        }

        @Override
        public int compareTo(Mono o) {
            int g = Integer.compare(o.grado(), grado());
            if (g != 0) {
                return g;
            }
            TreeSet<Character> u = new TreeSet<Character>(e.keySet());
            u.addAll(o.e.keySet());
            for (Character c : u) {
                int a = exp(c);
                int b = o.exp(c);
                if (a != b) {
                    return Integer.compare(b, a);
                }
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Mono && e.equals(((Mono) o).e);
        }

        @Override
        public int hashCode() {
            return e.hashCode();
        }
    }

    static final class Poly {
        final TreeMap<Mono, Frac> t = new TreeMap<Mono, Frac>();

        static Poly cte(Frac f) {
            Poly p = new Poly();
            if (!f.esCero()) {
                p.t.put(Mono.UNO, f);
            }
            return p;
        }

        static Poly de(Frac f, Mono m) {
            Poly p = new Poly();
            if (!f.esCero()) {
                p.t.put(m, f);
            }
            return p;
        }

        static Poly var(char v) {
            return de(Frac.UNO, Mono.de(v, 1));
        }

        Frac get(Mono m) {
            Frac f = t.get(m);
            return f == null ? Frac.CERO : f;
        }

        Frac k() {
            return get(Mono.UNO);
        }

        void sumar(Mono m, Frac c) {
            Frac s = get(m).mas(c);
            if (s.esCero()) {
                t.remove(m);
            } else {
                t.put(m, s);
            }
        }

        Poly copia() {
            Poly p = new Poly();
            p.t.putAll(t);
            return p;
        }

        boolean esCero() {
            return t.isEmpty();
        }

        boolean esCte() {
            return t.isEmpty() || (t.size() == 1 && t.containsKey(Mono.UNO));
        }

        boolean igual(Poly o) {
            return t.equals(o.t);
        }

        Poly mas(Poly o) {
            Poly r = copia();
            for (Map.Entry<Mono, Frac> en : o.t.entrySet()) {
                r.sumar(en.getKey(), en.getValue());
            }
            return r;
        }

        Poly esc(Frac f) {
            Poly r = new Poly();
            if (!f.esCero()) {
                for (Map.Entry<Mono, Frac> en : t.entrySet()) {
                    r.t.put(en.getKey(), en.getValue().por(f));
                }
            }
            return r;
        }

        Poly neg() {
            return esc(Frac.de(-1));
        }

        Poly menos(Poly o) {
            return mas(o.neg());
        }

        Poly por(Poly o) {
            Poly r = new Poly();
            for (Map.Entry<Mono, Frac> a : t.entrySet()) {
                for (Map.Entry<Mono, Frac> b : o.t.entrySet()) {
                    r.sumar(a.getKey().por(b.getKey()), a.getValue().por(b.getValue()));
                }
            }
            return r.reducir();
        }

        Poly reducir() {
            boolean hay = false;
            for (Mono m : t.keySet()) {
                if (m.exp(S) >= 2) {
                    hay = true;
                    break;
                }
            }
            if (!hay) {
                return this;
            }
            Poly r = new Poly();
            for (Map.Entry<Mono, Frac> en : t.entrySet()) {
                Mono m = en.getKey();
                int ex = m.exp(S);
                if (ex < 2) {
                    r.sumar(m, en.getValue());
                    continue;
                }
                TreeMap<Character, Integer> mm = new TreeMap<Character, Integer>(m.e);
                if (ex % 2 == 0) {
                    mm.remove(S);
                } else {
                    mm.put(S, 1);
                }
                Frac f = en.getValue().por(new Frac(sD, BigInteger.ONE).pot(ex / 2));
                r.sumar(new Mono(mm), f);
            }
            return r;
        }

        Poly pot(int n) {
            Poly r = cte(Frac.UNO);
            for (int i = 0; i < n; i++) {
                r = r.por(this);
            }
            return r;
        }

        Poly entre(Poly o) {
            if (!o.esCte()) {
                throw new IllegalArgumentException("no admito divisiones entre incógnitas");
            }
            if (o.esCero()) {
                throw new IllegalArgumentException("hay una división entre cero");
            }
            return esc(Frac.UNO.entre(o.k()));
        }

        Poly sustituir(char v, Poly e) {
            Poly r = new Poly();
            for (Map.Entry<Mono, Frac> en : t.entrySet()) {
                Mono m = en.getKey();
                int ex = m.exp(v);
                if (ex == 0) {
                    r.sumar(m, en.getValue());
                } else {
                    r = r.mas(de(en.getValue(), m.sin(v)).por(e.pot(ex)));
                }
            }
            return r;
        }

        TreeSet<Character> vars() {
            TreeSet<Character> r = new TreeSet<Character>();
            for (Mono m : t.keySet()) {
                for (Character c : m.e.keySet()) {
                    if (c != S) {
                        r.add(c);
                    }
                }
            }
            return r;
        }

        boolean tiene(char v) {
            for (Mono m : t.keySet()) {
                if (m.exp(v) > 0) {
                    return true;
                }
            }
            return false;
        }

        boolean tieneS() {
            return tiene(S);
        }

        int grado() {
            int g = 0;
            for (Mono m : t.keySet()) {
                g = Math.max(g, m.grado());
            }
            return g;
        }

        int gradoEn(char v) {
            int g = 0;
            for (Mono m : t.keySet()) {
                g = Math.max(g, m.exp(v));
            }
            return g;
        }

        Frac coefLineal(char v) {
            Frac r = null;
            for (Map.Entry<Mono, Frac> en : t.entrySet()) {
                Mono m = en.getKey();
                if (m.exp(v) == 0) {
                    continue;
                }
                if (m.e.size() == 1 && m.exp(v) == 1) {
                    r = en.getValue();
                } else {
                    return null;
                }
            }
            return r;
        }

        Poly sinCte() {
            Poly r = copia();
            r.t.remove(Mono.UNO);
            return r;
        }

        double eval(Map<Character, Double> val) {
            double s = 0;
            for (Map.Entry<Mono, Frac> en : t.entrySet()) {
                double term = en.getValue().dbl();
                for (Map.Entry<Character, Integer> x : en.getKey().e.entrySet()) {
                    double base;
                    if (x.getKey() == S) {
                        base = Math.sqrt(sD.doubleValue());
                    } else {
                        Double dv = val.get(x.getKey());
                        base = dv == null ? 0 : dv;
                    }
                    term *= Math.pow(base, x.getValue());
                }
                s += term;
            }
            return s;
        }
    }

    static final class Parser {
        final String s;
        int i = 0;

        Parser(String s) {
            this.s = s;
        }

        Poly leer() {
            Poly r = expr();
            if (i < s.length()) {
                char ch = s.charAt(i);
                if (ch == ')') {
                    throw new IllegalArgumentException("hay un paréntesis que se cierra de más");
                }
                throw new IllegalArgumentException("no entiendo el símbolo '" + ch + "'");
            }
            return r;
        }

        Poly expr() {
            Poly r = termino();
            while (i < s.length() && (s.charAt(i) == '+' || s.charAt(i) == '-')) {
                char o = s.charAt(i++);
                Poly t = termino();
                r = o == '+' ? r.mas(t) : r.menos(t);
            }
            return r;
        }

        Poly termino() {
            Poly r = factor();
            while (i < s.length()) {
                char ch = s.charAt(i);
                if (ch == '*') {
                    i++;
                    r = r.por(factor());
                } else if (ch == '/') {
                    i++;
                    r = r.entre(factor());
                } else if (Character.isDigit(ch) || ch == '.' || Character.isLetter(ch) || ch == '(') {
                    r = r.por(factor());
                } else {
                    break;
                }
            }
            return r;
        }

        Poly factor() {
            if (i >= s.length()) {
                throw new IllegalArgumentException("la expresión termina de forma incompleta");
            }
            char ch = s.charAt(i);
            if (ch == '+') {
                i++;
                return factor();
            }
            if (ch == '-') {
                i++;
                return factor().neg();
            }
            return potencia();
        }

        Poly potencia() {
            Poly b = primario();
            while (i < s.length() && s.charAt(i) == '^') {
                i++;
                b = b.pot(exponente());
            }
            return b;
        }

        int exponente() {
            boolean par = i < s.length() && s.charAt(i) == '(';
            if (par) {
                i++;
            }
            if (i < s.length() && s.charAt(i) == '-') {
                throw new IllegalArgumentException("solo admito exponentes enteros positivos");
            }
            if (i < s.length() && s.charAt(i) == '+') {
                i++;
            }
            int j = i;
            while (i < s.length() && Character.isDigit(s.charAt(i))) {
                i++;
            }
            if (j == i) {
                throw new IllegalArgumentException("el exponente debe ser un número entero");
            }
            String d = s.substring(j, i);
            if (d.length() > 1) {
                throw new IllegalArgumentException("exponente demasiado grande (el máximo es 9)");
            }
            if (par) {
                if (i >= s.length() || s.charAt(i) != ')') {
                    throw new IllegalArgumentException("falta cerrar el paréntesis del exponente");
                }
                i++;
            }
            return Integer.parseInt(d);
        }

        Poly primario() {
            char ch = s.charAt(i);
            if (Character.isDigit(ch) || ch == '.') {
                int j = i;
                while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
                    i++;
                }
                String t = s.substring(j, i);
                try {
                    return Poly.cte(Frac.decimal(t));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("número mal escrito: " + t);
                }
            }
            if (Character.isLetter(ch)) {
                i++;
                return Poly.var(Character.toLowerCase(ch));
            }
            if (ch == '(') {
                i++;
                Poly r = expr();
                if (i >= s.length() || s.charAt(i) != ')') {
                    throw new IllegalArgumentException("falta cerrar un paréntesis");
                }
                i++;
                return r;
            }
            throw new IllegalArgumentException("no entiendo el símbolo '" + ch + "'");
        }
    }

    static final class P {
        final boolean neg;
        final String s;

        P(boolean neg, String s) {
            this.neg = neg;
            this.s = s;
        }
    }

    static final class T {
        final Frac f;
        final Mono m;

        T(Frac f, Mono m) {
            this.f = f;
            this.m = m;
        }
    }

    static final class Item {
        final int id;
        Poly p;

        Item(int id, Poly p) {
            this.id = id;
            this.p = p;
        }
    }

    static final class Doc {
        final List<String> bloques = new ArrayList<String>();
        final List<Integer> niveles = new ArrayList<Integer>();
        StringBuilder cur = new StringBuilder();
        int nivelCur = 0;

        void nuevo(String titulo) {
            nuevo(titulo, 0);
        }

        void nuevo(String titulo, int nivel) {
            cerrar();
            nivelCur = nivel;
            cur.append(titulo).append("\n");
        }

        void cerrar() {
            if (cur.length() > 0) {
                bloques.add(cur.toString());
                niveles.add(nivelCur);
            }
            cur = new StringBuilder();
        }

        void l(String s) {
            cur.append(s).append("\n");
        }
    }

    static final class Estado {
        List<Item> trabajo = new ArrayList<Item>();
        List<Character> pv = new ArrayList<Character>();
        List<Poly> pd = new ArrayList<Poly>();
        List<String> pec = new ArrayList<String>();
        int paso = 0;
        boolean primero = true;
        String et = "";

        Estado copia() {
            Estado e = new Estado();
            for (Item it : trabajo) {
                e.trabajo.add(new Item(it.id, it.p));
            }
            e.pv.addAll(pv);
            e.pd.addAll(pd);
            e.pec.addAll(pec);
            e.paso = paso;
            e.primero = primero;
            e.et = et;
            return e;
        }
    }

    static final class Sol {
        final Map<Character, Poly> fin;
        final int libres;
        final String et;

        Sol(Map<Character, Poly> fin, int libres, String et) {
            this.fin = fin;
            this.libres = libres;
            this.et = et;
        }
    }

    static final class Ctx {
        Doc doc = new Doc();
        List<Poly> ecs = new ArrayList<Poly>();
        TreeSet<Character> vars = new TreeSet<Character>();
        List<Sol> sols = new ArrayList<Sol>();
        boolean verificar;
        int metodo;
        int sigId;
        Character prefVar;
        Integer prefEq;
        String fallo;
    }

    static final class Pivote {
        final int idx;
        final int id;
        final char v;
        final Frac a;
        final int score;
        String nota;

        Pivote(int idx, int id, char v, Frac a, int score) {
            this.idx = idx;
            this.id = id;
            this.v = v;
            this.a = a;
            this.score = score;
        }
    }

    static final class Res {
        final String texto;
        final String resumen;
        final List<String> bloques;
        final List<Poly> ecs;
        final TreeSet<Character> vars;
        final List<Map<Character, Double>> numericas;
        int[] niveles;
        List<List<double[]>> tablas;
        List<Map<Character, Poly>> exactas = new ArrayList<Map<Character, Poly>>();

        Res(List<String> bloques, String resumen, List<Poly> ecs, TreeSet<Character> vars,
                List<Map<Character, Double>> numericas) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < bloques.size(); i++) {
                if (i > 0) {
                    b.append("\n");
                }
                b.append(bloques.get(i));
            }
            this.texto = b.toString();
            this.resumen = resumen;
            this.bloques = bloques;
            this.ecs = ecs;
            this.vars = vars;
            this.numericas = numericas;
        }
    }

    static String rep(String s, int n) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) {
            b.append(s);
        }
        return b.toString();
    }

    static String limpiar(String crudo) {
        String sup = "\u2070\u00b9\u00b2\u00b3\u2074\u2075\u2076\u2077\u2078\u2079";
        StringBuilder pre = new StringBuilder();
        boolean enSup = false;
        for (int j = 0; j < crudo.length(); j++) {
            char ch = crudo.charAt(j);
            int ix = sup.indexOf(ch);
            if (ix >= 0) {
                if (!enSup) {
                    pre.append('^');
                }
                pre.append((char) ('0' + ix));
                enSup = true;
            } else {
                enSup = false;
                pre.append(ch);
            }
        }
        String t = Normalizer.normalize(pre.toString(), Normalizer.Form.NFKC);
        StringBuilder b = new StringBuilder();
        for (int j = 0; j < t.length(); j++) {
            char ch = t.charAt(j);
            if (Character.isWhitespace(ch) || Character.isSpaceChar(ch) || ch == '\u200b') {
                continue;
            }
            switch (ch) {
                case '\u2212':
                case '\u2010':
                case '\u2011':
                case '\u2012':
                case '\u2013':
                case '\u2014':
                    b.append('-');
                    break;
                case '\u00b7':
                case '\u2022':
                case '\u2219':
                case '\u22c5':
                case '\u2217':
                    b.append('*');
                    break;
                case '\u00f7':
                case '\u2215':
                case '\u2044':
                    b.append('/');
                    break;
                case '[':
                case '{':
                    b.append('(');
                    break;
                case ']':
                case '}':
                    b.append(')');
                    break;
                case '\u23a7':
                case '\u23a8':
                case '\u23a9':
                case '\u23ab':
                case '\u23ac':
                case '\u23ad':
                    break;
                case ',':
                    b.append('.');
                    break;
                case '\u00d7':
                case '\u2715':
                case '\u2716':
                case '\u2a2f':
                    b.append('\u0001');
                    break;
                case '\u0445':
                case '\u0425':
                    b.append('x');
                    break;
                default:
                    b.append(Character.toLowerCase(ch));
            }
        }
        String s = b.toString().replace("==", "=").replace("**", "^");
        StringBuilder r = new StringBuilder();
        for (int j = 0; j < s.length(); j++) {
            char ch = s.charAt(j);
            if (ch == '\u0001') {
                char p = r.length() > 0 ? r.charAt(r.length() - 1) : 0;
                char q = j + 1 < s.length() ? s.charAt(j + 1) : 0;
                boolean pOk = Character.isDigit(p) || Character.isLetter(p) || p == ')' || p == '.';
                boolean qOk = Character.isDigit(q) || Character.isLetter(q) || q == '(' || q == '.';
                r.append(pOk && qOk ? '*' : 'x');
            } else {
                r.append(ch);
            }
        }
        return r.toString();
    }

    static List<String> separar(String texto) {
        List<String> r = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        for (int j = 0; j < texto.length(); j++) {
            char ch = texto.charAt(j);
            boolean sep = ch == '\n' || ch == '\r' || ch == ';' || ch == '\uff1b';
            if (ch == ',') {
                boolean dec = j > 0 && j + 1 < texto.length()
                        && Character.isDigit(texto.charAt(j - 1)) && Character.isDigit(texto.charAt(j + 1));
                sep = !dec;
            }
            if (sep) {
                r.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        r.add(cur.toString());
        List<String> fin = new ArrayList<String>();
        for (String s : r) {
            String t = s.trim();
            int ab = 0;
            int ce = 0;
            for (int j = 0; j < t.length(); j++) {
                if (t.charAt(j) == '{') {
                    ab++;
                }
                if (t.charAt(j) == '}') {
                    ce++;
                }
            }
            if (ab > ce && t.startsWith("{")) {
                t = t.substring(1).trim();
            }
            if (ce > ab && t.endsWith("}")) {
                t = t.substring(0, t.length() - 1).trim();
            }
            if (!t.isEmpty()) {
                fin.add(t);
            }
        }
        return fin;
    }

    static Poly leer(String s, int n, List<String> avisos) {
        int p = s.indexOf('=');
        if (p < 0) {
            avisos.add("La ecuación (" + n + ") no tiene '=': se tomó como igual a 0.");
            return new Parser(s).leer();
        }
        if (s.indexOf('=', p + 1) >= 0) {
            throw new IllegalArgumentException("hay más de un signo =");
        }
        String a = s.substring(0, p);
        String b = s.substring(p + 1);
        if (a.isEmpty() || b.isEmpty()) {
            throw new IllegalArgumentException("falta un lado de la ecuación");
        }
        return new Parser(a).leer().menos(new Parser(b).leer());
    }

    static Map<Character, Frac> leerRespuesta(String texto) {
        Map<Character, Frac> r = new TreeMap<Character, Frac>();
        try {
            for (String sg : separar(texto)) {
                String lim = limpiar(sg);
                if (lim.isEmpty()) {
                    continue;
                }
                Poly p = leer(lim, 1, new ArrayList<String>());
                TreeSet<Character> vs = p.vars();
                if (vs.size() != 1) {
                    return null;
                }
                char v = vs.first();
                Frac a = p.coefLineal(v);
                if (a == null) {
                    return null;
                }
                Frac val = p.k().neg().entre(a);
                if (r.containsKey(v) && !r.get(v).equals(val)) {
                    return null;
                }
                r.put(v, val);
            }
        } catch (RuntimeException ex) {
            return null;
        }
        return r.isEmpty() ? null : r;
    }

    static String potTxt(int ex) {
        if (ex == 2) {
            return "\u00b2";
        }
        if (ex == 3) {
            return "\u00b3";
        }
        return "^" + ex;
    }

    static String sub(int i) {
        return String.valueOf((char) ('\u2080' + i));
    }

    static String monoTxt(Mono m) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<Character, Integer> en : m.e.entrySet()) {
            if (en.getKey() == S) {
                b.append("\u221a").append(sD);
            } else {
                b.append(en.getKey());
            }
            if (en.getValue() > 1) {
                b.append(potTxt(en.getValue()));
            }
        }
        return b.toString();
    }

    static String coefTxt(Frac a) {
        return a.esEntero() ? a.toString() : "(" + a + ")";
    }

    static String par(Frac f) {
        return f.signo() >= 0 && f.esEntero() ? f.toString() : "(" + f + ")";
    }

    static P pTermino(Frac f, Mono m) {
        Frac a = f.abs();
        String s = m.esCte() ? a.toString() : (a.esUno() ? "" : coefTxt(a)) + monoTxt(m);
        return new P(f.signo() < 0, s);
    }

    static P terminoSub(Frac f, Mono m, Map<Character, Poly> sub) {
        boolean usa = false;
        if (sub != null) {
            for (Character v : m.e.keySet()) {
                if (sub.containsKey(v)) {
                    usa = true;
                }
            }
        }
        if (!usa) {
            return pTermino(f, m);
        }
        StringBuilder cuerpo = new StringBuilder();
        for (Map.Entry<Character, Integer> en : m.e.entrySet()) {
            char v = en.getKey();
            int ex = en.getValue();
            if (cuerpo.length() > 0) {
                cuerpo.append("\u00b7");
            }
            cuerpo.append(sub.containsKey(v) ? envolver(sub.get(v)) : monoTxt(Mono.de(v, 1)));
            if (ex > 1) {
                cuerpo.append(potTxt(ex));
            }
        }
        Frac a = f.abs();
        return new P(f.signo() < 0, a.esUno() ? cuerpo.toString() : coefTxt(a) + "\u00b7" + cuerpo);
    }

    static String unirP(List<P> ps) {
        if (ps.isEmpty()) {
            return "0";
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < ps.size(); i++) {
            P p = ps.get(i);
            if (i == 0) {
                b.append(p.neg ? "-" : "").append(p.s);
            } else {
                b.append(p.neg ? " - " : " + ").append(p.s);
            }
        }
        return b.toString();
    }

    static String envolver(Poly e) {
        if (e.esCte()) {
            Frac k = e.k();
            return k.signo() >= 0 && k.esEntero() ? k.toString() : "(" + k + ")";
        }
        return "(" + unirP(piezas(e, true, null)) + ")";
    }

    static List<P> piezas(Poly p, boolean ctePrimero, Map<Character, Poly> sub) {
        List<P> r = new ArrayList<P>();
        P c = null;
        for (Map.Entry<Mono, Frac> en : p.t.entrySet()) {
            if (en.getKey().esCte()) {
                c = pTermino(en.getValue(), en.getKey());
            } else {
                r.add(terminoSub(en.getValue(), en.getKey(), sub));
            }
        }
        if (c != null) {
            if (ctePrimero) {
                r.add(0, c);
            } else {
                r.add(c);
            }
        }
        return r;
    }

    static List<P> aP(List<T> ts) {
        List<P> r = new ArrayList<P>();
        for (T t : ts) {
            r.add(pTermino(t.f, t.m));
        }
        return r;
    }

    static List<T> expandido(Poly p, char v, Poly e) {
        List<T> r = new ArrayList<T>();
        for (Map.Entry<Mono, Frac> en : p.t.entrySet()) {
            Mono m = en.getKey();
            if (m.esCte()) {
                continue;
            }
            if (m.exp(v) == 0) {
                r.add(new T(en.getValue(), m));
                continue;
            }
            Poly parte = Poly.de(en.getValue(), m.sin(v)).por(e.pot(m.exp(v)));
            if (!parte.k().esCero()) {
                r.add(new T(parte.k(), Mono.UNO));
            }
            for (Map.Entry<Mono, Frac> q : parte.t.entrySet()) {
                if (!q.getKey().esCte()) {
                    r.add(new T(q.getValue(), q.getKey()));
                }
            }
        }
        return r;
    }

    static String lhs(Poly p) {
        return unirP(piezas(p.sinCte(), false, null));
    }

    static String rhs(Poly p) {
        return unirP(piezas(Poly.cte(p.k().neg()), false, null));
    }

    static String ec(Poly p) {
        return lhs(p) + " = " + rhs(p);
    }

    static String def(char v, Poly e) {
        return v + " = " + unirP(piezas(e, true, null));
    }

    static String fmt(double v) {
        return BigDecimal.valueOf(v).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    static String aprox(Poly c) {
        if (c.esCte()) {
            Frac k = c.k();
            return k.esEntero() ? null : k.aprox();
        }
        if (c.vars().isEmpty()) {
            return "\u2248 " + fmt(c.eval(new HashMap<Character, Double>()));
        }
        return null;
    }

    static void imprimir(Doc doc, List<String> ls, String sangria) {
        String prev = null;
        for (String s : ls) {
            if (s.equals(prev)) {
                continue;
            }
            doc.l(sangria + s);
            prev = s;
        }
    }

    static String tag(Estado st) {
        return st.et.isEmpty() ? "" : " \u00b7 rama " + st.et;
    }

    static Poly polyDe(Frac[] c, int g, char v) {
        Poly r = new Poly();
        for (int i = 0; i <= g; i++) {
            if (!c[i].esCero()) {
                r.sumar(i == 0 ? Mono.UNO : Mono.de(v, i), c[i]);
            }
        }
        return r;
    }

    static boolean contiene(List<Poly> l, Poly p) {
        for (Poly q : l) {
            if (q.igual(p)) {
                return true;
            }
        }
        return false;
    }

    static List<Poly> cuadratica(Frac a, Frac b, Frac c, char v, Doc doc) {
        List<Poly> r = new ArrayList<Poly>();
        doc.l("    a = " + a + ",  b = " + b + ",  c = " + c);
        Frac disc = b.por(b).menos(Frac.de(4).por(a).por(c));
        doc.l("    \u0394 = " + par(b) + "\u00b2 - 4\u00b7" + par(a) + "\u00b7" + par(c) + " = " + disc);
        if (disc.signo() < 0) {
            doc.l("    \u0394 < 0: no hay raíces reales.");
            return r;
        }
        Frac dos = Frac.de(2).por(a);
        if (disc.esCero()) {
            doc.l("    \u0394 = 0: hay una única raíz.");
            doc.l("    " + v + " = -b / 2a = " + b.neg() + " / " + par(dos));
            r.add(Poly.cte(b.neg().entre(dos)));
            return r;
        }
        BigInteger nn = disc.n.multiply(disc.d);
        BigInteger q0 = isqrt(nn);
        if (q0.multiply(q0).equals(nn)) {
            Frac sq = new Frac(q0, disc.d);
            doc.l("    " + v + " = (-b \u00b1 \u221a\u0394) / 2a = (" + b.neg() + " \u00b1 " + sq + ") / " + par(dos));
            r.add(Poly.cte(b.neg().mas(sq).entre(dos)));
            r.add(Poly.cte(b.neg().menos(sq).entre(dos)));
        } else {
            BigInteger[] km = extraerCuadrado(nn);
            if (sD != null && !sD.equals(km[1])) {
                throw new IllegalStateException("aparecen dos raíces cuadradas distintas y no puedo combinarlas");
            }
            sD = km[1];
            Frac q = new Frac(km[0], disc.d);
            String raiz = (q.esUno() ? "" : coefTxt(q)) + "\u221a" + sD;
            doc.l("    \u221a\u0394 = " + raiz);
            doc.l("    " + v + " = (-b \u00b1 \u221a\u0394) / 2a = (" + b.neg() + " \u00b1 " + raiz + ") / " + par(dos));
            Frac base = b.neg().entre(dos);
            Frac cs = q.entre(dos);
            r.add(Poly.cte(base).mas(Poly.de(cs, Mono.de(S, 1))));
            r.add(Poly.cte(base).mas(Poly.de(cs.neg(), Mono.de(S, 1))));
        }
        return r;
    }

    static Frac evalFrac(Frac[] c, int g, Frac x) {
        Frac v = c[g];
        for (int i = g - 1; i >= 0; i--) {
            v = v.por(x).mas(c[i]);
        }
        return v;
    }

    static Frac buscarRaiz(Frac[] c, int g) {
        BigInteger mcm = BigInteger.ONE;
        for (int i = 0; i <= g; i++) {
            mcm = mcm.multiply(c[i].d).divide(mcm.gcd(c[i].d));
        }
        BigInteger a0 = c[0].n.multiply(mcm.divide(c[0].d)).abs();
        BigInteger an = c[g].n.multiply(mcm.divide(c[g].d)).abs();
        List<BigInteger> ps = divisores(a0);
        List<BigInteger> qs = divisores(an);
        if ((long) ps.size() * qs.size() > 4000000L) {
            throw new IllegalStateException("hay demasiadas posibles raíces racionales que probar");
        }
        for (BigInteger p : ps) {
            for (BigInteger q : qs) {
                Frac x = new Frac(p, q);
                if (evalFrac(c, g, x).esCero()) {
                    return x;
                }
                if (evalFrac(c, g, x.neg()).esCero()) {
                    return x.neg();
                }
            }
        }
        return null;
    }

    static Frac[] dividir(Frac[] c, int g, Frac r) {
        Frac[] b = new Frac[g];
        b[g - 1] = c[g];
        for (int i = g - 1; i >= 1; i--) {
            b[i - 1] = c[i].mas(r.por(b[i]));
        }
        return b;
    }

    static List<Poly> general(Frac[] c0, int d, char v, Doc doc) {
        Frac[] c = c0;
        int g = d;
        List<Frac> rac = new ArrayList<Frac>();
        while (g > 0 && c[0].esCero()) {
            if (!rac.contains(Frac.CERO)) {
                rac.add(Frac.CERO);
            }
            c = Arrays.copyOfRange(c, 1, g + 1);
            g--;
        }
        while (g > 2) {
            Frac r = buscarRaiz(c, g);
            if (r == null) {
                break;
            }
            if (!rac.contains(r)) {
                rac.add(r);
            }
            c = dividir(c, g, r);
            g--;
        }
        if (g > 2) {
            throw new IllegalStateException("la ecuación es de grado " + d
                    + " y no encuentro todas sus raíces (resuelvo grado 2, o mayor si tiene raíces racionales)");
        }
        List<Poly> res = new ArrayList<Poly>();
        if (!rac.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Frac f : rac) {
                if (sb.length() > 0) {
                    sb.append(",  ");
                }
                sb.append(v).append(" = ").append(f);
            }
            doc.l("    Raíces racionales encontradas: " + sb);
        }
        for (Frac f : rac) {
            res.add(Poly.cte(f));
        }
        if (g == 2) {
            doc.l("    Queda " + ec(polyDe(c, g, v)) + ":");
            for (Poly q : cuadratica(c[2], c[1], c[0], v, doc)) {
                if (!contiene(res, q)) {
                    res.add(q);
                }
            }
        } else if (g == 1) {
            Poly q = Poly.cte(c[0].neg().entre(c[1]));
            if (!contiene(res, q)) {
                res.add(q);
            }
        }
        return res;
    }

    static List<Poly> raices(Poly p, char v, Doc doc) {
        if (p.tieneS()) {
            throw new IllegalStateException("la ecuación ya contiene una raíz cuadrada y no puedo seguir resolviéndola");
        }
        int d = p.gradoEn(v);
        Frac[] c = new Frac[d + 1];
        Arrays.fill(c, Frac.CERO);
        for (Map.Entry<Mono, Frac> en : p.t.entrySet()) {
            int ex = en.getKey().exp(v);
            c[ex] = c[ex].mas(en.getValue());
        }
        List<Poly> res = d == 2 ? cuadratica(c[2], c[1], c[0], v, doc) : general(c, d, v, doc);
        final Map<Character, Double> vacio = new HashMap<Character, Double>();
        Collections.sort(res, new Comparator<Poly>() {
            @Override
            public int compare(Poly a, Poly b) {
                return Double.compare(a.eval(vacio), b.eval(vacio));
            }
        });
        for (int i = 0; i < res.size(); i++) {
            String ap = aprox(res.get(i));
            doc.l("    " + (res.size() > 1 ? v + sub(i + 1) : String.valueOf(v)) + " = "
                    + unirP(piezas(res.get(i), true, null)) + (ap == null ? "" : "      (" + ap + ")"));
        }
        return res;
    }

    static Pivote elegir(Ctx cx, Estado st) {
        List<Pivote> cand = new ArrayList<Pivote>();
        for (int i = 0; i < st.trabajo.size(); i++) {
            Item it = st.trabajo.get(i);
            for (Character v : it.p.vars()) {
                Frac a = it.p.coefLineal(v);
                if (a != null) {
                    int sc = it.p.grado() * 100000 + (a.abs().esUno() ? 0 : 10000) + it.p.t.size() * 100 + i;
                    cand.add(new Pivote(i, it.id, v, a, sc));
                }
            }
        }
        if (cand.isEmpty()) {
            return null;
        }
        String nota = null;
        if (cx.prefVar != null) {
            List<Pivote> f = new ArrayList<Pivote>();
            for (Pivote q : cand) {
                if (q.v == cx.prefVar) {
                    f.add(q);
                }
            }
            if (!f.isEmpty()) {
                cand = f;
            } else {
                nota = "No se puede despejar " + cx.prefVar + " en este momento; se eligió otra opción";
            }
        }
        if (cx.prefEq != null && st.primero) {
            List<Pivote> f = new ArrayList<Pivote>();
            for (Pivote q : cand) {
                if (q.id == cx.prefEq) {
                    f.add(q);
                }
            }
            if (!f.isEmpty()) {
                cand = f;
            } else {
                String n2 = "En la ecuación (" + cx.prefEq + ") no se puede despejar eso; se eligió otra opción";
                nota = nota == null ? n2 : nota + ". " + n2;
            }
        }
        Pivote mejor = cand.get(0);
        for (Pivote q : cand) {
            if (q.score < mejor.score) {
                mejor = q;
            }
        }
        mejor.nota = nota;
        return mejor;
    }

    static void sustituirEnRestantes(Ctx cx, Estado st, char v, Poly e) {
        Doc doc = cx.doc;
        for (Item f : st.trabajo) {
            if (!f.p.tiene(v)) {
                continue;
            }
            Map<Character, Poly> sub = Collections.singletonMap(Character.valueOf(v), e);
            Poly nueva = f.p.sustituir(v, e);
            doc.l("  Sustituir " + def(v, e) + " en la ecuación (" + f.id + "):");
            List<String> m = new ArrayList<String>();
            m.add(ec(f.p));
            m.add(unirP(piezas(f.p.sinCte(), false, sub)) + " = " + rhs(f.p));
            m.add(unirP(aP(expandido(f.p, v, e))) + " = " + rhs(f.p));
            m.add(ec(nueva));
            imprimir(doc, m, "      ");
            f.p = nueva;
        }
    }

    static List<String> lineasDespeje(Poly p, char v, Frac a, Poly derecha, Poly e) {
        List<String> ls = new ArrayList<String>();
        ls.add(ec(p));
        List<P> pr = piezas(derecha, true, null);
        ls.add(unirP(Arrays.asList(pTermino(a, Mono.de(v, 1)))) + " = " + unirP(pr));
        if (!a.esUno()) {
            String numr = unirP(pr);
            if (pr.size() > 1 || numr.contains("/")) {
                numr = "(" + numr + ")";
            }
            ls.add(v + " = " + numr + " / " + envolver(Poly.cte(a)));
        }
        ls.add(def(v, e));
        return ls;
    }

    static void verificacion(Doc doc, List<Poly> ecs, Map<Character, Poly> fin) {
        for (int i = 0; i < ecs.size(); i++) {
            Poly p = ecs.get(i);
            Poly solo = p.sinCte();
            Poly val = solo;
            for (Character w : new ArrayList<Character>(solo.vars())) {
                val = val.sustituir(w, fin.get(w));
            }
            Poly der = Poly.cte(p.k().neg());
            boolean ok = val.menos(der).esCero();
            List<String> m = new ArrayList<String>();
            m.add("(" + (i + 1) + ")  " + unirP(piezas(solo, false, fin)) + " = " + rhs(p));
            m.add("     " + unirP(piezas(val, true, null)) + " = " + unirP(piezas(der, true, null))
                    + (ok ? "   \u2713" : "   \u2717"));
            imprimir(doc, m, "  ");
        }
    }

    static String lhsPar(Poly p) {
        String s = lhs(p);
        return piezas(p.sinCte(), false, null).size() > 1 ? "(" + s + ")" : s;
    }

    static String rhsPar(Poly p) {
        String s = rhs(p);
        return s.startsWith("-") ? "(" + s + ")" : s;
    }

    static String ids(List<Item> its) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < its.size(); i++) {
            if (i > 0) {
                b.append(i == its.size() - 1 ? " y " : ", ");
            }
            b.append("(").append(its.get(i).id).append(")");
        }
        return b.toString();
    }

    static final class Mult {
        Frac mi;
        Frac mj;
        boolean restar;
    }

    static Mult mult(Frac ai, Frac aj) {
        Frac t = aj.entre(ai);
        Mult m = new Mult();
        BigInteger r = t.n;
        m.restar = r.signum() > 0;
        m.mi = new Frac(r.abs(), BigInteger.ONE);
        m.mj = new Frac(t.d, BigInteger.ONE);
        return m;
    }

    static int tope(Frac f) {
        return f.n.min(BigInteger.valueOf(1000)).intValue();
    }

    static final class Cand {
        final char v;
        final int piv;
        final int costo;
        String nota;

        Cand(char v, int piv, int costo) {
            this.v = v;
            this.piv = piv;
            this.costo = costo;
        }
    }

    static boolean pasoIgualRed(Ctx cx, Estado st) {
        Doc doc = cx.doc;
        boolean igual = cx.metodo == 1;
        TreeSet<Character> todas = new TreeSet<Character>();
        for (Item it : st.trabajo) {
            todas.addAll(it.p.vars());
        }
        List<Character> candV = new ArrayList<Character>();
        for (Character v : todas) {
            boolean ok = true;
            for (Item it : st.trabajo) {
                if (it.p.tiene(v) && it.p.coefLineal(v) == null) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                candV.add(v);
            }
        }
        if (candV.isEmpty()) {
            return false;
        }
        String nota = null;
        if (cx.prefVar != null) {
            if (candV.contains(cx.prefVar)) {
                candV = new ArrayList<Character>();
                candV.add(cx.prefVar);
            } else {
                nota = "No se puede usar " + cx.prefVar + " en este momento; se eligió otra opción";
            }
        }
        Cand mejor = null;
        for (Character v : candV) {
            List<Integer> idx = new ArrayList<Integer>();
            for (int i = 0; i < st.trabajo.size(); i++) {
                if (st.trabajo.get(i).p.tiene(v)) {
                    idx.add(i);
                }
            }
            Integer forzada = null;
            String notaEq = null;
            if (cx.prefEq != null && st.primero) {
                for (int i : idx) {
                    if (st.trabajo.get(i).id == cx.prefEq) {
                        forzada = i;
                    }
                }
                if (forzada == null) {
                    notaEq = "En la ecuación (" + cx.prefEq + ") no se puede usar " + v + "; se eligió otra opción";
                }
            }
            int piv = -1;
            int costoPiv = Integer.MAX_VALUE;
            int suma = 0;
            for (int i : idx) {
                Poly p = st.trabajo.get(i).p;
                Frac a = p.coefLineal(v);
                int c;
                if (igual) {
                    c = (a.abs().esUno() ? 0 : 1000) + p.t.size() * 10 + i;
                    suma += c;
                } else {
                    c = 0;
                    for (int j : idx) {
                        if (j != i) {
                            Mult m = mult(a, st.trabajo.get(j).p.coefLineal(v));
                            c += tope(m.mi) + tope(m.mj);
                        }
                    }
                    c = c * 100 + p.t.size() * 10 + i;
                }
                if (forzada != null && i != forzada) {
                    continue;
                }
                if (c < costoPiv) {
                    costoPiv = c;
                    piv = i;
                }
            }
            int costoVar = (igual ? suma : costoPiv) + (idx.size() == 1 ? 100000 : 0);
            if (mejor == null || costoVar < mejor.costo) {
                mejor = new Cand(v, piv, costoVar);
                mejor.nota = notaEq;
            }
        }

        char v = mejor.v;
        Item base = st.trabajo.get(mejor.piv);
        List<Item> otros = new ArrayList<Item>();
        for (Item it : st.trabajo) {
            if (it != base && it.p.tiene(v)) {
                otros.add(it);
            }
        }
        String notaFinal = nota == null ? mejor.nota : (mejor.nota == null ? nota : nota + ". " + mejor.nota);
        Frac ab = base.p.coefLineal(v);
        Poly derB = base.p.sustituir(v, Poly.cte(Frac.CERO)).neg();
        Poly eb = derB.esc(Frac.UNO.entre(ab));
        st.primero = false;

        if (otros.isEmpty()) {
            st.paso++;
            doc.nuevo("PASO " + st.paso + " \u00b7 Despejar " + v + " de la ecuación (" + base.id + ")" + tag(st));
            if (notaFinal != null) {
                doc.l("    (" + notaFinal + ")");
            }
            imprimir(doc, lineasDespeje(base.p, v, ab, derB, eb), "    ");
            if (st.trabajo.size() > 1) {
                doc.l("  " + v + " no aparece en otras ecuaciones: se despeja ahora y se usará al final.");
            }
            st.trabajo.remove(base);
            st.pv.add(v);
            st.pd.add(eb);
            st.pec.add("(" + base.id + ")  " + ec(base.p));
            return true;
        }

        List<Item> todos = new ArrayList<Item>();
        todos.add(base);
        todos.addAll(otros);
        List<Item> nuevos = new ArrayList<Item>();

        if (igual) {
            st.paso++;
            doc.nuevo("PASO " + st.paso + " \u00b7 Despejar " + v + " en las ecuaciones " + ids(todos) + tag(st));
            if (notaFinal != null) {
                doc.l("    (" + notaFinal + ")");
            }
            List<Poly> exprs = new ArrayList<Poly>();
            for (Item it : todos) {
                Frac a = it.p.coefLineal(v);
                Poly der = it.p.sustituir(v, Poly.cte(Frac.CERO)).neg();
                Poly e = der.esc(Frac.UNO.entre(a));
                exprs.add(e);
                doc.l("  Ecuación (" + it.id + "):");
                imprimir(doc, lineasDespeje(it.p, v, a, der, e), "      ");
            }
            st.paso++;
            doc.nuevo("PASO " + st.paso + " \u00b7 Igualar las expresiones de " + v + tag(st));
            for (int k = 1; k < exprs.size(); k++) {
                Poly np = exprs.get(0).menos(exprs.get(k));
                int nid = cx.sigId++;
                doc.l("  Igualamos las expresiones de (" + base.id + ") y (" + todos.get(k).id + "):");
                List<String> m = new ArrayList<String>();
                m.add(unirP(piezas(exprs.get(0), true, null)) + " = " + unirP(piezas(exprs.get(k), true, null)));
                m.add(ec(np) + "      \u2190 nueva ecuación (" + nid + ")");
                imprimir(doc, m, "    ");
                nuevos.add(new Item(nid, np));
            }
            st.pec.add("");
        } else {
            st.paso++;
            doc.nuevo("PASO " + st.paso + " \u00b7 Reducción: eliminar " + v + " con las ecuaciones " + ids(todos) + tag(st));
            if (notaFinal != null) {
                doc.l("    (" + notaFinal + ")");
            }
            for (Item o : otros) {
                Frac ai = base.p.coefLineal(v);
                Frac aj = o.p.coefLineal(v);
                Mult m = mult(ai, aj);
                Poly pi2 = base.p.esc(m.mi);
                Poly pj2 = o.p.esc(m.mj);
                Poly np = m.restar ? pj2.menos(pi2) : pj2.mas(pi2);
                int nid = cx.sigId++;
                if (otros.size() > 1) {
                    doc.l("");
                    doc.l("  Con las ecuaciones (" + base.id + ") y (" + o.id + "):");
                }
                doc.l("    Coeficiente de " + v + ":  (" + base.id + ") \u2192 " + ai + ",  (" + o.id + ") \u2192 " + aj);
                if (m.mi.esUno() && m.mj.esUno()) {
                    doc.l(m.restar ? "    Los coeficientes son iguales: restamos las ecuaciones."
                            : "    Los coeficientes ya son opuestos: sumamos las ecuaciones.");
                } else {
                    doc.l("    Multiplicamos para igualar los coeficientes de " + v + " (en valor absoluto):");
                    if (!m.mi.esUno()) {
                        doc.l("      (" + base.id + ") \u00d7 " + m.mi + ":   " + ec(pi2));
                    }
                    if (!m.mj.esUno()) {
                        doc.l("      (" + o.id + ") \u00d7 " + m.mj + ":   " + ec(pj2));
                    }
                    doc.l(m.restar ? "    Ahora los coeficientes son iguales: restamos."
                            : "    Ahora los coeficientes son opuestos: sumamos.");
                }
                String raw = m.restar
                        ? lhsPar(pj2) + " - " + lhsPar(pi2) + " = " + rhsPar(pj2) + " - " + rhsPar(pi2)
                        : lhsPar(pi2) + " + " + lhsPar(pj2) + " = " + rhsPar(pi2) + " + " + rhsPar(pj2);
                List<String> ls = new ArrayList<String>();
                ls.add(raw);
                ls.add(ec(np) + "      \u2190 nueva ecuación (" + nid + ")");
                imprimir(doc, ls, "    ");
                nuevos.add(new Item(nid, np));
            }
            doc.l("");
            doc.l("  La ecuación (" + base.id + ") se guarda para hallar " + v + " al final.");
            st.pec.add("(" + base.id + ")  " + ec(base.p));
        }
        st.pv.add(v);
        st.pd.add(eb);
        st.trabajo.removeAll(todos);
        st.trabajo.addAll(nuevos);
        return true;
    }

    static void cerrar(Ctx cx, Estado st) {
        Doc doc = cx.doc;
        Map<Character, Poly> fin = new LinkedHashMap<Character, Poly>();
        int n = st.pv.size();
        if (n > 1) {
            doc.nuevo("PASO " + (st.paso + 1) + " \u00b7 Sustitución hacia atrás" + tag(st));
        }
        for (int i = n - 1; i >= 0; i--) {
            char v = st.pv.get(i);
            Poly d = st.pd.get(i);
            List<String> m = new ArrayList<String>();
            if (cx.metodo == 2 && !st.pec.get(i).isEmpty()) {
                m.add("Con la ecuación " + st.pec.get(i) + ":");
            }
            m.add(def(v, d));
            TreeSet<Character> dv = d.vars();
            boolean usa = false;
            for (Character w : dv) {
                if (fin.containsKey(w)) {
                    usa = true;
                }
            }
            Poly nd = d;
            if (usa) {
                m.add(v + " = " + unirP(piezas(d, true, fin)));
                for (Character w : new ArrayList<Character>(dv)) {
                    if (fin.containsKey(w)) {
                        nd = nd.sustituir(w, fin.get(w));
                    }
                }
                m.add(def(v, nd));
            }
            fin.put(v, nd);
            if (n > 1) {
                imprimir(doc, m, "    ");
            }
        }
        int libres = 0;
        for (Character v : cx.vars) {
            if (!fin.containsKey(v)) {
                libres++;
            }
        }
        if (cx.verificar && libres == 0 && !cx.vars.isEmpty()) {
            doc.nuevo("VERIFICACIÓN" + tag(st));
            verificacion(doc, cx.ecs, fin);
        }
        cx.sols.add(new Sol(fin, libres, st.et));
    }

    static void avanzar(Ctx cx, Estado st) {
        Doc doc = cx.doc;
        while (true) {
            boolean imposible = false;
            for (int i = 0; i < st.trabajo.size();) {
                Item it = st.trabajo.get(i);
                if (it.p.vars().isEmpty()) {
                    doc.l("");
                    if (it.p.esCero()) {
                        doc.l("  La ecuación (" + it.id
                                + ") se reduce a 0 = 0: siempre se cumple, así que sobra y se descarta.");
                        st.trabajo.remove(i);
                    } else {
                        doc.l("  La ecuación (" + it.id + ") queda " + ec(it.p) + ": es imposible.");
                        imposible = true;
                        break;
                    }
                } else {
                    i++;
                }
            }
            if (imposible) {
                doc.l("  Esta rama no tiene solución.");
                return;
            }
            if (st.trabajo.isEmpty()) {
                cerrar(cx, st);
                return;
            }
            if (cx.sols.size() > 24) {
                cx.fallo = "hay demasiadas soluciones para mostrarlas todas";
                return;
            }

            if (cx.metodo == 0) {
                Pivote pv = elegir(cx, st);
                if (pv != null) {
                    st.paso++;
                    Item it = st.trabajo.get(pv.idx);
                    Poly p = it.p;
                    char v = pv.v;
                    Frac a = pv.a;
                    Poly derecha = p.sustituir(v, Poly.cte(Frac.CERO)).neg();
                    Poly e = derecha.esc(Frac.UNO.entre(a));
                    doc.nuevo("PASO " + st.paso + " \u00b7 Despejar " + v + " de la ecuación (" + it.id + ")" + tag(st));
                    if (pv.nota != null) {
                        doc.l("    (" + pv.nota + ")");
                    }
                    imprimir(doc, lineasDespeje(p, v, a, derecha, e), "    ");
                    st.trabajo.remove(pv.idx);
                    st.pv.add(v);
                    st.pd.add(e);
                    st.pec.add("");
                    st.primero = false;
                    sustituirEnRestantes(cx, st, v, e);
                    continue;
                }
            } else if (pasoIgualRed(cx, st)) {
                continue;
            }

            Item uni = null;
            for (Item it : st.trabajo) {
                if (it.p.vars().size() == 1 && (uni == null || it.p.grado() < uni.p.grado())) {
                    uni = it;
                }
            }
            if (uni == null) {
                if (cx.metodo == 0) {
                    cx.fallo = "ninguna ecuación permite despejar una incógnita; hace falta al menos una que sea lineal"
                            + " en alguna incógnita (o una con una sola incógnita)";
                } else {
                    cx.fallo = "este sistema no se puede resolver por " + (cx.metodo == 1 ? "igualación" : "reducción")
                            + ": ninguna incógnita se puede despejar o eliminar en todas las ecuaciones donde aparece;"
                            + " prueba con el método de sustitución";
                }
                doc.l("");
                doc.l("  No puedo seguir: " + cx.fallo + ".");
                return;
            }
            char v = uni.p.vars().first();
            st.paso++;
            st.primero = false;
            doc.nuevo("PASO " + st.paso + " \u00b7 Resolver la ecuación (" + uni.id + ") de grado "
                    + uni.p.gradoEn(v) + " en " + v + tag(st));
            doc.l("    " + ec(uni.p));
            List<Poly> rs;
            try {
                rs = raices(uni.p, v, doc);
            } catch (IllegalStateException ex) {
                cx.fallo = ex.getMessage();
                doc.l("    No puedo resolverla: " + ex.getMessage() + ".");
                return;
            }
            st.trabajo.remove(uni);
            if (rs.isEmpty()) {
                doc.l("  Esta rama no tiene solución real.");
                return;
            }
            if (rs.size() == 1) {
                st.pv.add(v);
                st.pd.add(rs.get(0));
                st.pec.add("");
                sustituirEnRestantes(cx, st, v, rs.get(0));
                continue;
            }
            doc.l("  Hay " + rs.size() + " valores posibles para " + v + ": se sigue cada uno por separado.");
            for (int r = 0; r < rs.size(); r++) {
                Estado hijo = st.copia();
                hijo.et = st.et.isEmpty() ? String.valueOf(r + 1) : st.et + "." + (r + 1);
                doc.nuevo("RAMA " + hijo.et + " \u00b7 " + def(v, rs.get(r)));
                hijo.pv.add(v);
                hijo.pd.add(rs.get(r));
                hijo.pec.add("");
                sustituirEnRestantes(cx, hijo, v, rs.get(r));
                avanzar(cx, hijo);
            }
            return;
        }
    }

    static final String[] DESC_METODO = {
        "sustitución (se despeja una incógnita y se sustituye en las demás ecuaciones)",
        "igualación (se despeja la misma incógnita en las ecuaciones y se igualan las expresiones)",
        "reducción (se suman o restan las ecuaciones para eliminar una incógnita)"
    };

    static Res resolver(String texto, boolean verificar, Character prefVar, Integer prefEq) {
        return resolver(texto, verificar, prefVar, prefEq, 0);
    }

    static Res resolver(String texto, boolean verificar, Character prefVar, Integer prefEq, int metodo) {
        sD = null;
        List<String> segs = separar(texto);
        List<Poly> ecs = new ArrayList<Poly>();
        List<String> orig = new ArrayList<String>();
        List<String> errores = new ArrayList<String>();
        List<String> avisos = new ArrayList<String>();
        int num = 0;
        for (String sg : segs) {
            String lim = limpiar(sg);
            if (lim.isEmpty()) {
                continue;
            }
            num++;
            try {
                ecs.add(leer(lim, num, avisos));
                orig.add(sg);
            } catch (RuntimeException ex) {
                errores.add("  (" + num + ")  " + sg + "\n        \u2192 " + ex.getMessage());
            }
        }
        List<String> un = new ArrayList<String>();
        TreeSet<Character> vacias = new TreeSet<Character>();
        if (num == 0) {
            un.add("Escribe al menos una ecuación y pulsa Resolver.\n");
            return new Res(un, "Sin ecuaciones", new ArrayList<Poly>(), vacias, new ArrayList<Map<Character, Double>>());
        }
        if (!errores.isEmpty()) {
            StringBuilder o = new StringBuilder("No pude leer algunas ecuaciones:\n\n");
            for (String e : errores) {
                o.append(e).append("\n\n");
            }
            o.append("Revisa el formato en Ayuda \u203a Formato aceptado.\n");
            un.add(o.toString());
            return new Res(un, "Hay errores en la entrada", new ArrayList<Poly>(), vacias,
                    new ArrayList<Map<Character, Double>>());
        }

        Ctx cx = new Ctx();
        cx.verificar = verificar;
        cx.metodo = metodo;
        cx.sigId = ecs.size() + 1;
        cx.ecs = ecs;
        for (Poly p : ecs) {
            cx.vars.addAll(p.vars());
        }
        cx.prefVar = prefVar != null && cx.vars.contains(prefVar) ? prefVar : null;
        cx.prefEq = prefEq != null && prefEq >= 1 && prefEq <= ecs.size() ? prefEq : null;
        Doc doc = cx.doc;
        String linea = rep("\u2500", 58);

        doc.nuevo("ECUACIONES\n" + linea);
        for (int i = 0; i < ecs.size(); i++) {
            String std = ec(ecs.get(i));
            String l = "  (" + (i + 1) + ")  " + std;
            if (!orig.get(i).replaceAll("\\s", "").equals(std.replaceAll("\\s", ""))) {
                l += "      \u2190 escrito: " + orig.get(i);
            }
            doc.l(l);
        }
        for (String a : avisos) {
            doc.l("  Nota: " + a);
        }
        StringBuilder nv = new StringBuilder();
        for (Character v : cx.vars) {
            if (nv.length() > 0) {
                nv.append(", ");
            }
            nv.append(v);
        }
        doc.l("");
        doc.l("  Incógnitas: " + (cx.vars.isEmpty() ? "ninguna" : nv.toString()));
        doc.l("  Método: " + DESC_METODO[Math.max(0, Math.min(2, metodo))]);

        Estado st = new Estado();
        for (int i = 0; i < ecs.size(); i++) {
            st.trabajo.add(new Item(i + 1, ecs.get(i)));
        }
        avanzar(cx, st);

        doc.nuevo(linea + "\nRESULTADO");
        String resumen;
        int totalLibres = 0;
        if (cx.sols.isEmpty()) {
            if (cx.fallo != null) {
                doc.l("  No pude resolver el sistema: " + cx.fallo + ".");
                resumen = "No pude resolverlo";
            } else {
                boolean noLineal = false;
                for (Poly p : ecs) {
                    if (p.grado() > 1) {
                        noLineal = true;
                    }
                }
                doc.l(noLineal ? "  El sistema no tiene solución real." : "  El sistema no tiene solución (es incompatible).");
                resumen = "Sin solución";
            }
        } else {
            boolean multi = cx.sols.size() > 1;
            for (int k = 0; k < cx.sols.size(); k++) {
                Sol sol = cx.sols.get(k);
                String ind = multi ? "    " : "  ";
                if (multi) {
                    doc.l("  Solución " + (k + 1) + ":");
                }
                for (Character v : cx.vars) {
                    if (sol.fin.containsKey(v)) {
                        Poly e = sol.fin.get(v);
                        String ap = e.vars().isEmpty() ? aprox(e) : null;
                        doc.l(ind + def(v, e) + (ap == null ? "" : "      (" + ap + ")"));
                    } else {
                        doc.l(ind + v + " = libre (puede valer cualquier número)");
                    }
                }
                totalLibres += sol.libres;
            }
            if (cx.fallo != null) {
                doc.l("");
                doc.l("  Aviso: alguna rama no se pudo resolver (" + cx.fallo + ").");
            }
            if (totalLibres > 0) {
                doc.l("");
                doc.l("  Hay infinitas soluciones: elige un valor para cada variable libre");
                doc.l("  y calcula las demás con las expresiones de arriba.");
                resumen = multi ? cx.sols.size() + " familias de soluciones" : "Infinitas soluciones \u00b7 "
                        + totalLibres + " variable(s) libre(s)";
            } else if (multi) {
                resumen = cx.sols.size() + " soluciones";
            } else if (cx.vars.isEmpty()) {
                doc.l("  No hay incógnitas: todas las igualdades son ciertas.");
                resumen = "Sin incógnitas";
            } else {
                resumen = "Solución única \u00b7 " + ecs.size() + " ecuación(es), " + cx.vars.size() + " incógnita(s)";
            }
        }
        doc.cerrar();

        List<Map<Character, Double>> numericas = new ArrayList<Map<Character, Double>>();
        List<Map<Character, Poly>> exactas = new ArrayList<Map<Character, Poly>>();
        for (Sol sol : cx.sols) {
            if (sol.libres == 0) {
                Map<Character, Double> m = new HashMap<Character, Double>();
                for (Character v : cx.vars) {
                    m.put(v, sol.fin.get(v).eval(new HashMap<Character, Double>()));
                }
                numericas.add(m);
                exactas.add(sol.fin);
            }
        }
        Res res = new Res(doc.bloques, resumen, ecs, cx.vars, numericas);
        res.exactas = exactas;
        return res;
    }

    static String pad(String s, int w) {
        StringBuilder b = new StringBuilder();
        for (int i = s.length(); i < w; i++) {
            b.append(' ');
        }
        return b.append(s).toString();
    }

    static String fila(char nombre, List<String> celdas, int w) {
        StringBuilder b = new StringBuilder();
        b.append(nombre).append(" |");
        for (String c : celdas) {
            b.append("  ").append(pad(c, w));
        }
        return b.toString();
    }

    static String puntoTxt(Map<Character, Poly> s, char a, char b) {
        return "(" + unirP(piezas(s.get(a), true, null)) + ", " + unirP(piezas(s.get(b), true, null)) + ")";
    }

    static String puntoAprox(Map<Character, Poly> s, char a, char b) {
        String va = aprox(s.get(a));
        String vb = aprox(s.get(b));
        if (va == null && vb == null) {
            return null;
        }
        return "En decimales:  " + a + " " + (va == null ? "= " + unirP(piezas(s.get(a), true, null)) : va)
                + ",  " + b + " " + (vb == null ? "= " + unirP(piezas(s.get(b), true, null)) : vb);
    }

    static int[] aArray(List<Integer> l) {
        int[] r = new int[l.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = l.get(i);
        }
        return r;
    }

    static Res grafico(Res base, boolean verificar) {
        if (base.ecs.isEmpty()) {
            return base;
        }
        Doc doc = new Doc();
        String linea = rep("\u2500", 58);
        List<Poly> ecs = base.ecs;
        TreeSet<Character> vars = base.vars;
        int n = ecs.size();
        if (vars.size() != 2) {
            doc.nuevo("MÉTODO GRÁFICO\n" + linea, 0);
            doc.l("  Este método necesita exactamente 2 incógnitas (por ejemplo x e y).");
            doc.l("  Tu sistema tiene " + vars.size() + (vars.size() == 1 ? " incógnita." : " incógnitas."));
            doc.l("  Prueba con sustitución, igualación o reducción.");
            doc.cerrar();
            Res r = new Res(doc.bloques, "Gráfico: necesita 2 incógnitas", ecs, vars,
                    new ArrayList<Map<Character, Double>>());
            r.niveles = aArray(doc.niveles);
            return r;
        }
        char a = vars.first();
        char b = vars.last();
        char[] forma = new char[n];
        Poly[] fn = new Poly[n];
        for (int i = 0; i < n; i++) {
            Poly p = ecs.get(i);
            Frac cb = p.coefLineal(b);
            Frac ca = p.coefLineal(a);
            if (cb != null) {
                forma[i] = 'y';
                fn[i] = p.sustituir(b, Poly.cte(Frac.CERO)).neg().esc(Frac.UNO.entre(cb));
            } else if (ca != null) {
                forma[i] = 'x';
                fn[i] = p.sustituir(a, Poly.cte(Frac.CERO)).neg().esc(Frac.UNO.entre(ca));
            } else {
                forma[i] = 'i';
            }
        }

        doc.nuevo("MÉTODO GRÁFICO\n" + linea, 0);
        doc.l("  Cada ecuación dibuja una línea (o curva) en el plano.");
        doc.l("  La solución es el punto donde todas se cruzan.");
        doc.l("");
        for (int i = 0; i < n; i++) {
            doc.l("  (" + (i + 1) + ")  " + ec(ecs.get(i)));
        }
        doc.l("");
        doc.l("  Eje horizontal: " + a + "      Eje vertical: " + b);

        doc.nuevo("PASO 1 \u00b7 Despejar " + b + " en cada ecuación", 0);
        for (int i = 0; i < n; i++) {
            doc.l("  (" + (i + 1) + ")  " + ec(ecs.get(i)));
            if (forma[i] == 'y') {
                doc.l("        " + def(b, fn[i]));
            } else if (forma[i] == 'x') {
                doc.l("        " + def(a, fn[i]) + "      (se despeja " + a + " en lugar de " + b + ")");
            } else {
                doc.l("        no se puede despejar de forma sencilla: se dibuja evaluando puntos");
            }
        }

        List<List<double[]>> tablas = new ArrayList<List<double[]>>();
        List<List<String>> txt = new ArrayList<List<String>>();
        doc.nuevo("PASO 2 \u00b7 Tabla de valores", 0);
        for (int i = 0; i < n; i++) {
            List<double[]> pts = new ArrayList<double[]>();
            List<String> pt = new ArrayList<String>();
            tablas.add(pts);
            txt.add(pt);
            doc.l("  (" + (i + 1) + ")");
            if (forma[i] == 'i') {
                doc.l("      sin tabla: es una curva que no se despeja de forma sencilla");
                continue;
            }
            char ind = forma[i] == 'y' ? a : b;
            char dep = forma[i] == 'y' ? b : a;
            List<String> top = new ArrayList<String>();
            List<String> bot = new ArrayList<String>();
            int w = 1;
            for (int k = -2; k <= 2; k++) {
                Frac xv = Frac.de(k);
                Frac yv = fn[i].sustituir(ind, Poly.cte(xv)).k();
                top.add(xv.toString());
                bot.add(yv.toString());
                w = Math.max(w, Math.max(xv.toString().length(), yv.toString().length()));
                if (forma[i] == 'y') {
                    pts.add(new double[]{xv.dbl(), yv.dbl()});
                    pt.add("(" + xv + ", " + yv + ")");
                } else {
                    pts.add(new double[]{yv.dbl(), xv.dbl()});
                    pt.add("(" + yv + ", " + xv + ")");
                }
            }
            doc.l("      " + fila(ind, top, w));
            doc.l("      " + fila(dep, bot, w));
        }

        for (int i = 0; i < n; i++) {
            doc.nuevo("PASO " + (3 + i) + " \u00b7 Dibujar la ecuación (" + (i + 1) + ")", i + 1);
            Poly p = ecs.get(i);
            if (forma[i] != 'i') {
                StringBuilder sb = new StringBuilder();
                for (String s : txt.get(i)) {
                    if (sb.length() > 0) {
                        sb.append("  ");
                    }
                    sb.append(s);
                }
                doc.l("  Marcamos los puntos de la tabla: " + sb);
                doc.l("  y los unimos.");
            }
            if (forma[i] == 'y') {
                int gr = fn[i].grado();
                if (gr == 0) {
                    doc.l("  Es una recta horizontal: " + b + " = " + fn[i].k());
                } else if (gr == 1) {
                    doc.l("  Es una recta: pendiente " + fn[i].get(Mono.de(a, 1)) + ", corta el eje " + b + " en "
                            + fn[i].k());
                } else {
                    doc.l("  Es una curva de grado " + gr + ".");
                }
            } else if (forma[i] == 'x') {
                int gr = fn[i].grado();
                if (gr == 0) {
                    doc.l("  Es una recta vertical: " + a + " = " + fn[i].k());
                } else if (gr == 1) {
                    doc.l("  Es una recta.");
                } else {
                    doc.l("  Es una curva de grado " + gr + ".");
                }
            } else {
                doc.l("  Se dibuja evaluando la ecuación en muchos puntos: es una curva de grado " + p.grado() + ".");
            }
        }

        int nivelFin = n + 1;
        int ns = base.exactas.size();
        boolean todasRectas = true;
        for (Poly p : ecs) {
            if (p.grado() > 1) {
                todasRectas = false;
            }
        }
        boolean coinc = base.resumen.startsWith("Infinitas") || base.resumen.contains("familias");
        doc.nuevo("PASO " + (3 + n) + " \u00b7 Buscar el punto de corte", nivelFin);
        if (ns == 1) {
            Map<Character, Poly> s = base.exactas.get(0);
            doc.l("  Las líneas se cortan en un solo punto.");
            doc.l("  Punto de corte: " + puntoTxt(s, a, b));
            String ap = puntoAprox(s, a, b);
            if (ap != null) {
                doc.l("  " + ap);
                doc.l("  No cae en un cruce exacto de la cuadrícula: en el dibujo se lee aproximado.");
            }
        } else if (ns > 1) {
            doc.l("  Las curvas se cortan en " + ns + " puntos:");
            for (int k = 0; k < ns; k++) {
                Map<Character, Poly> s = base.exactas.get(k);
                doc.l("    Punto " + (k + 1) + ": " + puntoTxt(s, a, b));
                String ap = puntoAprox(s, a, b);
                if (ap != null) {
                    doc.l("      " + ap);
                }
            }
        } else if (coinc) {
            doc.l("  Las líneas coinciden: todos sus puntos son soluciones (infinitas soluciones).");
        } else if (base.resumen.startsWith("No pude")) {
            doc.l("  No pude calcular el punto de corte de este sistema.");
        } else if (todasRectas) {
            doc.l(n == 2 ? "  Las rectas son paralelas: nunca se cortan, así que no hay solución."
                    : "  No hay ningún punto que esté en todas las rectas: no hay solución.");
        } else {
            doc.l("  Las curvas no se cortan: el sistema no tiene solución real.");
        }

        if (verificar && ns > 0) {
            doc.nuevo("VERIFICACIÓN", nivelFin);
            for (int k = 0; k < ns; k++) {
                if (ns > 1) {
                    doc.l("  Punto " + (k + 1) + ":");
                }
                verificacion(doc, ecs, base.exactas.get(k));
            }
        }

        doc.nuevo(linea + "\nRESULTADO", nivelFin);
        String resumen;
        if (ns >= 1) {
            for (int k = 0; k < ns; k++) {
                Map<Character, Poly> s = base.exactas.get(k);
                String ind = ns > 1 ? "    " : "  ";
                if (ns > 1) {
                    doc.l("  Punto " + (k + 1) + ":");
                }
                for (Character v : vars) {
                    Poly e = s.get(v);
                    String ap = aprox(e);
                    doc.l(ind + def(v, e) + (ap == null ? "" : "      (" + ap + ")"));
                }
            }
            resumen = ns == 1 ? "Gráfico \u00b7 un punto de corte" : "Gráfico \u00b7 " + ns + " puntos de corte";
        } else if (coinc) {
            doc.l("  Infinitas soluciones: las líneas coinciden.");
            resumen = "Gráfico \u00b7 líneas coincidentes";
        } else if (base.resumen.startsWith("No pude")) {
            doc.l("  No pude resolverlo con este método.");
            resumen = "Gráfico \u00b7 no pude resolverlo";
        } else {
            doc.l("  El sistema no tiene solución.");
            resumen = "Gráfico \u00b7 sin puntos de corte";
        }
        doc.cerrar();
        Res r = new Res(doc.bloques, resumen, ecs, vars, base.numericas);
        r.niveles = aArray(doc.niveles);
        r.tablas = tablas;
        r.exactas = base.exactas;
        return r;
    }
}
