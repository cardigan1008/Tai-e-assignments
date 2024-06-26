/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2022 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2022 Yue Li <yueli@nju.edu.cn>
 *
 * This file is part of Tai-e.
 *
 * Tai-e is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Tai-e is distributed in the hope that it will be useful,but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tai-e. If not, see <https://www.gnu.org/licenses/>.
 */

package pascal.taie.analysis.dataflow.analysis;

import pascal.taie.analysis.dataflow.fact.SetFact;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.exp.LValue;
import pascal.taie.ir.exp.RValue;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Stmt;

/**
 * Implementation of classic live variable analysis.
 */
public class LiveVariableAnalysis extends
        AbstractDataflowAnalysis<Stmt, SetFact<Var>> {

    public static final String ID = "livevar";

    public LiveVariableAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public boolean isForward() {
        return false;
    }

    // For liveness analysis, the initial boundary fact is an empty set.
    @Override
    public SetFact<Var> newBoundaryFact(CFG<Stmt> cfg) {
        return new SetFact<Var>();
    }

    // For liveness analysis, the initial fact for each bb is an empty set.
    @Override
    public SetFact<Var> newInitialFact() {
        return new SetFact<Var>();
    }

    // For liveness analysis, moveinto is to union fact(IN) into target(OUT).
    @Override
    public void meetInto(SetFact<Var> fact, SetFact<Var> target) {
        target.union(fact);
    }

    // For liveness analysis, transfer function here is to calculate in = use U (out - def).
    @Override
    public boolean transferNode(Stmt stmt, SetFact<Var> in, SetFact<Var> out) {
        // // Check whether the stmt is var = x, otherwise, no function will be applied.
        // if (stmt.getDef().isPresent()) {
        //     LValue def = stmt.getDef().get();

        //     // We'll only consider vars on the left hand.
        //     // NO!!! Only vars in right hand is also ok!
        //     if (def instanceof Var) {
        //         // Calculate uses (vars on the right hand) into a set.
        //         SetFact<Var> use = new SetFact<>();
        //         for (RValue rVar: stmt.getUses()) {
        //             if (rVar instanceof Var) {
        //                 use.add((Var) rVar);
        //             }
        //         }
        //         // in = use U (out - def).
        //         SetFact<Var> newIn = new SetFact<>();
        //         newIn.union(out);
        //         newIn.remove((Var) def);
        //         newIn.union(use);

        //         // Return true when in changes!
        //         if (in.equals(newIn)) {
        //             return false;
        //         }
                
        //         in.set(newIn);
        //         return true;
        //     }
        // }

        // Here I originally set newIn to out, which also changes out!
        SetFact<Var> newIn = new SetFact<>();
        newIn.union(out);

        if (stmt.getDef().isPresent()) {
            LValue lValue = stmt.getDef().get();

            if (lValue instanceof Var) {
                newIn.remove((Var) lValue);
            }
        }

        for (RValue rVar: stmt.getUses()) {
            if (rVar instanceof Var) {
                newIn.add((Var) rVar);
            }
        }

        if (!in.equals(newIn)) {
            in.set(newIn);
            return true;
        }
        
        return false;
    }
}
