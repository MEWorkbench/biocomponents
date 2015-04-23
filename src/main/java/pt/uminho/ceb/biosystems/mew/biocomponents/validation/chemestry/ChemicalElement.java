package pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry;

public class ChemicalElement {

	protected int atomicNumber;
	protected String name;
	protected String symbol;
	protected Double bolingPoint;
	protected Double meltingPoint;
	protected Double density;
	protected int group;
	protected Double atomicWeight;
	protected Double iEnergy;

	public ChemicalElement(int atomicNumber, String name, String symbol,
			double bolingPoint, double meltingPoint, double density, int group,
			double atomicWeight, double iEnergy) {
		super();
		this.atomicNumber = atomicNumber;
		this.name = name;
		this.symbol = symbol;
		this.bolingPoint = bolingPoint;
		this.meltingPoint = meltingPoint;
		this.density = density;
		this.group = group;
		this.atomicWeight = atomicWeight;
		this.iEnergy = iEnergy;
	}

	public int getAtomicNumber() {
		return atomicNumber;
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	public double getBolingPoint() {
		return bolingPoint;
	}

	public double getMeltingPoint() {
		return meltingPoint;
	}

	public double getDensity() {
		return density;
	}

	public int getGroup() {
		return group;
	}

	public double getAtomicWeight() {
		return atomicWeight;
	}

	public double getiEnergy() {
		return iEnergy;
	}
}
