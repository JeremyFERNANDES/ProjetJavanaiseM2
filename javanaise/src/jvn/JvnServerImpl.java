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



public class JvnServerImpl 	
              extends UnicastRemoteObject 
							implements JvnLocalServer, JvnRemoteServer{
	
  // A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;
	
	private Hashtable<Integer, JvnObject> cache;
	
	private Hashtable<String, JvnObject> correspondanceNomObjet;
	
	private JvnRemoteCoord coordinateur;
	


  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		this.cache = new Hashtable<Integer, JvnObject>();
		this.correspondanceNomObjet = new Hashtable<String, JvnObject>();
		Registry registry = LocateRegistry.getRegistry("localhost");
		this.coordinateur = (JvnRemoteCoord) registry.lookup("coordinateur");
	}
	
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public void jvnTerminate()
	throws jvn.JvnException {
    // to be completed 
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public JvnObject jvnCreateObject(Serializable o)
	throws jvn.JvnException {
		JvnObject o1 = null;
		int id = 0;
		try {
			id = this.coordinateur.jvnGetObjectId();
			o1 = new JvnObjectImpl(o, id, js);
			//par défaut, demande le verrou en écriture à la création de l'objet
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		this.cache.put(id, o1);
		return this.cache.get(id);
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public void jvnRegisterObject(String jon, JvnObject jo)
	throws jvn.JvnException {
		this.correspondanceNomObjet.put(jon, jo);
		try {
			this.coordinateur.jvnRegisterObject(jon, jo, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public JvnObject jvnLookupObject(String jon)
	throws jvn.JvnException {
		JvnObject o = null;
		JvnObject serveurObject = null;
		try {
			serveurObject = this.coordinateur.jvnLookupObject(jon, js);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (this.correspondanceNomObjet.get(jon) != null) {
			o = this.correspondanceNomObjet.get(jon);
		}
		else if ( serveurObject != null ) {
			o = serveurObject;
			o.setServeur(this);
			this.correspondanceNomObjet.put(jon, o);
		}
		return o;
	}
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
	   Serializable s = null;
		try {
			s = this.coordinateur.jvnLockRead(joi, this.js);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;

	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
	 throws JvnException {
	    Serializable s = null;
		try {
			s = this.coordinateur.jvnLockWrite(joi, this.js);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,jvn.JvnException {
		// to be completed 
	};
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException { 
		// to be completed 
		return this.cache.get(joi).jvnInvalidateWriter();
	};
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
		// to be completed
		return this.cache.get(joi).jvnInvalidateWriterForReader();
	 };

}

 
