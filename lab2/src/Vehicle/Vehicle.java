package Vehicle;

public class Vehicle {
	String plate;
	String name;

	public Vehicle(String plate, String name) {
		this.plate = plate;
		this.name = name;
	}
	public String getPlate() {
		return plate;
	}

	public void setPlate(String plate) {
		this.plate = plate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}