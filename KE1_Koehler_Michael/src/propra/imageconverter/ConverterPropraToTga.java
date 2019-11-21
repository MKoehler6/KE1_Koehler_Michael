package propra.imageconverter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Michael Köhler
 * Eine Instanz dieser Klasse übernimmt die Konvertierung 
 * vom Propra-Format in das TGA-Format
 *
 */

public class ConverterPropraToTga implements IConverter {
	
	private IDataModel dataModel;
	private byte[] dataInputArray;
	private byte[] dataOutputArray;

	/* 
	 * überprüft alle Anforderungen an die Propra-Datei und die Korrektheit der Daten im Header 
	 * und die Prüfsumme
	 */
	public void checkData(IDataModel dataModel) throws ConverterException {
		this.dataModel = dataModel;
		dataInputArray = dataModel.getDataInputArray();
//		prüfe optionale Anforderungen an Propra-Datei
		checkImageDimensions(); // Bildbreite oder -höhe dürfen nicht 0 sein
		checkTypeOfCompression(); // Kompressionstyp muss 0 sein
//		checkCorrectAmountOfPixel();
//		checkCheckSum();
//		checkSizeOfDataSegmentInHeader();
//		prüfe, ob Spezifikationen der Propra-Dateien eingehalten werden
		checkNameProPraWS19();
		checkBitsPerPixel(); // Anzahl der Bits pro Pixel muss 24 sein
	}
	

	private void checkImageDimensions() throws ConverterException {
		if ((dataInputArray[10] == 0 && dataInputArray[11] == 0) ||
				(dataInputArray[12] == 0 && dataInputArray[13] == 0)) 
			throw new ConverterException("mind. eine Bilddimension ist 0");
	}
	
	private void checkTypeOfCompression() throws ConverterException {
		if (dataInputArray[15] != 0) throw new ConverterException("nicht unterstützter Kompressionstyp");
	}
	
	/**
	 * überprüft, ob die Anzahl der Pixel mit den Angaben im Header übereinstimmen
	 */
	private void checkCorrectAmountOfPixel() throws ConverterException{
		byte[] dataSegment = getDataSegment();
//		erstellt ein Byte-Array der Länge 4 mit den Bildbreite-Daten aus dem Header
//		und berechnet daraus die Bildbreite als Integer
		byte[] widthArray = {19, 100,0,0};
		int width = byteToInt4(widthArray);
		System.out.println(width);
//		erstellt ein Byte-Array der Länge 4 mit den Bildhöhe-Daten aus dem Header
//		und berechnet daraus die Bildhöhe als Integer
		byte[] heightArray = {dataInputArray[12], dataInputArray[13],0,0};
		int height = byteToInt4(heightArray);
		if (dataSegment.length != width * height * 3) throw new ConverterException("Pixelanzahl stimmt"
				+ " nicht mit Breite x Höhe im Header überein");
	}
	
	private void checkCheckSum() throws ConverterException{
		byte[] dataSegment = getDataSegment();
		long calculatedCheckSum = calculateCheckSum(dataSegment);
		System.out.println("Pruefsumme: " + calculatedCheckSum);
		long checkSumFromHeader = getcheckSumFromHeader();
		if (checkSumFromHeader != calculatedCheckSum) {
			throw new ConverterException("Prüfsumme falsch");
		}
	}
	
	/**
	 * überprüft die Angaben der Bildbreite und -höhe im Header
	 */
	private void checkSizeOfDataSegmentInHeader() throws ConverterException{
		int sizeOfDataSegment = getDataSegment().length;
		byte[] sizeInHeaderArray = new byte[8];
		for (int i = 0; i < 8; i++) {
			sizeInHeaderArray[i] = dataInputArray[16+i];
		}
		long sizeOfDataSegmentInHeader = byteToLong8(sizeInHeaderArray);
		if (sizeOfDataSegment != sizeOfDataSegmentInHeader) {
			throw new ConverterException("falsche Dateigröße im Header");
		}
	}

	/**
	 * überprüft die Angabe "ProPraWS19" im Header
	 */
	private void checkNameProPraWS19() throws ConverterException{
		byte[] nameOfFormat = {80, 114, 111, 80, 114, 97, 87, 83, 49, 57}; // = ProPraWS19
		for (int i = 0; i < nameOfFormat.length; i++) {
			if (dataInputArray[i] != nameOfFormat[i]) throw new ConverterException("Formatname nicht "
					+ "korrekt angegeben");
		}
	}
	
	private void checkBitsPerPixel() throws ConverterException {
		if (dataInputArray[14] != 24) throw new ConverterException("Bits pro Pixel nicht korrekt");
	}
	
	/* 
	 * konvertiert den Header und die Pixel in das TGA-Format
	 */
	public void convert() {
		byte[] dataSegment = getDataSegment();
		dataOutputArray = new byte[18 + dataSegment.length];
		for (int i = 0; i < 18; i++) {
			dataOutputArray[i] = 0;
		}
		dataOutputArray[2] = 2; // Bildtyp
		dataOutputArray[10] = dataInputArray[12]; //Y-Koordinate
		dataOutputArray[11] = dataInputArray[13];
		dataOutputArray[12] = dataInputArray[10]; // Bildbreite
		dataOutputArray[13] = dataInputArray[11];
		dataOutputArray[14] = dataInputArray[12]; // Bildhöhe
		dataOutputArray[15] = dataInputArray[13];
		dataOutputArray[16] = 24; // Bits pro Pixel
		dataOutputArray[17] = 32; // Bild-Attribut-Byte
		// konvertiert das Datensegment
		for (int i = 0; i < dataSegment.length; i++) {
			if (i % 3 == 0) dataOutputArray[18 + i] = dataSegment[i + 1];
			if (i % 3 == 1) dataOutputArray[18 + i] = dataSegment[i - 1];
			if (i % 3 == 2) dataOutputArray[18 + i] = dataSegment[i];
		}
		dataModel.setDataOutputArraySize(dataOutputArray.length);
		dataModel.setDataOutputArray(dataOutputArray);
	}

	private long calculateCheckSum(byte[] dataSegment) {
		long an = 0;
		long bn = 1;
		int x = 65513;
		for (int i = 0; i < dataSegment.length; i++) {
			an = an + (Byte.toUnsignedInt(dataSegment[i])+i+1);
			bn = (bn + an % x) % x;
		}
		an = an % x;
		return an*65536 + bn;
	}
	
	/**
	 * gibt ein Byte-Array mit den Daten des Datensegments zurück 
	 */
	private byte[] getDataSegment() {
		byte[] data = new byte[dataInputArray.length - 28];
		for (int i = 28; i < dataInputArray.length; i++) {
			data[i-28] = dataInputArray[i];
		}
		return data;
	}
	
	/**
	 * berechnet aus den CheckSum-Angaben im Header einen long-Wert
	 */
	private long getcheckSumFromHeader() {
		byte[] checkSumArray = new byte[8]; // Methode zum Umrechnen in long benötigt Array mit Länge 8
		for (int i = 0; i < 4; i++) { 
			checkSumArray[i] = dataInputArray[24+i];
			checkSumArray[i+4] = 0;
		}
		long checkSum = byteToLong8(checkSumArray); 
		System.out.println(checkSum);
		return checkSum;
	}
	
	/**
	 * berechnet aus einem Byte-Array der Länge 8 im Little-Endian-Format einen long-Wert
	 */
	private long byteToLong8(byte[] data) { // benötigt Array mit Länge 8
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
	}
	
	/**
	 * berechnet aus einem Byte-Array der Länge 4 im Little-Endian-Format einen Integer-Wert
	 */
	private int byteToInt4(byte[] data) { // benötigt Array mit Länge 4
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt();
	}
	

}
