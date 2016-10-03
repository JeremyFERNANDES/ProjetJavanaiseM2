package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

	private enum lockStates {
		R, W, RC, WC, RWC, NL;
	}

	// ID
	private int id;

	// Verrou sur l'objet
	private transient lockStates lock;

	// Référence vers l'objet applicatif
	private Serializable data;

	// Référence vers le serveur local
	public transient JvnLocalServer server;

	public JvnObjectImpl(Serializable o, int id, JvnLocalServer js) {
		System.out.println("JvnObjectImpl.JvnObjectImpl()");
		this.data = o;
		this.id = id;
		this.lock = lockStates.W;
		this.server = js;
	}

	//TODO : synchro seulement sur la portion de code nécessaire
	public synchronized void jvnLockRead() throws JvnException {
		System.out.println("JvnObjectImpl.jvnLockRead() : " + this.lock);
		
		// TODO : A completer
		switch (this.lock) {
		case WC:
			this.lock = lockStates.RWC;
			break;
		case RC:
			this.lock = lockStates.R;
			break;
		case R:
			//en théorie : cas d'erreur
			break;
		case W:
			//en théorie : cas d'erreur
			break;
		case NL:
			this.data = this.server.jvnLockRead(this.id);
			this.lock = lockStates.R;
			break;
		default:
			//en théorie : cas d'erreur
		}
	}

	//TODO : synchro seulement sur la portion de code nécessaire
	public synchronized void jvnLockWrite() throws JvnException {
		System.out.println("JvnObjectImpl.jvnLockWrite() : " + this.lock);
		// TODO : A completer
		switch (this.lock) {
		case WC:
			this.lock = lockStates.W;
			break;
		case RC:
			this.data = this.server.jvnLockWrite(this.id);
			this.lock = lockStates.W;
			break;
		case R:
			this.data = this.server.jvnLockWrite(this.id);
			this.lock = lockStates.W;
			break;
		case W:
			this.lock = lockStates.W;
			break;
		case NL:
			this.data = this.server.jvnLockWrite(this.id);
			this.lock = lockStates.W;
			break;
		default:
			
		}
	}
	
	//TODO : synchro seulement sur la portion de code nécessaire
	public synchronized void jvnUnLock() throws JvnException {
		System.out.println("JvnObjectImpl.jvnUnLock() : " + this.lock);
		// TODO : A completer (notify)
		switch (this.lock) {
		case R:
			this.lock = lockStates.RC;
			break;
		case W:
			this.lock = lockStates.WC;
			break;
		case NL:
			this.lock = lockStates.NL;
			break;
		default:
			//cas d'erreur
		}
		notifyAll();
	}

	public int jvnGetObjectId() throws JvnException {
		System.out.println("JvnObjectImpl.jvnGetObjectId()");
		return this.id;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		System.out.println("JvnObjectImpl.jvnGetObjectState()");
		return this.data;
	}

	//todo : synchro seulement sur la portion de code nécessaire
	public synchronized void jvnInvalidateReader() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateReader()");
		
		//TODO : compléter wait
		switch (this.lock) {
		case R:
			try {
				this.wait();
				this.lock = lockStates.NL;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
			break;
		case W:
			//cas d'erreur
			break;
		case RC:
			this.lock = lockStates.NL;
			break;
		case WC:
			this.lock = lockStates.NL;
			break;
		case RWC:
			this.lock = lockStates.NL;
			break;
		case NL:
			//cas d'erreur
			break;
		default:
			//cas d'erreur
		}
	}

	//TODO : synchro seulement sur la portion de code nécessaire
	public synchronized Serializable jvnInvalidateWriter() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateWriter()");
		
		//TODO : compléter wait
		switch (this.lock) {
		case R:
			try {
				this.wait();
				this.lock = lockStates.NL;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
			break;
		case W:
			try {
				this.wait();
				this.lock = lockStates.NL;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case RC:
			this.lock = lockStates.NL;
			break;
		case WC:
			this.lock = lockStates.NL;
			break;
		case RWC:
			this.lock = lockStates.NL;
			break;
		case NL:
			//cas d'erreur
			break;
		default:
			//cas d'erreur
		}		
		
		return this;
		// TODO : pareil que invalidateReader
	}

	//TODO : synchro seulement sur la portion de code nécessaire
	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateWriterForReader()");

		//TODO : compléter wait
		switch (this.lock) {
		case R:
			//cas d'erreur
		case W:
			try {
				this.wait();
				this.lock = lockStates.NL;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case RC:
			this.lock = lockStates.NL;
			break;
		case WC:
			this.lock = lockStates.NL;
			break;
		case RWC:
			this.lock = lockStates.NL;
			break;
		case NL:
			//cas d'erreur
			break;
		default:
			//cas d'erreur
		}		
		
		return this;
	}
	
	public void setLockNL() {
		this.lock = lockStates.NL;
	}

	public void setServer(JvnServerImpl jvnServerImpl) {
		this.server = jvnServerImpl;
	}
}
