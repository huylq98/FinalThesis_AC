/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core;

import core.util.XMLSerializable;
import java.io.PrintWriter;

public interface Tokenizer extends XMLSerializable {

	static final String TOKEN_KEY = "token";

	/**
	 * Tokenize a file into a buffer; any tokenization method is OK Filename is to
	 * be used only for error reporting
	 */
	void tokenize(String source, String sourceFile, PrintWriter out);

	/**
	 * Retrieve comment strings from a source file
	 */
	void retrieveComments(String source, String sourceFile, PrintWriter out);

	/**
	 * Get a number for a token string
	 */
	int tokenId(String token);
}
