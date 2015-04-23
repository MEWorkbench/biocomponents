package pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class PeriodicTable {

	protected HashMap<String, ChemicalElement> allElements;

	public PeriodicTable(
		HashMap<String, ChemicalElement> allElementsHashMap) {
		super();
		this.allElements = allElementsHashMap;
	}

	public ChemicalElement getElementBySymbol(String symbol) {
		return allElements.get(symbol);
	}

//	public ChemicalElement getElementByAtomicNumber(int atomicNumber) {
//		return allElements.getValueAt(atomicNumber);
//	}

	public String getRegularExpression() {
		Set<String> allKeys = allElements.keySet();

		String regExp = "";
		for (String elem : allKeys)
			regExp += elem + "|";

		return regExp.substring(0, regExp.length() - 1);
	}

	static public PeriodicTable constructPeriodicTable(String table)
			throws IOException {

		HashMap<String, ChemicalElement> elemental = new HashMap<String, ChemicalElement>();

		FileReader fileR = new FileReader(table);

		BufferedReader reader = new BufferedReader(fileR);

		String line = reader.readLine();

		while (line != null) {

			System.out.println(line);
			String[] data = line.split("\t");

			int atomicNumber = Integer.parseInt(data[0]);
			double atomicWeight = Double.parseDouble(data[2]);
			String name = data[3].trim();
			String symbol = data[4].trim();
			Double bolingPoint = parserDoubleValues(data[6]);
			Double meltingPoint = parserDoubleValues(data[5]);

			Double density = parserDoubleValues(data[7]);
			int group = Integer.parseInt(data[10]);

			Double iEnergy = Double.NaN;
			if (data.length >= 12)
				iEnergy = parserDoubleValues(data[12]);

			ChemicalElement elem = new ChemicalElement(atomicNumber, name,
					symbol, bolingPoint, meltingPoint, density, group,
					atomicWeight, iEnergy);
			elemental.put(elem.symbol, elem);

			line = reader.readLine();
		}

		reader.close();
		fileR.close();

		return new PeriodicTable(elemental);
	}

	static private Double parserDoubleValues(String value) {

		if (value == null || value.equals(""))
			return Double.NaN;

		return Double.parseDouble(value);
	}
}
