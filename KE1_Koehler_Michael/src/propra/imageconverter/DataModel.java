package propra.imageconverter;

/**
 * @author Michael Köhler
 * In einer Instanz dieser Klasse werden die Daten aus der Input-Datei
 * in einem Byte-Array dataInputArray gespeichert
 * nach der Konvertierung befinden sich die neuen Daten im Byte-Array dataOutputArray
 *
 */

public class DataModel implements IDataModel{
	
	private byte[] dataInputArray; // Array für die Daten der Input-Datei
	private byte[] dataOutputArray; // Array für die Daten der Output-Datei

	public void setDataInputArray(byte[] data) {
		this.dataInputArray = data;
	}

	public byte[] getDataInputArray() {
		return dataInputArray;
	}
	
	public void setDataInputArraySize(int size) {
		dataInputArray = new byte[size];
	}

	public byte[] getDataOutputArray() {
		return dataOutputArray;
	}

	public void setDataOutputArraySize(int size) {
		dataOutputArray = new byte[size];
	}

	public void setDataOutputArray(byte[] data) {
		this.dataOutputArray = data;
	}

	/* 
	 * Gibt die ersten 40 Bytes des übergebenen byte-Arrays auf der Konsole aus
	 * dient der Kontrolle
//	public void consoleOutput(byte[] data) {
	 */
//		for (int i = 0; i < 40; i++) {
//			System.out.print(Byte.toUnsignedInt(data[i]) + " ");
//		}
//		System.out.println();
//		for (int i = 0; i < 40; i++) {
//			System.out.print(data[i] + " ");
//		}
//		System.out.println();
//		for (int i = 0; i < 40; i++) {
//			System.out.print(Integer.toHexString(Byte.toUnsignedInt(data[i])) + " ");
//		}
//		System.out.println();
//		System.out.println();
//	}
}
