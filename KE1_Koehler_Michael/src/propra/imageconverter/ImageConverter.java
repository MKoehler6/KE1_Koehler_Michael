package propra.imageconverter;

import java.util.Date;

/**
 * @author Michael Köhler
 * Diese Klasse enthält die main-Methode, die die beiden Kommandozeilenparameter input-Pfad 
 * und output-Pfad entgegennimmt
 * die run-Methode steuert den grundsätzlichen Ablauf des Programms "Image-Converter"
 *
 */
public class ImageConverter {
	
	private String pfadInput;
	private String pfadOutput;
	private boolean doConversion; 	// wenn true, Konvertierung wird durchgeführt
									// wenn false, Datei wird auf Konsistenz getestet und kopiert
	
	public static void main(String[] args) throws ConverterException {
		long anfang = new Date().getTime();
		long heapFreeSize = Runtime.getRuntime().freeMemory();
		System.out.println(heapFreeSize);
		ImageConverter imageConverter = new ImageConverter();
		imageConverter.run(args);
		System.out.print("Laufzeit " );
		System.out.println(new Date().getTime()-anfang );
	}
	
	/**
	 * steuert den Ablauf der Konvertierung
	 * @param args
	 * 		2 Strings: input-Pfad und output-Pfad
	 * @throws ConverterException
	 * 		zentrale Fehlerbehandlung des Programms, gibt Fehlermeldung aus und beendet Programm mit 
	 * 		Fehlercode 123
	 */
	public void run(String[] args) throws ConverterException {
		IDataModel dataModel = new DataModel();
		IConverter converter = chooseConverter(args);
		if (converter == null) throw new ConverterException("Ungültiger Dateityp");
		try {
			pfadInput = args[0].split("=")[1]; // auslesen des Input- und Output-Pfades
			pfadOutput = args[1].split("=")[1];
		} catch (Exception e) {
			throw new ConverterException("Parametereingaben ungültig");
		}
		FileHandler fileHandler = new FileHandler(pfadInput, pfadOutput, dataModel);
		fileHandler.readByteStreamFromFile();
		converter.checkData(dataModel); // überprüfen der Input-Datei
		if (doConversion) { // Konvertierung wird nur durchgeführt, wenn die Dateitypen verschieden sind
			converter.convert();
		} else { // wenn Dateitypen gleich, wird die Input-Datei nur kopiert
			CopyInputFile copyInputFile = new CopyInputFile(dataModel);
			copyInputFile.copy();
		}
		fileHandler.writeByteStreamToFile();
	}

	
	/**
	 * wählt aus, erzeugt und gibt zurück eine Instanz der entsprechenden
	 * Converter-Klasse anhand der Dateiendung 
	 * bei gleicher Dateiendung wird doConversion auf false gesetzt
	 * wenn Dateiendung nicht unterstützt wird, wird null zurückgegeben
	 */
	public IConverter chooseConverter(String[] args) throws ConverterException {
		if (args[0].endsWith(".tga") && args[1].endsWith(".propra")) {
			doConversion = true;
			return new ConverterTgaToPropra();
		}
		if (args[0].endsWith(".propra") && args[1].endsWith(".tga")){
			doConversion = true;
			return new ConverterPropraToTga();
		}
		if (args[0].endsWith(".tga") && args[1].endsWith(".tga")){
			doConversion = false;
			return new ConverterTgaToPropra();
		}
		if (args[0].endsWith(".propra") && args[1].endsWith(".propra")){
			doConversion = false;
			return new ConverterPropraToTga();
		}
		return null;
		
	}
}
