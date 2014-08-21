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
 * ENN.java
 * 
 * Copyright (C) 2008 Daniel Rodriguez
 */

package weka.filters.supervised.instance;

import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;
import weka.core.neighboursearch.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * <!-- globalinfo-start --> A filter that duplicates some random minority
 * instances until the total amount of minority instances reaches the percentage
 * given.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -P &lt;Percent&gt;
 *  Specifies the proportion of final minority class respecting the majority class. (default 25).
 *  If the percentage specified is lower than the current minority percentage, the filter does nothing,
 *  else, the filter duplicates minority classes randomly until the minority proportion reaches the percentage specified.
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author Daniel Rodriguez - University of Alcala
 * @version $Revision: 1 $
 */

public class ENN extends Filter implements SupervisedFilter, OptionHandler,
		TechnicalInformationHandler {

	/** for serialization. */
	private static final long serialVersionUID = 8790232981816685267L;
	
	/** number of neighbours */
	protected int m_NumberOfNN = 3;

	/**
	 * Returns a string describing this classifier.
	 * 
	 * @return a description of the classifier suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "ENN"
				+ "For more information, see\n\n"
				+ getTechnicalInformation().toString();
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed
	 * information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result = new TechnicalInformation(Type.ARTICLE);

		result.setValue(Field.AUTHOR,
				"Wilson");
		result.setValue(
				Field.TITLE,
				"ENN");
		result.setValue(Field.JOURNAL, "SIGKDD Explorations");
		result.setValue(Field.YEAR, "2004");
		result.setValue(Field.VOLUME, "6");
		result.setValue(Field.PAGES, "Issue 1, 20-29");

		return result;
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 1 $");
	}

	/**
	 * Returns the Capabilities of this filter.
	 * 
	 * @return the capabilities of this object
	 * @see Capabilities
	 */
	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();

		result.disableAll();

		// attributes
		result.enableAllAttributes();
	    result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.BINARY_CLASS);
		result.enable(Capability.NOMINAL_CLASS);
	    result.enable(Capability.MISSING_CLASS_VALUES);

		return result;
	}

	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	public Enumeration listOptions() {
		Vector newVector = new Vector();

		newVector.addElement(new Option(
			"\t Number of neighbours.\n"
			+ "\t(default 3)\n", "K", 3, "-K <n>"));

		return newVector.elements();
	}

	/**
	 * Parses a given list of options.
	 * 
	 * <!-- options-start --> Valid options are:
	 * <p/>
	 * 
	 * <pre>
	 * -K &lt;n&gt;
	 *  Specifies k  (default 3).
	 * </pre>
	 * 
	 * <!-- options-end -->
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {

		String nStr = Utils.getOption('K', options);
		if (nStr.length() != 0) {
			setNumberOfNN(new Integer(nStr).intValue());
		} else {
			setNumberOfNN(3);
		}

	}

	/**
	 * Gets the current settings of the filter.
	 * 
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {
		Vector<String> result;

		result = new Vector<String>();

		result.add("-K");
		result.add("" + getNumberOfNN());

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String NumberOfNNTipText() {
		return "Specifies the number of neighbours (k).";
	}

	/**
	 * Sets the percentage of minority class.
	 * 
	 * @param value
	 *            the percentage to use
	 */
	public void setNumberOfNN(int value) {
		if (value < 1 || value > 99) {
			throw new IllegalArgumentException(
					"No. of neighbouts must be between 1 and 99.");
		}
		this.m_NumberOfNN = value;
	}

	/**
	 * Gets the percentage of minority class.
	 * 
	 * @return the percentage of minority class
	 */
	public int getNumberOfNN() {
		return m_NumberOfNN;
	}

	/**
	 * Sets the format of the input instances.
	 * 
	 * @param instanceInfo
	 *            an Instances object containing the input instance structure
	 *            (any instances contained in the object are ignored - only the
	 *            structure is required).
	 * @return true if the outputFormat may be collected immediately
	 * @throws Exception
	 *             if the input format can't be set successfully
	 */
	public boolean setInputFormat(Instances instanceInfo) throws Exception {
		super.setInputFormat(instanceInfo);
		super.setOutputFormat(instanceInfo);
		return true;
	}

	/**
	 * Input an instance for filtering. Filter requires all training instances
	 * be read before producing output.
	 * 
	 * @param instance
	 *            the input instance
	 * @return true if the filtered instance may now be collected with output().
	 * @throws IllegalStateException
	 *             if no input structure has been defined
	 */
	public boolean input(Instance instance) {
		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}
		if (m_NewBatch) {
			resetQueue();
			m_NewBatch = false;
		}
		if (m_FirstBatchDone) {
			push(instance);
			return true;
		} else {
			bufferInput(instance);
			return false;
		}
	}

	/**
	 * Signify that this batch of input to the filter is finished. If the filter
	 * requires all instances prior to filtering, output() may now be called to
	 * retrieve the filtered instances.
	 * 
	 * @return true if there are instances pending output
	 * @throws IllegalStateException
	 *             if no input structure has been defined
	 * @throws Exception
	 *             if provided options cannot be executed on input instances
	 */
	public boolean batchFinished() throws Exception {
		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}

		if (!m_FirstBatchDone) {
			// Do ENN, and clear the input instances.
			doENN();
		}
		flushInput();

		m_NewBatch = true;
		m_FirstBatchDone = true;
		return (numPendingOutput() != 0);
	}

	/**
	 * The procedure implementing the ENN algorithm. The output instances are
	 * pushed onto the output queue for collection.
	 * 
	 * @throws Exception
	 *             if provided options cannot be executed on input instances
	 */
	protected void doENN() throws Exception {
		Instances sourceInstances = getInputFormat();
		int sampleSize = getInputFormat().numInstances();
		
		int nClassIndex = getInputFormat().classIndex(); // Get the class attribute
		AttributeStats stats = sourceInstances.attributeStats(nClassIndex);
		
		int nClasses = stats.distinctCount; // Gets the number of distinct classes
		int[] nNerarestNeighborsClasses = new int[nClasses];  // To get the majority class of the retrieved neighbours
		
		NearestNeighbourSearch NB = new LinearNNSearch(sourceInstances); // subclass of abstract NearestNeighbourSearch();

		
		// ENN - For all instances, an instance is kept if its class and 
		// the majority of its k nearest neighbours have the same class value
	    for (int i = 0; i < sampleSize; i++) {
	    	// 1. Current Instance
	    	Instance target=getInputFormat().instance(i); 
	    		    	
	        // 2. Get k neighbours of current instance (target)
	        Instances neighbours = NB.kNearestNeighbours(target, m_NumberOfNN);
            
	        // 3. Get majority class value of the k nearest neighbours
	        
            // 3.1. Initialize counters of classes
            for(int nIdx=0; nIdx < nClasses; nIdx++){
                nNerarestNeighborsClasses[nIdx] = 0;
            }
            int classValue;
     	    
            // 3.2 Get the number of neighbours per class value
	     	//System.out.println("For neighbour: " + target.toString());
	     	//System.out.println("  with class: " + target.classValue());
	     	for (int j = 0; j < neighbours.numInstances(); j++) {
                classValue = (int) neighbours.get(j).classValue();
                nNerarestNeighborsClasses[classValue]++;
	     		//System.out.println("\t -> " + neighbours.get(j).toString());
	        	//System.out.println("\t -> Class: " + neighbours.get(j).classValue());

	   	    }
	     	
	     	// 4. Check if current instance must be removed
	     	// There is another class value as majority
	     	int currentInstClassValue = (int)target.classValue();
	     	int nAgreeNeighbors = nNerarestNeighborsClasses[currentInstClassValue];
	     	boolean bRemove = false;
	     	
	     	for(int nIdx=0;(nIdx < nNerarestNeighborsClasses.length) && !bRemove; nIdx++){
                if(nIdx!=currentInstClassValue){
                    bRemove = nNerarestNeighborsClasses[nIdx] > nAgreeNeighbors;
                }
            }
	     	
	     	// 5. Push onto queue if
	     	if (!bRemove) {
	     		push((Instance) target.copy());
	     	}
	     	

	     }
	}

	/**
	 * Main method for running this filter.
	 * 
	 * @param args
	 *            should contain arguments to the filter: use -h for help
	 */
	public static void main(String[] args) {
		runFilter(new ENN(), args);
	}
}

