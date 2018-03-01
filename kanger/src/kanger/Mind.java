package kanger;

import kanger.calculator.Calculator;
import kanger.compiler.Compiler;
import kanger.compiler.PTree;
import kanger.compiler.Parser;
import kanger.compiler.SysOp;
import kanger.enums.Enums;
import kanger.enums.LogMode;
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
    private final LogStore log = new LogStore(this);                                      // Протокол вывода
    private final SolutionsStore solves = new SolutionsStore();                             // Список пешений
    private final ValuesStore values = new ValuesStore();                             // Список величин
    private final LibraryStore library = new LibraryStore(this);

    private final Map<TVariable, TValue> tValues = new HashMap<>();
    private Map<Domain, Set<Domain>> sources = new HashMap<>();
    private Map<Domain, Set<Domain>> destinations = new HashMap<>();

    private List<Long> substituted = new ArrayList<>();
    private List<Long> calculated = new ArrayList<>();

    private final Calculator calculator = new Calculator(this);
    private final Analiser analiser = new Analiser(this);
    private final Compiler compiler = new Compiler(this);
    private final Linker linker = new Linker(this);

    private volatile boolean changed = false;
    private String sourceFileName = "mind.k";
    private String compiledFileName = "mind.e";

    private ScriptEngine scryptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

    private Set<Long> usedDomains = new HashSet<>();
    private Set<Long> queuedDomains = new HashSet<>();
    private Set<Long> usedTrees = new HashSet<>();
    private Set<Long> closedTrees = new HashSet<>();

    private Set<Long> activeRights = new HashSet<>();

//    private Set<Long> acceptorDomains = new HashSet<>();
//    private Set<Long> markAcceptor = new HashSet<>();

    private transient Map<Term, Long> dictionaryLinks = null;
    private transient Map<Domain, Long> domainLinks = null;
    private transient Map<TVariable, Long> tVariableLinks = null;

    private transient volatile int currentLevel = 0;
    private int debugLevel = Enums.DEBUG_LEVEL_DEBUG | (Enums.DEBUG_OPTION_STATUS | Enums.DEBUG_OPTION_VALUES);

    public int getDebugLevel() {
        return debugLevel;
    }

    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    public Map<Domain, Set<Domain>> getDestinations() {
        return destinations;
    }

    public Map<Domain, Set<Domain>> getSources() {
        return sources;
    }

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

    public void clearSavedResults() {
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
        return analiser;
    }

    public Object getCompiler() {
        return compiler;
    }

    public Linker getLinker() {
        return linker;
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

        dropLinks();
        clearQueryStatus();
    }

    public void clearQueryStatus() {
        usedDomains.clear();
        usedTrees.clear();
        closedTrees.clear();
//        acceptorDomains.clear();
        queuedDomains.clear();

//        sources.clear();
//        destinations.clear();
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
        linker.link(true);
        if(analiser.analiser(true)) {
            getLog().add(LogMode.ANALIZER, "ERROR: Collisions in Program");
        } else {
            mark();
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

    private void removeTreeRecords(Right r) {
        Tree o = null;
        for (Tree t = trees.getRoot(); t != null; t = t.getNext()) {

            if (t.getRight() == r) {
                if (o == null) {
                    trees.setRoot(t.getNext());
                } else {
                    o.setNext(t.getNext());
                }
            } else {
                o = t;
            }
        }
    }

    private void removeDomainRecords(Right r) {
        Domain o = null;
        for (Domain t = domains.getRoot(); t != null; t = t.getNext()) {

            if (t.getRight() == r) {
                if (o == null) {
                    domains.setRoot(t.getNext());
                } else {
                    o.setNext(t.getNext());
                }
            } else {
                o = t;
            }
        }
    }

    private void removeTVarRecords(Right r) {
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

    private void removeCVarRecords(Right r) {
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
        release();

        removeTVarRecords(r);
        removeCVarRecords(r);
        removeDomainRecords(r);
        removeTreeRecords(r);
        removeRightRecord(r);

        mark();
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
        analiser.analiser(true);

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
        return analiser.query(line, false);
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
        return usedTrees;
    }

    public Set<Long> getClosedTrees() {
        return closedTrees;
    }

    public void dropLinks() {
        tValues.clear();
        sources.clear();
        destinations.clear();
    }
//    public Set<Long> getAcceptorDomains() {
//        return acceptorDomains;
//    }
//
//    public void markAcceptors() {
//        markAcceptor.clear();
//        markAcceptor.addAll(acceptorDomains);
//    }
//
//    public void releaseAcceptors() {
//        acceptorDomains.clear();
//        acceptorDomains.addAll(markAcceptor);
//    }

    public Set<Long> getQueuedDomains() {
        return queuedDomains;
    }

    public Set<Long> getActiveRights() {
        return activeRights;
    }

    public List<Long> getSubstituted() {
        return substituted;
    }

    public List<Long> getCalculated() {
        return substituted;
    }

    public Set<Right> getActualRights() {
        Set<Right> set = new HashSet<>();
        if (substituted.isEmpty() && calculated.isEmpty()) {
            for (Right r = rights.getRoot(); r != null; r = r.getNext()) {
                set.add(r);
            }
        } else {
            if (tVars.size() > 0) {
                for (long id : substituted) {
                    if (tVars.get(id) != null) {
                        for (Domain d : tVars.get(id).getUsage()) {
                            set.addAll(d.getPredicate().getRights());
                        }
                    }
                }
            }
            for (long id : calculated) {
                set.addAll(domains.get(id).getPredicate().getRights());
            }
        }
        return set;
    }

    public Set<Tree> getActualTrees() {
        Set<Tree> set = new HashSet<>();
        for (Right r : getActualRights()) {
            set.addAll(r.getTree());
        }
        return set;
    }
}
