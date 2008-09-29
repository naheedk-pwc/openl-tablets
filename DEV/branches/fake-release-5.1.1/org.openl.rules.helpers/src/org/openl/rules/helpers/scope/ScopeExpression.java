package org.openl.rules.helpers.scope;

import org.openl.OpenL;
import org.openl.OpenlTool;
import org.openl.base.NamedThing;
import org.openl.binding.IBindingContext;
import org.openl.binding.impl.BoundError;
import org.openl.binding.impl.module.ModuleBindingContext;
import org.openl.binding.impl.module.ModuleOpenClass;
import org.openl.meta.StringValue;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMethod;
import org.openl.types.IParameterDeclaration;
import org.openl.types.impl.CompositeMethod;
import org.openl.types.impl.MethodSignature;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.types.java.JavaOpenClass;
import org.openl.util.Log;
import org.openl.vm.IRuntimeEnv;

public class ScopeExpression extends NamedThing
{
    
    
    IOpenMethod method;
    StringValue expression;
    
    IOpenClass methodType = JavaOpenClass.STRING;

    String description;

    public ScopeExpression()
    {
    }

    public IOpenMethod getMethod()
    {
	return this.method;
    }

    public void makeMethod(ModuleOpenClass scope, IParameterDeclaration[] params)
	    throws BoundError
    {

	if (expression == null)
	{
	    Log.warn("{0} does not have an expression", getName());
	    return;
	}

	IMethodSignature signature = new MethodSignature(makeParams(params));
	OpenMethodHeader header = new OpenMethodHeader(getMethodName(),
		getMethodType(), signature, scope);

	OpenL openl = scope.getOpenl();
	IBindingContext bcxt = openl.getBinder().makeBindingContext();

	ModuleBindingContext cxt = new ModuleBindingContext(bcxt, scope);

	CompositeMethod cm = OpenlTool.makeMethod(expression
		.asSourceCodeModule(), openl, header, cxt);

	IOpenClass type = cm.getMethodBodyBoundNode().getType();

	header.setTypeClass(type);

	method = cm;

    }

    public StringValue getExpression()
    {
	return expression;
    }

    public void setExpression(StringValue sourceCode)
    {
	this.expression = sourceCode;
    }

    public String getDescription()
    {
	return description;
    }

    public void setDescription(String description)
    {
	this.description = description;
    }

    public void setMethod(IOpenMethod method)
    {
	this.method = method;
    }

    protected IOpenClass getMethodType()
    {
	return methodType;
    }

    protected String getMethodName()
    {
	return getName();
    }

    protected IParameterDeclaration[] makeParams(IParameterDeclaration[] params)
	    throws BoundError
    {
	return params;
    }

    public void validatePreconditions() throws BoundError
    {

    }

    public void validatePostconditions() throws BoundError
    {

    }

    public void setMethodType(IOpenClass methodType)
    {
        this.methodType = methodType;
    }
    
    class ScopeField implements IOpenField
    {



	public synchronized Object get(Object target, IRuntimeEnv env)
	{
	   ScopeInstance scopeInstance =  (ScopeInstance)target; 
	   Object res = scopeInstance.getFieldValue(getName());
	   if (res == null)
	   {
	       res = method.invoke(target, null, env);
	       res = res == null ? NullObject : res;
	       scopeInstance.setFieldValue(getName(), res);
	   }
	   
	   
	   return res == NullObject ? null : res;
	}

	public boolean isConst()
	{
	    return true;
	}

	public boolean isReadable()
	{
	    return true;
	}

	public boolean isWritable()
	{
	    return true;
	}

	public void set(Object target, Object value, IRuntimeEnv env)
	{
	    ((ScopeInstance)target).setFieldValue(getName(), value);
	}

	public IOpenClass getDeclaringClass()
	{
	    // TODO Auto-generated method stub
	    return null;
	}

	public IMemberMetaInfo getInfo()
	{
	    // TODO Auto-generated method stub
	    return null;
	}

	public IOpenClass getType()
	{
	    return getMethodType();
	}

	public boolean isStatic()
	{
	    return false;
	}

	public String getDisplayName(int mode)
	{
	    return ScopeExpression.this.getDisplayName(mode);
	}

	public String getName()
	{
	    return ScopeExpression.this.getName();
	}
	
    }
    
	private static final Object NullObject = new Object();
}
