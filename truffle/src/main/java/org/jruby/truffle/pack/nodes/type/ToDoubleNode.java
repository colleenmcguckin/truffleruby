/*
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.pack.nodes.type;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jruby.truffle.nodes.dispatch.CallDispatchHeadNode;
import org.jruby.truffle.nodes.dispatch.DispatchHeadNodeFactory;
import org.jruby.truffle.nodes.dispatch.DispatchNode;
import org.jruby.truffle.nodes.dispatch.MissingBehavior;
import org.jruby.truffle.pack.nodes.PackNode;
import org.jruby.truffle.pack.runtime.exceptions.CantConvertException;
import org.jruby.truffle.pack.runtime.exceptions.NoImplicitConversionException;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.core.RubyBignum;
import org.jruby.truffle.runtime.core.RubyNilClass;

/**
 * Convert a value to a {@code double}.
 */
@NodeChildren({
        @NodeChild(value = "value", type = PackNode.class),
})
public abstract class ToDoubleNode extends PackNode {

    public abstract double executeToDouble(VirtualFrame frame, Object object);

    @Specialization
    public double toDouble(VirtualFrame frame, int value) {
        return value;
    }

    @Specialization
    public double toDouble(VirtualFrame frame, long value) {
        return value;
    }

    @Specialization
    public double toDouble(VirtualFrame frame, double value) {
        return value;
    }

}
