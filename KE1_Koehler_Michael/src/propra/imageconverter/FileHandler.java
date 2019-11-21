package propra.imageconverter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Michael Köhler
 * Eine Instanz dieser Klasse öffnet die Input-Datei, legt die Daten im DataModel ab und
 * speichert am Ende der Konvertierung die konvertierten Daten in der Output-Datei 
 *
 */
public class FileHandler {
	
	private String pfadInput;
	private String pfadOutput;
	private IDataModel dataModel;
	
	public FileHandler(String pfadInput, String pfadOutput, IDataModel dataModel) {
		this.pfadInput = pfadInput;
		this.pfadOutput = pfadOutput;
		this.dataModel = dataModel;
	}
	
	/**
	 * liest aus der Input-Datei alle Bytes und speichert sie im dataInputArray
	 */
	public void readByteStreamFromFile() throws ConverterException {
		try {
			File file = new File(pfadInput);
			FileInputStream inputStream1 = new FileInputStream(file);
			BufferedInputStream inputStream = new BufferedInputStream(inputStream1);
			byte[] temp = inputStream.readAllBytes();
			dataModel.setDataInputArraySize(temp.length);
			dataModel.setDataInputArray(temp);
			inputStream.close();
		} catch (FileNotFoundException e) {
			throw new ConverterException("Datei kann nicht gefunden werden");
		} catch (IOException e) {
			throw new ConverterException("Beim Lesen der Datei ist ein Fehler aufgetreten");
		}
	}
	/**
	 * speichert das dataOutputArray in der Output-Datei
	 */
	public void writeByteStreamToFile() throws ConverterException {
		try {
			File file = new File(pfadOutput);
			FileOutputStream outputStream1 = new FileOutputStream(file);
			BufferedOutputStream outputStream = new BufferedOutputStream(outputStream1);
			outputStream.write(dataModel.getDataOutputArray());
			outputStream.flush();
			outputStream.close();
		} catch (FileNotFoundException e) {
			throw new ConverterException("Datei kann nicht gefunden werden");
		} catch (IOException e) {
			throw new ConverterException("Beim Schreiben der Datei ist ein Fehler aufgetreten");
		}
	}
}
