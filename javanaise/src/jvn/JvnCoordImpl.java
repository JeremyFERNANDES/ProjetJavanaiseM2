/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
	
	private Hashtable <Integer, ArrayList<JvnRemoteServer>> correspondanceIdServeur;
	
	private Hashtable <String, Integer> correspondanceNomId;
	
	private Hashtable <Integer, JvnObject> correspondanceIdObjet;
	
	private Hashtable <Integer, LockType> correspondanceObjetVerrou;
	
  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
		System.out.println("JvnCoordImpl.JvnCoordImpl()");
		this.correspondanceIdServeur = new Hashtable<Integer, ArrayList<JvnRemoteServer>>();
		this.correspondanceNomId = new Hashtable<String, Integer>();
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
	  int id = jo.jvnGetObjectId();
	  this.correspondanceNomId.put(jon, id);
	  
	  this.correspondanceIdServeur.put(id, new ArrayList<JvnRemoteServer>());
	  this.correspondanceIdServeur.get(id).add(js);
	  
	  this.correspondanceObjetVerrou.put(id, LockType.W);
	  this.correspondanceIdObjet.put(id, jo);
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
	  Integer id = this.correspondanceNomId.get(jon);
	  if (id != null) 
		  return this.correspondanceIdObjet.get(id);
	  else return null;
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
	   if ( this.correspondanceObjetVerrou.get(joi) == LockType.R ) {
		   this.correspondanceIdServeur.get(joi).add(js);
		   return this.correspondanceIdObjet.get(joi).jvnGetObjectState();
	   }
	   
	   else {
		   // MAJ DANS UNE SEUL FONCTION ?
		   Serializable o = this.correspondanceIdServeur.get(joi).get(0).jvnInvalidateWriterForReader(joi);
		   
		   this.correspondanceIdObjet.put(joi, ((JvnObject) o));
		   this.correspondanceIdServeur.get(joi).clear();
		   this.correspondanceIdServeur.get(joi).add(js);
		   
		   this.correspondanceObjetVerrou.put(joi, LockType.R);
		   return ((JvnObject) o).jvnGetObjectState();
	   }
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
	   if ( this.correspondanceObjetVerrou.get(joi) == LockType.R ) {
		   for (JvnRemoteServer s : this.correspondanceIdServeur.get(joi)) {
			   s.jvnInvalidateReader(joi);
		   }
		   this.correspondanceObjetVerrou.put(joi, LockType.W);
		   
		   this.correspondanceIdServeur.get(joi).clear();
		   this.correspondanceIdServeur.get(joi).add(js);
		   
		   return this.correspondanceIdObjet.get(joi).jvnGetObjectState();
	   }
	   else {
		   // MAJ DANS UNE SEUL FONCTION ?
		   Serializable o = this.correspondanceIdServeur.get(joi).get(0).jvnInvalidateWriter(joi);
		   
		   this.correspondanceIdObjet.put(joi, ((JvnObject) o));
		   
		   this.correspondanceIdServeur.get(joi).clear();
		   this.correspondanceIdServeur.get(joi).add(js);
		   
		   this.correspondanceObjetVerrou.put(joi, LockType.W);
		   return ((JvnObject) o).jvnGetObjectState();
	   }
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

 
