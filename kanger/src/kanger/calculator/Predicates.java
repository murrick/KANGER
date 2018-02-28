package kanger.calculator;

import kanger.compiler.SysOp;
import kanger.enums.LibMode;
import kanger.exception.TValueOutOfOrver;
import kanger.interfaces.IRunnable;
import kanger.primitives.Argument;
import kanger.primitives.Domain;
import kanger.primitives.TSubst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by murray on 18.01.17.
 */
public class Predicates {
    private final Map<String, SysOp> sysOps = new HashMap<String, SysOp>() {

        private boolean isEmpty(Object d, int index) {
            return ((Domain) d).getArguments().get(index).isEmpty()
                    || (((Domain) d).getArguments().get(index).isFSet()
                    && ((Domain) d).getArguments().get(index).getF().getResult() == null)
                    || (((Domain) d).getArguments().get(index).isTSet()
                    && ((Domain) d).getArguments().get(index).getT().getSrcSolve().getId() == ((Domain) d).getId());
        }

        /// Системные предикаты
        {

            put("_eq(2)", new SysOp(LibMode.PREDICATE, "_eq", 2, new IRunnable() {

                @Override
                public Object run(Object o) {
                    int i = -1;
                    List<Argument> arg = ((Domain) o).getArguments();
                    if (!isEmpty(o, 0) && isEmpty(o, 1)) {
                        if (arg.get(1).setValue((Domain) o, arg.get(0).getValue())) {
                            i = 1;
                        }
                    } else if (isEmpty(o, 0) && !isEmpty(o, 1)) {
                        if (arg.get(1).setValue((Domain) o, arg.get(1).getValue())) {
                            i = 1;
                        }
                    } else if (!isEmpty(o, 0) && !isEmpty(o, 1)) {
                        if (arg.get(0).getValue().compareTo(arg.get(1).getValue()) == 0) {
                            i = 1;
                        } else if ((arg.get(0).getValue().isCVar() && arg.get(1).getValue().isCVar()) || (!arg.get(0).getValue().isCVar() && !arg.get(1).getValue().isCVar())) {
                            i = 0;
                        }
//                        else //if(!arg.get(0).getValue().isCVar() && !arg.get(1).getValue().isCVar())
//                            i = 0;
                    }
                    return i;
                }
            }));
        }


        {
            put("_ne(2)", new SysOp(LibMode.PREDICATE, "_ne", 2, new IRunnable() {
                public Object run(Object o) {
                    int i = -1;
                    List<Argument> arg = ((Domain) o).getArguments();
                    if (!isEmpty(o, 0) && !isEmpty(o, 1)) {
                        int rc = arg.get(0).getValue().compareTo(arg.get(1).getValue());
                        if (rc == -1 || rc == 1) {
                            i = 1;
                        } else if (rc == 0) {
                            i = 0;
                        }
                    }
                    return i;
                }
            }));
        }

        {
            put("_gr(2)", new SysOp(LibMode.PREDICATE, "_gr", 2, new IRunnable() {
                public Object run(Object o) {
                    int i = -1;
                    List<Argument> arg = ((Domain) o).getArguments();
                    if (!isEmpty(o, 0) && !isEmpty(o, 1) && !arg.get(0).getValue().isCVar() && !arg.get(1).getValue().isCVar()) {
                        int rc = arg.get(0).getValue().compareTo(arg.get(1).getValue());
                        if (rc != -2) {
                            i = rc > 0 ? 1 : 0;
                        }
                    }
                    return i;
                }
            }));
        }

        {
            put("_ge(2)", new SysOp(LibMode.PREDICATE, "_ge", 2, new IRunnable() {
                public Object run(Object o) {
                    int i = -1;
                    List<Argument> arg = ((Domain) o).getArguments();
                    if (!isEmpty(o, 0) && !isEmpty(o, 1) && !arg.get(0).getValue().isCVar() && !arg.get(1).getValue().isCVar()) {
                        int rc = arg.get(0).getValue().compareTo(arg.get(1).getValue());
                        if (rc != -2) {
                            i = rc >= 0 ? 1 : 0;
                        }
                    }
                    return i;
                }
            }));
        }

        {
            put("_lr(2)", new SysOp(LibMode.PREDICATE, "_lr", 2, new IRunnable() {
                public Object run(Object o) {
                    int i = -1;
                    List<Argument> arg = ((Domain) o).getArguments();
                    if (!isEmpty(o, 0) && !isEmpty(o, 1) && !arg.get(0).getValue().isCVar() && !arg.get(1).getValue().isCVar()) {
                        int rc = arg.get(0).getValue().compareTo(arg.get(1).getValue());
                        if (rc != -2) {
                            i = rc < 0 ? 1 : 0;
                        }
                    }
                    return i;
                }
            }));
        }

        {
            put("_le(2)", new SysOp(LibMode.PREDICATE, "_le", 2, new IRunnable() {
                public Object run(Object o) {
                    int i = -1;
                    List<Argument> arg = ((Domain) o).getArguments();
                    if (!isEmpty(o, 0) && !isEmpty(o, 1) && !arg.get(0).getValue().isCVar() && !arg.get(1).getValue().isCVar()) {
                        int rc = arg.get(0).getValue().compareTo(arg.get(1).getValue());
                        if (rc != -2) {
                            i = rc <= 0 ? 1 : 0;
                        }
                    }
                    return i;
                }
            }));
        }

        //TODO: Добавить ret значение 2 для всех заполненных и совпадающих полей
        {
            put("match(2)", new SysOp(LibMode.PREDICATE, "match", 2, new IRunnable() {
                public Object run(Object o) {
                    int i = -1;
                    List<Argument> arg = ((Domain) o).getArguments();
                    if (arg.get(0).getValue() != null && arg.get(1).getValue() != null) {
                        if (arg.get(0).getValue().isCVar() || arg.get(1).getValue().isCVar()) {
                            i = -1;
                        } else {
//                            i = maskcmp(arg.get(0).getValue().getTerm().getName(), arg.get(1).getValue().getTerm().getName()) == 0 ? 1 : 0;
                            try {
                                i = Pattern.matches((String) arg.get(0).getValue().getValue(), (String) arg.get(1).getValue().getValue()) ? 1 : 0;
                            } catch (PatternSyntaxException ex) {
                                System.err.println("Regexp error: " + ex.getDescription());
                            }
                        }
                    }
                    return i;
                }
            }));
        }

    };


    public Map<String, SysOp> getSysOps() {
        return sysOps;
    }
}
