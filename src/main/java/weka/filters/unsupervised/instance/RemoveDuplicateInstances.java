/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * RemoveDuplicateInstances.java
 * 
 * Copyright (C) 2008 Sergio Garcia, Daniel Rodriguez
 */

package weka.filters.unsupervised.instance;

import java.util.Enumeration;
import java.util.TreeSet;
import java.util.Vector;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.InstanceComparator;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.SimpleBatchFilter;
import weka.filters.UnsupervisedFilter;

/**
 * <!-- globalinfo-start --> A filter that removes duplicate instances of a
 * dataset or returns those duplicate instances (it can consider the class separately).
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -I &lt;invert&gt;
 *  Specifies whether it will be returned the unique or duplicated instances. (default false) 
 *  false will return unique instances
 *  true will return duplicated instances
 * </pre>
 * 
 * <pre>
 * -C &lt;ifClass&gt;
 * Specifies whether the attribute class will be used in the comparison. (default true)
 * False will not use it,
 * true will use it.;
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author Sergio Garcia Charameli
 * @author Daniel Rodriguez - University of Alcala
 * @version $Revision: 1 $
 */

public class RemoveDuplicateInstances extends SimpleBatchFilter implements
		UnsupervisedFilter, OptionHandler {

	/* for serialization */
	private static final long serialVersionUID = -625792014317428164L;

	protected boolean m_Invert = false;

	protected boolean m_IfClass = true;

	public boolean getInvert() {
		return m_Invert;
	}

	public void setInvert(boolean invert) {
		this.m_Invert = invert;
	}

	public String invertTipText() {
		return "Specifies whether unique or duplicated instances will be returned. "
				+ "False will return unique instances, "
				+ "true will return duplicated instances.";
	}

	public boolean getIfClass() {
		return m_IfClass;
	}

	public void setIfClass(boolean ifClass) {
		this.m_IfClass = ifClass;
	}

	public String ifClassTipText() {
		return "Specifies whether the attribute class will be used in the comparison. "
				+ "False will not use it, " + "true will use it.";
	}

	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();

		// attributes
		result.enableAllAttributes();
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enableAllClasses();
		result.disable(Capability.NO_CLASS);

		return result;
	}

	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();

		newVector
				.addElement(new Option(
						"\tSpecifies whether unique or duplicated instances will be returned.\n",
						"I", 0, "-I"));

		newVector
				.addElement(new Option(
						"\tSpecifies whether the attribute class will be used in the comparison.\n",
						"C", 0, "-C"));

		return newVector.elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {

		String invertString = Utils.getOption('I', options);

		if (invertString.length() != 0) {
			setInvert(true);
		} else {
			setInvert(false);
		}

		String ifClassString = Utils.getOption('C', options);

		if (ifClassString.length() != 0) {
			setIfClass(true);
		} else {
			setIfClass(false);
		}

		if (getInputFormat() != null)
			setInputFormat(getInputFormat());
	}

	@Override
	public String[] getOptions() {

		String[] options = new String[2];
		int current = 0;

		if (getInvert()) {
			options[current++] = "-I";
		}

		if (getIfClass()) {
			options[current++] = "-C";
		}

		while (current < options.length) {
			options[current++] = "";
		}
		return options;
	}

	public String globalInfo() {
		return "A filter that removes duplicated instances of a dataset or returns them (it can also consider the class attribute separately)";
	}

	protected Instances determineOutputFormat(Instances inputFormat)
			throws Exception {
		Instances result = new Instances(inputFormat, 0);

		return result;
	}

	protected Instances process(Instances instances) throws Exception {
		Instances result = new Instances(determineOutputFormat(instances), 0);
		Instances duplicated = new Instances(determineOutputFormat(instances),
				0);

		// Classifying the instances in two groups, uniques and duplicated
		// (incl. class) -> InstanceComparator(true) ->
		// InstanceComparator(m_IfClass)
		InstanceComparator comp = new InstanceComparator(m_IfClass);
		TreeSet<Instance> set = new TreeSet<Instance>(comp);

		int uniques = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			set.add(instances.instance(i));
			if (uniques + 1 == set.size()) {
				result.add(instances.instance(i));
				uniques++;
			} else {
				duplicated.add(instances.instance(i));
			}
		}

		String exceptionMsg = new String(
				"0 instances will be returned. Additional information:"
						+ "\n\t" + uniques + ": unique instances." + "\n\t"
						+ duplicated.size() + ": duplicated instances."
						+ "\n\tifClass: " + m_IfClass + "\n\t"
						+ getInputFormat().classAttribute().toString());

		if (m_Invert) {
			if (duplicated.size() == 0)
				throw new Exception(exceptionMsg);
			return duplicated;
		} else {
			if (result.size() == 0)
				throw new Exception(exceptionMsg);
			return result;
		}
	}

	/**
	 * 
	 * @param args
	 *            the command-line attributes
	 * @throws Exception
	 *             if something goes wrong
	 */
	public static void main(String[] argv) {
		runFilter(new RemoveDuplicateInstances(), argv);
	}
}
