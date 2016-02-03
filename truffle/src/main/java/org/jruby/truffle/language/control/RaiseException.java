/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.language.control;

import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Ruby exceptions are just Ruby objects, so they cannot also be exceptions unless we made all Ruby
 * objects exceptions. A simpler approach is to wrap Ruby exceptions in Java exceptions when we want
 * to throw them. The error messages match MRI. Note that throwing is different to raising in Ruby,
 * which is the reason we have both {@link ThrowException} and {@link RaiseException}.
 */
public class RaiseException extends ControlFlowException {

    private final DynamicObject rubyException;

    public RaiseException(DynamicObject rubyException) {
        this.rubyException = rubyException;
    }

    @Override
    public String toString() {
        return rubyException.toString();
    }

    @Override
    public String getMessage() {
        return rubyException.toString();
    }

    public DynamicObject getRubyException() {
        return rubyException;
    }

    private static final long serialVersionUID = 7501185855599094740L;

}
