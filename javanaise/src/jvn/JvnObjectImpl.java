package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
	
	private enum lockStates {
		R,
		W,
		RC,
		WC,
		RWC,
		NL;
	}
	
	// ID
	private int id;
	
	// verrou
	private lockStates lock;
	
	//référence vers l'objet applicatif
	private Serializable data;
	
	//référence vers le serveur
	public transient JvnLocalServer server;
	
	public JvnObjectImpl (Serializable o, int id, JvnLocalServer js) {
		System.out.println("JvnObjectImpl.JvnObjectImpl()");
		this.data = o;
		this.id = id;
		this.lock = lockStates.W;
		this.server = js ;
	}

	public void jvnLockRead() throws JvnException {
		System.out.println("JvnObjectImpl.jvnLockRead() : " + this.lock);
		switch (this.lock)
		{
			case WC:
				break;
			case RC:
				this.lock = lockStates.R;
				break;
			case R:
				break;
			case W:
				//todo
				break;
			case NL:
				this.data = this.server.jvnLockRead(this.id);
				this.lock = lockStates.R;
				break;
		  default:          

		}
		//todo : switch(R,W..)
	}

	public void jvnLockWrite() throws JvnException {
		System.out.println("JvnObjectImpl.jvnLockWrite() : " + this.lock);
		switch (this.lock)
		{
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
		switch (this.lock)
		{
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
		//todo : switch(R,W..)
		// - si RC, WC, NL : on donne le verrou$
		// - si R ou W : wait()
		//terminer l'attente = notify()
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateWriter()");
		// TODO Auto-generated method stub
		return this.data;
		//todo : pareil que invalidateReader
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateWriterForReader()");
		// TODO Auto-generated method stub
		return this.data;
	}
	
	public void setServeur(JvnServerImpl s) {
		System.out.println("JvnObjectImpl.setServeur()");
		this.server = s;
	}
}
