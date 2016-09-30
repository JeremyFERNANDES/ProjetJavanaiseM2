package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

	private enum lockStates {
		R, W, RC, WC, RWC, NL;
	}

	// ID
	private int id;

	// Verrou sur l'objet
	private lockStates lock;

	// Référence vers l'objet applicatif
	private Serializable data;

	// Référence vers le serveur local
	public transient JvnLocalServer server;

	public JvnObjectImpl(Serializable o, int id, JvnLocalServer js, boolean write) {
		System.out.println("JvnObjectImpl.JvnObjectImpl()");
		this.data = o;
		this.id = id;
		if (write)
			this.lock = lockStates.W;
		else
			this.lock = lockStates.NL;
		this.server = js;
	}

	public void jvnLockRead() throws JvnException {
		System.out.println("JvnObjectImpl.jvnLockRead() : " + this.lock);
		
		// TODO : A completer
		switch (this.lock) {
		case WC:
			break;
		case RC:
			this.lock = lockStates.R;
			break;
		case R:
			break;
		case W:
			break;
		case NL:
			this.data = this.server.jvnLockRead(this.id);
			this.lock = lockStates.R;
			break;
		default:

		}
	}

	public void jvnLockWrite() throws JvnException {
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

	public void jvnUnLock() throws JvnException {
		System.out.println("JvnObjectImpl.jvnUnLock() : " + this.lock);
		// TODO : A completer
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

		}
	}

	public int jvnGetObjectId() throws JvnException {
		System.out.println("JvnObjectImpl.jvnGetObjectId()");
		return this.id;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		System.out.println("JvnObjectImpl.jvnGetObjectState()");
		return this.data;
	}

	public void jvnInvalidateReader() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateReader()");
		this.lock = lockStates.NL;
		// TODO : switch(R,W..)
		// - si RC, WC, NL : on donne le verrou$
		// - si R ou W : wait()
		// terminer l'attente = notify()
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateWriter()");
		// TODO Auto-generated method stub
		this.lock = lockStates.NL;
		return this;
		// TODO : pareil que invalidateReader
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateWriterForReader()");
		// TODO Auto-generated method stub
		this.lock = lockStates.NL;
		return this;
	}

	public void setServeur(JvnServerImpl s) {
		System.out.println("JvnObjectImpl.setServeur()");
		this.server = s;
	}
}
