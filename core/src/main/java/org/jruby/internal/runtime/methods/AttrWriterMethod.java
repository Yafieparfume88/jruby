/*
 **** BEGIN LICENSE BLOCK *****
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2011 Charles O Nutter <headius@headius.com>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.internal.runtime.methods;

import java.util.Arrays;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubySymbol;
import org.jruby.runtime.ivars.VariableAccessor;
import org.jruby.internal.runtime.methods.JavaMethod.JavaMethodOne;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.ivars.MethodData;
import org.jruby.util.StringSupport;

/**
 * A method type for attribute writers (as created by attr_writer or attr_accessor).
 */
public class AttrWriterMethod extends JavaMethodOne {
    private MethodData methodData;
    private final RubySymbol variableName;
    private VariableAccessor accessor = VariableAccessor.DUMMY_ACCESSOR;

    public AttrWriterMethod(RubyModule implementationClass, Visibility visibility, RubySymbol variableName) {
        // FIXME: JavaMethods need to be RubySymbols to work with non java charsets
        super(implementationClass, visibility, variableName.accessorFromSymbol().asJavaString());
        this.variableName = variableName;
    }

    @Deprecated
    public AttrWriterMethod(RubyModule implementationClass, Visibility visibility, String variableName) {
        this(implementationClass, visibility, implementationClass.getRuntime().newSymbol(variableName));
    }

    public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg1) {
        verifyAccessor(self.getMetaClass().getRealClass()).set(self, arg1);
        return arg1;
    }
    
    public String getVariableName() {
        return StringSupport.byteListAsString(variableName.getBytes());
    }

    private VariableAccessor verifyAccessor(RubyClass cls) {
        VariableAccessor localAccessor = accessor;
        if (localAccessor.getClassId() != cls.id) {
            // FIXME: Change to use symbol once we change ivar tables.
            localAccessor = cls.getVariableAccessorForWrite(StringSupport.byteListAsString(variableName.getBytes()));
            accessor = localAccessor;
        }
        return localAccessor;
    }
    
    @Override
    public MethodData getMethodData() {
        if (methodData == null){
            // FIXME: Use symbol once MethodData is converted to Symbols
            methodData = new MethodData(name, "dummyfile", Arrays.asList(StringSupport.byteListAsString(variableName.getBytes())));
        }
        return methodData;
    }

    // Used by racc extension, needed for backward-compat with 1.7.
    @Deprecated
    public AttrWriterMethod(RubyModule implementationClass, Visibility visibility, CallConfiguration callConfiguration, String variableName) {
        this(implementationClass, visibility, variableName);
    }
}
