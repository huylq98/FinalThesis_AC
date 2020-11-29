/*
 *@author ThomasLe
 *@date Nov 29, 2020
*/
package core.extract;

import java.io.IOException;
import org.jdom2.Element;

public abstract class PatternFilter extends FileTreeFilter {

	protected String pattern;

	protected PatternFilter() {
	}

	protected PatternFilter(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Returns this filter's pattern
	 * 
	 * @return
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Sets this patterns filter to something else
	 * 
	 * @param pattern
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void loadFromXML(Element element) throws IOException {
		setPattern(element.getText().trim());
	}

	@Override
	public void saveInner(Element e) throws IOException {
		e.setAttribute("class", this.getClass().getName());
		e.setText(getPattern().trim());
	}
}