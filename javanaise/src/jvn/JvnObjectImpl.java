package jvn;

import java.io.Serializable;
import java.util.Date;

public class JvnObjectImpl implements JvnObject {
	
	// ID
	private int id;
	
	private Date dateUnlock;

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

	public void jvnLockRead() throws JvnException {
		System.out.println("JvnObjectImpl.jvnLockRead() : " + this.lock);
		
		switch (this.lock) {
		case WC:
			this.lock = lockStates.RWC;
			break;
		case RC:
			this.lock = lockStates.R;
			break;
		case NL:
			this.data = this.server.jvnLockRead(this.id);
			this.lock = lockStates.R;
			break;
		default:
			throw new JvnException("opération impossible");
		}
	}


	public void jvnLockWrite() throws JvnException {
		System.out.println("JvnObjectImpl.jvnLockWrite() : " + this.lock);

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
			throw new JvnException("opération impossible");
		}
	}

	public synchronized void jvnUnLock() throws JvnException {
		System.out.println("JvnObjectImpl.jvnUnLock() : " + this.lock);
		this.dateUnlock = new Date();
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
		case RWC:
			this.lock = lockStates.WC;
			break;
		default:
			throw new JvnException("opération impossible");
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

	public synchronized void jvnInvalidateReader() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateReader()");

		switch (this.lock) {
		case R:
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
		default:
			throw new JvnException("opération impossible");
		}
	}

	public synchronized Serializable jvnInvalidateWriter() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateWriter()");

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
		default:
			throw new JvnException("opération impossible");
		}

		return this;
	}

	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		System.out.println("JvnObjectImpl.jvnInvalidateWriterForReader()");

		switch (this.lock) {
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
		default:
			throw new JvnException("opération impossible");
		}

		return this;
	}

	public void setLockNL() {
		this.lock = lockStates.NL;
	}

	public void setServer(JvnServerImpl jvnServerImpl) {
		this.server = jvnServerImpl;
	}
	
	public lockStates getVerrou() {
		return this.lock;
	}
	
	public Date getTimeUnlock() {
		return this.dateUnlock;
	}
}
