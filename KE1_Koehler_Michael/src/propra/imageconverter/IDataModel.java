package propra.imageconverter;

/**
 * @author Michael Köhler 
 * Interface für DataModel-Klassen, in der die Daten 
 * für die Konvertierung zwischengespeichert werden
 *
 */
public interface IDataModel {
	
	void setDataInputArraySize(int size);
	
	void setDataOutputArraySize(int size);
	
	void setDataInputArray(byte[] data);
	
	void setDataOutputArray(byte[] data);
	
	byte[] getDataInputArray();
	
	byte[] getDataOutputArray();
	
//	void consoleOutput(byte[] data); // Kontrollausgabe auf der Konsole

}
