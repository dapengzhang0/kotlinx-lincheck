/*
 *  Lincheck - Linearizability checker
 *  Copyright (C) 2015 Devexperts LLC
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.devexperts.dxlab.lincheck.tests.boundary;

/*
 * #%L
 * libtest
 * %%
 * Copyright (C) 2015 - 2017 Devexperts, LLC
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.devexperts.dxlab.lincheck.LinChecker;
import com.devexperts.dxlab.lincheck.annotations.*;
import com.devexperts.dxlab.lincheck.paramgen.IntGen;
import com.devexperts.dxlab.lincheck.stress.StressCTest;
import org.cliffc.high_scale_lib.NonBlockingSetInt;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

@StressCTest(iterations = 300, actorsPerThread = {"1:3", "1:3", "1:3"})
@Param(name = "key", gen = IntGen.class, conf = "1:10")
public class BitVectorCorrect1 {
    private Set<Integer> q;

    @Reset
    public void reload() {
        q = new NonBlockingSetInt();
    }

    @Operation(params = {"key"})
    public boolean add(int key) {
        return q.add(key);
    }

    @Operation
    public boolean remove(@Param(name = "key") int key) {
        return q.remove(key);
    }

    @Test
    public void test() {
        LinChecker.check(BitVectorCorrect1.class);
    }
}
