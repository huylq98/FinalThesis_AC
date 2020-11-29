/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package lexers.parser;

import core.Tokenizer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.Trees;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.jdom2.Element;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;

public class AntlrTokenizer implements Tokenizer {

	private static final Logger log = LogManager
			.getLogger(AntlrTokenizer.class);

	public static boolean parse = false;

	private class LanguageSupport {
		public final Constructor<?> lexerConstructor;
		public final Constructor<?> parserConstructor;
		public final Method parserMethod;

		public LanguageSupport(String prefix, String entryPoint) {
			final String lexerClassName = prefix + "Lexer";
			final String parserClassName = prefix + "Parser";

			ClassLoader loader = getClass().getClassLoader();

			try {
				final Class<? extends Lexer> lexerClass = loader.loadClass(
						lexerClassName).asSubclass(Lexer.class);
				final Class<? extends Parser> parserClass = loader.loadClass(
						parserClassName).asSubclass(Parser.class);
				parserMethod = parserClass.getMethod(entryPoint);
				lexerConstructor = lexerClass.getConstructor(CharStream.class);
				parserConstructor = parserClass
						.getConstructor(TokenStream.class);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Could not initialize lexer/parser pair for " + prefix,
						e);
			}
		}
	}

	private static HashMap<String, LanguageSupport> languages = new HashMap<String, LanguageSupport>();

	private LanguageSupport language;

	public AntlrTokenizer(String lang, String entryPoint) {
		if (!languages.containsKey(lang)) {
			languages.put(lang, new LanguageSupport(lang, entryPoint));
		}
		this.language = languages.get(lang);
	}

	public void tokenize(String source, String sourceFile, PrintWriter out) {
		Writer debugWriter = null;
		try {
			Lexer lexer = (Lexer) language.lexerConstructor
					.newInstance(new ANTLRInputStream(source));
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			tokens.fill();

			if (log.isDebugEnabled()) {
				try {
					debugWriter = new BufferedWriter(new FileWriter(Files
							.createTempFile(
									"tokens-" + ThreadContext.peek() + "-",
									".txt").toFile()));
				} catch (IOException ioe) {
					log.warn("Could not create debugWriter", ioe);
				}
			}

			for (final Token tok : tokens.getTokens()) {
				out.print(tokenToString(tok));
				if (log.isDebugEnabled()) {
					log.debug(tok);
					if (debugWriter != null) {
						debugWriter.write(tokenToString(tok));
					}
				}
			}

			if (parse) {
				Parser parser = (Parser) language.parserConstructor
						.newInstance(tokens);
				parser.setErrorHandler(new BailErrorStrategy());
				ParserRuleContext parserRuleContext = (ParserRuleContext) language.parserMethod
						.invoke(parser);

				if (log.isDebugEnabled()) {
					log.debug(Trees.toStringTree(parserRuleContext, parser));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(
					"Bad token in source, or failed to parse", e);
		} finally {
			out.flush();
			if (log.isDebugEnabled() && debugWriter != null) {
				try {
					debugWriter.close();
				} catch (IOException ioe) {
					log.warn("Could not close debugWriter", ioe);
				}
			}
		}
	}

	private String tokenToString(Token token) {
		return "" + Integer.toString(token.getType(), 32) + " ";
	}

	public int tokenId(String token) {
		return Integer.parseInt(token, 32);
	}

	public void retrieveComments(String source, String sourceFile,
			PrintWriter out) {
		throw new UnsupportedOperationException("Not yet supported");
	}

	public Element saveToXML() throws IOException {
		throw new UnsupportedOperationException("Not yet supported");
	}

	public void loadFromXML(Element element) throws IOException {
		throw new UnsupportedOperationException("Not yet supported");
	}
}
