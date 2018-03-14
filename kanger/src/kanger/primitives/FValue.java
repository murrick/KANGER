package kanger.primitives;

import java.util.*;

import kanger.*;

import java.io.*;

public class FValue {
    private long id = -1;
    private Term value = null;
    private Map<Long, Long> condition = new HashMap<>();
    private Function function = null;

    private FValue next = null;
    private Mind mind = null;

    public FValue(Function f, Mind mind) {
        function = f;
        value = f.getResult();
        for (TVariable t : f.getTVariables()) {
            condition.put(t.getId(), t.isEmpty() ? -1 : t.getValue().getId());
        }
        this.mind = mind;
    }

    public FValue(DataInputStream dis, Mind mind) throws IOException {
        id = dis.readLong();
        function = mind.getFunctions().get(dis.readLong());
        value = mind.getTerms().get(dis.readLong());
        int count = dis.readInt();
        while (--count >= 0) {
            condition.put(dis.readLong(), dis.readLong());
        }
        this.mind = mind;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setValue(Term value) {
        this.value = value;
    }

    public Term getValue() {
        return value;
    }

    public void setCondition(Map<Long, Long> condition) {
        this.condition = condition;
    }

    public Map<Long, Long> getCondition() {
        return condition;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    public void setNext(FValue next) {
        this.next = next;
    }

    public FValue getNext() {
        return next;
    }

    public void setMind(Mind mind) {
        this.mind = mind;
    }

    public Mind getMind() {
        return mind;
    }

    public void writeCompiledData(DataOutputStream dos) throws IOException {
        dos.writeLong(id);
        dos.writeLong(function.getId());
        dos.writeLong(value == null ? -1 : value.getId());
        dos.writeInt(condition.size());
        for (Map.Entry<Long, Long> e : condition.entrySet()) {
            dos.writeLong(e.getKey());
            dos.writeLong(e.getValue());
        }
    }

    public boolean isActual() {
        for (Map.Entry<Long, Long> e : condition.entrySet()) {
            TVariable tv = mind.getTVars().get(e.getKey());
            if (tv == null || tv.isEmpty() || tv.getValue().getId() != e.getValue()) {
                return false;
            }
        }
        return true;
    }

}
