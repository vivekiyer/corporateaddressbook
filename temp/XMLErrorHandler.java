package net.vivekiyer.GAL;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import android.util.Log;

public class XMLErrorHandler implements ErrorHandler {

	private String TAG = "XMLErrorHandler";
	private int column = 0;

	public void setColumn(int column) {
		this.column = column;
	}

	public int getColumn() {
		return column;
	}

	@Override
	public void error(SAXParseException exception) {
		// Lets do nothing but log the error
		Log.e(TAG, exception.toString());
	}

	@Override
	public void fatalError(SAXParseException exception)
			throws SAXParseException {
		// Lets do nothing, but log the error
		setColumn(exception.getColumnNumber());
		Log.e(TAG, "Error column: " + exception.getColumnNumber());
		Log.e(TAG, exception.toString());
		throw exception;
	}

	@Override
	public void warning(SAXParseException exception) {
		// Lets do nothing but log the error
		Log.d(TAG, exception.toString());

	}

}
