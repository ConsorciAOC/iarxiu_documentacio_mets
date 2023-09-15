package com.hp.iarxiu.metsgenerator.exception;

/**
 * Throws when an exception occurred in the Mets treatment.
 * @author Toni Marcos
 *
 */
public class MetsException extends Exception {

	/**
	 * Only for serialization purposes
	 */
	private static final long serialVersionUID = -1074629289021653260L;

	public MetsException() {
		super();
	}

	public MetsException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetsException(String message) {
		super(message);
	}

	public MetsException(Throwable cause) {
		super(cause);
	}
	
}
