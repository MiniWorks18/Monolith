package au.com.addstar.monolith.template.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import au.com.addstar.monolith.template.TemplateSetting;

public abstract class MethodLinkedTemplateSetting<HolderType, T, V> extends TemplateSetting<HolderType, T, V>
{
	private MethodHandle mHandle;
	
	protected MethodLinkedTemplateSetting(String name, String[] aliases, V def, Class<V> type, MethodHandle handle)
	{
		super(name, aliases, def, type);
		mHandle = handle;
	}

	@Override
	public void apply(T object, V value)
	{
		try
		{
			mHandle.invoke(object, value);
		}
		// Any exceptions should be extremely rare
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected static MethodHandle getHandle(Class<?> forClass, String method, Class<?> type)
	{
		// Get the primitive version of the class
		Class<?> alternate = null;
		if (type == Integer.class)
			alternate = Integer.TYPE;
		else if (type == Short.class)
			alternate = Short.TYPE;
		else if (type == Byte.class)
			alternate = Byte.TYPE;
		else if (type == Long.class)
			alternate = Long.TYPE;
		else if (type == Float.class)
			alternate = Float.TYPE;
		else if (type == Double.class)
			alternate = Double.TYPE;
		else if (type == Character.class)
			alternate = Character.TYPE;
		else if (type == Boolean.class)
			alternate = Boolean.TYPE;
		
		// Create the MethodHandle
		Method reflect = null;
		try
		{
			reflect = forClass.getMethod(method, type);
		}
		catch (NoSuchMethodException e)
		{
			// Handled later
		}
		
		// Try the primitive version
		if (reflect == null && alternate != null)
		{
			try
			{
				reflect = forClass.getMethod(method, alternate);
			}
			catch (NoSuchMethodException e)
			{
				// Handled later
			}
		}
		
		if (reflect == null)
			throw new IllegalArgumentException("Unable to find method " + method + " in " + forClass.getName());
		
		// Now try to create the method handle
		MethodHandle handle;
		try
		{
			handle = MethodHandles.lookup().unreflect(reflect);
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalArgumentException("Unable to create handle for " + method + ". IllegalAccessException occurred");
		}
		
		return handle;
	}
	
	
	
}
