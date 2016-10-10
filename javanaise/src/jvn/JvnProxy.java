package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import annotation.Lock;
import annotation.LockType;
import irc.Sentence;

public class JvnProxy implements InvocationHandler {

	private JvnObject jo;
	
	private JvnProxy(JvnObject obj) {
		this.jo = obj;
	}
	
	public static Object newInstance(String name, Class<?> c) {
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		
		JvnObject jo = null;
		try {
			jo = js.jvnLookupObject(name);
			
			if (jo == null) {
				jo = js.jvnCreateObject((Serializable) c.newInstance());				
				jo.jvnUnLock(); // after creation, I have a write lock on the object
				js.jvnRegisterObject(name, jo);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Proxy.newProxyInstance(
			c.getClassLoader(),
			c.getInterfaces(),
			new JvnProxy(jo)
		);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) {
		
		Object result = null;
		
		try {		
			
			if(method.isAnnotationPresent(Lock.class)){
				System.out.println("azdqssdsdsd");
				Lock lock = method.getAnnotation(Lock.class);	
				if(lock.type().toString().equals("read")) {
					this.jo.jvnLockRead();
				} else if(lock.type().toString().equals("write")) {
					this.jo.jvnLockWrite();
				}
			}
			
			result = method.invoke(this.jo.jvnGetObjectState(), args);
			
			this.jo.jvnUnLock();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return result;
	}
}