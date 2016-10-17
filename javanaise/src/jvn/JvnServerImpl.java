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
import java.util.Iterator;
import java.util.Set;
import java.io.*;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	private Hashtable<Integer, JvnObject> cache;

	private Hashtable<String, Integer> correspondanceNomId;
	
	private final int cacheMaxSize = 3; 

	private JvnRemoteCoord coordinateur;

	private JvnServerImpl() throws Exception {
		super();
		//System.out.println("JvnServerImpl.JvnServerImpl()");
		this.cache = new Hashtable<Integer, JvnObject>();
		this.correspondanceNomId = new Hashtable<String, Integer>();
		Registry registry = LocateRegistry.getRegistry("localhost");
		this.coordinateur = (JvnRemoteCoord) registry.lookup("coordinateur");
	}

	public static JvnServerImpl jvnGetServer() {
		//System.out.println("JvnServerImpl.jvnGetServer()");
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
		//System.out.println("JvnServerImpl.jvnTerminate()");
		// to be completed
	}

	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
		//System.out.println("JvnServerImpl.jvnCreateObject()");
		if (this.cache.size() == this.cacheMaxSize) {
			suppressionElementCache();
		}
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

	public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
		//System.out.println("JvnServerImpl.jvnRegisterObject()");
		synchronized(this) {
			this.correspondanceNomId.put(jon, jo.jvnGetObjectId());
		}
		try {
			this.coordinateur.jvnRegisterObject(jon, jo, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		afficheToiTchounibabe();
	}

	public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		//System.out.println("JvnServerImpl.jvnLookupObject()");
		// TODO : to be completed
		if (this.cache.size() == this.cacheMaxSize) {
			suppressionElementCache();
		}
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
		//System.out.println(o.getVerrou());
		afficheToiTchounibabe();
		return o;
	}

	public Serializable jvnLockRead(int joi) throws JvnException {
		//System.out.println("JvnServerImpl.jvnLockRead()");
		Serializable s = null;
		try {
			s = this.coordinateur.jvnLockRead(joi, this.js);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return s;
	}

	public Serializable jvnLockWrite(int joi) throws JvnException {
		//System.out.println("JvnServerImpl.jvnLockWrite()");
		Serializable s = null;
		try {
			s = this.coordinateur.jvnLockWrite(joi, this.js);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return s;
	}

	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		//System.out.println("JvnServerImpl.jvnInvalidateReader()");
		// TODO : to be completed
		this.cache.get(joi).jvnInvalidateReader();
	}

	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		//System.out.println("JvnServerImpl.jvnInvalidateWriter()");
		// TODO : to be completed
		return this.cache.get(joi).jvnInvalidateWriter();
	}

	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		//System.out.println("JvnServerImpl.jvnInvalidateWriter()");
		// TODO : to be completed
		return this.cache.get(joi).jvnInvalidateWriterForReader();
	}
	
	public void suppressionElementCache() throws JvnException {
		Set cles = this.cache.keySet();
		Iterator it = cles.iterator();
		int idObjASupprimmer = -1;
		
		lockStates currentLock;
		lockStates toRemoveLock;
		while (it.hasNext()){
		   Object cle = it.next();
		   currentLock = this.cache.get(cle).getVerrou();
		   toRemoveLock = idObjASupprimmer != -1 ? this.cache.get(idObjASupprimmer).getVerrou() : null;
		   System.out.println(currentLock);
		   
		   if ( (currentLock == lockStates.NL || currentLock == lockStates.RC || currentLock == lockStates.WC) &&  idObjASupprimmer == -1 ){
			   idObjASupprimmer = (Integer) cle;
		   } else if ( currentLock == lockStates.NL && (toRemoveLock == lockStates.RC || toRemoveLock == lockStates.WC) ){
			   idObjASupprimmer = (Integer) cle;
		   } else if ( currentLock == lockStates.NL && toRemoveLock == lockStates.NL && this.cache.get(cle).getTimeUnlock().before(this.cache.get(idObjASupprimmer).getTimeUnlock()) ){
			   idObjASupprimmer = (Integer) cle;
		   } else if ( (currentLock == lockStates.WC || currentLock == lockStates.RC) && 
				   (toRemoveLock == lockStates.WC || toRemoveLock == lockStates.RC) &&
				   this.cache.get(cle).getTimeUnlock().before(this.cache.get(idObjASupprimmer).getTimeUnlock()) ){
			   idObjASupprimmer = (Integer) cle;
		   }
		}
		   
	   if (idObjASupprimmer == -1) {
		   throw new JvnException("Impossible de trouver de la place");
	   }
	   else {
		   if( this.cache.get(idObjASupprimmer).getVerrou() == lockStates.WC || this.cache.get(idObjASupprimmer).getVerrou() == lockStates.RC) {
			   try {
				this.coordinateur.jvnObjectDeleteToTheChache(js, idObjASupprimmer);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   }
			this.cache.remove(idObjASupprimmer);
			Set cles2 = this.correspondanceNomId.keySet();
			Iterator it2 = cles2.iterator();
			while (it2.hasNext()){
			   Object cle = it2.next();
			   if ( this.correspondanceNomId.get(cle) == idObjASupprimmer )
				   it2.remove();
		   }
	   }
	}
	
	public void afficheToiTchounibabe() {
		System.out.println("cache :");
		System.out.println(cache);
		System.out.println("correspondanceNomId :");
		System.out.println(correspondanceNomId);
	}
}
