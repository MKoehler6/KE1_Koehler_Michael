package propra.imageconverter;

/**
 * zentrale Fehlerbehandlung des Programms, gibt Fehlermeldung aus und beendet Programm mit 
 * 		Fehlercode 123
 * @author Michael Köhler
 *
 */
public class ConverterException extends Exception {
	
	public ConverterException(String message) {
		System.err.println(message);
		System.exit(123);
	}

}
