/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.io.*;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	private Hashtable<Integer, JvnObject> cache;

	private Hashtable<String, Integer> correspondanceNomId;

	private JvnRemoteCoord coordinateur;

	private JvnServerImpl() throws Exception {
		super();
		System.out.println("JvnServerImpl.JvnServerImpl()");
		this.cache = new Hashtable<Integer, JvnObject>();
		this.correspondanceNomId = new Hashtable<String, Integer>();
		Registry registry = LocateRegistry.getRegistry("localhost");
		this.coordinateur = (JvnRemoteCoord) registry.lookup("coordinateur");
	}

	public static JvnServerImpl jvnGetServer() {
		System.out.println("JvnServerImpl.jvnGetServer()");
		if (js == null) {
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}

	public void jvnTerminate() throws jvn.JvnException {
		System.out.println("JvnServerImpl.jvnTerminate()");
		// to be completed
	}

	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
		System.out.println("JvnServerImpl.jvnCreateObject()");
		JvnObject o1 = null;
		int id = 0;
		try {
			id = this.coordinateur.jvnGetObjectId();
			o1 = new JvnObjectImpl(o, id, js);
			synchronized(this) {
				this.cache.put(id, o1);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
		return this.cache.get(id);
	}

	//TODO : synchro seulement sur la portion de code nécessaire
	public synchronized void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
		System.out.println("JvnServerImpl.jvnRegisterObject()");
		synchronized(this) {
			this.correspondanceNomId.put(jon, jo.jvnGetObjectId());
		}
		try {
			this.coordinateur.jvnRegisterObject(jon, jo, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		System.out.println("JvnServerImpl.jvnLookupObject()");
		// TODO : to be completed
		JvnObject o = null;
		JvnObject serveurObject = null;
		try {
			serveurObject = this.coordinateur.jvnLookupObject(jon, js);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (this.correspondanceNomId.get(jon) != null) {
			o = this.cache.get(this.correspondanceNomId.get(jon));
		} else if (serveurObject != null) {
			o = serveurObject;
			o.setLockNL();
			o.setServer(this);
			synchronized(this) {
				this.correspondanceNomId.put(jon, o.jvnGetObjectId());
				this.cache.put(o.jvnGetObjectId(), o);
			}
		}
		return o;
	}

	public Serializable jvnLockRead(int joi) throws JvnException {
		System.out.println("JvnServerImpl.jvnLockRead()");
		Serializable s = null;
		try {
			s = this.coordinateur.jvnLockRead(joi, this.js);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return s;
	}

	public Serializable jvnLockWrite(int joi) throws JvnException {
		System.out.println("JvnServerImpl.jvnLockWrite()");
		Serializable s = null;
		try {
			s = this.coordinateur.jvnLockWrite(joi, this.js);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return s;
	}

	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		System.out.println("JvnServerImpl.jvnInvalidateReader()");
		// TODO : to be completed
		this.cache.get(joi).jvnInvalidateReader();
	};

	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		System.out.println("JvnServerImpl.jvnInvalidateWriter()");
		// TODO : to be completed
		return this.cache.get(joi).jvnInvalidateWriter();
	};

	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		System.out.println("JvnServerImpl.jvnInvalidateWriter()");
		// TODO : to be completed
		return this.cache.get(joi).jvnInvalidateWriterForReader();
	};

}
