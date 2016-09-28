/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.io.Serializable;
import java.rmi.registry.*;


public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	private enum LockType {
		R,
		W
	}

	// Compteur
	private int compteur;
	
	private Hashtable <Integer, JvnRemoteServer> correspondanceIdServeur;
	
	private Hashtable <String, JvnObject> correspondanceNomObjet;
	
	private Hashtable <Integer, JvnObject> correspondanceIdObjet;
	
	private Hashtable <Integer, LockType> correspondanceObjetVerrou;
	
  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
		System.out.println("JvnCoordImpl.JvnCoordImpl()");
		this.correspondanceIdServeur = new Hashtable<Integer, JvnRemoteServer>();
		this.correspondanceNomObjet = new Hashtable<String, JvnObject>();
		this.correspondanceObjetVerrou = new Hashtable<Integer, LockType>();
		this.correspondanceIdObjet = new Hashtable<Integer, JvnObject>();
		this.compteur = 0;
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
    // to be completed 
	System.out.println("JvnCoordImpl.jvnGetObjectId()");
    return compteur++;
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	  System.out.println("JvnCoordImpl.jvnRegisterObject()");
	  this.correspondanceNomObjet.put(jon, jo);
	  this.correspondanceIdServeur.put(jo.jvnGetObjectId(), js);
	  this.correspondanceObjetVerrou.put(jo.jvnGetObjectId(), LockType.W);
	  this.correspondanceIdObjet.put(jo.jvnGetObjectId(), jo);
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	  System.out.println("JvnCoordImpl.jvnLookupObject()");
    return this.correspondanceNomObjet.get(jon);
  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
	   System.out.println("JvnCoordImpl.jvnLockRead()");
	   // voir comment le client à le verrou et en fonction faire soit InvalidateReader soit InvalidateWriter
	   if ( this.correspondanceObjetVerrou.get(joi) == LockType.R )
		   return this.correspondanceIdObjet.get(joi).jvnGetObjectState();
	   else return this.correspondanceIdServeur.get(joi).jvnInvalidateWriterForReader(joi);
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException {
	   System.out.println("JvnCoordImpl.jvnLockWrite()");
	   return this.correspondanceIdServeur.get(joi).jvnInvalidateWriter(joi);
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
    	System.out.println("JvnCoordImpl.jvnTerminate()");
	 // to be completed
    }
    
    public static void main(String[] args){
    	System.out.println("JvnCoordImpl.main()");
    	JvnCoordImpl coordinateur;
		try {
			coordinateur = new JvnCoordImpl();
			// create registry
			Registry r = LocateRegistry.createRegistry(1099);
			r.rebind("coordinateur", coordinateur);

			System.out.println("Server ready");
		}
		catch(Exception e){
			System.out.println("Error on server :" + e);
			e.printStackTrace();
		}
	}
    
}

 
