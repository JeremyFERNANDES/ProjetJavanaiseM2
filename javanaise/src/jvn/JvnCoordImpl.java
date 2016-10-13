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
import java.rmi.RemoteException;
import java.rmi.registry.*;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {
	private enum LockType {
		R, W, NL
	}

	// Compteur
	private int compteur;

	private Hashtable<Integer, ArrayList<JvnRemoteServer>> correspondanceIdServeur;

	private Hashtable<String, Integer> correspondanceNomId;

	private Hashtable<Integer, JvnObject> correspondanceIdObjet;

	private Hashtable<Integer, LockType> correspondanceObjetVerrou;

	private JvnCoordImpl() throws Exception {
		//System.out.println("JvnCoordImpl.JvnCoordImpl()");
		this.correspondanceIdServeur = new Hashtable<Integer, ArrayList<JvnRemoteServer>>();
		this.correspondanceNomId = new Hashtable<String, Integer>();
		this.correspondanceObjetVerrou = new Hashtable<Integer, LockType>();
		this.correspondanceIdObjet = new Hashtable<Integer, JvnObject>();
		this.compteur = 0;
	}

	public synchronized int jvnGetObjectId() throws java.rmi.RemoteException, jvn.JvnException {
		// to be completed
		//System.out.println("JvnCoordImpl.jvnGetObjectId()");
		return compteur++;
	}

	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {
		//System.out.println("JvnCoordImpl.jvnRegisterObject()");
		int id = jo.jvnGetObjectId();
		
		synchronized(this) {
			this.correspondanceNomId.put(jon, id);
			this.correspondanceIdObjet.put(id, jo);
			this.correspondanceObjetVerrou.put(id, LockType.W);
			this.correspondanceIdServeur.put(id, new ArrayList<JvnRemoteServer>());
			this.correspondanceIdServeur.get(id).add(js);	
		}
	}

	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, jvn.JvnException {
		//System.out.println("JvnCoordImpl.jvnLookupObject()");
		Integer id = this.correspondanceNomId.get(jon);
		if (id != null)
			return this.correspondanceIdObjet.get(id);
		else
			return null;
	}
	
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		//System.out.println("JvnCoordImpl.jvnLockRead()");
		if (this.correspondanceObjetVerrou.get(joi) == LockType.R) {
			synchronized(this) {
				this.correspondanceIdServeur.get(joi).add(js);
			}
			return this.correspondanceIdObjet.get(joi).jvnGetObjectState();
		} else if (this.correspondanceObjetVerrou.get(joi) == LockType.W) {
			Serializable o = this.correspondanceIdServeur.get(joi).get(0).jvnInvalidateWriterForReader(joi);
			
			synchronized(this) {
				this.correspondanceIdObjet.put(joi, ((JvnObject) o));
				this.correspondanceObjetVerrou.put(joi, LockType.R);
				this.correspondanceIdServeur.get(joi).clear();
				this.correspondanceIdServeur.get(joi).add(js);
			}
			// Fin de la MAJ
			return ((JvnObject) o).jvnGetObjectState();
		} else { // Cas NL
			synchronized(this) {
				this.correspondanceIdServeur.get(joi).add(js);
				this.correspondanceObjetVerrou.put(joi, LockType.W);
			}
			return this.correspondanceIdObjet.get(joi).jvnGetObjectState();
		}
	}

	public synchronized Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		//System.out.println("JvnCoordImpl.jvnLockWrite()");
		if (this.correspondanceObjetVerrou.get(joi) == LockType.R) {
			for (JvnRemoteServer s : this.correspondanceIdServeur.get(joi)) {
				if (js.equals(s)) continue;
				s.jvnInvalidateReader(joi);
			}
			this.correspondanceObjetVerrou.put(joi, LockType.W);
			this.correspondanceIdServeur.get(joi).clear();
			this.correspondanceIdServeur.get(joi).add(js);

			return this.correspondanceIdObjet.get(joi).jvnGetObjectState();
		} else if (this.correspondanceObjetVerrou.get(joi) == LockType.W) {
			Serializable o = this.correspondanceIdServeur.get(joi).get(0).jvnInvalidateWriter(joi);

			this.correspondanceIdObjet.put(joi, ((JvnObject) o));
			
			this.correspondanceObjetVerrou.put(joi, LockType.W);
			this.correspondanceIdServeur.get(joi).clear();
			this.correspondanceIdServeur.get(joi).add(js);			
			
			// Fin de la MAJ
			return ((JvnObject) o).jvnGetObjectState();
		} else { // cas NL
			this.correspondanceObjetVerrou.put(joi, LockType.W);
			this.correspondanceIdServeur.get(joi).clear();
			this.correspondanceIdServeur.get(joi).add(js);

			return this.correspondanceIdObjet.get(joi).jvnGetObjectState();
		}
	}

	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		//System.out.println("JvnCoordImpl.jvnTerminate()");
		// TODO to be completed
	}

	public static void main(String[] args) {
		//System.out.println("JvnCoordImpl.main()");
		JvnCoordImpl coordinateur;
		try {
			coordinateur = new JvnCoordImpl();
			// create registry
			Registry r = LocateRegistry.createRegistry(1099);
			r.rebind("coordinateur", coordinateur);

			//System.out.println("Server ready");
		} catch (Exception e) {
			System.out.println("Error on server :" + e);
			e.printStackTrace();
		}
	}

	public synchronized void jvnObjectDeleteToTheChache(JvnRemoteServer js, int joi) {
		if (this.correspondanceObjetVerrou.get(joi) == LockType.R) {
			this.correspondanceIdServeur.get(joi).remove(js);
		} else {
			this.correspondanceIdServeur.get(joi).clear();
			this.correspondanceObjetVerrou.put(joi, LockType.NL);	
		}
		
	}

}
