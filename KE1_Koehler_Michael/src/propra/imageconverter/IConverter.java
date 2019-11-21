package propra.imageconverter;

/**
 * @author Michael Köhler
 * Interface für Konverter-Klassen
 *
 */
public interface IConverter {
	
	/**
	 * überprüft die Daten der Input-Datei, ob sie den Anforderungen entsprechen
	 */
	void checkData(IDataModel dataModel) throws ConverterException;
	
	/**
	 * konvertiert von einem Bildformat in das andere Bildformat
	 */
	void convert() throws ConverterException;
}
