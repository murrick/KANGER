package kanger;

import kanger.calculator.Calculator;
import kanger.compiler.Compiler;
import kanger.compiler.PTree;
import kanger.compiler.Parser;
import kanger.compiler.SysOp;
import kanger.enums.Enums;
import kanger.enums.Tools;
import kanger.exception.ParseErrorException;
import kanger.exception.RuntimeErrorException;
import kanger.factory.*;
import kanger.primitives.*;
import kanger.stores.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Dmitry G. Qusnetsov on 20.05.15.
 */
public class Mind {

    private final DictionaryFactory terms = new DictionaryFactory(this);                      // Словарь констант
    private final PredicateFactory predicates = new PredicateFactory(this);                   // Предикаты
    private final TVariableFactory tVars = new TVariableFactory(this);                    // t-переменные
    private final DomainFactory domains = new DomainFactory(this);                            // Список доменов
    private final RightFactory rights = new RightFactory(this);                               // Список правил
    private final TreeFactory trees = new TreeFactory(this);                               // Список секвенций

    private final HypotesesStore hypoteses = new HypotesesStore();                    // Список гипотез
    private final LogStore log = new LogStore();                                      // Протокол вывода
    private final SolutionsStore solves = new SolutionsStore();                             // Список пешений
    private final ValuesStore values = new ValuesStore();                             // Список величин
    private final LibraryStore library = new LibraryStore(this);

    private final Map<TVariable, TValue> tValues = new HashMap<>();

    private final Calculator calculator = new Calculator(this);
    private final Analiser analyser = new Analiser(this);
    private final Compiler compiler = new Compiler(this);

    private volatile boolean changed = false;
    private String sourceFileName = "mind.k";
    private String compiledFileName = "mind.e";

    private ScriptEngine scryptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

    private Set<Long> usedDomains = new HashSet();
    private Set<Long> initiatedDomains = new HashSet();
    private Set<Long> usedTree = new HashSet();
    private Set<Long> closedTree = new HashSet();
    private Map<Domain, Map<Right, Set<Domain>>> causes = new HashMap<>();

    private transient Map<Term, Long> dictionaryLinks = null;
    private transient Map<Domain, Long> domainLinks = null;
    //    private transient Map<Solve, Long> solveLinks = null;
    private transient Map<TVariable, Long> tVariableLinks = null;


    private transient volatile int currentLevel = 0;
    //    private transient volatile Solve currentSolve = null;
    private transient volatile int substCount = 0;


    public DictionaryFactory getTerms() {
        return terms;
    }

    public PredicateFactory getPredicates() {
        return predicates;
    }

    public DomainFactory getDomains() {
        return domains;
    }

    public ValuesStore getValues() {
        return values;
    }

    public RightFactory getRights() {
        return rights;
    }

    public TreeFactory getTrees() {
        return trees;
    }

    public TVariableFactory getTVars() {
        return tVars;
    }

    public LibraryStore getLibrary() {
        return library;
    }

    public HypotesesStore getHypotesisStore() {
        return hypoteses;
    }

    public LogStore getLog() {
        return log;
    }

    public SolutionsStore getSolutions() {
        return solves;
    }

    public Map<TVariable, TValue> getTValues() {
        return tValues;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

//    public Solve getCurrentSolve() {
//        return currentSolve;
//    }
//
//    public void setCurrentSolve(Solve currentSolve) {
//        this.currentSolve = currentSolve;
//    }

    public void resetDummy() {
        solves.reset();
        values.reset();
    }

    public void resetPerm() {
        terms.reset();
        predicates.reset();
        tVars.reset();
        domains.reset();
        rights.reset();
    }


    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean b) {
        changed = b;
    }

    public Calculator getCalculator() {
        return calculator;
    }

    public Analiser getAnalyser() {
        return analyser;
    }

    public Object getCompiler() {
        return compiler;
    }

    public int getSubstCount() {
        return this.substCount;
    }

    public void incSubstCount() {
        ++this.substCount;
    }

    public void dropSubstCount() {
        this.substCount = 0;
    }

    public void mark() {
        terms.mark();
        predicates.mark();
        domains.mark();
        tVars.mark();
        rights.mark();
        trees.mark();
    }

    public void release() {
        terms.release();
        predicates.release();
        domains.release();
        tVars.release();
        rights.release();
        trees.release();

        tValues.clear();
    }

    public void clearQueryStatus() {
        usedDomains.clear();
        usedTree.clear();
        closedTree.clear();
        initiatedDomains.clear();
        causes.clear();
    }

    public void clear() throws ParseErrorException {
        terms.reset();
        predicates.reset();
        domains.reset();
        tVars.reset();
        rights.reset();

        solves.reset();
        values.reset();

        hypoteses.reset();
        log.reset();

    }

    public void compile(String src) throws ParseErrorException, RuntimeErrorException {

        int pos = 0;
        Object[] t = null;
        while ((t = Tools.extractLine(src, pos)) != null) {
            pos = (int) t[1];
            String line = (String) t[0];
            compileLine(line);
        }
    }

    public Object compileLine(String line) throws ParseErrorException, RuntimeErrorException {
        String orig = line.trim();
        Object r = null;
        Boolean suc = null;

        switch (line.charAt(0)) {
            case Enums.FOO:
                r = Parser.implement(line, this);
                calculator.register((SysOp) r);
                break;
            case Enums.INS:
            case Enums.ANT:
                suc = true;
                break;
            case Enums.DEL:
            case Enums.WIPE:
            case Enums.SUC:
                suc = false;
                break;
        }
        if (suc != null) {
            PTree p = Parser.parser(line.substring(1));
            r = compiler.compileLine(p, suc);
            ((Right) r).setOrig(orig);

//            if (!containsTVariables((Right) r) || containsCVariables((Right) r)) {
//                removeRightRecord((Right) r);
//            }
        }

        return r;
    }

    private boolean containsTVariables(Right r) {
        for (TVariable t = tVars.getRoot(); t != null; t = t.getNext()) {
            if (t.getRight() == r) {
                return true;
            }
        }
        return false;
    }

    private boolean containsCVariables(Right r) {
        for (Term t = terms.getRoot(); t != null; t = t.getNext()) {
            if (t.isCVar() && t.getRight() == r) {
                return true;
            }
        }
        return false;
    }

    /**
     * Удаление правила из дерева вывода
     * <p>
     * Удалять можно только правила не содержащие t-переменные
     *
     * @param r
     */
    private void removeRightRecord(Right r) {
        if (rights.getRoot() == r) {
            rights.setRoot(r.getNext());
        } else {
            for (Right p = rights.getRoot(); p != null; p = p.getNext()) {
                if (p.getNext() == r) {
                    p.setNext(r.getNext());
                    break;
                }
            }
        }
    }

    private void removeSolve() {
    }

    private void removeTVarsRecords(Right r) {
        TVariable o = null;
        for (TVariable t = tVars.getRoot(); t != null; t = t.getNext()) {

            if (t.getRight() == r) {
                if (o == null) {
                    tVars.setRoot(t.getNext());
                } else {
                    o.setNext(t.getNext());
                }
            } else {
                o = t;
            }
        }
    }

    private void removeCVarsRecords(Right r) {
        Term o = null;
        for (Term t = terms.getRoot(); t != null; t = t.getNext()) {
            if (t.getRight() == r && t.isCVar()) {
                if (o == null) {
                    terms.setRoot(t.getNext());
                } else {
                    o.setNext(t.getNext());
                }
            } else {
                o = t;
            }
        }
    }

    public void removeInsertionRight(Right r) {
        removeTVarsRecords(r);
        removeCVarsRecords(r);
        removeRightRecord(r);
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String name) {
        sourceFileName = name;
    }

    public String getCompiledFileName() {
        return compiledFileName;
    }

    public void setCompiledFileName(String compiledFileName) {
        this.compiledFileName = compiledFileName;
    }

    public void writeCompiledData(OutputStream os) throws IOException, RuntimeErrorException {
        analyser.analiser(true);

        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(19640207);
        dos.writeUTF("K3");
        dos.writeByte(0);
        dos.writeUTF(Version.VERSION_S);
        dos.writeByte(0);

        GZIPOutputStream zos = new GZIPOutputStream(os);
        dos = new DataOutputStream(zos);
        this.terms.writeCompiledData(dos);
        this.tVars.writeCompiledData(dos);
        this.predicates.writeCompiledData(dos);
        this.domains.writeCompiledData(dos);
        this.rights.writeCompiledData(dos);
        zos.finish();
    }

    public void readCompiledData(InputStream is) throws IOException, ClassNotFoundException, ParseErrorException {
        clear();

        DataInputStream dis = new DataInputStream(is);

        dictionaryLinks = new HashMap<>();
        domainLinks = new HashMap<>();
//        solveLinks = new HashMap<>();
        tVariableLinks = new HashMap<>();

        int signature = dis.readInt();
        String key = dis.readUTF();
        dis.readByte();
        String version = dis.readUTF();
        dis.readByte();

        GZIPInputStream zis = new GZIPInputStream(is);
        dis = new DataInputStream(zis);
        this.terms.readCompiledData(dis);
        this.tVars.readCompiledData(dis);
        this.predicates.readCompiledData(dis);
        this.domains.readCompiledData(dis);
        this.rights.readCompiledData(dis);
        for (Map.Entry<Term, Long> d : dictionaryLinks.entrySet()) {
            d.getKey().setRight(rights.get(d.getValue()));
        }
        for (Map.Entry<Domain, Long> d : domainLinks.entrySet()) {
            d.getKey().setRight(rights.get(d.getValue()));
        }
        //TODO: Загрузка causes
//        for(Map.Entry<Solve,Long> d: solveLinks.entrySet()) {
//            d.getKey().setRight(rights.get(d.getValue()));
//        }
        for (Map.Entry<TVariable, Long> d : tVariableLinks.entrySet()) {
            d.getKey().setRight(rights.get(d.getValue()));
        }

        dictionaryLinks = null;
        domainLinks = null;
//        solveLinks = null;
        tVariableLinks = null;

    }

    public ScriptEngine getScryptEngine() {
        return scryptEngine;
    }

    public Boolean query(String line) throws ParseErrorException, RuntimeErrorException {
        return analyser.query(line, false);
    }

    public String getVersion() {
        return Version.VERSION_S;
    }

    public Map<Term, Long> getDictionaryLinks() {
        return dictionaryLinks;
    }

    public Map<Domain, Long> getDomainLinks() {
        return domainLinks;
    }

//    public Map<Solve, Long> getSolveLinks() {
//        return solveLinks;
//    }

    public Map<TVariable, Long> gettVariableLinks() {
        return tVariableLinks;
    }


    public Set<Long> getUsedDomains() {
        return usedDomains;
    }

    public Set<Long> getUsedTrees() {
        return usedTree;
    }

    public Set<Long> getClosedTrees() {
        return closedTree;
    }

    public Set<Long> getInitiatedDomains() {
        return initiatedDomains;
    }

    public Map<Domain, Map<Right, Set<Domain>>> getCauses() {
        return causes;
    }
}

