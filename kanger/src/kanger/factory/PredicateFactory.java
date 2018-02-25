package kanger.factory;

import kanger.Mind;
import kanger.primitives.Predicate;
import kanger.primitives.Solve;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by murray on 25.05.15.
 */
public class PredicateFactory {

    private Predicate root = null;
    private Predicate saved = null;
    private long lastID = 0, saveLastID;

    private Mind mind = null;

    public PredicateFactory(Mind mind) {
        this.mind = mind;
    }

    public Predicate add(String line, int range) {
        Predicate p = (Predicate) find(line, range);
        if (p != null) {
            return p;
        } else {
            p = new Predicate();
            p.setId(lastID++);
            p.setNext(root);
            p.setRange(range);
            p.setName(line);
            root = p;
            return p;
        }
    }

    public Object find(String line, int range) {
        for (Predicate p = root; p != null; p = p.getNext()) {
            if (line.equals(p.getName()) && p.getRange() == range) {
                return p;
            }
        }
        return null;
    }

    public Predicate get(long id) {
        for (Predicate p = root; p != null; p = p.getNext()) {
            if (id == p.getId()) {
                return p;
            }
        }
        return null;
    }

    public Predicate getRoot() {
        return root;
    }

    public void reset() {
        root = null;
        saved = null;
        lastID = 0;
        saveLastID = 0;
    }

    public void mark() {
        for (Predicate p = root; p != null; p = p.getNext()) {
            p.setSavedSolve(p.getSolve());
            p.setHypo(null);
            for (Solve s = p.getSolve(); s != null; s = s.getNext()) {
                s.setCuted(false);
                s.setLoged(0);
//                s.getSubst().clear();
            }
        }
        saved = root;
        saveLastID = lastID;
    }

    public void release() {
        if(root != null && saved != null && root.getId() != saved.getId()) {
            for (Predicate p = root; p != null; p = p.getNext()) {
                if (p.getNext() != null && p.getNext().getId() == saved.getId()) {
                    p.setNext(null);
                    break;
                }
            }
            for (Predicate p = root; p != null; p = p.getNext()) {
                if(p.getSolve().getId() != p.getSavedSolve().getId()) {
                    for (Solve s = p.getSolve(); p != null; p = p.getNext()) {
                        if (p.getNext() != null && p.getNext().getId() == p.getSavedSolve().getId()) {
                            p.setNext(null);
                            break;
                        }
                    }
                    p.setHypo(null);
                    p.setSolve(p.getSavedSolve());
//                    for (Solve s = p.getSolve(); s != null; s = s.getNext()) {
//                        s.setCuted(false);
//                        s.setLoged(0);
//                    }
                }
            }
            root = saved;
            lastID = saveLastID;
        }
    }

    public int size() {
        int cnt = 0;
        for (Predicate q = root; q != null; q = q.getNext()) {
            ++cnt;
        }
        return cnt;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(lastID);
        dos.writeInt(size());
        for (Predicate p = root; p != null; p = p.getNext()) {
            p.writeCompiledData(dos);
        }
        List<Long[]> links = new ArrayList<>();
        //TODO: Save causes
//        for(Predicate p = root; p != null; p = p.getNext()) {
//            for(Solve s = p.getSolve(); s != null; s = s.getNext()) {
//                for(Solve x : s.getCauses()) {
//                    links.add(new Long[]{s.getPredicate().getId(), s.getId(), x.getPredicate().getId(), x.getId()});
//                }
//            }
//        }
        dos.writeInt(links.size());
        for(Long[] l : links) {
            dos.writeLong(l[0]);
            dos.writeLong(l[1]);
            dos.writeLong(l[2]);
            dos.writeLong(l[3]);
        }
    }

    public void readCompiledData(DataInputStream dis) throws IOException {
        lastID = dis.readLong();
        int count = dis.readInt();
        root = null;
        Predicate a = null, b;
        while (count-- > 0) {
            b = new Predicate(dis, mind);
            if (a == null) {
                root = b;
            } else {
                a.setNext(b);
            }
            a = b;
        }
        //TODO: Load causes
//        count = dis.readInt();
//        while (count-- > 0) {
//            Predicate p = get(dis.readLong());
//            Solve s = p.getSolve(dis.readLong());
//            Predicate xp = get(dis.readLong());
//            Solve xs = xp.getSolve(dis.readLong());
//            s.getCauses().add(xs);
//        }
    }

}
