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
		this.data = o;
		this.id = id;
		this.lock = lockStates.W;
		this.server = js ;
	}

	public void jvnLockRead() throws JvnException {
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
		this.lock = lockStates.NL;
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
		return this.id;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		return this.data;
	}

	public void jvnInvalidateReader() throws JvnException {
		this.lock = lockStates.NL;
		//todo : switch(R,W..)
		// - si RC, WC, NL : on donne le verrou$
		// - si R ou W : wait()
		//terminer l'attente = notify()
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		// TODO Auto-generated method stub
		return this.data;
		//todo : pareil que invalidateReader
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		// TODO Auto-generated method stub
		return this.data;
	}
	
	public void setServeur(JvnServerImpl s) {
		this.server = s;
	}
}
